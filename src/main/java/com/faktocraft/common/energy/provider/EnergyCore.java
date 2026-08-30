package com.faktocraft.common.energy.provider;

import com.faktocraft.IndReb;
import com.faktocraft.common.energy.EnergyLookup;
import com.faktocraft.common.energy.interfaces.IEnergy;
import com.faktocraft.common.energy.interfaces.IEnergyCore;
import com.faktocraft.common.entity.block.IndRebBlockEntity;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.interfaces.entity.IChargePad;
import com.faktocraft.common.interfaces.entity.IElectricSlot;
import com.faktocraft.common.interfaces.entity.ITransformer;
import com.faktocraft.common.interfaces.item.IElectricItem;
import com.faktocraft.common.network.packet.PacketParticle;
import com.faktocraft.common.util.Constants;
import com.faktocraft.common.util.ItemStackHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.WeakHashMap;

public class EnergyCore extends SavedData implements IEnergyCore {

  public static final String DATA_NAME = IndReb.MODID + "_energy_core";

  private static final Map<Level, EnergyCore> CLIENT_CORES = new WeakHashMap<>();

  private Level level;
  private EnergyNetworks networks;
  private final java.util.LinkedHashSet<BlockPos> energyBlocks = new java.util.LinkedHashSet<>();
  @Nullable
  private CompoundTag pendingTag;

  private final HashMap<BlockPos, Integer> energyReceivedBlock = new HashMap<>();
  private final HashMap<BlockPos, Integer> energyExtractedBlock = new HashMap<>();
  private final HashMap<EnergyNetwork, Integer> energyReceivedNetwork = new HashMap<>();
  private final HashMap<EnergyNetwork, Integer> energyExtractedNetwork = new HashMap<>();

  public EnergyCore() {
  }

  public static EnergyCore load(CompoundTag tag) {
    EnergyCore core = new EnergyCore();
    core.pendingTag = tag;
    return core;
  }

  public static EnergyCore get(Level level) {
    if (level instanceof ServerLevel serverLevel) {
      EnergyCore core = serverLevel.getDataStorage().computeIfAbsent(EnergyCore::load, EnergyCore::new, DATA_NAME);
      core.bindLevel(serverLevel);
      return core;
    }
    return CLIENT_CORES.computeIfAbsent(level, l -> {
      EnergyCore core = new EnergyCore();
      core.bindLevel(l);
      return core;
    });
  }

  public static void removeClientCore(Level level) {
    CLIENT_CORES.remove(level);
  }

  private void bindLevel(Level level) {
    if (this.level == null) {
      this.level = level;
      this.networks = new EnergyNetworks(level);
      if (pendingTag != null) {
        deserializeData(pendingTag);
        pendingTag = null;
      }
    }
  }

  @Override
  public void addEnergyBlock(BlockPos pos) {
    if (energyBlocks.add(pos.immutable())) {
      setDirty();
    }
  }

  @Override
  public void removeEnergyBlock(BlockPos pos) {
    energyBlocks.remove(pos);
    setDirty();
  }

  @Override
  public EnergyNetworks getNetworks() {
    return networks;
  }

  public int repairNetworks() {
    int count = networks.repairAll(new ArrayList<>(energyBlocks));
    setDirty();
    return count;
  }

  private int getEnergyReceivedBlock(BlockPos pos) {
    return energyReceivedBlock.getOrDefault(pos, 0);
  }

  private int getEnergyExtractedBlock(BlockPos pos) {
    return energyExtractedBlock.getOrDefault(pos, 0);
  }

  private int getEnergyReceivedNetwork(EnergyNetwork network) {
    return energyReceivedNetwork.getOrDefault(network, 0);
  }

  private int getEnergyExtractedNetwork(EnergyNetwork network) {
    return energyExtractedNetwork.getOrDefault(network, 0);
  }

  private void addEnergyReceivedBlock(BlockPos pos, int amount) {
    energyReceivedBlock.merge(pos, amount, Integer::sum);
  }

  private void addEnergyExtractedBlock(BlockPos pos, int amount) {
    energyExtractedBlock.merge(pos, amount, Integer::sum);
  }

  private void addEnergyReceivedNetwork(EnergyNetwork network, int amount) {
    energyReceivedNetwork.merge(network, amount, Integer::sum);
    network.addFlowReceived(amount);
  }

  private void addEnergyExtractedNetwork(EnergyNetwork network, int amount) {
    energyExtractedNetwork.merge(network, amount, Integer::sum);
    network.addFlowDelivered(amount);
  }

  @Override
  public void tick() {
    if (level == null || level.isClientSide()) {
      return;
    }

    networks.repairLoaded();

    energyReceivedBlock.clear();
    energyExtractedBlock.clear();
    energyReceivedNetwork.clear();
    energyExtractedNetwork.clear();
    for (EnergyNetwork network : networks.getNetworks()) {
      network.resetCurrentTier();
      network.rollFlowStats();
    }

    computeDemand();

    transferInternal();
    transferTouching();
    transferFromGenerators();
    transferFromBatteries();
    transferBetweenNetworks();
    transferFromCables();

    setDirty();
  }

  private int networkReceiveBudget(EnergyNetwork network) {
    return Math.max(0, Math.min(network.maxReceiveTick() - getEnergyReceivedNetwork(network),
        network.maxEnergy() - network.energyStored()));
  }

  private int networkExtractBudget(EnergyNetwork network) {
    return Math.max(0, Math.min(network.maxExtractTick() - getEnergyExtractedNetwork(network),
        network.energyStored()));
  }

  private final HashSet<EnergyNetwork> demandNetworks = new HashSet<>();

  private boolean networkHasOutlet(EnergyNetwork network) {
    return demandNetworks.contains(network);
  }

  private void computeDemand() {
    demandNetworks.clear();
    ArrayDeque<EnergyNetwork> queue = new ArrayDeque<>();
    for (EnergyNetwork network : networks.getNetworks()) {
      if (hasLocalDemand(network) && demandNetworks.add(network)) {
        queue.add(network);
      }
    }
    while (!queue.isEmpty()) {
      EnergyNetwork current = queue.poll();
      for (BlockPos tx : current.getTransmitters()) {
        EnergyNetwork other = networks.getNetwork(tx);
        if (other != null && other != current && demandNetworks.add(other)) {
          queue.add(other);
        }
      }
    }
  }

  private boolean hasLocalDemand(EnergyNetwork network) {
    for (BlockPos cablePos : network.getConnections()) {
      for (Direction direction : Constants.DIRECTIONS) {
        BlockPos relative = cablePos.relative(direction);
        if (!network.getElectrics().contains(relative) || !level.isLoaded(relative)) {
          continue;
        }
        IEnergy energy = EnergyLookup.find(level, relative, direction.getOpposite());
        if (energy == null || !energy.canReceiveEnergy(direction.getOpposite())) {
          continue;
        }
        EnergyType type = energy.energyType();
        if (type != EnergyType.BOTH && type != EnergyType.RECEIVE && type != EnergyType.TRANSFORMER) {
          continue;
        }
        if (energy.energyStored() < energy.maxEnergy()) {
          return true;
        }
      }
    }
    return false;
  }

  private void transferBetweenNetworks() {
    ArrayDeque<EnergyNetwork> queue = new ArrayDeque<>();
    HashSet<EnergyNetwork> queued = new HashSet<>();

    HashMap<EnergyNetwork, HashSet<EnergyNetwork>> receivedFrom = new HashMap<>();
    for (EnergyNetwork network : new ArrayList<>(networks.getNetworks())) {
      if (network.getEnergyFlowing() != null && queued.add(network)) {
        queue.add(network);
      }
    }
    while (!queue.isEmpty()) {
      EnergyNetwork network = queue.poll();
      if (!networks.getNetworks().contains(network)) {
        continue;
      }
      EnergyTier current = network.getEnergyFlowing();
      if (current == null) {
        continue;
      }
      int leftExtract = networkExtractBudget(network);

      ArrayList<EnergyNetwork> targets = new ArrayList<>();
      boolean burned = false;
      for (BlockPos pos : new ArrayList<>(network.getTransmitters())) {
        if (!level.isLoaded(pos)) {
          continue;
        }
        EnergyNetwork other = networks.getNetwork(pos);
        if (other == null || other == network) {
          continue;
        }
        if (current.getLvl() > other.getEnergyTier().getLvl()) {
          createBurn(other, pos, current.getLvl() - other.getEnergyTier().getLvl());
          burned = true;
          break;
        }
        other.setCurrentTier(current);
        if (queued.add(other)) {
          queue.add(other);
        }
        int receive = networkReceiveBudget(other);

        if (receive > 0
            && (long) other.energyStored() * network.maxEnergy() < (long) network.energyStored() * other.maxEnergy()
            && !targets.contains(other)
            && !receivedFrom.getOrDefault(network, EMPTY_NETWORK_SET).contains(other)) {
          targets.add(other);
        }
      }
      if (burned || targets.isEmpty() || leftExtract <= 0) {
        continue;
      }

      targets.sort(Comparator.comparingInt(EnergyNetwork::maxReceive));
      int left = Math.min(leftExtract, network.energyStored());
      int size = targets.size();
      int split = size > 0 ? left / size : 0;
      for (EnergyNetwork other : targets) {
        if (left > 0 && split > 0) {
          int receive = networkReceiveBudget(other);
          int maxEnergy = Math.min(split, receive);
          int moved = network.extractEnergy(maxEnergy, false);
          other.receiveEnergy(moved, false);
          addEnergyExtractedNetwork(network, moved);
          addEnergyReceivedNetwork(other, moved);
          if (moved > 0) {
            receivedFrom.computeIfAbsent(other, k -> new HashSet<>()).add(network);
          }
          left -= moved;
          split = (left > 0 && --size > 0) ? left / size : 0;
        }
      }
    }
  }

  private static final HashSet<EnergyNetwork> EMPTY_NETWORK_SET = new HashSet<>();

  private static boolean acceptsTier(IEnergy receiver, @Nullable ITransformer transformer, EnergyTier tier) {
    return transformer != null ? transformer.energyReceiveTier() == tier : receiver.acceptsTier(tier);
  }

  private static int maxAcceptedLvl(IEnergy receiver, @Nullable ITransformer transformer) {
    return transformer != null ? transformer.energyReceiveTier().getLvl() : receiver.maxAcceptedTier().getLvl();
  }

  private void createExplosion(BlockPos pos, int tierDiff) {
    float power = 1.0F + 1.5F * Math.max(1, tierDiff);
    level.destroyBlock(pos, false);
    level.explode(null, com.faktocraft.common.registries.ModDamageTypes.machineExplosion(level), null,
        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, power, false,
        Level.ExplosionInteraction.BLOCK);
  }

  private void createBurn(EnergyNetwork network, BlockPos soundPos, int tierDiff) {
    HashSet<BlockPos> cables = new HashSet<>(network.getConnections());
    for (BlockPos cablePos : cables) {
      level.removeBlock(cablePos, false);
      if (level instanceof ServerLevel serverLevel) {
        PacketParticle.send(serverLevel, cablePos);
      }
      net.minecraft.world.level.block.state.BlockState fire = net.minecraft.world.level.block.BaseFireBlock
          .getState(level, cablePos);
      if (level.getBlockState(cablePos).isAir() && fire.canSurvive(level, cablePos)) {
        level.setBlock(cablePos, fire, 3);
      }
    }
    level.playSound(null, soundPos, SoundEvents.GENERIC_BURN, SoundSource.BLOCKS, 1F, 1F);
    networks.removeNetwork(network);
    if (tierDiff >= 2) {
      float power = Math.min(2.0F, 0.5F + 0.5F * tierDiff);
      level.explode(null, com.faktocraft.common.registries.ModDamageTypes.machineExplosion(level), null,
          soundPos.getX() + 0.5, soundPos.getY() + 0.5, soundPos.getZ() + 0.5, power, false,
          Level.ExplosionInteraction.BLOCK);
    }
  }

  private record ItemTransfer(IEnergy energy, ItemStack stack) {
  }

  private void transferInternal() {
    for (BlockPos pos : new ArrayList<>(energyBlocks)) {
      if (!level.isLoaded(pos)) {
        continue;
      }
      BlockEntity be = level.getBlockEntity(pos);
      if (!(be instanceof IndRebBlockEntity indRebBe) || !indRebBe.hasEnergy()) {
        continue;
      }

      IEnergy machineEnergy = indRebBe.getEnergyStorage();

      if (indRebBe.hasBattery()) {
        ItemStackHandler batteryHandler = indRebBe.getBatteryStackHandler();
        ArrayList<ItemTransfer> chargeList = new ArrayList<>();
        ArrayList<ItemTransfer> dischargeList = new ArrayList<>();
        boolean exploded = false;

        for (int i = 0; i < batteryHandler.getSlots(); i++) {
          ItemStack stack = batteryHandler.getStackInSlot(i);
          if (stack.isEmpty() || !(stack.getItem() instanceof IElectricItem electricItem)) {
            continue;
          }
          IEnergy itemEnergy = electricItem.getEnergy(stack);
          if (itemEnergy == null) {
            continue;
          }

          IElectricSlot slotDef = i < indRebBe.getElectricSlot().size() ? indRebBe.getElectricSlot().get(i) : null;
          boolean charging = slotDef != null && slotDef.isCharging();

          if (charging) {
            if (itemEnergy.maxReceive() > 0 && machineEnergy.maxExtract() > 0) {
              chargeList.add(new ItemTransfer(itemEnergy, stack));
            }
          } else {
            if (itemEnergy.maxExtract() > 0 && machineEnergy.maxReceive() > 0) {
              if (itemEnergy.energyTier().getLvl() <= machineEnergy.energyTier().getLvl()) {
                dischargeList.add(new ItemTransfer(itemEnergy, stack));
              } else {
                createExplosion(pos, itemEnergy.energyTier().getLvl() - machineEnergy.energyTier().getLvl());
                exploded = true;
                break;
              }
            }
          }
        }

        if (exploded) {
          continue;
        }

        distributeToItems(pos, machineEnergy, chargeList, true);
        distributeFromItems(pos, machineEnergy, dischargeList);

        for (int i = 0; i < batteryHandler.getSlots(); i++) {
          ItemStack stack = batteryHandler.getStackInSlot(i);
          if (!stack.isEmpty() && stack.getItem() instanceof IElectricItem electricItem) {
            electricItem.tickElectric(stack);
          }
        }
      }

      if (indRebBe instanceof IChargePad chargePad) {
        tickChargePad(indRebBe, chargePad, pos, machineEnergy);
      }
    }
  }

  private void tickChargePad(IndRebBlockEntity blockEntity, IChargePad chargePad, BlockPos pos, IEnergy machineEnergy) {
    AABB area = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
    ArrayList<ItemTransfer> chargeList = new ArrayList<>();
    for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, area)) {
      Inventory inventory = player.getInventory();
      for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
        ItemStack stack = inventory.getItem(slot);
        if (stack.isEmpty() || !(stack.getItem() instanceof IElectricItem electricItem)) {
          continue;
        }
        IEnergy itemEnergy = electricItem.getEnergy(stack);
        if (itemEnergy == null) {
          continue;
        }
        if (itemEnergy.maxReceive() > 0
            && itemEnergy.energyStored() < itemEnergy.maxEnergy()
            && itemEnergy.energyTier().getLvl() <= chargePad.chargePadTier().getLvl()) {
          chargeList.add(new ItemTransfer(itemEnergy, stack));
        }
      }
    }
    blockEntity.setActive(!chargeList.isEmpty() && machineEnergy.energyStored() > 0);
    distributeToItems(pos, machineEnergy, chargeList, false);
  }

  private void distributeToItems(BlockPos pos, IEnergy source, ArrayList<ItemTransfer> targets, boolean tickItems) {
    if (targets.isEmpty()) {
      return;
    }
    targets.sort(Comparator.comparingInt(t -> t.energy().maxReceive()));

    int left = Math.max(0, source.maxExtract() - getEnergyExtractedBlock(pos));
    int size = targets.size();
    int split = size > 0 ? left / size : 0;
    for (ItemTransfer target : targets) {
      if (left > 0 && split > 0) {
        int maxEnergy = Math.min(split, target.energy().maxReceive());
        int distributed = source.extractEnergy(maxEnergy, false);
        target.energy().receiveEnergy(distributed, false);
        addEnergyExtractedBlock(pos, distributed);
        left -= distributed;
        split = (left > 0 && --size > 0) ? left / size : 0;
      }
      if (tickItems && target.stack().getItem() instanceof IElectricItem electricItem) {
        electricItem.tickElectric(target.stack());
      }
    }
  }

  private void distributeFromItems(BlockPos pos, IEnergy machine, ArrayList<ItemTransfer> sources) {
    if (sources.isEmpty()) {
      return;
    }
    sources.sort(Comparator.comparingInt(t -> t.energy().maxExtract()));

    int left = Math.max(0, machine.maxReceive() - getEnergyReceivedBlock(pos));
    int size = sources.size();
    int split = size > 0 ? left / size : 0;
    for (ItemTransfer source : sources) {
      if (left > 0 && split > 0) {
        int maxEnergy = Math.min(split, source.energy().maxExtract());
        int extracted = source.energy().extractEnergy(maxEnergy, false);
        machine.receiveEnergy(extracted, false);
        addEnergyReceivedBlock(pos, extracted);
        left -= extracted;
        split = (left > 0 && --size > 0) ? left / size : 0;
      }
    }
  }

  private record BlockTransfer(IEnergy energy, BlockPos pos, int amount) {
  }

  private void transferTouching() {
    for (BlockPos pos : new ArrayList<>(energyBlocks)) {
      if (!level.isLoaded(pos)) {
        continue;
      }
      IEnergy senderEnergy = EnergyLookup.find(level, pos, null);
      if (senderEnergy == null) {
        continue;
      }

      int leftExtract = Math.max(0, senderEnergy.maxExtract() - getEnergyExtractedBlock(pos));
      if (leftExtract <= 0) {
        continue;
      }

      BlockEntity senderBe = level.getBlockEntity(pos);
      ITransformer senderTransformer = senderBe instanceof ITransformer t ? t : null;

      ArrayList<BlockTransfer> targets = new ArrayList<>();
      boolean exploded = false;

      for (Direction direction : Constants.DIRECTIONS) {
        BlockPos relative = pos.relative(direction);
        if (!energyBlocks.contains(relative) || !level.isLoaded(relative)) {
          continue;
        }
        if (!senderEnergy.canExtractEnergy(direction)) {
          continue;
        }

        IEnergy receiverEnergy = EnergyLookup.find(level, relative, direction.getOpposite());
        if (receiverEnergy == null || !receiverEnergy.canReceiveEnergy(direction.getOpposite())) {
          continue;
        }

        int leftReceive = Math.max(0, receiverEnergy.maxReceive() - getEnergyReceivedBlock(relative));
        if (leftReceive <= 0) {
          continue;
        }

        BlockEntity receiverBe = level.getBlockEntity(relative);
        ITransformer receiverTransformer = receiverBe instanceof ITransformer t ? t : null;

        EnergyTier senderTier = senderTransformer != null ? senderTransformer.energyExtractTier()
            : senderEnergy.energyTier();

        if (acceptsTier(receiverEnergy, receiverTransformer, senderTier)) {
          targets.add(new BlockTransfer(receiverEnergy, relative, leftReceive));
        } else if (senderTier.getLvl() > maxAcceptedLvl(receiverEnergy, receiverTransformer)) {
          createExplosion(relative, senderTier.getLvl() - maxAcceptedLvl(receiverEnergy, receiverTransformer));
          exploded = true;
          break;
        } else if (receiverBe instanceof IndRebBlockEntity receiverIndReb) {
          receiverIndReb.markUndervoltage();
        }
      }

      if (exploded || targets.isEmpty()) {
        continue;
      }

      targets.sort(Comparator.comparingInt(BlockTransfer::amount));
      int left = leftExtract;
      int size = targets.size();
      int split = size > 0 ? left / size : 0;
      for (BlockTransfer target : targets) {
        if (left > 0 && split > 0) {
          int maxEnergy = Math.min(split, Math.min(target.energy().maxReceive(), target.amount()));
          int distributed = senderEnergy.extractEnergy(maxEnergy, false);
          target.energy().receiveEnergy(distributed, false);
          addEnergyExtractedBlock(pos, distributed);
          addEnergyReceivedBlock(target.pos(), distributed);
          left -= distributed;
          split = (left > 0 && --size > 0) ? left / size : 0;
        }
      }
    }
  }

  private void transferFromGenerators() {
    transferBlocksToNetworks(EnergyType.EXTRACT);
  }

  private void transferFromBatteries() {
    transferBlocksToNetworks(EnergyType.BOTH);
    transferBlocksToNetworks(EnergyType.TRANSFORMER);
  }

  private ArrayList<BlockPos> orderedExtractBlocks() {
    ArrayList<BlockPos> ordered = new ArrayList<>(energyBlocks);
    HashMap<BlockPos, Integer> priorities = new HashMap<>();
    for (BlockPos pos : ordered) {
      if (level.isLoaded(pos)
          && level.getBlockEntity(pos) instanceof IndRebBlockEntity indRebBe
          && indRebBe.isGenerator()) {
        priorities.put(pos, indRebBe.effectiveGeneratorPriority());
      }
    }
    ordered.sort(Comparator.comparingInt((BlockPos pos) -> priorities.getOrDefault(pos, 0))
        .reversed()
        .thenComparingLong(BlockPos::asLong));
    return ordered;
  }

  private void transferBlocksToNetworks(EnergyType type) {
    Iterable<BlockPos> iterationOrder = type == EnergyType.EXTRACT
        ? orderedExtractBlocks()
        : new ArrayList<>(energyBlocks);
    for (BlockPos pos : iterationOrder) {
      if (!level.isLoaded(pos)) {
        continue;
      }
      IEnergy energy = EnergyLookup.find(level, pos, null);
      if (energy == null || energy.energyType() != type) {
        continue;
      }

      int leftExtract = Math.max(0, energy.maxExtract() - getEnergyExtractedBlock(pos));
      if (leftExtract <= 0) {
        continue;
      }

      BlockEntity be = level.getBlockEntity(pos);
      ITransformer transformer = be instanceof ITransformer t ? t : null;

      ArrayList<EnergyNetwork> targets = new ArrayList<>();
      boolean burned = false;
      for (Direction direction : Constants.DIRECTIONS) {
        if (!energy.canExtractEnergy(direction)) {
          continue;
        }
        BlockPos relative = pos.relative(direction);
        EnergyNetwork network = networks.getNetwork(relative);
        if (network == null) {
          continue;
        }

        EnergyTier sourceTier = transformer != null ? transformer.energyExtractTier() : energy.energyTier();
        network.setCurrentTier(sourceTier);
        if (sourceTier.getLvl() <= network.getEnergyTier().getLvl()) {
          int networkReceive = networkReceiveBudget(network);
          if (networkReceive > 0 && networkHasOutlet(network) && !targets.contains(network)) {
            targets.add(network);
          }
        } else {
          createBurn(network, relative, sourceTier.getLvl() - network.getEnergyTier().getLvl());
          burned = true;
          break;
        }
      }

      if (burned || targets.isEmpty()) {
        continue;
      }

      targets.sort(Comparator.comparingInt(EnergyNetwork::maxReceive));
      int left = leftExtract;
      int size = targets.size();
      int split = size > 0 ? left / size : 0;
      for (EnergyNetwork network : targets) {
        if (left > 0 && split > 0) {
          int networkReceive = networkReceiveBudget(network);
          int maxEnergy = Math.min(split, networkReceive);
          int distributed = energy.extractEnergy(maxEnergy, false);
          network.receiveEnergy(distributed, false);
          addEnergyExtractedBlock(pos, distributed);
          addEnergyReceivedNetwork(network, distributed);
          left -= distributed;
          split = (left > 0 && --size > 0) ? left / size : 0;
        }
      }
    }
  }

  private void transferFromCables() {
    for (EnergyNetwork network : new ArrayList<>(networks.getNetworks())) {
      int leftExtract = networkExtractBudget(network);
      if (leftExtract <= 0) {
        continue;
      }

      HashSet<BlockPos> posChecked = new HashSet<>();
      ArrayList<BlockTransfer> targets = new ArrayList<>();
      boolean exploded = false;

      for (BlockPos cablePos : network.getConnections()) {
        for (Direction direction : Constants.DIRECTIONS) {
          BlockPos relative = cablePos.relative(direction);
          if (!network.getElectrics().contains(relative) || posChecked.contains(relative)) {
            continue;
          }
          if (!level.isLoaded(relative)) {
            posChecked.add(relative);
            continue;
          }

          IEnergy energy = EnergyLookup.find(level, relative, direction.getOpposite());
          if (energy == null || !energy.canReceiveEnergy(direction.getOpposite())) {
            continue;
          }
          posChecked.add(relative);
          EnergyType type = energy.energyType();
          if (type != EnergyType.BOTH && type != EnergyType.RECEIVE && type != EnergyType.TRANSFORMER) {
            continue;
          }

          int leftReceive = Math.max(0, energy.maxReceive() - getEnergyReceivedBlock(relative));
          if (leftReceive <= 0) {
            continue;
          }

          BlockEntity be = level.getBlockEntity(relative);
          ITransformer transformer = be instanceof ITransformer t ? t : null;

          EnergyTier current = network.getCurrentTier();
          if (acceptsTier(energy, transformer, current)) {
            targets.add(new BlockTransfer(energy, relative, leftReceive));
          } else if (current.getLvl() > maxAcceptedLvl(energy, transformer)) {
            createExplosion(relative, current.getLvl() - maxAcceptedLvl(energy, transformer));
            exploded = true;
            break;
          } else if (be instanceof IndRebBlockEntity receiverIndReb) {
            receiverIndReb.markUndervoltage();
          }
        }
        if (exploded) {
          break;
        }
      }

      if (exploded || targets.isEmpty()) {
        continue;
      }

      for (BlockTransfer target : targets) {
        network.addFlowDemand(target.amount());
      }

      targets.sort(Comparator.comparingInt(BlockTransfer::amount));
      int left = Math.min(leftExtract, network.energyStored());
      int size = targets.size();
      int split = size > 0 ? left / size : 0;
      for (BlockTransfer target : targets) {
        if (left > 0 && split > 0) {
          int maxEnergy = Math.min(split, Math.min(target.energy().maxReceive(), target.amount()));
          int distributed = network.extractEnergy(maxEnergy, false);
          target.energy().receiveEnergy(distributed, false);
          addEnergyExtractedNetwork(network, distributed);
          addEnergyReceivedBlock(target.pos(), distributed);
          left -= distributed;
          split = (left > 0 && --size > 0) ? left / size : 0;
        }
      }
    }
  }

  public CompoundTag serializeData() {
    CompoundTag tag = new CompoundTag();
    tag.putLongArray("energyBlocks", energyBlocks.stream().mapToLong(BlockPos::asLong).toArray());
    if (networks != null) {
      tag.put("networks", networks.serializeNBT());
    } else if (pendingTag != null) {
      return pendingTag;
    }
    return tag;
  }

  public void deserializeData(CompoundTag tag) {
    energyBlocks.clear();
    for (long pos : tag.getLongArray("energyBlocks")) {
      energyBlocks.add(BlockPos.of(pos));
    }
    if (networks != null) {
      networks.deserializeNBT(tag.getCompound("networks"));
    }
  }

  @Override
  public CompoundTag save(CompoundTag tag) {
    tag.merge(serializeData());
    return tag;
  }

  @Override
  public CompoundTag getNetworkTag(@Nullable BlockPos pos) {
    if (pos == null) {
      return serializeData();
    }
    CompoundTag tag = new CompoundTag();
    tag.putLongArray("energyBlock", new long[0]);
    CompoundTag networksTag = new CompoundTag();
    EnergyNetwork network = networks.getNetworkOther(pos);
    if (network != null) {
      networksTag.put("1", network.serializeNBT());
    }
    tag.put("networks", networksTag);
    return tag;
  }

  @Override
  public void setNetworkTag(CompoundTag tag) {
    deserializeData(tag);
  }
}

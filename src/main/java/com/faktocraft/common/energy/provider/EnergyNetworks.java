package com.faktocraft.common.energy.provider;

import com.faktocraft.common.block.impl.cable.BlockCable;
import com.faktocraft.common.energy.EnergyLookup;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.util.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;

public class EnergyNetworks {

  private final ArrayList<EnergyNetwork> networks = new ArrayList<>();
  private final HashMap<BlockPos, EnergyNetwork> cableOwner = new HashMap<>();
  private boolean ownerIndexDirty;
  private final Level level;

  public EnergyNetworks(Level level) {
    this.level = level;
  }

  public Level getLevel() {
    return level;
  }

  public List<EnergyNetwork> getNetworks() {
    return Collections.unmodifiableList(networks);
  }

  public void addNetwork(EnergyNetwork network) {
    attach(network);
  }

  @Nullable
  public EnergyNetwork getNetwork(BlockPos pos) {
    if (ownerIndexDirty) {
      rebuildOwnerIndex();
    }
    return cableOwner.get(pos);
  }

  void claim(BlockPos pos, EnergyNetwork network) {
    if (ownerIndexDirty) {
      return;
    }
    EnergyNetwork previous = cableOwner.putIfAbsent(pos, network);
    if (previous != null && previous != network) {
      ownerIndexDirty = true;
    }
  }

  void release(BlockPos pos, EnergyNetwork network) {
    if (ownerIndexDirty) {
      return;
    }
    if (cableOwner.get(pos) == network) {
      cableOwner.remove(pos);
    }
  }

  private void attach(EnergyNetwork network) {
    networks.add(network);
    network.setOwner(this);
    for (BlockPos pos : network.getConnections()) {
      claim(pos, network);
    }
  }

  private void detach(EnergyNetwork network) {
    if (networks.remove(network)) {
      network.setOwner(null);
      ownerIndexDirty = true;
    }
  }

  private void rebuildOwnerIndex() {
    cableOwner.clear();
    for (EnergyNetwork network : networks) {
      for (BlockPos pos : network.getConnections()) {
        cableOwner.putIfAbsent(pos, network);
      }
    }
    ownerIndexDirty = false;
  }

  @Nullable
  public EnergyNetwork getNetworkOther(BlockPos pos) {
    for (EnergyNetwork network : networks) {
      if (network.getConnections().contains(pos) || network.getElectrics().contains(pos)
          || network.getTransmitters().contains(pos)) {
        return network;
      }
    }
    return null;
  }

  public EnergyNetwork createNetwork(BlockPos pos, EnergyTier tier) {
    EnergyNetwork network = new EnergyNetwork(pos, tier);
    attach(network);
    return network;
  }

  public void removeNetwork(EnergyNetwork network) {
    detach(network);
  }

  private static boolean linkAllowed(BlockState state, Direction direction) {
    if (state.getBlock() instanceof com.faktocraft.common.block.impl.cable.BlockBreaker) {

      return state.getValue(com.faktocraft.common.block.impl.cable.BlockBreaker.ON)
          && state.getValue(com.faktocraft.common.block.impl.cable.BlockBreaker.AXIS) == direction.getAxis();
    }
    return true;
  }

  @org.jetbrains.annotations.Nullable
  private EnergyTier tierOf(BlockPos pos, BlockCable cable) {
    if (cable instanceof com.faktocraft.common.block.impl.cable.BlockBreaker
        && level.getBlockEntity(pos) instanceof com.faktocraft.common.block.impl.cable.BlockEntityBreaker breakerBe) {
      return breakerBe.getAdoptedTier();
    }
    return cable.getCableTier().getEnergyTier();
  }

  public void onPlaced(BlockPos pos, BlockState state, EnergyTier tier) {

    for (EnergyNetwork stale : new ArrayList<>(networks)) {
      if (stale.getConnections().remove(pos) && stale.getConnections().isEmpty()) {
        detach(stale);
      }
    }
    for (Direction direction : Constants.DIRECTIONS) {
      if (linkAllowed(state, direction)) {
        adoptOrphans(pos.relative(direction));
      }
    }

    ArrayList<EnergyNetwork> adjacentNetworks = new ArrayList<>();
    ArrayList<BlockPos> transmitters = new ArrayList<>();
    ArrayList<BlockPos> electrics = new ArrayList<>();

    for (Direction direction : Constants.DIRECTIONS) {
      if (!linkAllowed(state, direction)) {
        continue;
      }
      BlockPos relative = pos.relative(direction);
      BlockState relativeState = level.getBlockState(relative);
      if (relativeState.getBlock() instanceof BlockCable cable) {
        if (!linkAllowed(relativeState, direction.getOpposite())) {
          continue;
        }
        if (tierOf(relative, cable) == tier) {
          EnergyNetwork network = getNetwork(relative);
          if (network != null && !adjacentNetworks.contains(network)) {
            adjacentNetworks.add(network);
          }
        } else {
          transmitters.add(relative);
        }
      } else if (EnergyLookup.isPresent(level, relative, direction.getOpposite())) {
        electrics.add(relative);
      }
    }

    EnergyNetwork target;
    if (adjacentNetworks.size() > 1) {
      int energy = 0;
      target = new EnergyNetwork(energy, tier);
      target.getConnections().add(pos);
      for (EnergyNetwork network : adjacentNetworks) {
        target.getConnections().addAll(network.getConnections());
        target.getElectrics().addAll(network.getElectrics());
        target.getTransmitters().addAll(network.getTransmitters());
        target.setEnergy(Math.min(target.maxEnergy(), target.energyStored() + network.energyStored()));
        detach(network);
      }
      attach(target);
    } else if (adjacentNetworks.size() == 1) {
      target = adjacentNetworks.get(0);
      target.getConnections().add(pos);
    } else {
      target = createNetwork(pos, tier);
    }

    target.getConnections().add(pos);
    rebuildLinks(target, pos);

    for (BlockPos transmitter : new HashSet<>(target.getTransmitters())) {
      EnergyNetwork other = getNetwork(transmitter);
      if (other != null && other != target) {
        other.getConnections().add(transmitter);
        rebuildLinks(other, transmitter);
      }
    }
    EnergyAudit.check(this, "onPlaced");
  }

  private void rebuildLinks(EnergyNetwork network, BlockPos start) {
    HashSet<BlockPos> candidates = new HashSet<>(network.getConnections());
    network.getConnections().clear();
    network.getTransmitters().clear();
    network.getElectrics().clear();
    floodFill(network, start, candidates);

    candidates.removeAll(network.getConnections());
    while (!candidates.isEmpty()) {
      BlockPos orphanStart = candidates.iterator().next();
      if (!(level.getBlockState(orphanStart).getBlock() instanceof BlockCable cable)) {
        candidates.remove(orphanStart);
        continue;
      }
      EnergyTier tier = tierOf(orphanStart, cable);
      EnergyNetwork rebuilt = new EnergyNetwork(0, tier == null ? network.getEnergyTier() : tier);
      floodFill(rebuilt, orphanStart, candidates);
      candidates.removeAll(rebuilt.getConnections());
      candidates.remove(orphanStart);
      if (!rebuilt.getConnections().isEmpty()) {
        attach(rebuilt);
      }
    }
    EnergyAudit.check(this, "rebuildLinks");
  }

  private void adoptOrphans(BlockPos start) {
    if (!(level.getBlockState(start).getBlock() instanceof BlockCable startCable) || getNetwork(start) != null) {
      return;
    }
    EnergyTier tier = tierOf(start, startCable);
    if (tier == null) {
      return;
    }
    EnergyNetwork network = new EnergyNetwork(0, tier);
    ArrayList<BlockPos> queue = new ArrayList<>();
    HashSet<BlockPos> visited = new HashSet<>();
    queue.add(start);
    while (!queue.isEmpty()) {
      BlockPos current = queue.remove(queue.size() - 1);
      if (!visited.add(current)) {
        continue;
      }
      BlockState state = level.getBlockState(current);
      if (!(state.getBlock() instanceof BlockCable cable)
          || tierOf(current, cable) != tier || getNetwork(current) != null) {
        continue;
      }
      network.getConnections().add(current);
      for (Direction direction : Constants.DIRECTIONS) {
        if (!linkAllowed(state, direction)) {
          continue;
        }
        BlockPos relative = current.relative(direction);
        if (!visited.contains(relative) && linkAllowed(level.getBlockState(relative), direction.getOpposite())) {
          queue.add(relative);
        }
      }
    }
    if (network.getConnections().isEmpty()) {
      return;
    }
    attach(network);
    rebuildLinks(network, start);
    EnergyAudit.check(this, "adoptOrphans");
  }

  public void onRemove(BlockPos pos) {
    EnergyNetwork network = getNetwork(pos);
    if (network == null) {
      EnergyAudit.check(this, "onRemove(unclaimed)");
      return;
    }

    network.getConnections().remove(pos);
    int oldEnergy = network.energyStored();
    EnergyTier tier = network.getEnergyTier();
    HashSet<BlockPos> remaining = new HashSet<>(network.getConnections());

    ArrayList<EnergyNetwork> rebuiltSegments = new ArrayList<>();
    for (Direction direction : Constants.DIRECTIONS) {
      BlockPos relative = pos.relative(direction);
      if (remaining.contains(relative)) {
        EnergyNetwork rebuilt = new EnergyNetwork(0, tier);
        floodFill(rebuilt, relative, remaining);

        remaining.removeAll(rebuilt.getConnections());
        attach(rebuilt);
        rebuiltSegments.add(rebuilt);
      }
    }
    if (!rebuiltSegments.isEmpty()) {
      int share = oldEnergy / rebuiltSegments.size();
      for (EnergyNetwork rebuilt : rebuiltSegments) {
        rebuilt.setEnergy(Math.min(rebuilt.maxEnergy(), share));
      }
    }
    detach(network);
    EnergyAudit.check(this, "onRemove");
  }

  public int repairAll(Iterable<BlockPos> extraSeeds) {
    for (EnergyNetwork network : new ArrayList<>(networks)) {
      if (!networks.contains(network)) {
        continue;
      }
      BlockPos start = null;
      boolean loaded = true;
      for (BlockPos pos : network.getConnections()) {
        if (!level.isLoaded(pos)) {
          loaded = false;
          break;
        }
        if (start == null) {
          start = pos;
        }
      }
      if (loaded && start != null) {
        rebuildLinks(network, start);
      }
    }
    HashSet<BlockPos> seeds = new HashSet<>();
    for (EnergyNetwork network : new ArrayList<>(networks)) {
      for (BlockPos pos : network.getConnections()) {
        for (Direction direction : Constants.DIRECTIONS) {
          seeds.add(pos.relative(direction));
        }
      }
    }
    for (BlockPos pos : extraSeeds) {
      for (Direction direction : Constants.DIRECTIONS) {
        seeds.add(pos.relative(direction));
      }
    }
    for (BlockPos pos : seeds) {
      if (level.isLoaded(pos)) {
        adoptOrphans(pos);
      }
    }
    EnergyAudit.check(this, "repairAll");
    return networks.size();
  }

  private final HashSet<EnergyNetwork> pendingRepair = new HashSet<>();
  private final HashSet<BlockPos> pendingAdopt = new HashSet<>();

  public void scheduleRepair(EnergyNetwork network) {
    if (networks.contains(network)) {
      pendingRepair.add(network);
    }
  }

  public void scheduleAdopt(BlockPos pos) {
    pendingAdopt.add(pos.immutable());
  }

  public void adoptPending() {
    if (pendingAdopt.isEmpty()) {
      return;
    }
    for (BlockPos pos : new ArrayList<>(pendingAdopt)) {
      pendingAdopt.remove(pos);
      if (!level.isLoaded(pos) || getNetwork(pos) != null) {
        continue;
      }
      BlockState state = level.getBlockState(pos);
      if (!(state.getBlock() instanceof BlockCable cable)) {
        continue;
      }
      EnergyTier tier = tierOf(pos, cable);
      if (tier != null) {
        onPlaced(pos, state, tier);
      }
    }
  }

  public void repairLoaded() {
    if (pendingRepair.isEmpty()) {
      return;
    }
    for (EnergyNetwork network : new HashSet<>(pendingRepair)) {
      if (!networks.contains(network)) {
        pendingRepair.remove(network);
        continue;
      }
      BlockPos start = null;
      for (BlockPos pos : network.getConnections()) {
        if (!level.isLoaded(pos)) {
          start = null;
          break;
        }
        if (start == null) {
          start = pos;
        }
      }
      if (start == null) {
        continue;
      }
      pendingRepair.remove(network);
      rebuildLinks(network, start);
    }
  }

  private void floodFill(EnergyNetwork network, BlockPos start, HashSet<BlockPos> candidates) {
    ArrayList<BlockPos> queue = new ArrayList<>();
    queue.add(start);
    while (!queue.isEmpty()) {
      BlockPos current = queue.remove(queue.size() - 1);
      if (!candidates.contains(current) || network.getConnections().contains(current)) {
        continue;
      }

      BlockState currentState = level.getBlockState(current);

      if (!(currentState.getBlock() instanceof BlockCable)) {
        continue;
      }
      network.getConnections().add(current);
      for (Direction direction : Constants.DIRECTIONS) {
        if (!linkAllowed(currentState, direction)) {
          continue;
        }
        BlockPos relative = current.relative(direction);
        BlockState relativeState = level.getBlockState(relative);
        if (candidates.contains(relative) && linkAllowed(relativeState, direction.getOpposite())) {
          queue.add(relative);
        }
        if (relativeState.getBlock() instanceof BlockCable cable) {
          if (linkAllowed(relativeState, direction.getOpposite())
              && tierOf(relative, cable) != network.getEnergyTier()) {
            network.getTransmitters().add(relative);
          }
        } else if (EnergyLookup.isPresent(level, relative, direction.getOpposite())) {
          network.getElectrics().add(relative);
        }
      }
    }
  }

  public void neighborChanged(BlockPos pos, BlockPos neighborPos) {
    EnergyNetwork network = getNetwork(pos);
    if (network == null) {
      return;
    }
    BlockPos delta = neighborPos.subtract(pos);
    Direction direction = Direction.getApproximateNearest(delta.getX(), delta.getY(), delta.getZ());
    if (direction == null) {
      return;
    }

    BlockState neighborState = level.getBlockState(neighborPos);
    boolean linked = linkAllowed(level.getBlockState(pos), direction)
        && linkAllowed(neighborState, direction.getOpposite());
    if (linked && neighborState.getBlock() instanceof BlockCable cable) {
      EnergyNetwork other = getNetwork(neighborPos);
      if (other != null && other != network && tierOf(neighborPos, cable) != network.getEnergyTier()) {
        network.getTransmitters().add(neighborPos);
      }
    } else if (linked && EnergyLookup.isPresent(level, neighborPos, null)) {
      network.getElectrics().add(neighborPos);
    } else {
      network.getElectrics().remove(neighborPos);
      network.getTransmitters().remove(neighborPos);
      EnergyNetwork other = getNetworkOther(neighborPos);
      if (other != null && other != network) {
        other.getTransmitters().remove(neighborPos);
        other.getElectrics().remove(neighborPos);
      }
    }
    EnergyAudit.check(this, "neighborChanged");
  }

  public CompoundTag serializeNBT() {
    CompoundTag tag = new CompoundTag();
    int i = 1;
    for (EnergyNetwork network : networks) {
      tag.put(String.valueOf(i), network.serializeNBT());
      i++;
    }
    return tag;
  }

  public void deserializeNBT(CompoundTag tag) {
    for (EnergyNetwork network : networks) {
      network.setOwner(null);
    }
    networks.clear();
    cableOwner.clear();
    ownerIndexDirty = false;
    for (String key : tag.keySet()) {
      if (tag.contains(key)) {
        EnergyNetwork network = new EnergyNetwork();
        network.deserializeNBT(tag.getCompoundOrEmpty(key));
        attach(network);
        pendingRepair.add(network);
      }
    }
    dropDuplicateClaims();
    EnergyAudit.check(this, "deserializeNBT");
  }

  private void dropDuplicateClaims() {
    HashSet<BlockPos> claimed = new HashSet<>();
    for (EnergyNetwork network : new ArrayList<>(networks)) {
      network.getConnections().removeIf(pos -> !claimed.add(pos));
      if (network.getConnections().isEmpty()) {
        detach(network);
        pendingRepair.remove(network);
      }
    }
  }
}

package com.faktocraft.common.block.impl.logistics;

import com.faktocraft.common.util.NbtBridge;
import com.faktocraft.Faktocraft;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.entity.slot.FaktocraftSlot;
import com.faktocraft.common.enums.GuiSlotType;
import com.faktocraft.common.enums.InventorySlotType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import com.faktocraft.common.util.transfer.Capability;
import com.faktocraft.common.util.transfer.ForgeCapabilities;
import com.faktocraft.common.util.transfer.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BlockEntityRequestTable extends FaktocraftBlockEntity {

  public static final int STORAGE_SLOTS = 27;

  private ItemStack ghostTarget = ItemStack.EMPTY;
  private boolean autoExtract;
  private int extractCooldown;
  private final Set<MenuRequestTable> openMenus = new LinkedHashSet<>();
  private final com.faktocraft.common.util.ItemStackHandler craftMatrix =
      new com.faktocraft.common.util.ItemStackHandler(
          9) {
        @Override
        protected void onContentsChanged(int slot) {
          BlockEntityRequestTable.this.setChanged();
        }
      };

  public BlockEntityRequestTable(BlockPos pos, BlockState state) {
    super(LogisticsRegistry.REQUEST_TABLE_BLOCK_ENTITY, pos, state);
  }

  public com.faktocraft.common.util.ItemStackHandler getCraftMatrix() {
    return craftMatrix;
  }

  void attachMenu(MenuRequestTable menu) {
    openMenus.add(menu);
  }

  void detachMenu(MenuRequestTable menu) {
    openMenus.remove(menu);
  }

  void refreshOpenMenus() {
    for (MenuRequestTable menu : List.copyOf(openMenus)) {
      menu.refreshResult();
    }
  }

  @Override
  public ArrayList<FaktocraftSlot> addInventorySlot(ArrayList<FaktocraftSlot> slots) {
    for (int i = 0; i < STORAGE_SLOTS; i++) {
      slots.add(new FaktocraftSlot(i, 8 + (i % 9) * 18, 105 + (i / 9) * 18, InventorySlotType.INPUT,
          GuiSlotType.NORMAL, 7 + (i % 9) * 18, 104 + (i / 9) * 18));
    }
    return super.addInventorySlot(slots);
  }

  @Override
  public boolean isItemValidForSlot(int slot, ItemStack stack) {
    return true;
  }

  @Override
  public void onLoad() {
    super.onLoad();
    if (level != null && !level.isClientSide()) {
      LogisticsCores.markDirtyNear(level, worldPosition);
    }
  }

  public boolean isAutoExtract() {
    return autoExtract;
  }

  public void setAutoExtract(boolean value) {
    this.autoExtract = value;
    setChanged();
  }

  @Override
  public void tickWork(BlockState state) {
    super.tickWork(state);
    if (!autoExtract || level == null || level.isClientSide()) {
      return;
    }
    if (--extractCooldown > 0) {
      return;
    }
    extractCooldown = Math.max(1, com.faktocraft.common.config.ModConfig.server().logistics_extractor_interval);
    BlockEntityLogisticsController core = findCore();
    if (core == null || !core.networkOnline()) {
      return;
    }
    LogisticsGraph graph = core.graph();
    if (graph == null) {
      return;
    }
    int perItem = Math.max(0, com.faktocraft.common.config.ModConfig.server().logistics_energy_per_item);
    int budget = Math.max(1, com.faktocraft.common.config.ModConfig.server().logistics_extractor_items_per_op);
    Endpoint source = Endpoint.tableBuffer(worldPosition);
    List<BlockEntityChassis.SinkCandidate> sinks = null;
    for (int slot = 0; slot < STORAGE_SLOTS && budget > 0; slot++) {
      ItemStack peek = getItemStackHandler().getStackInSlot(slot);
      if (peek.isEmpty()) {
        continue;
      }
      if (sinks == null) {
        sinks = BlockEntityChassis.sinkCandidates(level, graph, null);
      }
      BlockEntityChassis.SinkTarget sink = BlockEntityChassis.findSink(level, sinks, peek);
      if (sink == null) {
        continue;
      }
      int maxByEnergy = perItem > 0 ? core.getEnergyStorage().energyStored() / perItem : peek.getCount();
      int take = Math.min(peek.getCount(), Math.min(budget, maxByEnergy));
      if (take <= 0) {
        return;
      }
      ItemKey key = ItemKey.of(peek);
      int taken = source.extract(level, key, take, false);
      if (taken <= 0) {
        continue;
      }
      core.consumeEnergy(taken * perItem);
      TaskLedger.DeliveryTask moved = core.getLedger().createDelivery(key.stack(taken), source,
          sink.endpoint(), graph.route(worldPosition, sink.nodePos()), 0);
      moved.originPos = worldPosition.asLong();
      budget -= taken;
    }
  }

  public ItemStack getGhostTarget() {
    return ghostTarget;
  }

  public void setGhostTarget(ItemStack stack) {
    this.ghostTarget = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
    setChanged();
  }

  @Nullable
  public BlockEntityLogisticsController findCore() {
    return level != null ? LogisticsCores.coreFor(level, worldPosition) : null;
  }

  public void request(@Nullable ServerPlayer player, int quantity) {
    if (level == null || level.isClientSide()) {
      return;
    }
    if (ghostTarget.isEmpty()) {
      message(player, "no_target");
      return;
    }
    BlockEntityLogisticsController core = findCore();
    if (core == null) {
      message(player, "no_controller");
      return;
    }
    core.submitRequest(ItemKey.of(ghostTarget), quantity, Endpoint.tableBuffer(worldPosition), worldPosition,
        true, player, false);
  }

  private void message(@Nullable ServerPlayer player, String key) {
    LogisticsMessages.error(player, Component.translatable("logistics." + Faktocraft.MODID + "." + key));
  }

  @NotNull
  @Override
  public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
    if (cap == ForgeCapabilities.ITEM_HANDLER) {
      return LazyOptional.empty();
    }
    return super.getCapability(cap, side);
  }

  @Override
  public void preRemoveSideEffects(BlockPos pos, BlockState state) {
    if (level != null && !level.isClientSide()) {
      for (int i = 0; i < craftMatrix.getSlots(); i++) {
        ItemStack stack = craftMatrix.getStackInSlot(i);
        if (!stack.isEmpty()) {
          net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
        }
      }
      LogisticsCores.markDirtyNear(level, pos);
    }
    super.preRemoveSideEffects(pos, state);
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    if (!ghostTarget.isEmpty()) {
      tag.put("ghostTarget", NbtBridge.saveStack(ghostTarget));
    }
    CompoundTag matrixTag = new CompoundTag();
    craftMatrix.save(matrixTag);
    tag.put("craftMatrix", matrixTag);
    tag.putBoolean("autoExtract", autoExtract);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    ghostTarget = tag.contains("ghostTarget")
        ? NbtBridge.loadStack(tag.getCompoundOrEmpty("ghostTarget")) : ItemStack.EMPTY;
    if (tag.contains("craftMatrix")) {
      craftMatrix.load(tag.getCompoundOrEmpty("craftMatrix"));
    }
    autoExtract = tag.getBooleanOr("autoExtract", false);
  }
}

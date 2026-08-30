package com.faktocraft.common.block.impl.pipe;

import com.faktocraft.common.entity.block.FluidStorage;
import com.faktocraft.common.registries.PipeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockEntityTank extends BlockEntity {

  public static final int CAPACITY_MB = 16_000;
  private static final int SETTLE_MB_PER_TICK = 400;

  public final FluidStorage tank = new FluidStorage(CAPACITY_MB);
  public final TankColumnStorage columnStorage = new TankColumnStorage(this);
  private boolean dirtySync;

  private final LazyOptional<IFluidHandler> columnCap = LazyOptional.of(() -> columnStorage);

  public BlockEntityTank(BlockPos pos, BlockState state) {
    super(PipeRegistry.TANK_BLOCK_ENTITY, pos, state);
    tank.setChangeListener(() -> {
      setChanged();
      dirtySync = true;
    });
  }

  public void tick() {
    if (level == null || level.isClientSide()) {
      return;
    }
    BlockEntityTank below = level.getBlockEntity(worldPosition.below()) instanceof BlockEntityTank b ? b : null;

    if (!tank.isEmpty() && below != null
        && (below.tank.isEmpty() || below.tank.getFluidStack().isFluidEqual(tank.getFluidStack()))) {
      int moved = below.tank.fillFluid(tank.getFluidStack(),
          Math.min(SETTLE_MB_PER_TICK, tank.getFluidAmount()), false);
      if (moved > 0) {
        tank.takeFluid(moved, false);
      }
    }

    BlockState state = getBlockState();
    if (state.hasProperty(BlockTank.JOINED_BELOW)) {
      boolean joined = below != null
          && (below.tank.isEmpty() || tank.isEmpty()
              || below.tank.getFluidStack().isFluidEqual(tank.getFluidStack()));
      if (state.getValue(BlockTank.JOINED_BELOW) != joined) {
        level.setBlockAndUpdate(worldPosition, state.setValue(BlockTank.JOINED_BELOW, joined));
      }
    }

    if (dirtySync && level.getGameTime() % 10 == 0) {
      dirtySync = false;
      level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
  }

  @NotNull
  @Override
  public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
    if (cap == ForgeCapabilities.FLUID_HANDLER) {
      return columnCap.cast();
    }
    return super.getCapability(cap, side);
  }

  @Override
  public void setRemoved() {
    super.setRemoved();
    columnCap.invalidate();
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    CompoundTag tankTag = new CompoundTag();
    tank.save(tankTag);
    tag.put("tank", tankTag);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    if (tag.contains("tank")) {
      tank.load(tag.getCompound("tank"));
    }
  }

  @Override
  public CompoundTag getUpdateTag() {
    CompoundTag tag = new CompoundTag();
    saveAdditional(tag);
    return tag;
  }

  @Nullable
  @Override
  public Packet<ClientGamePacketListener> getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }
}

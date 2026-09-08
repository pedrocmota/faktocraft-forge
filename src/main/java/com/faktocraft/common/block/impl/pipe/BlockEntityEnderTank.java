package com.faktocraft.common.block.impl.pipe;

import com.faktocraft.common.entity.block.FluidStorage;
import com.faktocraft.common.registries.PipeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Locale;

public class BlockEntityEnderTank extends BlockEntity {

  private static final int SYNC_INTERVAL = 5;

  private int code = EnderTankChannels.CODE_NONE;
  public final FluidStorage view = new FluidStorage(EnderTankChannels.CAPACITY_MB);
  private final IFluidHandler handler = new Handler();
  private final LazyOptional<IFluidHandler> handlerCap = LazyOptional.of(() -> handler);

  public BlockEntityEnderTank(BlockPos pos, BlockState state) {
    super(PipeRegistry.ENDER_TANK_BLOCK_ENTITY, pos, state);
    view.setChangeListener(() -> {
    });
  }

  public int code() {
    return code;
  }

  public boolean hasCode() {
    return EnderTankChannels.isValidCode(code);
  }

  public String codeText() {
    return hasCode() ? String.format(Locale.ROOT, "%0" + EnderTankChannels.CODE_DIGITS + "d", code) : "";
  }

  public void setCode(int newCode) {
    int accepted = EnderTankChannels.isValidCode(newCode) ? newCode : EnderTankChannels.CODE_NONE;
    if (accepted == code) {
      return;
    }
    code = accepted;
    setChanged();
    sync(true);
  }

  @Nullable
  private FluidStorage channel() {
    if (!hasCode() || !(level instanceof ServerLevel serverLevel)) {
      return null;
    }
    return EnderTankChannels.get(serverLevel).channel(code);
  }

  public void tick() {
    if (level == null || level.isClientSide() || level.getGameTime() % SYNC_INTERVAL != 0) {
      return;
    }
    sync(false);
  }

  private void sync(boolean force) {
    if (level == null || level.isClientSide()) {
      return;
    }
    FluidStorage channel = channel();
    FluidStack current = channel != null ? channel.getFluidStack() : FluidStack.EMPTY;
    if (!force && current.isFluidStackIdentical(view.getFluidStack())) {
      return;
    }
    view.setFluid(current, current.getAmount());
    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
  }

  @NotNull
  @Override
  public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
    if (cap == ForgeCapabilities.FLUID_HANDLER) {
      return handlerCap.cast();
    }
    return super.getCapability(cap, side);
  }

  @Override
  public void setRemoved() {
    super.setRemoved();
    handlerCap.invalidate();
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    tag.putInt("code", code);
    CompoundTag viewTag = new CompoundTag();
    view.save(viewTag);
    tag.put("view", viewTag);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    code = tag.contains("code") ? tag.getInt("code") : EnderTankChannels.CODE_NONE;
    if (tag.contains("view")) {
      view.load(tag.getCompound("view"));
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

  private final class Handler implements IFluidHandler {

    @Override
    public int getTanks() {
      return 1;
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
      FluidStorage channel = channel();
      return channel != null ? channel.getFluidInTank(0) : view.getFluidInTank(0);
    }

    @Override
    public int getTankCapacity(int tank) {
      return EnderTankChannels.CAPACITY_MB;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
      return hasCode();
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
      FluidStorage channel = channel();
      return channel != null ? channel.fill(resource, action) : 0;
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
      FluidStorage channel = channel();
      return channel != null ? channel.drain(resource, action) : FluidStack.EMPTY;
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
      FluidStorage channel = channel();
      return channel != null ? channel.drain(maxDrain, action) : FluidStack.EMPTY;
    }
  }
}

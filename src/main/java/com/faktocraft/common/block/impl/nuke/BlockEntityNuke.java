package com.faktocraft.common.block.impl.nuke;

import com.faktocraft.common.util.LegacyNbtBlockEntity;
import com.faktocraft.common.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityNuke extends LegacyNbtBlockEntity {

  private static final int FLASH_INTERVAL = 6;
  private static final int BEEP_INTERVAL = 20;

  private int fuse = -1;
  private int total = 1;

  public BlockEntityNuke(BlockPos pos, BlockState state) {
    super(NukeRegistry.NUKE_BLOCK_ENTITY, pos, state);
  }

  public boolean isPrimed() {
    return fuse >= 0;
  }

  public int getFuse() {
    return fuse;
  }

  public void prime() {
    if (fuse < 0) {
      total = Math.max(1, ModConfig.server().nuke_fuse_ticks);
      fuse = total;
      setChanged();
    }
  }

  public void tick() {
    if (fuse < 0 || !(level instanceof ServerLevel serverLevel)) {
      return;
    }
    if (fuse == 0) {
      fuse = -1;
      serverLevel.removeBlock(worldPosition, false);
      NukeBlast.start(serverLevel, worldPosition);
      return;
    }
    fuse--;
    BlockState state = getBlockState();
    if (fuse % FLASH_INTERVAL == 0 && state.hasProperty(BlockNuke.LIT)) {
      serverLevel.setBlock(worldPosition, state.cycle(BlockNuke.LIT), Block.UPDATE_ALL);
    }
    if (fuse % BEEP_INTERVAL == 0 && fuse > 0) {
      float pitch = 0.8F + (1.0F - fuse / (float) total) * 0.8F;
      serverLevel.playSound(null, worldPosition, SoundEvents.NOTE_BLOCK_BIT.value(), SoundSource.BLOCKS, 1.0F, pitch);
    }
    setChanged();
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    tag.putInt("fuse", fuse);
    tag.putInt("total", total);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    fuse = tag.contains("fuse") ? tag.getIntOr("fuse", 0) : -1;
    total = Math.max(1, tag.getIntOr("total", 0));
  }
}

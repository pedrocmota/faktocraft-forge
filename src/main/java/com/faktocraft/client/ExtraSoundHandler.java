package com.faktocraft.client;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class ExtraSoundHandler {

  private static final Long2ObjectMap<SoundInstance> EXTRA_SOUNDS = new Long2ObjectOpenHashMap<>();

  private static class LoopingSound extends AbstractSoundInstance {

    LoopingSound(SoundEvent soundEvent, BlockPos pos) {
      super(soundEvent, SoundSource.BLOCKS, RandomSource.create());
      this.x = pos.getX() + 0.5;
      this.y = pos.getY() + 0.5;
      this.z = pos.getZ() + 0.5;
      this.looping = true;
      this.delay = 0;
    }
  }

  public static void ensurePlaying(SoundEvent soundEvent, BlockPos pos) {
    long key = pos.asLong();
    var soundManager = Minecraft.getInstance().getSoundManager();
    SoundInstance current = EXTRA_SOUNDS.get(key);
    if (current == null || !soundManager.isActive(current)) {
      SoundInstance sound = new LoopingSound(soundEvent, pos);
      EXTRA_SOUNDS.put(key, sound);
      soundManager.play(sound);
    }
  }

  public static void stop(BlockPos pos) {
    SoundInstance sound = EXTRA_SOUNDS.remove(pos.asLong());
    if (sound != null) {
      Minecraft.getInstance().getSoundManager().stop(sound);
    }
  }
}

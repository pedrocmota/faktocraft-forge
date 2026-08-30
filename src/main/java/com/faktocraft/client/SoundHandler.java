package com.faktocraft.client;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class SoundHandler {

  private static final Long2ObjectMap<SoundInstance> SOUND_MAP = new Long2ObjectOpenHashMap<>();

  public static void startTileSound(SoundEvent soundEvent, float volume, BlockPos pos) {
    long key = pos.asLong();
    SoundInstance existing = SOUND_MAP.get(key);
    if (existing != null) {
      if (Minecraft.getInstance().getSoundManager().isActive(existing)) {
        if (existing instanceof TileTickableSound tile) {
          tile.cancelFadeOut();
        }
        return;
      }
      SOUND_MAP.remove(key);
    }
    TileTickableSound sound = new TileTickableSound(soundEvent, SoundSource.BLOCKS, volume, pos, key);
    SOUND_MAP.put(key, sound);
    Minecraft.getInstance().getSoundManager().play(sound);
  }

  public static void stopTileSound(BlockPos pos) {
    long key = pos.asLong();
    SoundInstance sound = SOUND_MAP.get(key);
    if (sound instanceof TileTickableSound tile) {
      tile.beginFadeOut();
    } else if (sound != null) {
      SOUND_MAP.remove(key);
      Minecraft.getInstance().getSoundManager().stop(sound);
    }
  }

  private static class TileTickableSound extends AbstractTickableSoundInstance {

    private static final int FADE_OUT_TICKS = 8;

    private final float baseVolume;
    private final long mapKey;
    private int fadeTicksLeft = -1;

    TileTickableSound(SoundEvent soundEvent, SoundSource source, float volume, BlockPos pos, long mapKey) {
      super(soundEvent, source, RandomSource.create());
      this.x = pos.getX() + 0.5F;
      this.y = pos.getY() + 0.5F;
      this.z = pos.getZ() + 0.5F;
      this.looping = true;
      this.delay = 0;
      this.baseVolume = volume;
      this.mapKey = mapKey;
      this.volume = volume * ClientSoundConfig.machineVolume();
    }

    void beginFadeOut() {
      if (fadeTicksLeft < 0) {
        fadeTicksLeft = FADE_OUT_TICKS;
      }
    }

    void cancelFadeOut() {
      fadeTicksLeft = -1;
    }

    @Override
    public void tick() {
      float target = baseVolume * ClientSoundConfig.machineVolume();
      if (fadeTicksLeft < 0) {
        this.volume = target;
        return;
      }
      if (fadeTicksLeft == 0) {
        this.volume = 0.0F;
        SOUND_MAP.remove(mapKey, this);
        stop();
        return;
      }
      this.volume = target * fadeTicksLeft / (float) FADE_OUT_TICKS;
      fadeTicksLeft--;
    }

    @Override
    public boolean canStartSilent() {
      return true;
    }
  }
}

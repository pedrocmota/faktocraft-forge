package com.faktocraft.client;

import com.faktocraft.common.item.impl.nano.ItemNanosaber;
import com.faktocraft.common.registries.ModSounds;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public final class NanoSaberSoundHandler {

  private static final Int2ObjectMap<HumSound> SOUNDS = new Int2ObjectOpenHashMap<>();

  private NanoSaberSoundHandler() {
  }

  public static void tick(Minecraft minecraft) {
    if (minecraft.level == null) {
      SOUNDS.clear();
      return;
    }
    for (Player player : minecraft.level.players()) {
      if (!holdsActiveSaber(player)) {
        continue;
      }
      HumSound current = SOUNDS.get(player.getId());
      if (current == null || !minecraft.getSoundManager().isActive(current)) {
        HumSound sound = new HumSound(player);
        SOUNDS.put(player.getId(), sound);
        minecraft.getSoundManager().play(sound);
      }
    }
  }

  private static boolean holdsActiveSaber(Player player) {
    for (InteractionHand hand : InteractionHand.values()) {
      var stack = player.getItemInHand(hand);
      if (stack.getItem() instanceof ItemNanosaber && ItemNanosaber.isActive(stack)) {
        return true;
      }
    }
    return false;
  }

  private static class HumSound extends AbstractTickableSoundInstance {

    private final Player holder;

    HumSound(Player holder) {
      super(ModSounds.NANO_SABER_HUM, SoundSource.PLAYERS, RandomSource.create());
      this.holder = holder;
      this.looping = true;
      this.delay = 0;
      this.volume = 0.5F;
      this.x = holder.getX();
      this.y = holder.getY();
      this.z = holder.getZ();
    }

    @Override
    public void tick() {
      if (holder.isRemoved() || holder.level() != Minecraft.getInstance().level
          || !holdsActiveSaber(holder)) {
        SOUNDS.remove(holder.getId(), this);
        stop();
        return;
      }
      this.x = holder.getX();
      this.y = holder.getY();
      this.z = holder.getZ();
    }
  }
}

package com.faktocraft.client;

import com.faktocraft.common.item.impl.armor.JetpackItem;
import com.faktocraft.common.registries.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class JetpackSoundHandler {

  private static WhooshSound current;

  private JetpackSoundHandler() {
  }

  public static void tick(Minecraft minecraft) {
    if (minecraft.level == null || minecraft.player == null) {
      current = null;
      return;
    }
    if (!isThrusting(minecraft.player)) {
      return;
    }
    if (current == null || !minecraft.getSoundManager().isActive(current)) {
      current = new WhooshSound(minecraft.player);
      minecraft.getSoundManager().play(current);
    }
  }

  private static boolean isThrusting(Player player) {
    ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
    return chest.getItem() instanceof JetpackItem
        && player.getPersistentData().getBooleanOr(JetpackItem.TAG_THRUST, false)
        && !player.getAbilities().flying
        && JetpackItem.drainFuel(chest, 1, true);
  }

  private static class WhooshSound extends AbstractTickableSoundInstance {

    private final Player holder;

    WhooshSound(Player holder) {
      super(ModSounds.JETPACK, SoundSource.PLAYERS, RandomSource.create());
      this.holder = holder;
      this.looping = true;
      this.delay = 0;
      this.volume = 0.3F;
      this.x = holder.getX();
      this.y = holder.getY();
      this.z = holder.getZ();
    }

    @Override
    public void tick() {
      if (holder.isRemoved() || holder.level() != Minecraft.getInstance().level || !isThrusting(holder)) {
        stop();
        return;
      }
      this.x = holder.getX();
      this.y = holder.getY();
      this.z = holder.getZ();
    }
  }
}

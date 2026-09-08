package com.faktocraft.common.util;

import com.faktocraft.common.capabilities.player.PlayerData;
import com.faktocraft.common.interfaces.item.IArmorProperties;
import com.faktocraft.common.registries.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class NightVisionHandler {

  public static final int COST_PER_SECOND = 20;

  private static final net.minecraft.world.entity.EquipmentSlot[] ARMOR_SLOTS = {
      net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.entity.EquipmentSlot.CHEST,
      net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.FEET
  };

  public static Iterable<ItemStack> armorItems(Player player) {
    java.util.List<ItemStack> list = new java.util.ArrayList<>(4);
    for (var slot : ARMOR_SLOTS) {
      list.add(player.getItemBySlot(slot));
    }
    return list;
  }

  @org.jetbrains.annotations.Nullable
  private static ItemStack nightVisionStack(Player player) {
    for (ItemStack stack : armorItems(player)) {
      if (!stack.isEmpty() && stack.getItem() instanceof IArmorProperties armorProperties
          && armorProperties.supportsNightVision()) {
        return stack;
      }
    }
    return null;
  }

  private static boolean tryDrain(ItemStack stack) {
    if (!(stack.getItem() instanceof com.faktocraft.common.interfaces.item.IElectricItem electricItem)) {
      return true;
    }
    var energy = electricItem.getEnergy(stack);
    if (energy == null || energy.energyStored() < COST_PER_SECOND) {
      return false;
    }
    energy.consumeEnergy(COST_PER_SECOND, false);
    electricItem.tickElectric(stack);
    return true;
  }

  public static void toggle(Player player) {
    ItemStack stack = nightVisionStack(player);
    if (stack == null) {
      return;
    }
    boolean enable = !PlayerData.getNightVision(player);
    if (enable && !tryDrain(stack)) {
      return;
    }
    apply(player, enable);
  }

  public static void check(Player player) {
    if (!PlayerData.getNightVision(player)) {
      return;
    }
    ItemStack stack = nightVisionStack(player);
    if (stack == null || !tryDrain(stack)) {
      apply(player, false);
    }
  }

  private static void apply(Player player, boolean enable) {
    PlayerData.setNightVision(player, enable);
    if (enable) {
      MobEffectInstance effect = new MobEffectInstance(MobEffects.NIGHT_VISION, 1000000, 100, false, false);
      player.addEffect(effect);
    } else {
      player.removeEffect(MobEffects.NIGHT_VISION);
    }
    float pitch = 0.8F / (player.getRandom().nextFloat() * 0.4F + 0.8F);
    player.level().playSound(null, player.blockPosition(), ModSounds.NIGHT_VISION, SoundSource.PLAYERS, 1F, pitch);
  }
}

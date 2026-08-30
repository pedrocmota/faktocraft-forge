package com.faktocraft.common.item.impl.armor;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class AdvancedJetpackItem extends JetpackItem {

  private static final String TAG_MODE = "JetpackMode";

  public AdvancedJetpackItem(Properties properties) {
    super(properties, 12000);
  }

  public static boolean isElytraMode(ItemStack stack) {
    return stack.hasTag() && stack.getTag().getInt(TAG_MODE) == 1;
  }

  public static void toggleMode(ServerPlayer player) {
    ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
    if (!(chest.getItem() instanceof AdvancedJetpackItem)) {
      return;
    }
    boolean elytra = !isElytraMode(chest);
    chest.getOrCreateTag().putInt(TAG_MODE, elytra ? 1 : 0);
    player.displayClientMessage(Component.translatable(
        elytra ? "gui.faktocraft.jetpack.mode_elytra" : "gui.faktocraft.jetpack.mode_vertical"), true);
  }

  @Override
  protected void jetpackTick(ItemStack stack, Level level, Player player) {
    if (player.noCulling && !player.isFallFlying()) {
      player.noCulling = false;
    }
    if (!isElytraMode(stack)) {
      super.jetpackTick(stack, level, player);
    } else if (!player.isFallFlying()
        && player.getPersistentData().getBoolean(TAG_THRUST)
        && !drainFuel(stack, 1, true)) {
      glideTick(level, player);
    }
  }

  @Override
  public boolean canElytraFly(ItemStack stack, LivingEntity entity) {

    return isElytraMode(stack) && drainFuel(stack, 1, true)
        && (entity.isFallFlying() || entity.getPersistentData().getInt(TAG_AIR_TICKS) >= 3);
  }

  @Override
  public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
    Level level = entity.level();

    entity.noCulling = true;
    entity.getPersistentData().putBoolean(TAG_GLIDE, true);
    boolean boosting = entity.getPersistentData().getBoolean(TAG_THRUST)
        && drainFuel(stack, BOOST_MB_PER_TICK, true);
    if (boosting) {
      Vec3 look = entity.getLookAngle();
      Vec3 dm = entity.getDeltaMovement();
      entity.setDeltaMovement(dm.add(
          look.x * 0.1 + (look.x * 1.5 - dm.x) * 0.5,
          look.y * 0.1 + (look.y * 1.5 - dm.y) * 0.5,
          look.z * 0.1 + (look.z * 1.5 - dm.z) * 0.5));
      if (level.isClientSide()) {
        double yOff = com.faktocraft.client.ClientCamera.isFirstPerson() ? 0.6 : 0.0;
        Vec3 tail = entity.position().add(0, yOff, 0).subtract(look.scale(1.2));
        if (flightTicks % 5 == 0) {
          level.addParticle(net.minecraft.core.particles.ParticleTypes.FIREWORK,
              tail.x, tail.y, tail.z, -look.x * 0.3, -look.y * 0.3, -look.z * 0.3);
        }
        if (flightTicks % 10 == 0) {
          level.addParticle(net.minecraft.core.particles.ParticleTypes.FLAME,
              tail.x, tail.y, tail.z, -look.x * 0.2, -look.y * 0.2, -look.z * 0.2);
        }
      }
    }
    if (!level.isClientSide()) {
      if (boosting) {
        drainFuel(stack, BOOST_MB_PER_TICK, false);
      }
      if (flightTicks % 4 == 0) {
        drainFuel(stack, GLIDE_MB_PER_4_TICKS, false);
      }
    }
    return true;
  }

  @Nullable
  @Override
  public String getArmorTexture(ItemStack stack, net.minecraft.world.entity.Entity entity,
      EquipmentSlot slot, String type) {
    return "faktocraft:textures/models/armor/advanced_jetpack_layer_1.png";
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(Component.translatable(
        isElytraMode(stack) ? "gui.faktocraft.jetpack.mode_elytra" : "gui.faktocraft.jetpack.mode_vertical")
        .withStyle(ChatFormatting.DARK_AQUA));
    super.appendHoverText(stack, level, tooltip, flag);
  }
}

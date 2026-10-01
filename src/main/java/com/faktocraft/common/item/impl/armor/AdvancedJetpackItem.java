package com.faktocraft.common.item.impl.armor;

import com.faktocraft.common.enums.ModArmorMaterials;
import com.faktocraft.common.util.NbtBridge;
import com.faktocraft.common.util.PlayerMessages;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.function.Consumer;

public class AdvancedJetpackItem extends JetpackItem {
  private static final String TAG_MODE = "JetpackMode";

  public AdvancedJetpackItem(Properties properties) {
    super(ModArmorMaterials.ADVANCED_JETPACK, properties, 12000);
  }

  public static boolean isElytraMode(ItemStack stack) {
    return NbtBridge.hasCustomData(stack) && NbtBridge.customDataOrEmpty(stack).getIntOr(TAG_MODE, 0) == 1;
  }

  public static void toggleMode(ServerPlayer player) {
    ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
    if (!(chest.getItem() instanceof AdvancedJetpackItem)) {
      return;
    }
    boolean elytra = !isElytraMode(chest);
    NbtBridge.updateCustomData(chest, tag -> tag.putInt(TAG_MODE, elytra ? 1 : 0));
    syncGlider(chest, player);
    PlayerMessages.display(player, Component.translatable(
        elytra ? "gui.faktocraft.jetpack.mode_elytra" : "gui.faktocraft.jetpack.mode_vertical"), true);
  }

  @Override
  public void wornTick(ItemStack stack, Level level, Entity entity) {
    super.wornTick(stack, level, entity);
    if (!(entity instanceof Player player) || player.getItemBySlot(EquipmentSlot.CHEST) != stack) {
      return;
    }
    if (!level.isClientSide()) {
      syncGlider(stack, player);
    }
    if (player.isFallFlying() && canElytraFly(stack, player)) {
      elytraFlightTick(stack, player, player.getFallFlyingTicks());
    }
  }

  @Override
  protected void jetpackTick(ItemStack stack, Level level, Player player) {
    if (!isElytraMode(stack)) {
      super.jetpackTick(stack, level, player);
    } else if (!player.isFallFlying()
        && player.getPersistentData().getBooleanOr(TAG_THRUST, false)
        && !drainFuel(stack, 1, true)) {
      glideTick(level, player);
    }
  }

  public static boolean canElytraFly(ItemStack stack, LivingEntity entity) {
    return isElytraMode(stack) && drainFuel(stack, 1, true)
        && (entity.isFallFlying() || entity.getPersistentData().getIntOr(TAG_AIR_TICKS, 0) >= 3);
  }

  private static void syncGlider(ItemStack stack, LivingEntity entity) {
    boolean glider = canElytraFly(stack, entity);
    if (glider != stack.has(DataComponents.GLIDER)) {
      if (glider) {
        stack.set(DataComponents.GLIDER, Unit.INSTANCE);
      } else {
        stack.remove(DataComponents.GLIDER);
      }
    }
  }

  public void elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
    Level level = entity.level();

    entity.getPersistentData().putBoolean(TAG_GLIDE, true);
    boolean boosting = entity.getPersistentData().getBooleanOr(TAG_THRUST, false)
        && drainFuel(stack, BOOST_MB_PER_TICK, true);
    if (boosting) {
      Vec3 look = entity.getLookAngle();
      Vec3 dm = entity.getDeltaMovement();
      entity.setDeltaMovement(dm.add(
          look.x * 0.1 + (look.x * 1.5 - dm.x) * 0.5,
          look.y * 0.1 + (look.y * 1.5 - dm.y) * 0.5,
          look.z * 0.1 + (look.z * 1.5 - dm.z) * 0.5));
      if (level.isClientSide()) {
        double yOff = com.faktocraft.common.util.ClientProxy.get().isFirstPerson() ? 0.6 : 0.0;
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
  }

  @Override
  public void onGlideDamage(ItemStack stack, LivingEntity wearer, EquipmentSlot slot) {
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext level, TooltipDisplay display,
      Consumer<Component> tooltip, TooltipFlag flag) {
    tooltip.accept(Component.translatable(
        isElytraMode(stack) ? "gui.faktocraft.jetpack.mode_elytra" : "gui.faktocraft.jetpack.mode_vertical")
        .withStyle(ChatFormatting.DARK_AQUA));
    super.appendHoverText(stack, level, display, tooltip, flag);
  }
}

package com.faktocraft.common.item.impl.tools;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.item.base.BaseItem;
import com.faktocraft.common.radiation.RadiationManager;
import com.faktocraft.common.registries.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Locale;

public class GeigerCounter extends BaseItem {

  private static final String TAG_DOSE = "GeigerDose";
  private static final int MEASURE_INTERVAL = 10;
  private static final float SILENT_BELOW = 0.05F;
  private static final float BACKGROUND_RATE = 0.015F;
  private static final float BASE_RATE = 0.08F;
  private static final float RATE_PER_RAD = 0.35F;
  private static final float MAX_RATE = 0.9F;

  public GeigerCounter(Properties properties) {
    super(properties.stacksTo(1));
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(Component.translatable("geiger." + Faktocraft.MODID + ".desc").withStyle(ChatFormatting.GRAY));
    super.appendHoverText(stack, level, tooltip, flag);
  }

  @Override
  public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
    return slotChanged || oldStack.getItem() != newStack.getItem();
  }

  private static boolean held(Player player, ItemStack stack) {
    return player.getMainHandItem() == stack || player.getOffhandItem() == stack;
  }

  public static float dose(ItemStack stack) {
    return stack.hasTag() ? stack.getTag().getFloat(TAG_DOSE) : 0.0F;
  }

  public static float doseLevel(ItemStack stack) {
    float dose = dose(stack);
    if (dose < SILENT_BELOW) {
      return 0.0F;
    }
    if (dose < 0.5F) {
      return 0.25F;
    }
    if (dose < 1.0F) {
      return 0.5F;
    }
    return dose < 4.0F ? 0.75F : 1.0F;
  }

  @Override
  public void inventoryTick(ItemStack stack, Level level, Entity owner, int slot, boolean selected) {
    if (!(level instanceof ServerLevel serverLevel) || !(owner instanceof Player player) || !held(player, stack)) {
      return;
    }
    float dose = dose(stack);
    if (level.getGameTime() % MEASURE_INTERVAL == 0) {
      RadiationManager.Reading reading = RadiationManager.measure(serverLevel, player);
      dose = reading.total();
      stack.getOrCreateTag().putFloat(TAG_DOSE, dose);
      player.displayClientMessage(readingLine(dose), true);
    }
    boolean quiet = dose < SILENT_BELOW;
    float rate = quiet ? BACKGROUND_RATE : Math.min(MAX_RATE, BASE_RATE + dose * RATE_PER_RAD);
    if (level.random.nextFloat() < rate) {
      float pitch = 0.9F + level.random.nextFloat() * 0.2F;
      level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.GEIGER_CLICK,
          SoundSource.PLAYERS, quiet ? 0.25F : 0.45F, pitch);
    }
  }

  private static Component readingLine(float dose) {
    ChatFormatting color = dose < SILENT_BELOW ? ChatFormatting.GREEN
        : dose < 1.0F ? ChatFormatting.YELLOW
            : dose < 4.0F ? ChatFormatting.GOLD
                : ChatFormatting.RED;
    return Component.translatable("geiger." + Faktocraft.MODID + ".reading", format(dose)).withStyle(color);
  }

  private static String format(float value) {
    return String.format(Locale.ROOT, "%.2f", value);
  }
}

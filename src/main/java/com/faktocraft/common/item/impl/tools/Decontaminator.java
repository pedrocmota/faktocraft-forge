package com.faktocraft.common.item.impl.tools;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.item.base.BaseItem;
import com.faktocraft.common.radiation.RadiationExposure;
import com.faktocraft.common.radiation.RadiationSources;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class Decontaminator extends BaseItem {

  public static final double RANGE = 64.0;

  public Decontaminator(Properties properties) {
    super(properties.stacksTo(1));
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (!(level instanceof ServerLevel serverLevel)) {
      return InteractionResultHolder.sidedSuccess(stack, true);
    }
    if (!player.isCreative()) {
      player.displayClientMessage(Component.translatable("chat." + Faktocraft.MODID + ".decontaminator_creative")
          .withStyle(ChatFormatting.RED), true);
      return InteractionResultHolder.fail(stack);
    }
    int removed = RadiationSources.get(serverLevel).clearAftermathNear(serverLevel, player.blockPosition(), RANGE);
    RadiationExposure.set(player, 0.0F);
    RadiationExposure.setPendingDamage(player, 0.0F);
    player.displayClientMessage(Component.translatable("chat." + Faktocraft.MODID + ".decontaminated", removed)
        .withStyle(removed > 0 ? ChatFormatting.GREEN : ChatFormatting.GRAY), true);
    serverLevel.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.8F, 1.4F);
    return InteractionResultHolder.consume(stack);
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(Component.translatable("tooltip." + Faktocraft.MODID + ".decontaminator")
        .withStyle(ChatFormatting.GRAY));
    tooltip.add(Component.translatable("tooltip." + Faktocraft.MODID + ".creative_only")
        .withStyle(ChatFormatting.LIGHT_PURPLE));
    super.appendHoverText(stack, level, tooltip, flag);
  }
}

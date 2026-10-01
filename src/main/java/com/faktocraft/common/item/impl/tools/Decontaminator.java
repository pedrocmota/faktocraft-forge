package com.faktocraft.common.item.impl.tools;

import com.faktocraft.common.util.PlayerMessages;
import net.minecraft.world.item.Item;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class Decontaminator extends BaseItem {

  public static final double RANGE = 64.0;

  public Decontaminator(Properties properties) {
    super(properties.stacksTo(1));
  }

  @Override
  public InteractionResult use(Level level, Player player, InteractionHand hand) {
    if (!(level instanceof ServerLevel serverLevel)) {
      return InteractionResult.SUCCESS;
    }
    if (!player.isCreative()) {
      PlayerMessages.display(player, Component.translatable("chat." + Faktocraft.MODID + ".decontaminator_creative")
          .withStyle(ChatFormatting.RED), true);
      return InteractionResult.FAIL;
    }
    int removed = RadiationSources.get(serverLevel).clearAftermathNear(serverLevel, player.blockPosition(), RANGE);
    RadiationExposure.set(player, 0.0F);
    RadiationExposure.setPendingDamage(player, 0.0F);
    PlayerMessages.display(player, Component.translatable("chat." + Faktocraft.MODID + ".decontaminated", removed)
        .withStyle(removed > 0 ? ChatFormatting.GREEN : ChatFormatting.GRAY), true);
    serverLevel.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.8F, 1.4F);
    return InteractionResult.CONSUME;
  }

  @Override
  @SuppressWarnings("deprecation")
  public void appendHoverText(ItemStack stack, Item.TooltipContext level, TooltipDisplay display,
      Consumer<Component> tooltip, TooltipFlag flag) {
    tooltip.accept(Component.translatable("tooltip." + Faktocraft.MODID + ".decontaminator")
        .withStyle(ChatFormatting.GRAY));
    tooltip.accept(Component.translatable("tooltip." + Faktocraft.MODID + ".creative_only")
        .withStyle(ChatFormatting.LIGHT_PURPLE));
    super.appendHoverText(stack, level, display, tooltip, flag);
  }
}

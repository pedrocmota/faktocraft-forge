package com.faktocraft.common.item.impl.tools;

import com.faktocraft.IndReb;
import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.energy.WindSim;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.item.base.ElectricItem;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketWindInfo;
import com.faktocraft.common.registries.ModComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class WindMeter extends ElectricItem {

  public static final int ENERGY_PER_READING = 20;

  public WindMeter(Properties properties) {
    super(properties, 0, 2000, EnergyType.RECEIVE, EnergyTier.LOW);
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(Component.translatable("wind_meter." + IndReb.MODID + ".desc").withStyle(ChatFormatting.GRAY));
    super.appendHoverText(stack, level, tooltip, flag);
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (level instanceof ServerLevel serverLevel) {
      int energy = ModComponents.getEnergy(stack, initialEnergy);
      if (energy < ENERGY_PER_READING && !player.isCreative()) {
        player.displayClientMessage(
            Component.translatable("wind_meter." + IndReb.MODID + ".no_energy").withStyle(ChatFormatting.RED),
            true);
        return InteractionResultHolder.fail(stack);
      }
      if (!player.isCreative()) {
        ModComponents.setEnergy(stack, energy - ENERGY_PER_READING);
      }

      double wind = WindSim.getEffectiveStrength(serverLevel);
      int y = player.blockPosition().getY();
      int estimate = (int) Math.round(
          ModConfig.server().wind_generator_max_tick_generate * wind * WindSim.heightFactor(y));

      if (player instanceof ServerPlayer serverPlayer) {
        ModNetworking.sendToPlayer(serverPlayer,
            new PacketWindInfo((int) Math.round(wind * 100.0), y, estimate));
      }
    }
    return InteractionResultHolder.success(stack);
  }
}

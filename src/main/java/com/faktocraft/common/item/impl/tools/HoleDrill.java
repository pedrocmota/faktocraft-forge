package com.faktocraft.common.item.impl.tools;

import com.faktocraft.common.util.PlayerMessages;
import net.minecraft.world.item.Item;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.cover.CoverSupport;
import com.faktocraft.common.cover.DrillOps;
import com.faktocraft.common.energy.interfaces.IEnergy;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.item.base.ElectricItem;
import com.faktocraft.common.registries.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

public class HoleDrill extends ElectricItem {

  private static final String TARGET_KEY = "faktocraftDrillTarget";

  public HoleDrill(Properties properties) {
    super(properties, 0, 10_000, EnergyType.RECEIVE, EnergyTier.LOW);
  }

  public static int drillTicks() {
    return Math.max(1, ModConfig.server().hole_drill_ticks);
  }

  public static int energyCost() {
    return ModConfig.server().hole_drill_energy_cost;
  }

  @Nullable
  public static BlockHitResult target(Level level, Player player) {
    HitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
    return hit.getType() == HitResult.Type.BLOCK ? (BlockHitResult) hit : null;
  }

  private boolean hasEnergy(ItemStack stack) {
    IEnergy energy = getEnergy(stack);
    return energy != null && energy.energyStored() >= energyCost();
  }

  @Override
  public InteractionResult use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    BlockHitResult hit = target(level, player);
    if (hit == null) {
      return InteractionResult.PASS;
    }
    BlockPos pos = hit.getBlockPos();
    if (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos, hit.getDirection(), stack)) {
      return InteractionResult.FAIL;
    }
    BlockState state = level.getBlockState(pos);
    if (DrillOps.drillFace(level, pos, state, player, hit.getDirection()) == null) {
      if (DrillOps.isBored(state)) {
        return InteractionResult.PASS;
      }
      if (!level.isClientSide()) {
        PlayerMessages.display(player, Component.translatable("chat.faktocraft.drill_invalid")
            .withStyle(ChatFormatting.RED), true);
      }
      return InteractionResult.FAIL;
    }
    if (!hasEnergy(stack)) {
      if (!level.isClientSide()) {
        PlayerMessages.display(player, Component.translatable("chat.faktocraft.drill_no_energy")
            .withStyle(ChatFormatting.RED), true);
      }
      return InteractionResult.FAIL;
    }
    player.getPersistentData().putLong(TARGET_KEY, pos.asLong());
    player.startUsingItem(hand);
    return InteractionResult.CONSUME;
  }

  @Override
  public int getUseDuration(ItemStack stack, LivingEntity entity) {
    return drillTicks();
  }

  @Override
  public ItemUseAnimation getUseAnimation(ItemStack stack) {
    return ItemUseAnimation.NONE;
  }

  @Nullable
  private static BlockHitResult activeHit(Level level, LivingEntity living) {
    if (!(living instanceof Player player)) {
      return null;
    }
    BlockHitResult hit = target(level, player);
    if (hit == null || !player.getPersistentData().contains(TARGET_KEY)
        || hit.getBlockPos().asLong() != player.getPersistentData().getLongOr(TARGET_KEY, 0L)) {
      return null;
    }
    BlockPos pos = hit.getBlockPos();
    Direction face = DrillOps.drillFace(level, pos, level.getBlockState(pos), player, hit.getDirection());
    return face == null ? null : hit.withDirection(face);
  }

  @Override
  public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remaining) {
    BlockHitResult hit = activeHit(level, living);
    if (hit == null) {
      living.releaseUsingItem();
      return;
    }
    BlockPos pos = hit.getBlockPos();
    if (!(level instanceof ServerLevel serverLevel)) {
      BlockState target = level.getBlockState(pos);
      if (!DrillOps.isBored(target)) {
        CoverSupport.stashClient(level, pos, target, CoverSupport.bit(hit.getDirection()));
      }
      return;
    }
    int total = drillTicks();
    float progress = (total - remaining) / (float) total;
    if (remaining % 4 == 0) {
      BlockState state = level.getBlockState(pos);
      serverLevel.playSound(null, pos, ModSounds.HOLE_DRILL, SoundSource.PLAYERS, 0.7F, 0.9F + progress * 0.3F);
      serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
          pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.05);
    }
  }

  private static void clearProgress(Level level, LivingEntity living) {
    if (!level.isClientSide() && living instanceof Player player) {
      player.getPersistentData().remove(TARGET_KEY);
    }
  }

  @Override
  public boolean releaseUsing(ItemStack stack, Level level, LivingEntity living, int remaining) {
    clearProgress(level, living);
    return false;
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
    BlockHitResult hit = activeHit(level, living);
    if (hit != null && level instanceof ServerLevel serverLevel && living instanceof Player player) {
      BlockPos pos = hit.getBlockPos();
      BlockState before = level.getBlockState(pos);
      if (hasEnergy(stack) && level.mayInteract(player, pos)
          && player.mayUseItemAt(pos, hit.getDirection(), stack)
          && DrillOps.drill(level, pos, hit.getDirection())) {
        IEnergy energy = getEnergy(stack);
        if (energy != null) {
          energy.consumeEnergy(energyCost(), false);
        }
        serverLevel.playSound(null, pos, before.getSoundType(level, pos, player).getBreakSound(),
            SoundSource.BLOCKS, 1F, 0.7F);
        serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, before),
            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 24, 0.35, 0.35, 0.35, 0.08);
      }
    }
    clearProgress(level, living);
    return stack;
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext level, TooltipDisplay display,
      Consumer<Component> tooltip, TooltipFlag flag) {
    tooltip.accept(Component.translatable("tooltip.faktocraft.hole_drill").withStyle(ChatFormatting.GRAY));
    super.appendHoverText(stack, level, display, tooltip, flag);
  }
}

package com.faktocraft.common.item.impl.tools;

import com.faktocraft.IndReb;
import com.faktocraft.common.block.VoxelBlock;
import com.faktocraft.common.block.impl.pipe.BlockEntityFluidPipe;
import com.faktocraft.common.block.impl.pipe.BlockEntityTank;
import com.faktocraft.common.entity.block.FluidStorage;
import com.faktocraft.common.item.base.ToolItem;
import com.faktocraft.common.registries.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Plunger extends ToolItem {

  private static final int MAX_RANGE = 32;
  private static final int MAX_PIPES = 512;
  public static final int USE_DURATION_TICKS = 30;
  private static final String TAG_TARGET = "PlungeTarget";

  public Plunger(Properties properties) {
    super(properties, 40);
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(Component.translatable("plunger." + IndReb.MODID + ".desc").withStyle(ChatFormatting.GRAY));
    super.appendHoverText(stack, level, tooltip, flag);
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    Level level = context.getLevel();
    BlockPos pos = context.getClickedPos();
    BlockEntity blockEntity = level.getBlockEntity(pos);
    boolean target = blockEntity instanceof BlockEntityFluidPipe || blockEntity instanceof BlockEntityTank;
    if (!target) {
      return super.useOn(context);
    }
    Player player = context.getPlayer();
    if (player == null) {
      return InteractionResult.PASS;
    }
    context.getItemInHand().getOrCreateTag().putLong(TAG_TARGET, pos.asLong());
    player.startUsingItem(context.getHand());
    return InteractionResult.CONSUME;
  }

  @Override
  public int getUseDuration(ItemStack stack) {
    return USE_DURATION_TICKS;
  }

  @Override
  public UseAnim getUseAnimation(ItemStack stack) {
    return UseAnim.NONE;
  }

  @Override
  public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingTicks) {
    int elapsed = getUseDuration(stack) - remainingTicks;
    if (!level.isClientSide() && elapsed % 8 == 0) {
      level.playSound(null, entity.blockPosition(), ModSounds.PLUNGER, SoundSource.PLAYERS,
          0.5F, 0.85F + 0.3F * (elapsed / (float) getUseDuration(stack)));
    }
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
    CompoundTag tag = stack.getTag();
    if (tag == null || !tag.contains(TAG_TARGET)) {
      return stack;
    }
    BlockPos pos = BlockPos.of(tag.getLong(TAG_TARGET));
    tag.remove(TAG_TARGET);
    if (level.isClientSide() || !(entity instanceof Player player)) {
      return stack;
    }
    if (pos.distToCenterSqr(entity.position()) > 64.0) {
      return stack;
    }
    plunge(level, pos, player, stack, entity.getUsedItemHand());
    return stack;
  }

  @Override
  public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTicks) {
    if (stack.hasTag()) {
      stack.getTag().remove(TAG_TARGET);
    }
  }

  @Override
  public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
    return slotChanged || oldStack.getItem() != newStack.getItem();
  }

  private static void plunge(Level level, BlockPos pos, Player player, ItemStack stack,
      net.minecraft.world.InteractionHand hand) {
    BlockEntity blockEntity = level.getBlockEntity(pos);

    int clearedMb = 0;
    Component message;
    if (blockEntity instanceof BlockEntityFluidPipe) {
      int pipes = 0;
      Set<BlockPos> seen = new HashSet<>();
      ArrayDeque<BlockPos> open = new ArrayDeque<>();
      open.add(pos);
      seen.add(pos);
      while (!open.isEmpty() && seen.size() <= MAX_PIPES) {
        BlockPos current = open.poll();
        if (!(level.getBlockEntity(current) instanceof BlockEntityFluidPipe pipe)) {
          continue;
        }
        if (!pipe.tank.isEmpty()) {
          clearedMb += pipe.tank.getFluidAmount();
          pipe.tank.setFluid(FluidStack.EMPTY, 0);
          pipes++;
        }
        for (Direction direction : Direction.values()) {
          if (!pipe.getBlockState().getValue(VoxelBlock.FACING_TO_PROPERTY_MAP.get(direction))) {
            continue;
          }
          BlockPos next = current.relative(direction);
          if (Math.abs(next.getX() - pos.getX()) > MAX_RANGE
              || Math.abs(next.getY() - pos.getY()) > MAX_RANGE
              || Math.abs(next.getZ() - pos.getZ()) > MAX_RANGE
              || !seen.add(next)) {
            continue;
          }
          if (level.getBlockEntity(next) instanceof BlockEntityFluidPipe) {
            open.add(next);
          }
        }
      }
      message = Component.translatable("gui." + IndReb.MODID + ".plunger_pipes",
          String.valueOf(clearedMb), String.valueOf(pipes));
    } else if (blockEntity instanceof BlockEntityTank tankEntity) {
      for (FluidStorage part : tankEntity.columnStorage.parts()) {
        clearedMb += part.getFluidAmount();
        part.setFluid(FluidStack.EMPTY, 0);
      }
      message = Component.translatable("gui." + IndReb.MODID + ".plunger_tank", String.valueOf(clearedMb));
    } else {
      return;
    }

    player.displayClientMessage(message, true);
    if (clearedMb > 0) {
      level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
      stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
    }
  }
}

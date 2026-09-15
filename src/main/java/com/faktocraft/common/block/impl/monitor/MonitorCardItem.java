package com.faktocraft.common.block.impl.monitor;

import com.faktocraft.common.block.impl.machines.nuclear_reactor.BlockNuclearReactor;
import com.faktocraft.common.item.base.BaseItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class MonitorCardItem extends BaseItem {

  private static final String TAG_DIMENSION = "monitorDim";
  private static final String TAG_POS = "monitorPos";
  private static final String TAG_BLOCK = "monitorBlock";

  public MonitorCardItem(Properties properties) {
    super(properties.stacksTo(1));
  }

  public static boolean hasTarget(ItemStack stack) {
    CompoundTag tag = stack.getTag();
    return tag != null && tag.contains(TAG_DIMENSION) && tag.contains(TAG_POS);
  }

  @Nullable
  public static ResourceKey<Level> targetDimension(ItemStack stack) {
    if (!hasTarget(stack)) {
      return null;
    }
    ResourceLocation id = ResourceLocation.tryParse(stack.getOrCreateTag().getString(TAG_DIMENSION));
    return id != null ? ResourceKey.create(Registries.DIMENSION, id) : null;
  }

  @Nullable
  public static BlockPos targetPos(ItemStack stack) {
    return hasTarget(stack) ? BlockPos.of(stack.getOrCreateTag().getLong(TAG_POS)) : null;
  }

  public static Component targetName(ItemStack stack) {
    CompoundTag tag = stack.getTag();
    Block block = tag != null
        ? ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryParse(tag.getString(TAG_BLOCK)))
        : null;
    return block != null ? block.getName() : Component.literal("?");
  }

  public static void copy(ItemStack stack, Level level, BlockPos pos) {
    BlockState state = level.getBlockState(pos);
    BlockPos resolved = pos;
    if (state.getBlock() instanceof BlockNuclearReactor) {
      BlockPos core = BlockNuclearReactor.corePos(state, pos);
      if (core != null) {
        resolved = core;
        state = level.getBlockState(core);
      }
    }
    CompoundTag tag = stack.getOrCreateTag();
    tag.putString(TAG_DIMENSION, level.dimension().location().toString());
    tag.putLong(TAG_POS, resolved.asLong());
    tag.putString(TAG_BLOCK, String.valueOf(ForgeRegistries.BLOCKS.getKey(state.getBlock())));
  }

  public static void clear(ItemStack stack) {
    CompoundTag tag = stack.getTag();
    if (tag != null) {
      tag.remove(TAG_DIMENSION);
      tag.remove(TAG_POS);
      tag.remove(TAG_BLOCK);
    }
  }

  private static String position(BlockPos pos) {
    return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
  }

  @Override
  public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
    Level level = context.getLevel();
    Player player = context.getPlayer();
    BlockPos pos = context.getClickedPos();
    BlockState state = level.getBlockState(pos);
    if (level.isClientSide()) {
      return InteractionResult.SUCCESS;
    }
    if (state.getBlock() instanceof BlockStatusMonitor) {
      BlockPos master = BlockStatusMonitor.masterPos(state, pos);
      if (!(level.getBlockEntity(master) instanceof BlockEntityStatusMonitor monitor)) {
        return InteractionResult.PASS;
      }
      if (player != null && player.isSecondaryUseActive()) {
        monitor.clearTarget();
        message(player, Component.translatable("chat.faktocraft.status_monitor.cleared"));
      } else if (!hasTarget(stack)) {
        message(player, Component.translatable("chat.faktocraft.status_monitor.empty_card")
            .withStyle(ChatFormatting.RED));
      } else {
        ResourceKey<Level> dimension = targetDimension(stack);
        BlockPos target = targetPos(stack);
        if (dimension != null && target != null) {
          monitor.setTarget(dimension, target);
          message(player, Component.translatable("chat.faktocraft.monitor_card.pasted", targetName(stack)));
        }
      }
      return InteractionResult.CONSUME;
    }
    copy(stack, level, pos);
    message(player, Component.translatable("chat.faktocraft.monitor_card.copied", targetName(stack)));
    return InteractionResult.CONSUME;
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (player.isSecondaryUseActive() && hasTarget(stack)) {
      if (!level.isClientSide()) {
        clear(stack);
        message(player, Component.translatable("chat.faktocraft.monitor_card.cleared"));
      }
      return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
    return super.use(level, player, hand);
  }

  private static void message(@Nullable Player player, Component text) {
    if (player != null) {
      player.displayClientMessage(text, true);
    }
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    BlockPos target = targetPos(stack);
    if (target != null) {
      tooltip.add(Component.translatable("tooltip.faktocraft.monitor_card.target", targetName(stack),
          position(target)).withStyle(ChatFormatting.AQUA));
    } else {
      tooltip.add(Component.translatable("tooltip.faktocraft.monitor_card.empty").withStyle(ChatFormatting.GRAY));
    }
    tooltip.add(Component.translatable("tooltip.faktocraft.monitor_card").withStyle(ChatFormatting.DARK_GRAY));
    super.appendHoverText(stack, level, tooltip, flag);
  }
}

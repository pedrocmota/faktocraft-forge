package com.faktocraft.common.block.impl.logistics;

import com.faktocraft.Faktocraft;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class RemoteRequesterItem extends Item {

  public RemoteRequesterItem(Properties properties) {
    super(properties.stacksTo(1));
  }

  @Nullable
  private static BlockPos boundPos(ItemStack stack) {
    CompoundTag tag = stack.getTag();
    return tag != null && tag.contains("tablePos") ? BlockPos.of(tag.getLong("tablePos")) : null;
  }

  @Nullable
  private static ResourceKey<Level> boundDim(ItemStack stack) {
    CompoundTag tag = stack.getTag();
    if (tag == null || !tag.contains("tableDim")) {
      return null;
    }
    return ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,
        new ResourceLocation(tag.getString("tableDim")));
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    Level level = context.getLevel();
    if (!(level.getBlockState(context.getClickedPos()).getBlock() instanceof BlockRequestTable)) {
      return InteractionResult.PASS;
    }
    if (!level.isClientSide()) {
      ItemStack stack = context.getItemInHand();
      stack.getOrCreateTag().putLong("tablePos", context.getClickedPos().asLong());
      stack.getOrCreateTag().putString("tableDim", level.dimension().location().toString());
      if (context.getPlayer() != null) {
        context.getPlayer().displayClientMessage(
            Component.translatable("logistics." + Faktocraft.MODID + ".remote.bound"), true);
      }
    }
    return InteractionResult.sidedSuccess(level.isClientSide());
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
      return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
    BlockPos pos = boundPos(stack);
    ResourceKey<Level> dim = boundDim(stack);
    if (pos == null || dim == null) {
      serverPlayer.displayClientMessage(
          Component.translatable("logistics." + Faktocraft.MODID + ".remote.unbound"), true);
      return InteractionResultHolder.fail(stack);
    }
    ServerLevel targetLevel = serverPlayer.server.getLevel(dim);
    if (targetLevel == null || !targetLevel.isLoaded(pos)
        || !(targetLevel.getBlockEntity(pos) instanceof BlockEntityRequestTable)) {
      serverPlayer.displayClientMessage(
          Component.translatable("logistics." + Faktocraft.MODID + ".remote.unreachable"), true);
      return InteractionResultHolder.fail(stack);
    }
    NetworkHooks.openScreen(serverPlayer,
        new SimpleMenuProvider(
            (windowId, inventory, p) -> new MenuRequestTable(windowId, targetLevel, pos, inventory, p, true),
            Component.translatable("block." + Faktocraft.MODID + ".request_table")),
        buf -> {
          buf.writeBlockPos(pos);
          buf.writeBoolean(true);
        });
    return InteractionResultHolder.consume(stack);
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    super.appendHoverText(stack, level, tooltip, flag);
    tooltip.add(Component.translatable("logistics." + Faktocraft.MODID + ".remote.desc")
        .withStyle(ChatFormatting.GRAY));
    BlockPos pos = boundPos(stack);
    if (pos != null) {
      tooltip.add(Component.translatable("logistics." + Faktocraft.MODID + ".remote.bound_to",
          pos.getX() + ", " + pos.getY() + ", " + pos.getZ()).withStyle(ChatFormatting.DARK_GRAY));
    }
  }
}

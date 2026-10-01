package com.faktocraft.common.block.impl.teleport_anchor;

import com.faktocraft.common.block.FaktocraftEntityBlock;
import com.faktocraft.common.registries.ModComponents;
import com.faktocraft.common.util.TextComponentUtil;
import com.faktocraft.common.util.wrench.WrenchHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockTeleportAnchor extends FaktocraftEntityBlock {

  private final boolean interdimensional;

  public BlockTeleportAnchor(Properties properties) {
    this(properties, false);
  }

  public BlockTeleportAnchor(Properties properties, boolean interdimensional) {
    super(properties);
    this.interdimensional = interdimensional;
    WrenchHelper.registerAction(this);
  }

  public boolean isInterdimensional() {
    return interdimensional;
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BlockEntityTeleportAnchor(pos, state);
  }

  @Override
  public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
      BlockHitResult hitResult) {
    ItemStack stack = player.getItemInHand(hand);
    if (stack.getItem() instanceof com.faktocraft.common.item.impl.TeleportCardItem) {
      if (!level.isClientSide()) {
        useCard(level, pos, player, stack);
      }
      return InteractionResult.SUCCESS;
    }

    if (stack.getItem() instanceof com.faktocraft.common.item.impl.wrench.Wrench
        || stack.getItem() instanceof com.faktocraft.common.item.impl.wrench.ElectricWrench) {

      if (!level.isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
          && level.getBlockEntity(pos) instanceof BlockEntityTeleportAnchor anchor) {
        int maxCapacity = com.faktocraft.common.config.ModConfig.server().teleport_anchor_energy_capacity;
        com.faktocraft.common.network.ModNetworking.sendToPlayer(serverPlayer,
            new com.faktocraft.common.network.packet.PacketAnchorScreen(pos.immutable(),
                anchor.getBufferCapacity(),
                Math.min(BlockEntityTeleportAnchor.MIN_BUFFER, maxCapacity), maxCapacity));
      }
      return InteractionResult.SUCCESS;
    }

    if (!level.isClientSide() && level.getBlockEntity(pos) instanceof BlockEntityTeleportAnchor anchor) {
      BlockPos target = anchor.getDestination();
      if (target == null) {
        player.sendSystemMessage(Component.translatable("chat.faktocraft.anchor_no_target")
            .withStyle(ChatFormatting.YELLOW));
      } else {
        player.sendSystemMessage(Component.translatable("chat.faktocraft.anchor_status",
            posText(target, anchor.getDestinationDimension(), level.dimension()),
            Component.literal(TextComponentUtil.getFormattedEnergyUnit(anchor.getEnergyStorage().energyStored())
                + " / " + TextComponentUtil.getFormattedEnergyUnit(anchor.getEnergyStorage().maxEnergy()) + " IE")
                .withStyle(ChatFormatting.AQUA),
            Component.literal(TextComponentUtil.getFormattedEnergyUnit(anchor.getTeleportCost()) + " IE")
                .withStyle(ChatFormatting.YELLOW))
            .withStyle(ChatFormatting.GRAY));
      }
    }
    return InteractionResult.SUCCESS;
  }

  private void useCard(Level level, BlockPos pos, Player player, ItemStack stack) {
    BlockPos target = ModComponents.getTeleportTarget(stack);
    ResourceKey<Level> here = level.dimension();
    ResourceKey<Level> targetDimension = ModComponents.getTeleportTargetDimension(stack, here);

    if (target == null) {
      ModComponents.setTeleportTarget(stack, pos.immutable(), here);
      player.sendSystemMessage(Component.translatable("chat.faktocraft.anchor_saved",
          posText(pos)).withStyle(ChatFormatting.GREEN));
      return;
    }
    if (target.equals(pos) && targetDimension.equals(here)) {
      ModComponents.removeTeleportTarget(stack);
      player.sendSystemMessage(Component.translatable("chat.faktocraft.anchor_card_cleared")
          .withStyle(ChatFormatting.YELLOW));
      return;
    }
    if (!(level.getBlockEntity(pos) instanceof BlockEntityTeleportAnchor anchor)) {
      return;
    }

    boolean cross = !targetDimension.equals(here);
    if (cross && !interdimensional) {
      player.sendSystemMessage(Component.translatable("chat.faktocraft.anchor_needs_dimensional",
          dimensionText(targetDimension)).withStyle(ChatFormatting.RED));
      return;
    }

    anchor.setDestination(target, targetDimension);

    boolean bidirectional = false;
    Level targetLevel = level;
    if (cross) {
      MinecraftServer server = level.getServer();
      targetLevel = server == null ? null : server.getLevel(targetDimension);
    }
    if (targetLevel != null && targetLevel.isLoaded(target)
        && targetLevel.getBlockEntity(target) instanceof BlockEntityTeleportAnchor targetAnchor
        && (!cross || targetAnchor.isInterdimensional())) {
      targetAnchor.setDestination(pos.immutable(), here);
      bidirectional = true;
    }

    MutableComponent cost = Component
        .literal(TextComponentUtil.getFormattedEnergyUnit(anchor.getTeleportCost()) + " IE")
        .withStyle(ChatFormatting.AQUA);
    if (cross) {
      String key = bidirectional ? "chat.faktocraft.anchor_linked_dimensional_both"
          : "chat.faktocraft.anchor_linked_dimensional";
      player.sendSystemMessage(Component.translatable(key, posText(target), dimensionText(targetDimension), cost)
          .withStyle(ChatFormatting.GREEN));
      return;
    }
    double distance = Math.sqrt(pos.distSqr(target));
    String key = bidirectional ? "chat.faktocraft.anchor_linked_both" : "chat.faktocraft.anchor_linked";
    player.sendSystemMessage(Component.translatable(key,
        posText(target),
        Component.literal(String.valueOf((int) distance)).withStyle(ChatFormatting.YELLOW),
        cost)
        .withStyle(ChatFormatting.GREEN));
  }

  private static MutableComponent posText(BlockPos pos) {
    return Component.literal("[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]")
        .withStyle(ChatFormatting.AQUA);
  }

  private static MutableComponent posText(BlockPos pos, ResourceKey<Level> dimension,
      ResourceKey<Level> current) {
    if (dimension.equals(current)) {
      return posText(pos);
    }
    return TextComponentUtil.build(posText(pos), Component.literal(" @ ").withStyle(ChatFormatting.GRAY),
        dimensionText(dimension));
  }

  public static MutableComponent dimensionText(ResourceKey<Level> dimension) {
    return Component.literal(dimension.identifier().toString()).withStyle(ChatFormatting.LIGHT_PURPLE);
  }
}

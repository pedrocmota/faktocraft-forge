package com.faktocraft.common.block.impl.teleport_anchor;

import com.faktocraft.common.block.FaktocraftEntityBlock;
import com.faktocraft.common.registries.ModComponents;
import com.faktocraft.common.util.TextComponentUtil;
import com.faktocraft.common.util.wrench.WrenchHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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

  public BlockTeleportAnchor(Properties properties) {
    super(properties);
    WrenchHelper.registerAction(this);
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
        BlockPos target = ModComponents.getTeleportTarget(stack);
        if (target == null) {
          ModComponents.setTeleportTarget(stack, pos.immutable());
          player.sendSystemMessage(Component.translatable("chat.faktocraft.anchor_saved",
              posText(pos)).withStyle(ChatFormatting.GREEN));
        } else if (target.equals(pos)) {
          ModComponents.removeTeleportTarget(stack);
          player.sendSystemMessage(Component.translatable("chat.faktocraft.anchor_card_cleared")
              .withStyle(ChatFormatting.YELLOW));
        } else if (level.getBlockEntity(pos) instanceof BlockEntityTeleportAnchor anchor) {
          anchor.setDestination(target);
          boolean bidirectional = false;
          if (level.isLoaded(target)
              && level.getBlockEntity(target) instanceof BlockEntityTeleportAnchor targetAnchor) {
            targetAnchor.setDestination(pos.immutable());
            bidirectional = true;
          }
          double distance = Math.sqrt(pos.distSqr(target));
          String key = bidirectional ? "chat.faktocraft.anchor_linked_both" : "chat.faktocraft.anchor_linked";
          player.sendSystemMessage(Component.translatable(key,
              posText(target),
              Component.literal(String.valueOf((int) distance)).withStyle(ChatFormatting.YELLOW),
              Component.literal(TextComponentUtil.getFormattedEnergyUnit(anchor.getTeleportCost()) + " IE")
                  .withStyle(ChatFormatting.AQUA))
              .withStyle(ChatFormatting.GREEN));
        }
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
            posText(target),
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

  private static Component posText(BlockPos pos) {
    return Component.literal("[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]")
        .withStyle(ChatFormatting.AQUA);
  }
}

package com.faktocraft.common.block.impl.generators.geo_generator;

import com.faktocraft.common.block.BlockElectricMachine;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.interfaces.block.IGenerationInfo;
import com.faktocraft.common.interfaces.block.IHasMenu;
import com.faktocraft.common.util.FluidInteractionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockGeoGenerator extends BlockElectricMachine implements IHasMenu, IGenerationInfo {

  @Override
  public void appendGenerationInfo(java.util.List<net.minecraft.network.chat.Component> tooltip) {
    tooltip.add(IGenerationInfo.line("generation_with",
        net.minecraft.network.chat.Component.translatable("block.minecraft.lava"),
        IGenerationInfo.rate(com.faktocraft.common.config.ModConfig.server().geo_generator_tick_generate)));
  }

  public BlockGeoGenerator(Properties properties) {
    super(EnergyTier.LOW, properties);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BlockEntityGeoGenerator(pos, state);
  }

  @Override
  public MenuGeoGenerator getMenu(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    return new MenuGeoGenerator(windowId, level, pos, playerInventory, player);
  }

  @Override
  public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
      InteractionHand hand, BlockHitResult hitResult) {
    ItemStack stack = player.getItemInHand(hand);
    if (!level.isClientSide() && !player.isShiftKeyDown() && !stack.isEmpty()
        && level.getBlockEntity(pos) instanceof BlockEntityGeoGenerator be) {
      if (FluidInteractionHelper.tryFillTankFromHand(player, hand, be.fluidStorage, Fluids.LAVA)) {
        return InteractionResult.SUCCESS;
      }
    }
    return super.use(state, level, pos, player, hand, hitResult);
  }
}

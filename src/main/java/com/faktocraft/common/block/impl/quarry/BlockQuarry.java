package com.faktocraft.common.block.impl.quarry;

import com.faktocraft.common.block.IndRebEntityBlock;
import com.faktocraft.common.interfaces.block.IHasMenu;
import com.faktocraft.common.interfaces.block.IStateActive;
import com.faktocraft.common.interfaces.block.IStateFacing;
import com.faktocraft.common.util.wrench.WrenchHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class BlockQuarry extends IndRebEntityBlock implements IStateFacing, IStateActive, IHasMenu {

  public BlockQuarry(Properties properties) {
    super(properties);
    WrenchHelper.registerAction(this).add(WrenchHelper.rotationAction());
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BlockEntityQuarry(pos, state);
  }

  @Override
  public AbstractContainerMenu getMenu(int windowId, Level level, BlockPos pos, Inventory playerInventory,
      Player player) {
    return new MenuQuarry(windowId, level, pos, playerInventory, player);
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
      TooltipFlag flag) {
    tooltip.add(Component.translatable("tooltip.faktocraft.quarry").withStyle(ChatFormatting.GRAY));
    tooltip.add(Component.translatable("tooltip.faktocraft.quarry_area").withStyle(ChatFormatting.DARK_GRAY));
    super.appendHoverText(stack, level, tooltip, flag);
  }
}

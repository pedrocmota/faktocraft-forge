package com.faktocraft.common.block.impl.chunk_loader;

import com.faktocraft.common.block.IndRebEntityBlock;
import com.faktocraft.common.interfaces.block.IHasMenu;
import com.faktocraft.common.util.wrench.WrenchHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class BlockChunkLoader extends IndRebEntityBlock implements IHasMenu {

  public BlockChunkLoader(Properties properties) {
    super(properties);
    WrenchHelper.registerAction(this);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BlockEntityChunkLoader(pos, state);
  }

  @Override
  public MenuChunkLoader getMenu(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    return new MenuChunkLoader(windowId, level, pos, playerInventory, player);
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
      TooltipFlag flag) {
    tooltip.add(Component.translatable("tooltip.faktocraft.chunk_loader").withStyle(ChatFormatting.GRAY));
    super.appendHoverText(stack, level, tooltip, flag);
  }
}

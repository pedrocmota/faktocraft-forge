package com.faktocraft.common.block.impl.chunk_loader;

import net.minecraft.world.item.Item;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import com.faktocraft.common.block.FaktocraftEntityBlock;
import com.faktocraft.common.interfaces.block.IHasMenu;
import com.faktocraft.common.util.wrench.WrenchHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockChunkLoader extends FaktocraftEntityBlock implements IHasMenu {

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

  public void appendHoverText(ItemStack stack, Item.TooltipContext level, TooltipDisplay display,
      Consumer<Component> tooltip, TooltipFlag flag) {
    tooltip.accept(Component.translatable("tooltip.faktocraft.chunk_loader").withStyle(ChatFormatting.GRAY));
  }
}

package com.faktocraft.common.block.impl.transformer;

import com.faktocraft.common.block.FaktocraftEntityBlock;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.interfaces.block.IElectricMachine;
import com.faktocraft.common.interfaces.block.IHasMenu;
import com.faktocraft.common.interfaces.block.IStateFacing;
import com.faktocraft.common.tier.TransformerTier;
import com.faktocraft.common.util.BlockStateHelper;
import com.faktocraft.common.util.wrench.WrenchHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

public class BlockTransformer extends FaktocraftEntityBlock implements IStateFacing, IHasMenu, IElectricMachine {

  private final TransformerTier transformerTier;

  public BlockTransformer(TransformerTier transformerTier, Properties properties) {
    super(properties);
    this.transformerTier = transformerTier;
    WrenchHelper.registerAction(this).add(WrenchHelper.rotationHitAction());
  }

  public static BlockBehaviour.Properties transformerProperties() {
    return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5F, 3F).sound(SoundType.METAL);
  }

  public TransformerTier getTransformerTier() {
    return transformerTier;
  }

  @Override
  public void appendHoverText(net.minecraft.world.item.ItemStack stack,
      @Nullable net.minecraft.world.level.BlockGetter level,
      java.util.List<net.minecraft.network.chat.Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
    EnergyTier high = transformerTier.getMaxTier();
    EnergyTier low = transformerTier.getMinTier();
    tooltip.add(net.minecraft.network.chat.Component.translatable(
        "tooltip.faktocraft.transformer_conversion",
        high.getLang().getTranslationComponent(),
        com.faktocraft.common.util.TextComponentUtil.getFormattedLong(high.getBasicTransfer()),
        low.getLang().getTranslationComponent(),
        com.faktocraft.common.util.TextComponentUtil.getFormattedLong(low.getBasicTransfer()))
        .withStyle(net.minecraft.ChatFormatting.GRAY));
    tooltip.add(net.minecraft.network.chat.Component.translatable(
        "tooltip.faktocraft.transformer_redstone_default")
        .withStyle(net.minecraft.ChatFormatting.GOLD));
    super.appendHoverText(stack, level, tooltip, flag);
  }

  @Override
  public EnumProperty<Direction> getFacingProperty() {
    return BlockStateHelper.facingProperty;
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BlockEntityTransformer(pos, state);
  }

  @Override
  public MenuTransformer getMenu(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    return new MenuTransformer(windowId, level, pos, playerInventory, player);
  }

  @Override
  public EnergyTier getEnergyTier() {
    return transformerTier.getMaxTier();
  }
}

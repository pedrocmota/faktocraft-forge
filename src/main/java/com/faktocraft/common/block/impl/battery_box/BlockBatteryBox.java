package com.faktocraft.common.block.impl.battery_box;

import com.faktocraft.common.block.FaktocraftEntityBlock;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.interfaces.block.IElectricMachine;
import com.faktocraft.common.interfaces.block.IHasMenu;
import com.faktocraft.common.interfaces.block.IStateFacing;
import com.faktocraft.common.tier.BatteryBoxTier;
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

public class BlockBatteryBox extends FaktocraftEntityBlock implements IStateFacing, IHasMenu, IElectricMachine {

  private final BatteryBoxTier batteryBoxTier;

  public BlockBatteryBox(BatteryBoxTier batteryBoxTier, Properties properties) {
    super(properties);
    this.batteryBoxTier = batteryBoxTier;
    WrenchHelper.registerAction(this).add(WrenchHelper.rotationHitAction());
  }

  public static BlockBehaviour.Properties woodenProperties() {
    return BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1F, 3F).sound(SoundType.WOOD);
  }

  public static BlockBehaviour.Properties metalProperties() {
    return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5F, 3F).sound(SoundType.METAL);
  }

  public BatteryBoxTier getBatteryBoxTier() {
    return batteryBoxTier;
  }

  @Override
  public EnumProperty<Direction> getFacingProperty() {
    return BlockStateHelper.facingProperty;
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BlockEntityBatteryBox(pos, state);
  }

  @Override
  public MenuBatteryBox getMenu(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    return new MenuBatteryBox(windowId, level, pos, playerInventory, player);
  }

  @Override
  public EnergyTier getEnergyTier() {
    return batteryBoxTier.getEnergyTier();
  }
}

package com.faktocraft.common.block;

import com.faktocraft.common.interfaces.block.IStateActive;
import com.faktocraft.common.interfaces.block.IStateFacing;
import com.faktocraft.common.util.BlockStateHelper;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class BlockMachine extends IndRebEntityBlock implements IStateFacing, IStateActive {

  public BlockMachine(Properties properties) {
    super(properties);
  }

  public static BlockBehaviour.Properties machineProperties(int lightOn, int lightOff) {
    return BlockBehaviour.Properties.of()
        .mapColor(MapColor.METAL)
        .strength(2F, 3F)
        .sound(SoundType.METAL)
        .lightLevel(state -> state.hasProperty(BlockStateHelper.activeProperty)
            && state.getValue(BlockStateHelper.activeProperty) ? lightOn : lightOff);
  }

  public static BlockBehaviour.Properties machineProperties() {
    return BlockBehaviour.Properties.of()
        .mapColor(MapColor.METAL)
        .strength(2F, 3F)
        .sound(SoundType.METAL);
  }
}

package com.faktocraft.common.block.impl.cf;

import com.faktocraft.Faktocraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class BlockReinforcedStoneDoor extends DoorBlock {

  public static final BlockSetType SET_TYPE = BlockSetType.register(new BlockSetType(
      Faktocraft.MODID + ":reinforced_stone", false, SoundType.STONE,
      SoundEvents.IRON_DOOR_CLOSE, SoundEvents.IRON_DOOR_OPEN,
      SoundEvents.IRON_TRAPDOOR_CLOSE, SoundEvents.IRON_TRAPDOOR_OPEN,
      SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF, SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON,
      SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON));

  public BlockReinforcedStoneDoor(Properties properties) {
    super(properties, SET_TYPE);
  }

  public static BlockBehaviour.Properties reinforcedStoneDoorProperties() {
    return BlockBehaviour.Properties.of()
        .mapColor(MapColor.STONE)
        .strength(10.0F, 10000000000F)
        .sound(SoundType.STONE)
        .noOcclusion()
        .pushReaction(PushReaction.DESTROY);
  }
}

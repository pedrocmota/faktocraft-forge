package com.faktocraft.common.registries.machines;

import com.faktocraft.common.block.BlockMachine;
import com.faktocraft.common.block.impl.battery_box.BlockBatteryBox;
import com.faktocraft.common.block.impl.battery_box.BlockEntityBatteryBox;
import com.faktocraft.common.block.impl.battery_box.MenuBatteryBox;
import com.faktocraft.common.block.impl.charge_pad.BlockChargePad;
import com.faktocraft.common.block.impl.charge_pad.BlockEntityChargePad;
import com.faktocraft.common.block.impl.charge_pad.MenuChargePad;
import com.faktocraft.common.block.impl.generators.generator.BlockEntityGenerator;
import com.faktocraft.common.block.impl.generators.generator.BlockGenerator;
import com.faktocraft.common.block.impl.generators.generator.MenuGenerator;
import com.faktocraft.common.block.impl.generators.geo_generator.BlockEntityGeoGenerator;
import com.faktocraft.common.block.impl.generators.geo_generator.BlockGeoGenerator;
import com.faktocraft.common.block.impl.generators.geo_generator.MenuGeoGenerator;
import com.faktocraft.common.block.impl.generators.combustion_generator.BlockEntityCombustionGenerator;
import com.faktocraft.common.block.impl.generators.combustion_generator.BlockCombustionGenerator;
import com.faktocraft.common.block.impl.generators.combustion_generator.MenuCombustionGenerator;
import com.faktocraft.common.block.impl.generators.solar_panels.BlockEntitySolarGenerator;
import com.faktocraft.common.block.impl.generators.solar_panels.BlockSolarGenerator;
import com.faktocraft.common.block.impl.generators.solar_panels.MenuSolarGenerator;
import com.faktocraft.common.block.impl.generators.wind_generator.BlockEntityWindGenerator;
import com.faktocraft.common.block.impl.generators.wind_generator.BlockWindGenerator;
import com.faktocraft.common.block.impl.generators.wind_generator.MenuWindGenerator;
import com.faktocraft.common.block.impl.transformer.BlockEntityTransformer;
import com.faktocraft.common.block.impl.transformer.BlockTransformer;
import com.faktocraft.common.block.impl.transformer.MenuTransformer;
import com.faktocraft.common.registries.MenuTypeHelper;
import com.faktocraft.common.registries.ModBlocks;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.registries.RegistrationHandler;
import com.faktocraft.common.tier.BatteryBoxTier;
import com.faktocraft.common.tier.ChargePadTier;
import com.faktocraft.common.tier.SolarGeneratorTier;
import com.faktocraft.common.tier.TransformerTier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class M1Registry {

  public static final Block GENERATOR = ModBlocks.register("generator",
      BlockGenerator::new, BlockMachine.machineProperties(12, 0));

  public static final Block SOLAR_GENERATOR = ModBlocks.register("solar_generator",
      p -> new BlockSolarGenerator(SolarGeneratorTier.BASIC, p), BlockMachine.machineProperties(0, 0));
  public static final Block ADVANCED_SOLAR_GENERATOR = ModBlocks.register("advanced_solar_generator",
      p -> new BlockSolarGenerator(SolarGeneratorTier.ADVANCED, p),
      BlockMachine.machineProperties(0, 0));
  public static final Block HYBRID_SOLAR_GENERATOR = ModBlocks.register("hybrid_solar_generator",
      p -> new BlockSolarGenerator(SolarGeneratorTier.HYBRID, p), BlockMachine.machineProperties(0, 0));
  public static final Block QUANTUM_SOLAR_GENERATOR = ModBlocks.register("quantum_solar_generator",
      p -> new BlockSolarGenerator(SolarGeneratorTier.QUANTUM, p),
      BlockMachine.machineProperties(0, 0));

  public static final Block WIND_GENERATOR = ModBlocks.register("wind_generator",
      BlockWindGenerator::new, BlockMachine.machineProperties(0, 0));

  public static final Block GEO_GENERATOR = ModBlocks.register("geo_generator",
      BlockGeoGenerator::new, BlockMachine.machineProperties(12, 0));
  public static final Block COMBUSTION_GENERATOR = ModBlocks.register("combustion_generator",
      BlockCombustionGenerator::new, BlockMachine.machineProperties(12, 0));

  public static final Block BATTERY_BOX = ModBlocks.register("battery_box",
      p -> new BlockBatteryBox(BatteryBoxTier.BASIC, p), BlockBatteryBox.woodenProperties());
  public static final Block CESU = ModBlocks.register("cesu",
      p -> new BlockBatteryBox(BatteryBoxTier.STANDARD, p), BlockBatteryBox.metalProperties());
  public static final Block MFE = ModBlocks.register("mfe",
      p -> new BlockBatteryBox(BatteryBoxTier.ADVANCED, p), BlockBatteryBox.metalProperties());
  public static final Block MFSU = ModBlocks.register("mfsu",
      p -> new BlockBatteryBox(BatteryBoxTier.SUPER, p), BlockBatteryBox.metalProperties());

  public static final Block LOW_TRANSFORMER = ModBlocks.register("low_transformer",
      p -> new BlockTransformer(TransformerTier.LOW, p), BlockTransformer.transformerProperties());
  public static final Block MEDIUM_TRANSFORMER = ModBlocks.register("medium_transformer",
      p -> new BlockTransformer(TransformerTier.MEDIUM, p), BlockTransformer.transformerProperties());
  public static final Block HIGH_TRANSFORMER = ModBlocks.register("high_transformer",
      p -> new BlockTransformer(TransformerTier.HIGH, p), BlockTransformer.transformerProperties());
  public static final Block VERY_HIGH_TRANSFORMER = ModBlocks.register("very_high_transformer",
      p -> new BlockTransformer(TransformerTier.VERY_HIGH, p), BlockTransformer.transformerProperties());

  public static final Block CHARGE_PAD_BATTERY_BOX = ModBlocks.register("charge_pad_battery_box",
      p -> new BlockChargePad(ChargePadTier.BASIC, p), BlockChargePad.woodenProperties());
  public static final Block CHARGE_PAD_CESU = ModBlocks.register("charge_pad_cesu",
      p -> new BlockChargePad(ChargePadTier.STANDARD, p), BlockChargePad.metalProperties());
  public static final Block CHARGE_PAD_MFE = ModBlocks.register("charge_pad_mfe",
      p -> new BlockChargePad(ChargePadTier.ADVANCED, p), BlockChargePad.metalProperties());
  public static final Block CHARGE_PAD_MFSU = ModBlocks.register("charge_pad_mfsu",
      p -> new BlockChargePad(ChargePadTier.SUPER, p), BlockChargePad.metalProperties());

  public static final Item GENERATOR_ITEM = ModItems.registerElectricBlockItem(GENERATOR);
  public static final Item SOLAR_GENERATOR_ITEM = ModItems.registerElectricBlockItem(SOLAR_GENERATOR);
  public static final Item ADVANCED_SOLAR_GENERATOR_ITEM = ModItems.registerElectricBlockItem(ADVANCED_SOLAR_GENERATOR);
  public static final Item HYBRID_SOLAR_GENERATOR_ITEM = ModItems.registerElectricBlockItem(HYBRID_SOLAR_GENERATOR,
      net.minecraft.world.item.Rarity.RARE);
  public static final Item QUANTUM_SOLAR_GENERATOR_ITEM = ModItems.registerElectricBlockItem(QUANTUM_SOLAR_GENERATOR,
      net.minecraft.world.item.Rarity.EPIC);
  public static final Item WIND_GENERATOR_ITEM = ModItems.registerElectricBlockItem(WIND_GENERATOR);
  public static final Item GEO_GENERATOR_ITEM = ModItems.registerElectricBlockItem(GEO_GENERATOR);
  public static final Item COMBUSTION_GENERATOR_ITEM = ModItems.registerElectricBlockItem(COMBUSTION_GENERATOR);

  public static final Item BATTERY_BOX_ITEM = ModItems.registerElectricBlockItem(BATTERY_BOX);
  public static final Item CESU_ITEM = ModItems.registerElectricBlockItem(CESU);
  public static final Item MFE_ITEM = ModItems.registerElectricBlockItem(MFE);
  public static final Item MFSU_ITEM = ModItems.registerElectricBlockItem(MFSU);

  public static final Item LOW_TRANSFORMER_ITEM = ModItems.registerBlockItem(LOW_TRANSFORMER);
  public static final Item MEDIUM_TRANSFORMER_ITEM = ModItems.registerBlockItem(MEDIUM_TRANSFORMER);
  public static final Item HIGH_TRANSFORMER_ITEM = ModItems.registerBlockItem(HIGH_TRANSFORMER);
  public static final Item VERY_HIGH_TRANSFORMER_ITEM = ModItems.registerBlockItem(VERY_HIGH_TRANSFORMER,
      net.minecraft.world.item.Rarity.RARE);

  public static final Item CHARGE_PAD_BATTERY_BOX_ITEM = ModItems.registerElectricBlockItem(CHARGE_PAD_BATTERY_BOX);
  public static final Item CHARGE_PAD_CESU_ITEM = ModItems.registerElectricBlockItem(CHARGE_PAD_CESU);
  public static final Item CHARGE_PAD_MFE_ITEM = ModItems.registerElectricBlockItem(CHARGE_PAD_MFE);
  public static final Item CHARGE_PAD_MFSU_ITEM = ModItems.registerElectricBlockItem(CHARGE_PAD_MFSU);

  public static final BlockEntityType<BlockEntityGenerator> GENERATOR_BE = registerBlockEntity("generator",
      BlockEntityGenerator::new, GENERATOR);

  public static final BlockEntityType<BlockEntitySolarGenerator> SOLAR_GENERATOR_BE = registerBlockEntity(
      "solar_generator", BlockEntitySolarGenerator::new,
      SOLAR_GENERATOR, ADVANCED_SOLAR_GENERATOR, HYBRID_SOLAR_GENERATOR, QUANTUM_SOLAR_GENERATOR);

  public static final BlockEntityType<BlockEntityWindGenerator> WIND_GENERATOR_BE = registerBlockEntity(
      "wind_generator", BlockEntityWindGenerator::new, WIND_GENERATOR);

  public static final BlockEntityType<BlockEntityGeoGenerator> GEO_GENERATOR_BE = registerBlockEntity("geo_generator",
      BlockEntityGeoGenerator::new, GEO_GENERATOR);

  public static final BlockEntityType<BlockEntityCombustionGenerator> COMBUSTION_GENERATOR_BE = registerBlockEntity(
      "combustion_generator", BlockEntityCombustionGenerator::new, COMBUSTION_GENERATOR);

  public static final BlockEntityType<BlockEntityBatteryBox> BATTERY_BOX_BE = registerBlockEntity("battery_box",
      BlockEntityBatteryBox::new, BATTERY_BOX, CESU, MFE, MFSU);

  public static final BlockEntityType<BlockEntityTransformer> TRANSFORMER_BE = registerBlockEntity("transformer",
      BlockEntityTransformer::new,
      LOW_TRANSFORMER, MEDIUM_TRANSFORMER, HIGH_TRANSFORMER, VERY_HIGH_TRANSFORMER);

  public static final BlockEntityType<BlockEntityChargePad> CHARGE_PAD_BE = registerBlockEntity("charge_pad",
      BlockEntityChargePad::new,
      CHARGE_PAD_BATTERY_BOX, CHARGE_PAD_CESU, CHARGE_PAD_MFE, CHARGE_PAD_MFSU);

  public static final MenuType<MenuGenerator> GENERATOR_MENU = MenuTypeHelper.register("generator", MenuGenerator::new);
  public static final MenuType<MenuSolarGenerator> SOLAR_GENERATOR_MENU = MenuTypeHelper.register("solar_generator",
      MenuSolarGenerator::new);
  public static final MenuType<MenuWindGenerator> WIND_GENERATOR_MENU = MenuTypeHelper.register("wind_generator",
      MenuWindGenerator::new);
  public static final MenuType<com.faktocraft.common.item.impl.tools.ToolboxMenu> TOOLBOX_MENU = MenuTypeHelper
      .register("toolbox", com.faktocraft.common.item.impl.tools.ToolboxMenu::new);
  public static final MenuType<MenuGeoGenerator> GEO_GENERATOR_MENU = MenuTypeHelper.register("geo_generator",
      MenuGeoGenerator::new);
  public static final MenuType<MenuCombustionGenerator> COMBUSTION_GENERATOR_MENU = MenuTypeHelper
      .register("combustion_generator", MenuCombustionGenerator::new);
  public static final MenuType<MenuBatteryBox> BATTERY_BOX_MENU = MenuTypeHelper.register("battery_box",
      MenuBatteryBox::new);
  public static final MenuType<MenuTransformer> TRANSFORMER_MENU = MenuTypeHelper.register("transformer",
      MenuTransformer::new);
  public static final MenuType<MenuChargePad> CHARGE_PAD_MENU = MenuTypeHelper.register("charge_pad",
      MenuChargePad::new);

  private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(String name,
      BlockEntityType.BlockEntitySupplier<T> factory, Block... blocks) {
    return RegistrationHandler.blockEntity(name, factory, blocks);
  }

  public static void register() {
  }
}

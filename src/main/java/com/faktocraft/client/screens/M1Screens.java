package com.faktocraft.client.screens;

import com.faktocraft.common.block.impl.battery_box.ScreenBatteryBox;
import com.faktocraft.common.block.impl.charge_pad.ScreenChargePad;
import com.faktocraft.common.block.impl.generators.generator.ScreenGenerator;
import com.faktocraft.common.block.impl.generators.geo_generator.ScreenGeoGenerator;
import com.faktocraft.common.block.impl.generators.combustion_generator.ScreenCombustionGenerator;
import com.faktocraft.common.block.impl.generators.solar_panels.ScreenSolarGenerator;
import com.faktocraft.common.block.impl.generators.wind_generator.ScreenWindGenerator;
import com.faktocraft.common.block.impl.transformer.ScreenTransformer;
import com.faktocraft.common.registries.machines.M1Registry;
import net.minecraft.client.gui.screens.MenuScreens;

public class M1Screens {

  public static void register() {
    MenuScreens.register(M1Registry.GENERATOR_MENU, ScreenGenerator::new);
    MenuScreens.register(M1Registry.SOLAR_GENERATOR_MENU, ScreenSolarGenerator::new);
    MenuScreens.register(M1Registry.WIND_GENERATOR_MENU, ScreenWindGenerator::new);
    MenuScreens.register(M1Registry.TOOLBOX_MENU, com.faktocraft.common.item.impl.tools.ToolboxScreen::new);
    MenuScreens.register(M1Registry.GEO_GENERATOR_MENU, ScreenGeoGenerator::new);
    MenuScreens.register(M1Registry.COMBUSTION_GENERATOR_MENU, ScreenCombustionGenerator::new);
    MenuScreens.register(M1Registry.BATTERY_BOX_MENU, ScreenBatteryBox::new);
    MenuScreens.register(M1Registry.TRANSFORMER_MENU, ScreenTransformer::new);
    MenuScreens.register(M1Registry.CHARGE_PAD_MENU, ScreenChargePad::new);
    MenuScreens.register(com.faktocraft.common.block.impl.chunk_loader.ChunkLoaderRegistry.CHUNK_LOADER_MENU,
        com.faktocraft.common.block.impl.chunk_loader.ScreenChunkLoader::new);
  }
}

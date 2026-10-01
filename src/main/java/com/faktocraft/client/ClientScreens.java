package com.faktocraft.client;

import com.faktocraft.client.screens.BasicConfigScreen;
import com.faktocraft.client.screens.M1Screens;
import com.faktocraft.client.screens.M2Screens;
import com.faktocraft.client.screens.M3Screens;
import com.faktocraft.client.screens.M4Screens;
import com.faktocraft.common.block.impl.forester.ForesterRegistry;
import com.faktocraft.common.block.impl.forester.ScreenForester;
import com.faktocraft.common.block.impl.logistics.LogisticsRegistry;
import com.faktocraft.common.block.impl.logistics.ScreenAssemblyTable;
import com.faktocraft.common.block.impl.logistics.ScreenChassis;
import com.faktocraft.common.block.impl.logistics.ScreenCoreTasks;
import com.faktocraft.common.block.impl.logistics.ScreenCraftPipe;
import com.faktocraft.common.block.impl.logistics.ScreenLogisticsController;
import com.faktocraft.common.block.impl.logistics.ScreenModule;
import com.faktocraft.common.block.impl.logistics.ScreenRecipePipe;
import com.faktocraft.common.block.impl.logistics.ScreenRequestTable;
import com.faktocraft.common.block.impl.pipe.ScreenEnderTank;
import com.faktocraft.common.block.impl.pipe.ScreenExtractorPipe;
import com.faktocraft.common.block.impl.pipe.ScreenPump;
import com.faktocraft.common.block.impl.quarry.QuarryRegistry;
import com.faktocraft.common.block.impl.quarry.ScreenQuarry;
import com.faktocraft.common.item.impl.tools.ToolboxTooltip;
import com.faktocraft.common.registries.PipeRegistry;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public final class ClientScreens {
  private ClientScreens() {
  }

  public static void register(RegisterMenuScreensEvent event) {
    event.register(PipeRegistry.EXTRACTOR_PIPE_MENU, ScreenExtractorPipe::new);
    event.register(PipeRegistry.PUMP_MENU, ScreenPump::new);
    event.register(PipeRegistry.ENDER_TANK_MENU, ScreenEnderTank::new);
    event.register(QuarryRegistry.QUARRY_MENU, ScreenQuarry::new);
    event.register(ForesterRegistry.FORESTER_MENU, ScreenForester::new);
    event.register(LogisticsRegistry.CHASSIS_MENU, ScreenChassis::new);
    event.register(LogisticsRegistry.MODULE_MENU, ScreenModule::new);
    event.register(LogisticsRegistry.LOGISTICS_CONTROLLER_MENU, ScreenLogisticsController::new);
    event.register(LogisticsRegistry.REQUEST_TABLE_MENU, ScreenRequestTable::new);
    event.register(LogisticsRegistry.ASSEMBLY_TABLE_MENU, ScreenAssemblyTable::new);
    event.register(LogisticsRegistry.CRAFT_PIPE_MENU, ScreenCraftPipe::new);
    event.register(LogisticsRegistry.RECIPE_PIPE_MENU, ScreenRecipePipe::new);
    event.register(LogisticsRegistry.CORE_TASKS_MENU, ScreenCoreTasks::new);
    M1Screens.register(event);
    M2Screens.register(event);
    M3Screens.register(event);
    M4Screens.register(event);
  }

  public static void registerTooltipFactories(RegisterClientTooltipComponentFactoriesEvent event) {
    event.register(ToolboxTooltip.class, ClientToolboxTooltip::new);
  }

  public static void registerConfigScreen(ModContainer container) {
    container.registerExtensionPoint(IConfigScreenFactory.class,
        (modContainer, parent) -> new BasicConfigScreen(parent));
  }
}

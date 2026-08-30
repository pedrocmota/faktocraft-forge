package com.faktocraft;

import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.energy.provider.EnergyCore;
import com.faktocraft.common.registries.ModBlockEntities;
import com.faktocraft.common.registries.ModBlocks;
import com.faktocraft.common.registries.ModSounds;
import com.faktocraft.common.registries.RegistrationHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(IndReb.MODID)
public class IndReb {
  public static final String MODID = "faktocraft";
  public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

  public IndReb() {
    IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
    modBus.addListener(RegistrationHandler::onRegister);

    ModConfig.register();
    net.minecraftforge.fml.ModLoadingContext.get().registerConfig(
        net.minecraftforge.fml.config.ModConfig.Type.CLIENT,
        com.faktocraft.common.config.BasicConfig.CLIENT_SPEC, "faktocraft-basic-client.toml");
    net.minecraftforge.fml.ModLoadingContext.get().registerConfig(
        net.minecraftforge.fml.config.ModConfig.Type.SERVER,
        com.faktocraft.common.config.BasicConfig.SERVER_SPEC, "faktocraft-basic-server.toml");

    RegistrationHandler.setBootstrap(IndReb::bootstrapRegistries);

    com.faktocraft.common.capabilities.player.PlayerData.register(modBus);
    com.faktocraft.common.network.ModNetworking.init();

    MinecraftForge.EVENT_BUS.addListener(IndReb::onLevelTick);

    net.minecraftforge.common.world.ForgeChunkManager.setForcedChunkLoadingCallback(MODID,
        com.faktocraft.common.block.impl.chunk_loader.ChunkLoaderManager::validateTickets);

    com.faktocraft.gametest.ProgressTestReporter.installIfGameTestServer();

    LOGGER.info("Faktocraft (Forge) initialized");
  }

  private static void bootstrapRegistries() {
    ModBlocks.register();
    com.faktocraft.common.fluid.ModFluids.register();
    ModBlockEntities.register();
    com.faktocraft.common.registries.ModItems.register();
    com.faktocraft.common.registries.PipeRegistry.register();
    com.faktocraft.common.block.impl.machines.distillery.DistilleryRegistry.register();
    com.faktocraft.common.block.impl.machines.fueling_station.FuelingStationRegistry.register();
    com.faktocraft.common.block.impl.machines.geo_scanner.GeoScannerRegistry.register();
    com.faktocraft.common.block.impl.chunk_loader.ChunkLoaderRegistry.register();
    com.faktocraft.common.block.impl.quarry.QuarryRegistry.register();
    com.faktocraft.common.block.impl.logistics.LogisticsRegistry.register();
    com.faktocraft.common.worldgen.ModFeatures.register();
    com.faktocraft.common.registries.ModCreativeTab.register();
    com.faktocraft.common.registries.ModRecipeType.register();
    com.faktocraft.common.registries.ModRecipeSerializer.register();
    ModSounds.register();

    com.faktocraft.common.registries.machines.M1Registry.register();
    com.faktocraft.common.registries.machines.M2Registry.register();
    com.faktocraft.common.registries.machines.M3Registry.register();
    com.faktocraft.common.registries.machines.M4Registry.register();

    com.faktocraft.common.world.ModWorldGen.register();
  }

  private static void onLevelTick(TickEvent.LevelTickEvent event) {
    if (event.phase == TickEvent.Phase.END
        && event.level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
      EnergyCore.get(serverLevel).tick();
    }
  }
}

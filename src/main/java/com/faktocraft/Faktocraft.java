package com.faktocraft;

import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.energy.provider.EnergyCore;
import com.faktocraft.common.registries.ModBlockEntities;
import com.faktocraft.common.registries.ModBlocks;
import com.faktocraft.common.registries.ModSounds;
import com.faktocraft.common.registries.RegistrationHandler;
import com.faktocraft.common.util.NbtBridge;
import com.faktocraft.common.util.transfer.CapabilityBridge;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Faktocraft.MODID)
public class Faktocraft {
  public static final String MODID = "faktocraft";
  public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

  public Faktocraft(IEventBus modBus, ModContainer container) {
    modBus.addListener(RegistrationHandler::onRegister);
    modBus.addListener(com.faktocraft.common.network.ModNetworking::register);
    modBus.addListener(CapabilityBridge::register);
    modBus.addListener(com.faktocraft.common.block.impl.chunk_loader.ChunkLoaderManager::register);

    ModConfig.register();
    container.registerConfig(net.neoforged.fml.config.ModConfig.Type.CLIENT,
        com.faktocraft.common.config.BasicConfig.CLIENT_SPEC, "faktocraft-basic-client.toml");
    container.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER,
        com.faktocraft.common.config.BasicConfig.SERVER_SPEC, "faktocraft-basic-server.toml");

    RegistrationHandler.setBootstrap(Faktocraft::bootstrapRegistries);
    RegistrationHandler.addAliasHook(com.faktocraft.common.registries.LegacyItemRemaps::addAliases);

    com.faktocraft.common.capabilities.player.PlayerData.register(modBus);

    NeoForge.EVENT_BUS.addListener(Faktocraft::onLevelTick);
    NeoForge.EVENT_BUS.addListener(Faktocraft::onServerAboutToStart);
    NeoForge.EVENT_BUS.addListener(com.faktocraft.common.block.impl.forester.ForesterAreas::onSaplingGrow);
    NeoForge.EVENT_BUS.addListener(com.faktocraft.common.block.impl.forester.ForesterAreas::onLevelUnload);
    NeoForge.EVENT_BUS.addListener(com.faktocraft.common.block.impl.logistics.LogisticsCores::onLevelUnload);

    LOGGER.info("Faktocraft (NeoForge) initialized");
  }

  private static void bootstrapRegistries() {
    com.faktocraft.common.registries.ModDataComponents.register();
    ModBlocks.register();
    com.faktocraft.common.fluid.ModFluids.register();
    ModBlockEntities.register();
    com.faktocraft.common.registries.ModItems.register();
    com.faktocraft.common.registries.PipeRegistry.register();
    com.faktocraft.common.block.impl.machines.distillery.DistilleryRegistry.register();
    com.faktocraft.common.block.impl.machines.fueling_station.FuelingStationRegistry.register();
    com.faktocraft.common.block.impl.machines.geo_scanner.GeoScannerRegistry.register();
    com.faktocraft.common.block.impl.machines.nuclear_reactor.NuclearReactorRegistry.register();
    com.faktocraft.common.block.impl.nuke.NukeRegistry.register();
    com.faktocraft.common.block.impl.chunk_loader.ChunkLoaderRegistry.register();
    com.faktocraft.common.block.impl.quarry.QuarryRegistry.register();
    com.faktocraft.common.block.impl.forester.ForesterRegistry.register();
    com.faktocraft.common.block.impl.logistics.LogisticsRegistry.register();
    com.faktocraft.common.block.impl.monitor.MonitorRegistry.register();
    com.faktocraft.common.worldgen.ModFeatures.register();
    com.faktocraft.common.registries.ModCreativeTab.register();
    com.faktocraft.common.registries.ModRecipeType.register();
    com.faktocraft.common.registries.ModRecipeSerializer.register();
    ModSounds.register();
    com.faktocraft.common.registries.ModEffects.register();

    com.faktocraft.common.registries.machines.M1Registry.register();
    com.faktocraft.common.registries.machines.M2Registry.register();
    com.faktocraft.common.registries.machines.M3Registry.register();
    com.faktocraft.common.registries.machines.M4Registry.register();

    com.faktocraft.common.world.ModWorldGen.register();
  }

  private static void onLevelTick(LevelTickEvent.Post event) {
    if (event.getLevel() instanceof ServerLevel serverLevel) {
      EnergyCore.get(serverLevel).tick();
    }
  }

  private static void onServerAboutToStart(ServerAboutToStartEvent event) {
    NbtBridge.setFallbackRegistries(event.getServer().registryAccess());
  }
}

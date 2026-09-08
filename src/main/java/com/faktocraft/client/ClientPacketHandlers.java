package com.faktocraft.client;

import com.faktocraft.common.network.packet.PacketIEMeterInfo;
import com.faktocraft.common.network.packet.PacketParticle;
import com.faktocraft.common.network.packet.PacketTeleportFx;
import com.faktocraft.common.network.packet.PacketWindInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import java.util.Random;

public class ClientPacketHandlers {

  private static final Random RANDOM = new Random();

  private ClientPacketHandlers() {
  }

  public static void handleParticle(PacketParticle payload) {
    Minecraft minecraft = Minecraft.getInstance();
    Level level = minecraft.level;
    if (level == null) {
      return;
    }
    BlockPos pos = payload.blockPos();
    for (int i = 0; i < 5; i++) {
      double x = pos.getX() + 0.3D + RANDOM.nextDouble() * 0.4D;
      double y = pos.getY() + 0.3D + RANDOM.nextDouble() * 0.4D;
      double z = pos.getZ() + 0.3D + RANDOM.nextDouble() * 0.4D;
      level.addParticle(RANDOM.nextInt(5) == 0 ? ParticleTypes.LARGE_SMOKE : ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
    }
  }

  public static void handleTeleportFx(PacketTeleportFx payload) {
    TeleportFxOverlay.trigger(payload.dimensional());
  }

  public static void handleTeleportCharge(com.faktocraft.common.network.packet.PacketTeleportCharge payload) {
    if (payload.active()) {
      TeleportFxOverlay.startCharge(payload.durationTicks(), payload.dimensional());
    } else {
      TeleportFxOverlay.cancelCharge();
    }
  }

  public static void handleGeoScannerState(com.faktocraft.common.network.packet.PacketGeoScannerState payload) {
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.screen instanceof GeoScannerScreen screen && screen.matches(payload.blockPos())) {
      screen.applyState(payload);
    } else if (payload.openScreen()) {
      minecraft.setScreen(new GeoScannerScreen(payload));
    }
  }

  public static void handleAnchorScreen(com.faktocraft.common.network.packet.PacketAnchorScreen payload) {
    AnchorBufferScreen.open(payload.blockPos(), payload.current(), payload.min(), payload.max());
  }

  public static void handleIEMeterInfo(PacketIEMeterInfo payload) {
    Minecraft.getInstance().setScreen(new IEMeterScreen(payload));
  }

  public static void handleTableMessage(com.faktocraft.common.network.packet.PacketTableMessage payload) {
    if (Minecraft
        .getInstance().screen instanceof com.faktocraft.common.block.impl.logistics.ScreenRequestTable screen) {
      screen.showMessage(payload.message(), payload.error());
    } else {
      Minecraft.getInstance().gui.setOverlayMessage(payload.message(), false);
    }
  }

  public static void handleRecipePipeRecipes(
      com.faktocraft.common.network.packet.PacketRecipePipeRecipes payload) {
    Level level = Minecraft.getInstance().level;
    if (level != null && level.getBlockEntity(
        payload.blockPos()) instanceof com.faktocraft.common.block.impl.logistics.BlockEntityRecipePipe pipe) {
      pipe.loadRecipes(payload.recipes());
    }
  }

  public static void handleTableState(com.faktocraft.common.network.packet.PacketTableState payload) {
    if (Minecraft.getInstance().screen instanceof com.faktocraft.common.block.impl.logistics.ScreenCoreTasks coreScreen
        && coreScreen.matches(payload.blockPos())) {
      coreScreen.applyState(payload);
      return;
    }
    if (Minecraft.getInstance().screen instanceof com.faktocraft.common.block.impl.logistics.ScreenRequestTable screen
        && screen.matches(payload.blockPos())) {
      screen.applyState(payload);
    }
  }

  public static void handleWindInfo(PacketWindInfo payload) {
    InfoPanelScreen screen = new InfoPanelScreen(
        net.minecraft.network.chat.Component.translatable("item.faktocraft.wind_meter")) {
      @Override
      protected void init() {
        super.init();
        rows.clear();
        lines.clear();
        rows.add(new BarRow(net.minecraft.network.chat.Component
            .translatable("wind_meter.faktocraft.bar_wind").getString(),
            payload.windPercent() / 100.0f, 0x3E7BBF, payload.windPercent() + "%"));
        rows.add(new TextRow(net.minecraft.network.chat.Component
            .translatable("wind_meter.faktocraft.altitude", String.valueOf(payload.y())).getString(), 0x404040));
        rows.add(new SeparatorRow());
        rows.add(new TextRow(net.minecraft.network.chat.Component
            .translatable("wind_meter.faktocraft.estimate_header").getString(), 0x404040));
        int maxGenerate = Math.max(1,
            com.faktocraft.common.config.ModConfig.server().wind_generator_max_tick_generate);
        rows.add(new BarRow(net.minecraft.network.chat.Component
            .translatable("wind_meter.faktocraft.bar_output").getString(),
            (float) payload.estimate() / maxGenerate, 0x4CB20D, payload.estimate() + " IE/t"));
      }
    };
    Minecraft.getInstance().setScreen(screen);
  }
}

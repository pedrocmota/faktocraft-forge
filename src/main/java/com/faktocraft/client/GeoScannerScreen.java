package com.faktocraft.client;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.machines.geo_scanner.BlockEntityGeoScanner;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketGeoScannerControl;
import com.faktocraft.common.network.packet.PacketGeoScannerManual;
import com.faktocraft.common.network.packet.PacketGeoScannerPoll;
import com.faktocraft.common.network.packet.PacketGeoScannerState;
import com.faktocraft.common.network.packet.PacketScanCode;
import com.faktocraft.common.scan.ScanChannels;
import com.faktocraft.common.util.TextComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class GeoScannerScreen extends ScanMapScreen {

  private static final int RADIUS = BlockEntityGeoScanner.RADIUS;
  private static final int PAN_MARGIN = 12;

  private final BlockPos pos;

  private int revision;
  private boolean running;
  private int energy;
  private int capacity;
  private int scanned;
  private boolean jobActive;
  private int jobCx;
  private int jobCz;
  private int jobRemaining;
  private boolean manualPending;
  private int manualCx;
  private int manualCz;
  private int code = ScanChannels.DEFAULT_CODE;

  private Button toggleButton;
  private long lastClickMs;
  private int lastClickCx;
  private int lastClickCz;

  public GeoScannerScreen(PacketGeoScannerState state) {
    super(Component.translatable("block." + Faktocraft.MODID + ".geological_scanner"),
        new ChunkPos(state.blockPos()).x, new ChunkPos(state.blockPos()).z);
    this.pos = state.blockPos();
    applyState(state);
  }

  public boolean matches(BlockPos other) {
    return pos.equals(other);
  }

  public void applyState(PacketGeoScannerState state) {
    revision = state.revision();
    running = state.running();
    energy = state.energy();
    capacity = state.capacity();
    scanned = state.scanned();
    jobActive = state.jobActive();
    jobCx = state.jobCx();
    jobCz = state.jobCz();
    jobRemaining = state.jobRemaining();
    manualPending = state.manualPending();
    manualCx = state.manualCx();
    manualCz = state.manualCz();
    code = state.code();
    if (state.scans() != null) {
      setScans(state.scans());
    }
    if (toggleButton != null) {
      toggleButton.setMessage(toggleLabel());
      toggleButton.active = !complete();
    }
  }

  @Override
  protected String viewKey() {
    return "geo:" + pos.asLong();
  }

  @Override
  protected int panRadius() {
    return RADIUS + PAN_MARGIN;
  }

  @Override
  protected boolean outsideScanArea(int chunkX, int chunkZ) {
    return !BlockEntityGeoScanner.inRange(chunkX - centerChunkX, chunkZ - centerChunkZ);
  }

  @Override
  protected String codeText() {
    return ScanChannels.codeText(code);
  }

  @Override
  protected void applyCode(int value) {
    ModNetworking.sendToServer(PacketScanCode.forBlock(pos, value));
    requestPoll();
  }

  @Override
  protected void poll() {
    ModNetworking.sendToServer(new PacketGeoScannerPoll(pos, revision));
  }

  private boolean complete() {
    return !running && !jobActive && scanned >= BlockEntityGeoScanner.TOTAL_CHUNKS;
  }

  private Component toggleLabel() {
    return Component.translatable(GUI_PREFIX + (running ? "pause" : "start"));
  }

  private Component infoLine(int scannedValue, int energyValue) {
    return Component.translatable(GUI_PREFIX + "progress", scannedValue, BlockEntityGeoScanner.TOTAL_CHUNKS)
        .withStyle(ChatFormatting.GRAY)
        .append(Component.literal("  ·  ").withStyle(ChatFormatting.DARK_GRAY))
        .append(Component.literal(TextComponentUtil.getFormattedEnergyUnit(energyValue) + " / "
            + TextComponentUtil.getFormattedEnergyUnit(capacity) + " IE")
            .withStyle(energyValue >= BlockEntityGeoScanner.SCAN_COST ? ChatFormatting.AQUA : ChatFormatting.RED));
  }

  @Override
  protected int minMapWidth() {
    int infoW = font.width(infoLine(BlockEntityGeoScanner.TOTAL_CHUNKS, capacity)) + 12;
    int labelW = Math.max(font.width(Component.translatable(GUI_PREFIX + "start")),
        font.width(Component.translatable(GUI_PREFIX + "pause")));
    return Math.max(infoW, 2 * (labelW + 18) + 8);
  }

  @Override
  protected void addPrimaryButton(int left, int top, int width) {
    toggleButton = addRenderableWidget(new DeviceButton(left, top, width, 20, toggleLabel(), button -> {
      running = !running;
      button.setMessage(toggleLabel());
      ModNetworking.sendToServer(new PacketGeoScannerControl(pos, running));
    }));
    toggleButton.active = !complete();
  }

  @Override
  protected void renderFooter(GuiGraphics graphics, int footerTop) {
    Component status;
    ChatFormatting statusColor;
    if (jobActive) {
      status = Component.translatable(GUI_PREFIX + "status_scanning", jobCx, jobCz);
      statusColor = ChatFormatting.GREEN;
    } else if (running) {
      status = Component.translatable(GUI_PREFIX + "status_no_energy");
      statusColor = ChatFormatting.RED;
    } else if (scanned >= BlockEntityGeoScanner.TOTAL_CHUNKS) {
      status = Component.translatable(GUI_PREFIX + "status_done");
      statusColor = ChatFormatting.AQUA;
    } else {
      status = Component.translatable(GUI_PREFIX + "status_paused");
      statusColor = ChatFormatting.YELLOW;
    }
    graphics.drawCenteredString(font, status.copy().withStyle(statusColor),
        panelLeft + panelW / 2, footerTop + 8, 0xFFFFFF);
    graphics.drawCenteredString(font, infoLine(scanned, energy), panelLeft + panelW / 2, footerTop + 22,
        0xFFFFFF);
    float progress = 1.0F - jobRemaining / (float) BlockEntityGeoScanner.SCAN_DURATION_TICKS;
    drawProgressBar(graphics, footerTop + 34, progress, jobActive);
  }

  @Override
  @Nullable
  protected JobMarker activeJob() {
    return jobActive ? new JobMarker(jobCx, jobCz) : null;
  }

  @Override
  protected boolean isPendingChunk(int chunkX, int chunkZ) {
    return manualPending && manualCx == chunkX && manualCz == chunkZ;
  }

  @Override
  protected void addUnscannedHint(List<Component> lines) {
    lines.add(Component.translatable(GUI_PREFIX + "manual_hint").withStyle(ChatFormatting.AQUA,
        ChatFormatting.ITALIC));
  }

  @Override
  protected void onUnscannedClicked(int chunkX, int chunkZ) {
    if (jobActive && jobCx == chunkX && jobCz == chunkZ) {
      return;
    }
    long now = net.minecraft.Util.getMillis();
    if (chunkX == lastClickCx && chunkZ == lastClickCz && now - lastClickMs <= 350) {
      lastClickMs = 0;
      manualPending = true;
      manualCx = chunkX;
      manualCz = chunkZ;
      ModNetworking.sendToServer(new PacketGeoScannerManual(pos, chunkX, chunkZ));
    } else {
      lastClickMs = now;
      lastClickCx = chunkX;
      lastClickCz = chunkZ;
    }
  }
}

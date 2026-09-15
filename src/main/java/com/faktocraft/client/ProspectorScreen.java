package com.faktocraft.client;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.item.impl.tools.Prospector;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketProspectorScan;
import com.faktocraft.common.network.packet.PacketScanCode;
import com.faktocraft.common.registries.ModComponents;
import com.faktocraft.common.util.TextComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

public class ProspectorScreen extends ScanMapScreen {

  private static final int PAN_RADIUS = 512;
  private static final int CAPACITY = 400000;

  private Button scanButton;
  private boolean polled;

  public ProspectorScreen(ChunkPos center) {
    super(Component.translatable("item." + Faktocraft.MODID + ".prospector"), center.x, center.z);
    setScans(ProspectorClientData.scans());
  }

  public static ItemStack heldProspector(@Nullable Player player) {
    if (player == null) {
      return ItemStack.EMPTY;
    }
    if (player.getMainHandItem().getItem() instanceof Prospector) {
      return player.getMainHandItem();
    }
    if (player.getOffhandItem().getItem() instanceof Prospector) {
      return player.getOffhandItem();
    }
    return ItemStack.EMPTY;
  }

  public void refresh() {
    setScans(ProspectorClientData.scans());
  }

  @Override
  protected void init() {
    super.init();
    if (!polled) {
      polled = true;
      ProspectorClientData.poll(true);
    }
  }

  @Override
  protected String viewKey() {
    return "prospector";
  }

  @Override
  protected int panRadius() {
    return PAN_RADIUS;
  }

  @Override
  protected boolean isHomeChunk(int chunkX, int chunkZ) {
    return false;
  }

  @Override
  protected int focusChunkX() {
    return minecraft != null && minecraft.player != null ? minecraft.player.chunkPosition().x : centerChunkX;
  }

  @Override
  protected int focusChunkZ() {
    return minecraft != null && minecraft.player != null ? minecraft.player.chunkPosition().z : centerChunkZ;
  }

  @Override
  protected String codeText() {
    return ProspectorClientData.codeText();
  }

  @Override
  protected void applyCode(int value) {
    ModNetworking.sendToServer(PacketScanCode.forHeldProspector(value));
    requestPoll();
  }

  @Override
  protected void poll() {
    ProspectorClientData.poll(false);
  }

  private Component energyLine(int energy) {
    return Component.literal(TextComponentUtil.getFormattedEnergyUnit(energy) + " / "
        + TextComponentUtil.getFormattedEnergyUnit(CAPACITY) + " IE")
        .withStyle(energy >= Prospector.SCAN_COST ? ChatFormatting.AQUA : ChatFormatting.RED);
  }

  @Override
  protected int minMapWidth() {
    int infoW = font.width(energyLine(CAPACITY)) + 12;
    int labelW = font.width(Component.translatable("gui." + Faktocraft.MODID + ".prospector.scan"));
    return Math.max(infoW, 2 * (labelW + 18) + 8);
  }

  @Override
  protected void addPrimaryButton(int left, int top, int width) {
    scanButton = addRenderableWidget(new DeviceButton(left, top, width, 20,
        Component.translatable("gui." + Faktocraft.MODID + ".prospector.scan"),
        button -> ModNetworking.sendToServer(PacketProspectorScan.INSTANCE)));
  }

  @Override
  protected boolean stillValid() {
    return minecraft != null && !heldProspector(minecraft.player).isEmpty();
  }

  @Override
  protected void renderFooter(GuiGraphics graphics, int footerTop) {
    ItemStack stack = heldProspector(minecraft.player);
    CompoundTag job = Prospector.getJob(stack);
    int energy = ModComponents.getEnergy(stack, 0);
    boolean canAfford = energy >= Prospector.SCAN_COST;
    ChunkPos here = minecraft.player.chunkPosition();

    Component status;
    ChatFormatting statusColor;
    if (job != null) {
      status = Component.translatable(GUI_PREFIX + "status_scanning", job.getInt("cx"), job.getInt("cz"));
      statusColor = ChatFormatting.GREEN;
    } else if (!canAfford) {
      status = Component.translatable("gui." + Faktocraft.MODID + ".prospector.no_energy");
      statusColor = ChatFormatting.RED;
    } else {
      status = Component.translatable("gui." + Faktocraft.MODID + ".prospector.status_ready", here.x, here.z);
      statusColor = ChatFormatting.YELLOW;
    }
    graphics.drawCenteredString(font, status.copy().withStyle(statusColor),
        panelLeft + panelW / 2, footerTop + 8, 0xFFFFFF);
    graphics.drawCenteredString(font, energyLine(energy), panelLeft + panelW / 2, footerTop + 22, 0xFFFFFF);

    float progress = job == null ? 0.0F
        : 1.0F - job.getInt("remaining") / (float) Math.max(1, job.getInt("total"));
    drawProgressBar(graphics, footerTop + 34, progress, job != null);

    scanButton.active = canAfford && job == null;
  }

  @Override
  @Nullable
  protected JobMarker activeJob() {
    CompoundTag job = Prospector.getJob(heldProspector(minecraft.player));
    return job == null ? null : new JobMarker(job.getInt("cx"), job.getInt("cz"));
  }
}

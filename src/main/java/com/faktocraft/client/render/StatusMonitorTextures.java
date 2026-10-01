package com.faktocraft.client.render;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.monitor.BlockEntityStatusMonitor;
import com.faktocraft.common.block.impl.monitor.BlockStatusMonitor;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.WindowRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = Faktocraft.MODID, value = Dist.CLIENT)
public final class StatusMonitorTextures {
  public static final int SCALE = 4;
  private static final int TEXTURE_W = StatusMonitorContent.PANEL_W * SCALE;
  private static final int TEXTURE_H = StatusMonitorContent.PANEL_H * SCALE;
  private static final int IDLE_FRAMES = 200;
  private static final int NO_MOUSE = -10000;
  private static final int OUTLINE_COLOR = ARGB.black(102);
  private static final Map<GlobalPos, Entry> ENTRIES = new HashMap<>();
  private static final Map<GlobalPos, BlockEntityStatusMonitor> PENDING = new LinkedHashMap<>();
  private static final BitSet SLOTS = new BitSet();
  private static long frame;
  private static boolean reported;
  @Nullable
  private static GuiRenderState renderState;
  @Nullable
  private static GuiRenderer guiRenderer;

  private StatusMonitorTextures() {
  }

  private static final class Entry {
    private final int slot;
    private final Identifier location;
    private final TextureTarget target;
    private long lastSeen;

    private Entry(int slot, Identifier location, TextureTarget target) {
      this.slot = slot;
      this.location = location;
      this.target = target;
    }
  }

  private static final class TargetTexture extends AbstractTexture {
    private TargetTexture(RenderTarget target) {
      this.texture = target.getColorTexture();
      this.textureView = target.getColorTextureView();
      this.sampler = RenderSystem.getSamplerCache().getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE,
          FilterMode.LINEAR, FilterMode.LINEAR, false);
    }

    @Override
    protected void releaseTextures() {
      texture = null;
      textureView = null;
    }
  }

  private static final class PanelExtractor extends GuiGraphicsExtractor {
    private PanelExtractor(Minecraft mc, GuiRenderState state) {
      super(mc, state, NO_MOUSE, NO_MOUSE);
    }

    @Override
    public void enableScissor(int x0, int y0, int x1, int y1) {
    }

    @Override
    public void disableScissor() {
    }
  }

  @Nullable
  public static Identifier request(BlockEntityStatusMonitor monitor, boolean refresh) {
    Level level = monitor.getLevel();
    if (level == null) {
      return null;
    }
    GlobalPos key = GlobalPos.of(level.dimension(), monitor.getBlockPos());
    Entry entry = ENTRIES.get(key);
    if (refresh || entry == null) {
      PENDING.put(key, monitor);
    }
    if (entry == null) {
      return null;
    }
    entry.lastSeen = frame;
    return entry.location;
  }

  @SubscribeEvent
  public static void onRenderFrame(RenderFrameEvent.Pre event) {
    frame++;
    Minecraft mc = Minecraft.getInstance();
    if (mc.level == null) {
      PENDING.clear();
      releaseAll(mc);
      return;
    }
    if (!PENDING.isEmpty()) {
      update(mc);
    }
    evict(mc);
  }

  private static void update(Minecraft mc) {
    if (!StatusMonitorTarget.mixinApplied) {
      PENDING.clear();
      return;
    }
    WindowRenderState window = mc.gameRenderer.gameRenderState().windowRenderState;
    if (window.width <= 0 || window.height <= 0 || window.guiScale <= 0) {
      return;
    }
    float scaleX = window.width / (float) window.guiScale / StatusMonitorContent.PANEL_W;
    float scaleY = window.height / (float) window.guiScale / StatusMonitorContent.PANEL_H;
    if (guiRenderer == null || renderState == null) {
      renderState = new GuiRenderState();
      guiRenderer = new GuiRenderer(renderState, mc.gameRenderer.featureRenderDispatcher(), List.of());
    }
    RenderSystem.backupProjectionMatrix();
    try {
      for (Map.Entry<GlobalPos, BlockEntityStatusMonitor> pending : PENDING.entrySet()) {
        BlockEntityStatusMonitor monitor = pending.getValue();
        if (monitor.isRemoved()) {
          continue;
        }
        Entry entry = ENTRIES.computeIfAbsent(pending.getKey(), key -> create(mc));
        entry.lastSeen = frame;
        RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(entry.target.getColorTexture(),
            GuiRenderer.CLEAR_COLOR, entry.target.getDepthTexture(), 0.0);
        GuiGraphicsExtractor graphics = new PanelExtractor(mc, renderState);
        graphics.pose().pushMatrix();
        graphics.pose().scale(scaleX, scaleY);
        try {
          StatusMonitorContent.draw(graphics, monitor);
        } catch (RuntimeException e) {
          if (!reported) {
            reported = true;
            Faktocraft.LOGGER.warn("Status monitor content failed to render", e);
          }
        } finally {
          graphics.pose().popMatrix();
        }
        StatusMonitorTarget.override = entry.target;
        try {
          guiRenderer.render();
          guiRenderer.endFrame();
        } finally {
          StatusMonitorTarget.override = null;
        }
      }
      PENDING.clear();
    } finally {
      RenderSystem.restoreProjectionMatrix();
    }
  }

  private static Entry create(Minecraft mc) {
    int slot = SLOTS.nextClearBit(0);
    SLOTS.set(slot);
    TextureTarget target = new TextureTarget("Faktocraft status monitor " + slot, TEXTURE_W, TEXTURE_H,
        GpuFormat.RGBA8_UNORM, GpuFormat.D32_FLOAT);
    Identifier location = Identifier.fromNamespaceAndPath(Faktocraft.MODID, "status_monitor/" + slot);
    mc.getTextureManager().register(location, new TargetTexture(target));
    return new Entry(slot, location, target);
  }

  private static void evict(Minecraft mc) {
    List<GlobalPos> stale = new ArrayList<>();
    for (Map.Entry<GlobalPos, Entry> entry : ENTRIES.entrySet()) {
      if (frame - entry.getValue().lastSeen > IDLE_FRAMES) {
        stale.add(entry.getKey());
      }
    }
    for (GlobalPos key : stale) {
      release(mc, ENTRIES.remove(key));
    }
  }

  private static void releaseAll(Minecraft mc) {
    for (Entry entry : ENTRIES.values()) {
      release(mc, entry);
    }
    ENTRIES.clear();
  }

  private static void release(Minecraft mc, Entry entry) {
    mc.getTextureManager().release(entry.location);
    entry.target.destroyBuffers();
    SLOTS.clear(entry.slot);
  }

  @SubscribeEvent
  public static void onBlockOutline(ExtractBlockOutlineRenderStateEvent event) {
    Level level = event.getLevel();
    BlockPos pos = event.getBlockPos();
    BlockState state = event.getBlockState();
    if (!(state.getBlock() instanceof BlockStatusMonitor)) {
      return;
    }
    BlockPos master = BlockStatusMonitor.masterPos(state, pos);
    Direction right = BlockStatusMonitor.rightOf(BlockStatusMonitor.facingOf(state));
    AABB box = null;
    for (int x = 0; x < BlockStatusMonitor.WIDTH; x++) {
      for (int y = 0; y < BlockStatusMonitor.HEIGHT; y++) {
        BlockPos part = master.relative(right, x).above(y);
        BlockState partState = level.getBlockState(part);
        if (!(partState.getBlock() instanceof BlockStatusMonitor)) {
          continue;
        }
        AABB bounds = partState.getShape(level, part).bounds().move(part);
        box = box == null ? bounds : box.minmax(bounds);
      }
    }
    if (box == null) {
      return;
    }
    AABB combined = box;
    event.addCustomRenderer((BlockOutlineRenderState outline, SubmitNodeCollector collector, PoseStack poseStack,
        LevelRenderState levelState) -> {
      Vec3 camera = levelState.cameraRenderState.pos;
      Minecraft mc = Minecraft.getInstance();
      RenderType type = mc.gameRenderer.useImprovedTransparency() ? RenderTypes.linesTranslucentNoDepthWrite()
          : RenderTypes.linesTranslucent();
      VoxelShape shape = Shapes.create(combined.move(-combined.minX, -combined.minY, -combined.minZ));
      poseStack.pushPose();
      poseStack.translate(combined.minX - camera.x, combined.minY - camera.y, combined.minZ - camera.z);
      collector.submitShapeOutline(poseStack, shape, type, OUTLINE_COLOR,
          mc.gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth, outline.isTranslucent());
      poseStack.popPose();
      return true;
    });
  }
}

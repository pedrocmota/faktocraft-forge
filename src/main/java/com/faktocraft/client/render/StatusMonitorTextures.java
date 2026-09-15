package com.faktocraft.client.render;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.monitor.BlockEntityStatusMonitor;
import com.faktocraft.common.block.impl.monitor.BlockStatusMonitor;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Faktocraft.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StatusMonitorTextures {

  public static final int SCALE = 2;
  private static final int TEXTURE_W = StatusMonitorContent.PANEL_W * SCALE;
  private static final int TEXTURE_H = StatusMonitorContent.PANEL_H * SCALE;
  private static final int IDLE_FRAMES = 200;
  private static final float GUI_NEAR = 1000.0F;
  private static final float GUI_FAR = 21000.0F;
  private static final float GUI_Z = -11000.0F;
  private static final Map<GlobalPos, Entry> ENTRIES = new HashMap<>();
  private static final Map<GlobalPos, BlockEntityStatusMonitor> PENDING = new LinkedHashMap<>();
  private static final BitSet SLOTS = new BitSet();
  private static long frame;
  private static boolean reported;

  private StatusMonitorTextures() {
  }

  private static final class Entry {
    private final int slot;
    private final ResourceLocation location;
    private final TextureTarget target;
    private long lastSeen;

    private Entry(int slot, ResourceLocation location, TextureTarget target) {
      this.slot = slot;
      this.location = location;
      this.target = target;
    }
  }

  private static final class TargetTexture extends AbstractTexture {
    private final RenderTarget target;

    private TargetTexture(RenderTarget target) {
      this.target = target;
    }

    @Override
    public int getId() {
      return target.getColorTextureId();
    }

    @Override
    public void load(ResourceManager manager) {
    }
  }

  @Nullable
  public static ResourceLocation request(BlockEntityStatusMonitor monitor, boolean refresh) {
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
  public static void onRenderTick(TickEvent.RenderTickEvent event) {
    if (event.phase != TickEvent.Phase.START) {
      return;
    }
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
    Matrix4f projection = new Matrix4f(RenderSystem.getProjectionMatrix());
    VertexSorting sorting = RenderSystem.getVertexSorting();
    PoseStack modelView = RenderSystem.getModelViewStack();
    modelView.pushPose();
    modelView.setIdentity();
    modelView.translate(0.0F, 0.0F, GUI_Z);
    RenderSystem.applyModelViewMatrix();
    RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0.0F, StatusMonitorContent.PANEL_W,
        StatusMonitorContent.PANEL_H, 0.0F, GUI_NEAR, GUI_FAR), VertexSorting.ORTHOGRAPHIC_Z);
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    RenderSystem.enableBlend();
    RenderSystem.defaultBlendFunc();
    for (Map.Entry<GlobalPos, BlockEntityStatusMonitor> pending : PENDING.entrySet()) {
      BlockEntityStatusMonitor monitor = pending.getValue();
      if (monitor.isRemoved()) {
        continue;
      }
      Entry entry = ENTRIES.computeIfAbsent(pending.getKey(), key -> create(mc));
      entry.lastSeen = frame;
      entry.target.clear(Minecraft.ON_OSX);
      entry.target.bindWrite(true);
      GuiGraphics graphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
      try {
        StatusMonitorContent.draw(graphics, monitor);
      } catch (RuntimeException e) {
        if (!reported) {
          reported = true;
          Faktocraft.LOGGER.warn("Status monitor content failed to render", e);
        }
      } finally {
        graphics.flush();
      }
    }
    PENDING.clear();
    mc.getMainRenderTarget().bindWrite(true);
    modelView.popPose();
    RenderSystem.applyModelViewMatrix();
    RenderSystem.setProjectionMatrix(projection, sorting);
  }

  private static Entry create(Minecraft mc) {
    int slot = SLOTS.nextClearBit(0);
    SLOTS.set(slot);
    TextureTarget target = new TextureTarget(TEXTURE_W, TEXTURE_H, true, Minecraft.ON_OSX);
    target.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
    target.setFilterMode(GL11.GL_LINEAR);
    ResourceLocation location = new ResourceLocation(Faktocraft.MODID, "status_monitor/" + slot);
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
  public static void onHighlight(RenderHighlightEvent.Block event) {
    Level level = Minecraft.getInstance().level;
    if (level == null) {
      return;
    }
    BlockPos pos = event.getTarget().getBlockPos();
    BlockState state = level.getBlockState(pos);
    if (!(state.getBlock() instanceof BlockStatusMonitor)) {
      return;
    }
    event.setCanceled(true);
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
    Vec3 camera = event.getCamera().getPosition();
    VertexConsumer consumer = event.getMultiBufferSource().getBuffer(RenderType.lines());
    LevelRenderer.renderLineBox(event.getPoseStack(), consumer, box.move(-camera.x, -camera.y, -camera.z), 0.0F,
        0.0F, 0.0F, 0.4F);
  }
}

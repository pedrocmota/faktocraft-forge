package com.faktocraft.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.Util;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.jetbrains.annotations.Nullable;

public class TeleportFxOverlay implements IGuiOverlay {

  public static final String ID = "teleport_fx";

  private static final long FLASH_IN_MS = 120;
  private static final long FADE_OUT_MS = 900;
  private static final long TOTAL_MS = FLASH_IN_MS + FADE_OUT_MS;
  private static final long CANCEL_FADE_MS = 400;
  private static final long CHARGE_GRACE_MS = 1500;

  private static final int GLOW_RGB = 0x2FD8E8;
  private static final int CORE_RGB = 0xEAFBFF;
  private static final int DIMENSIONAL_GLOW_RGB = 0x8A3FE6;
  private static final int DIMENSIONAL_CORE_RGB = 0xF4E6FF;

  private static final ResourceLocation PORTAL_SPRITE = new ResourceLocation("minecraft", "block/nether_portal");

  private static final float[] PORTAL_TINT = { 0.35F, 0.95F, 1.0F };
  private static final float[] DIMENSIONAL_PORTAL_TINT = { 1.0F, 0.85F, 1.0F };

  private static long fxStart = Long.MIN_VALUE;
  private static long chargeStart = Long.MIN_VALUE;
  private static long chargeDurationMs = 1;
  private static boolean charging = false;
  private static long fadeStart = Long.MIN_VALUE;
  private static long fadeDurationMs = CANCEL_FADE_MS;
  private static float fadeFromAlpha = 0F;
  private static boolean dimensional = false;
  @Nullable
  private static SoundInstance chargeSound;

  public static void startCharge(int durationTicks, boolean dimensionalJump) {
    charging = true;
    chargeStart = Util.getMillis();
    chargeDurationMs = Math.max(1, durationTicks) * 50L;
    dimensional = dimensionalJump;
    fadeStart = Long.MIN_VALUE;
    stopChargeSound();
    chargeSound = SimpleSoundInstance.forUI(SoundEvents.PORTAL_TRIGGER, 0.9F, 0.6F);
    Minecraft.getInstance().getSoundManager().play(chargeSound);
  }

  public static void cancelCharge() {
    if (!charging) {
      return;
    }
    beginFade(portalAlpha(), CANCEL_FADE_MS);
    stopChargeSound();
    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.FIRE_EXTINGUISH, 0.7F, 0.6F));
  }

  public static void trigger(boolean dimensionalJump) {
    dimensional = dimensionalJump;
    fxStart = Util.getMillis();
    beginFade(Math.max(portalAlpha(), 0.8F), FADE_OUT_MS);
    stopChargeSound();
  }

  private static void beginFade(float fromAlpha, long durationMs) {
    charging = false;
    fadeStart = Util.getMillis();
    fadeDurationMs = durationMs;
    fadeFromAlpha = fromAlpha;
  }

  private static void stopChargeSound() {
    if (chargeSound != null) {
      Minecraft.getInstance().getSoundManager().stop(chargeSound);
      chargeSound = null;
    }
  }

  private static float portalAlpha() {
    long now = Util.getMillis();
    if (charging) {
      long elapsed = now - chargeStart;
      if (elapsed > chargeDurationMs + CHARGE_GRACE_MS) {
        beginFade(0.8F, CANCEL_FADE_MS);
        stopChargeSound();
        return fadeFromAlpha;
      }
      float progress = Mth.clamp(elapsed / (float) chargeDurationMs, 0F, 1F);
      return 0.12F + 0.68F * progress * progress;
    }
    if (fadeStart != Long.MIN_VALUE) {
      float t = Mth.clamp((now - fadeStart) / (float) fadeDurationMs, 0F, 1F);
      if (t >= 1F) {
        fadeStart = Long.MIN_VALUE;
        return 0F;
      }
      return fadeFromAlpha * (1F - t);
    }
    return 0F;
  }

  @Override
  public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
    float portal = portalAlpha();
    if (portal > 0F) {
      renderPortal(graphics, screenWidth, screenHeight, portal);
    }

    long elapsed = Util.getMillis() - fxStart;
    if (elapsed < 0 || elapsed > TOTAL_MS) {
      return;
    }

    float glowAlpha;
    float coreAlpha;
    if (elapsed <= FLASH_IN_MS) {
      float t = elapsed / (float) FLASH_IN_MS;
      glowAlpha = 0.85F * t;
      coreAlpha = 0.95F * t;
    } else {
      float t = (elapsed - FLASH_IN_MS) / (float) FADE_OUT_MS;
      float ease = 1F - (t * t);
      glowAlpha = 0.85F * ease;
      coreAlpha = 0.95F * Math.max(0F, 1F - (t * 2.2F));
    }

    int glow = dimensional ? DIMENSIONAL_GLOW_RGB : GLOW_RGB;
    int core = dimensional ? DIMENSIONAL_CORE_RGB : CORE_RGB;
    graphics.fill(0, 0, screenWidth, screenHeight, withAlpha(glow, glowAlpha));
    if (coreAlpha > 0) {
      graphics.fill(0, 0, screenWidth, screenHeight, withAlpha(core, coreAlpha));
    }
  }

  private static void renderPortal(GuiGraphics graphics, int screenWidth, int screenHeight, float alpha) {
    float[] tint = dimensional ? DIMENSIONAL_PORTAL_TINT : PORTAL_TINT;
    TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
        .apply(PORTAL_SPRITE);
    RenderSystem.disableDepthTest();
    RenderSystem.depthMask(false);
    RenderSystem.enableBlend();
    RenderSystem.defaultBlendFunc();
    graphics.setColor(tint[0], tint[1], tint[2], alpha);
    graphics.blit(0, 0, -90, screenWidth, screenHeight, sprite);
    graphics.setColor(1F, 1F, 1F, 1F);
    graphics.fill(0, 0, screenWidth, screenHeight, withAlpha(dimensional ? DIMENSIONAL_GLOW_RGB : GLOW_RGB,
        alpha * 0.3F));
    RenderSystem.depthMask(true);
    RenderSystem.enableDepthTest();
  }

  private static int withAlpha(int rgb, float alpha) {
    int a = Mth.clamp((int) (alpha * 255F), 0, 255);
    return (a << 24) | (rgb & 0xFFFFFF);
  }
}

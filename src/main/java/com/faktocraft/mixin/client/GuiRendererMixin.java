package com.faktocraft.mixin.client;

import com.faktocraft.client.render.StatusMonitorTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {
  @Redirect(method = "draw()V", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/client/renderer/GameRenderer;mainRenderTarget()"
          + "Lcom/mojang/blaze3d/pipeline/RenderTarget;"))
  private RenderTarget faktocraftMainRenderTarget(GameRenderer gameRenderer) {
    StatusMonitorTarget.mixinApplied = true;
    RenderTarget override = StatusMonitorTarget.override;
    return override != null ? override : gameRenderer.mainRenderTarget();
  }
}

package com.faktocraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public final class RenderStates {
  private static final int[] NO_TINTS = new int[0];

  private RenderStates() {
  }

  public static ItemStackRenderState item(@Nullable ItemStackRenderState reuse, ItemStack stack,
      @Nullable Level level, ItemDisplayContext context) {
    ItemStackRenderState state = reuse != null ? reuse : new ItemStackRenderState();
    Minecraft.getInstance().getItemModelResolver().updateForTopItem(state, stack, context, level, null, 0);
    return state;
  }

  public static ItemStackRenderState item(@Nullable ItemStackRenderState reuse, ItemStack stack,
      @Nullable Level level) {
    return item(reuse, stack, level, ItemDisplayContext.NONE);
  }

  public static void submitPart(SubmitNodeCollector collector, PoseStack poseStack, @Nullable BlockStateModelPart part,
      int light, int overlay) {
    if (part == null) {
      return;
    }
    collector.submitBlockModel(poseStack, Sheets.cutoutBlockItemSheet(), List.of(part), NO_TINTS, light, overlay, 0);
  }
}

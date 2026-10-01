package com.faktocraft.client;

import com.faktocraft.common.item.base.DiggerElectricItem;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class ToolWorkAnimation {

  private static final int HOLD_TICKS = 8;
  private static final float BLEND_TICKS = 3.0F;
  private static final Int2LongOpenHashMap LAST_SWING = new Int2LongOpenHashMap();
  private static final Int2LongOpenHashMap START = new Int2LongOpenHashMap();

  private ToolWorkAnimation() {
  }

  public static void tick(Minecraft minecraft) {
    LocalPlayer player = minecraft.player;
    if (player == null) {
      return;
    }
    if (minecraft.options.keyAttack.isDown()
        && player.getMainHandItem().getItem() instanceof DiggerElectricItem tool && tool.animatesWhileWorking()) {
      markSwing(player);
    }
  }

  public static void markSwing(LivingEntity entity) {
    int id = entity.getId();
    long now = entity.level().getGameTime();
    if (!LAST_SWING.containsKey(id) || now - LAST_SWING.get(id) > HOLD_TICKS) {
      START.put(id, now);
    }
    LAST_SWING.put(id, now);
  }

  public static float working(ItemStack stack, @Nullable LivingEntity entity) {
    if (entity == null || !ItemStack.isSameItem(entity.getMainHandItem(), stack)
        || !LAST_SWING.containsKey(entity.getId())) {
      return 0.0F;
    }
    return entity.level().getGameTime() - LAST_SWING.get(entity.getId()) <= HOLD_TICKS ? 1.0F : 0.0F;
  }

  public static float blend(LivingEntity entity, float partialTick) {
    int id = entity.getId();
    if (!LAST_SWING.containsKey(id)) {
      return 0.0F;
    }
    float now = entity.level().getGameTime() + partialTick;
    float last = LAST_SWING.get(id);
    if (now - last <= HOLD_TICKS) {
      return Mth.clamp((now - START.get(id)) / BLEND_TICKS, 0.0F, 1.0F);
    }
    return Mth.clamp(1.0F - (now - last - HOLD_TICKS) / BLEND_TICKS, 0.0F, 1.0F);
  }

  public static void clear() {
    LAST_SWING.clear();
    START.clear();
  }
}

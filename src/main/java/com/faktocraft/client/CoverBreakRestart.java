package com.faktocraft.client;

import com.faktocraft.common.cover.DrillOps;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;

public final class CoverBreakRestart {

  private CoverBreakRestart() {
  }

  public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
    if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.CLIENT_HOLD
        || !event.getLevel().isClientSide()) {
      return;
    }
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.gameMode == null || minecraft.gameMode.isDestroying()
        || !DrillOps.isBored(event.getLevel().getBlockState(event.getPos()))) {
      return;
    }
    event.setUseItem(Event.Result.DENY);
    minecraft.gameMode.startDestroyBlock(event.getPos(), event.getFace());
  }
}

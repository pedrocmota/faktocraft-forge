package com.faktocraft.common.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;
import java.util.function.Supplier;

public final class PacketContext {
  private final IPayloadContext context;

  public PacketContext(IPayloadContext context) {
    this.context = context;
  }

  public IPayloadContext neo() {
    return context;
  }

  public void enqueueWork(Runnable work) {
    context.enqueueWork(work);
  }

  @Nullable
  public ServerPlayer getSender() {
    return context.player() instanceof ServerPlayer player ? player : null;
  }

  @Nullable
  public Player getPlayer() {
    return context.player();
  }

  public void setPacketHandled(boolean handled) {
  }

  public static void runClient(Supplier<Runnable> work) {
    if (FMLEnvironment.getDist().isClient()) {
      work.get().run();
    }
  }
}

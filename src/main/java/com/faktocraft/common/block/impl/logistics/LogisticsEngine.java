package com.faktocraft.common.block.impl.logistics;

import net.neoforged.fml.common.EventBusSubscriber;
import com.faktocraft.Faktocraft;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

@EventBusSubscriber(modid = Faktocraft.MODID)
public final class LogisticsEngine {

  private static volatile ExecutorService executor;

  private LogisticsEngine() {
  }

  private static ExecutorService executor() {
    ExecutorService current = executor;
    if (current == null || current.isShutdown()) {
      synchronized (LogisticsEngine.class) {
        if (executor == null || executor.isShutdown()) {
          executor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "Faktocraft-Logistics-Engine");
            thread.setDaemon(true);
            return thread;
          });
        }
        current = executor;
      }
    }
    return current;
  }

  public static <T> CompletableFuture<T> submit(Supplier<T> job) {
    return CompletableFuture.supplyAsync(job, executor());
  }

  @SubscribeEvent
  public static void onServerStopping(ServerStoppingEvent event) {
    synchronized (LogisticsEngine.class) {
      if (executor != null) {
        executor.shutdownNow();
        executor = null;
      }
    }
  }
}

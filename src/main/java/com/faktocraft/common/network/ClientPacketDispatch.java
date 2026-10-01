package com.faktocraft.common.network;

import com.faktocraft.Faktocraft;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class ClientPacketDispatch {
  private static final Map<Class<?>, Consumer<Object>> HANDLERS = new HashMap<>();

  private ClientPacketDispatch() {
  }

  @SuppressWarnings("unchecked")
  public static <T> void register(Class<T> type, Consumer<T> handler) {
    HANDLERS.put(type, (Consumer<Object>) handler);
  }

  public static void dispatch(Object payload) {
    Consumer<Object> handler = HANDLERS.get(payload.getClass());
    if (handler != null) {
      handler.accept(payload);
    } else {
      Faktocraft.LOGGER.debug("No client handler registered for {}", payload.getClass().getSimpleName());
    }
  }
}

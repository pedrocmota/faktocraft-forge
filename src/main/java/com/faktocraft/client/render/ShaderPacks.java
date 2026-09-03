package com.faktocraft.client.render;

import java.lang.reflect.Method;

public final class ShaderPacks {

  private static final String IRIS_API = "net.irisshaders.iris.api.v0.IrisApi";

  private static Method shaderPackInUse;
  private static Object irisApi;
  private static boolean resolved;

  private ShaderPacks() {
  }

  public static boolean inUse() {
    if (!resolved) {
      resolve();
    }
    if (shaderPackInUse == null) {
      return false;
    }
    try {
      return (Boolean) shaderPackInUse.invoke(irisApi);
    } catch (ReflectiveOperationException | RuntimeException e) {
      shaderPackInUse = null;
      return false;
    }
  }

  private static void resolve() {
    resolved = true;
    try {
      Class<?> api = Class.forName(IRIS_API);
      irisApi = api.getMethod("getInstance").invoke(null);
      shaderPackInUse = api.getMethod("isShaderPackInUse");
    } catch (ReflectiveOperationException | RuntimeException e) {
      irisApi = null;
      shaderPackInUse = null;
    }
  }
}

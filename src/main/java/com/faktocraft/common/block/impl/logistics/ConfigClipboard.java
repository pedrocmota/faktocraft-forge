package com.faktocraft.common.block.impl.logistics;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public final class ConfigClipboard {

  private static final String ROOT = "faktocraftConfigClipboard";

  private ConfigClipboard() {
  }

  public static void put(Player player, String kind, CompoundTag payload) {
    CompoundTag root = new CompoundTag();
    root.putString("kind", kind);
    root.put("payload", payload);
    player.getPersistentData().put(ROOT, root);
  }

  @Nullable
  public static CompoundTag get(Player player, String kind) {
    CompoundTag root = player.getPersistentData().getCompound(ROOT);
    if (!root.getString("kind").equals(kind)) {
      return null;
    }
    return root.getCompound("payload");
  }
}

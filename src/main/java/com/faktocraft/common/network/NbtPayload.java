package com.faktocraft.common.network;

import io.netty.handler.codec.EncoderException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public final class NbtPayload {

  private static final long MAX_DECODED_BYTES = 64L * 1024 * 1024;

  private NbtPayload() {
  }

  public static void write(FriendlyByteBuf buf, @Nullable CompoundTag tag) {
    if (tag == null) {
      buf.writeBoolean(false);
      return;
    }
    buf.writeBoolean(true);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      NbtIo.writeCompressed(tag, out);
    } catch (IOException e) {
      throw new EncoderException(e);
    }
    buf.writeByteArray(out.toByteArray());
  }

  @Nullable
  public static CompoundTag read(FriendlyByteBuf buf) {
    if (!buf.readBoolean()) {
      return null;
    }
    byte[] bytes = buf.readByteArray();
    try (DataInputStream in = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes)))) {
      return NbtIo.read(in, new NbtAccounter(MAX_DECODED_BYTES));
    } catch (IOException | RuntimeException e) {
      return null;
    }
  }
}

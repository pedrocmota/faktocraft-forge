package com.faktocraft.common.network.packet;

import com.faktocraft.common.network.NbtPayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketProspectorState(int code, int revision, CompoundTag scans) {

  public static void encode(PacketProspectorState msg, FriendlyByteBuf buf) {
    buf.writeVarInt(msg.code);
    buf.writeInt(msg.revision);
    NbtPayload.write(buf, msg.scans);
  }

  public static PacketProspectorState decode(FriendlyByteBuf buf) {
    int code = buf.readVarInt();
    int revision = buf.readInt();
    CompoundTag scans = NbtPayload.read(buf);
    return new PacketProspectorState(code, revision, scans == null ? new CompoundTag() : scans);
  }

  public static void handle(PacketProspectorState msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
        () -> () -> com.faktocraft.client.ClientPacketHandlers.handleProspectorState(msg)));
    ctx.get().setPacketHandled(true);
  }
}

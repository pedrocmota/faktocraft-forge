package com.faktocraft.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.GameType;

public final class MockPlayers {

  public record Mock(ServerPlayer player, EmbeddedChannel channel) {

    public List<Object> drainOutbound() {
      List<Object> out = new ArrayList<>();
      Object message;
      while ((message = channel.readOutbound()) != null) {
        out.add(message);
      }
      return out;
    }

    public <P extends Packet<?>> List<P> drainOutbound(Class<P> type) {
      List<P> out = new ArrayList<>();
      for (Object message : drainOutbound()) {
        if (type.isInstance(message)) {
          out.add(type.cast(message));
        }
      }
      return out;
    }

    public void walk(double x, double y, double z) {
      player.connection.handleMovePlayer(new ServerboundMovePlayerPacket.Pos(x, y, z, true, false));
      player.connection.handleClientTickEnd(ServerboundClientTickEndPacket.INSTANCE);
    }

    public void remove() {
      player.level().getServer().getPlayerList().remove(player);
    }
  }

  private MockPlayers() {
  }

  public static Mock survival(GameTestHelper helper, double x, double y, double z) {
    CommonListenerCookie cookie = CommonListenerCookie
        .createInitial(new GameProfile(UUID.randomUUID(), "mock-player"), false);
    ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(),
        cookie.clientInformation()) {
      @Override
      public GameType gameMode() {
        return GameType.SURVIVAL;
      }
    };
    Connection connection = new Connection(PacketFlow.SERVERBOUND);
    EmbeddedChannel channel = new EmbeddedChannel(connection);
    helper.getLevel().getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
    player.connection.markClientLoaded();
    GameType.SURVIVAL.updatePlayerAbilities(player.getAbilities());
    player.onUpdateAbilities();
    player.teleportTo(x, y, z);
    try {
      var field = ServerGamePacketListenerImpl.class.getDeclaredField("awaitingTeleport");
      field.setAccessible(true);
      int id = field.getInt(player.connection);
      player.connection.handleAcceptTeleportPacket(new ServerboundAcceptTeleportationPacket(id, x, y, z, 0.0F, 0.0F));
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(e);
    }
    helper.onEachTick(() -> player.connection.tick());
    return new Mock(player, channel);
  }
}

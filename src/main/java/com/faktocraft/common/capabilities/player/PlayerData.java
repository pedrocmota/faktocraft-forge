package com.faktocraft.common.capabilities.player;

import com.faktocraft.common.registries.RegistrationHandler;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class PlayerData {
  public static class Data {
    public boolean nightVision;

    static final MapCodec<Data> CODEC = Codec.BOOL.optionalFieldOf("night_vision", false).xmap(value -> {
      Data data = new Data();
      data.nightVision = value;
      return data;
    }, data -> data.nightVision);
  }

  public static final AttachmentType<Data> ATTACHMENT = RegistrationHandler.enqueue(
      NeoForgeRegistries.Keys.ATTACHMENT_TYPES, "player_data",
      AttachmentType.builder(Data::new).serialize(Data.CODEC).copyOnDeath().build());

  public static boolean getNightVision(Player player) {
    return player.getData(ATTACHMENT).nightVision;
  }

  public static void setNightVision(Player player, boolean value) {
    player.getData(ATTACHMENT).nightVision = value;
  }

  public static void register(IEventBus modBus) {
  }
}

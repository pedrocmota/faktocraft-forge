package com.faktocraft.common.capabilities.player;

import com.faktocraft.Faktocraft;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerData {

  public static class Data {
    public boolean nightVision;
  }

  public static final Capability<Data> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
  });

  private static final ResourceLocation ID = new ResourceLocation(Faktocraft.MODID, "player_data");

  private static class Provider implements ICapabilitySerializable<CompoundTag> {
    private final Data data = new Data();
    private final LazyOptional<Data> optional = LazyOptional.of(() -> data);

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
      return cap == CAPABILITY ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
      CompoundTag tag = new CompoundTag();
      tag.putBoolean("night_vision", data.nightVision);
      return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
      data.nightVision = tag.getBoolean("night_vision");
    }
  }

  public static boolean getNightVision(Player player) {
    return player.getCapability(CAPABILITY).map(d -> d.nightVision).orElse(false);
  }

  public static void setNightVision(Player player, boolean value) {
    player.getCapability(CAPABILITY).ifPresent(d -> d.nightVision = value);
  }

  public static void register(IEventBus modBus) {
    modBus.addListener((RegisterCapabilitiesEvent event) -> event.register(Data.class));
    MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, PlayerData::onAttach);
    MinecraftForge.EVENT_BUS.addListener(PlayerData::onClone);
  }

  private static void onAttach(AttachCapabilitiesEvent<Entity> event) {
    if (event.getObject() instanceof Player) {
      event.addCapability(ID, new Provider());
    }
  }

  private static void onClone(PlayerEvent.Clone event) {
    event.getOriginal().reviveCaps();
    boolean value = getNightVision(event.getOriginal());
    setNightVision(event.getEntity(), value);
    event.getOriginal().invalidateCaps();
  }
}

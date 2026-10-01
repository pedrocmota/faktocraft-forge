package com.faktocraft.common.util;

import net.neoforged.neoforge.network.connection.ConnectionType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public final class BufUtil {
  private BufUtil() {
  }

  private static RegistryFriendlyByteBuf registry(FriendlyByteBuf buf) {
    if (buf instanceof RegistryFriendlyByteBuf registryBuf) {
      return registryBuf;
    }
    return new RegistryFriendlyByteBuf(buf, NbtBridge.registryAccess(), ConnectionType.NEOFORGE);
  }

  public static void writeItem(FriendlyByteBuf buf, ItemStack stack) {
    ItemStack.OPTIONAL_STREAM_CODEC.encode(registry(buf), stack);
  }

  public static ItemStack readItem(FriendlyByteBuf buf) {
    return ItemStack.OPTIONAL_STREAM_CODEC.decode(registry(buf));
  }

  public static void writeComponent(FriendlyByteBuf buf, net.minecraft.network.chat.Component component) {
    net.minecraft.network.chat.ComponentSerialization.TRUSTED_STREAM_CODEC.encode(registry(buf), component);
  }

  public static net.minecraft.network.chat.Component readComponent(FriendlyByteBuf buf) {
    return net.minecraft.network.chat.ComponentSerialization.TRUSTED_STREAM_CODEC.decode(registry(buf));
  }

  public static void writeFluidStack(FriendlyByteBuf buf, FluidStack stack) {
    FluidStack.OPTIONAL_STREAM_CODEC.encode(registry(buf), stack);
  }

  public static FluidStack readFluidStack(FriendlyByteBuf buf) {
    return FluidStack.OPTIONAL_STREAM_CODEC.decode(registry(buf));
  }
}

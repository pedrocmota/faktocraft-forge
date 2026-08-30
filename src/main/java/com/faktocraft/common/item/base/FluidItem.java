package com.faktocraft.common.item.base;

import net.minecraftforge.registries.ForgeRegistries;
import com.faktocraft.common.registries.ModComponentsFluids;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class FluidItem extends BaseItem {

  public int getFluidCapacity() {
    return 0;
  }

  @Override
  public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(
      net.minecraft.world.item.ItemStack stack, @org.jetbrains.annotations.Nullable net.minecraft.nbt.CompoundTag nbt) {
    int capacity = getFluidCapacity();
    return capacity > 0 ? new FluidItemHandlerProvider(stack, capacity) : null;
  }

  public FluidItem(Properties properties) {
    super(properties);
  }

  public static Fluid getFluid(ItemStack stack) {
    ResourceLocation id = ModComponentsFluids.getFluid(stack);
    if (id == null) {
      return Fluids.EMPTY;
    }
    return ForgeRegistries.FLUIDS.getValue(id);
  }

  public static int getFluidAmount(ItemStack stack) {
    if (getFluid(stack) == Fluids.EMPTY) {
      return 0;
    }
    return Math.max(0, ModComponentsFluids.getFluidAmount(stack, 0));
  }

  public static void setFluid(ItemStack stack, Fluid fluid, int amount) {
    if (fluid == Fluids.EMPTY || amount <= 0) {
      ModComponentsFluids.removeFluid(stack);
    } else {
      ModComponentsFluids.setFluid(stack, ForgeRegistries.FLUIDS.getKey(fluid));
      ModComponentsFluids.setFluidAmount(stack, amount);
    }
  }

  public static Component getFluidName(Fluid fluid) {
    Block block = fluid.defaultFluidState().createLegacyBlock().getBlock();
    if (block != net.minecraft.world.level.block.Blocks.AIR) {
      return block.getName();
    }
    ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid);
    return Component.literal(id.getPath().replace('_', ' '));
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    Fluid fluid = getFluid(stack);
    if (fluid != Fluids.EMPTY) {
      tooltip.add(Component.literal("< " + getFluidAmount(stack) + " mB, ")
          .append(getFluidName(fluid))
          .append(" >").withStyle(ChatFormatting.GRAY));
    } else {
      tooltip.add(Component.translatable("item.faktocraft.empty_fluid").withStyle(ChatFormatting.GRAY));
    }
    super.appendHoverText(stack, level, tooltip, flag);
  }

  @Override
  public boolean hasCraftingRemainingItem(ItemStack stack) {
    return getFluid(stack) != Fluids.EMPTY;
  }

  @Override
  public ItemStack getCraftingRemainingItem(ItemStack stack) {
    return getFluid(stack) != Fluids.EMPTY ? new ItemStack(this) : ItemStack.EMPTY;
  }
}

package com.faktocraft.common.item.base;

import com.faktocraft.common.registries.ModComponentsFluids;
import com.faktocraft.common.util.transfer.CapabilityBridge;
import com.faktocraft.common.util.transfer.IFluidHandlerItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import java.util.function.Consumer;

public class FluidItem extends BaseItem implements CapabilityBridge.IFluidHandlerItemProvider {
  public int getFluidCapacity() {
    return 0;
  }

  @Nullable
  @Override
  public IFluidHandlerItem createFluidHandler(ItemStack stack) {
    int capacity = getFluidCapacity();
    return capacity > 0 ? new FluidItemHandlerProvider(stack, capacity) : null;
  }

  public FluidItem(Properties properties) {
    super(properties);
  }

  public static Fluid getFluid(ItemStack stack) {
    Identifier id = ModComponentsFluids.getFluid(stack);
    if (id == null) {
      return Fluids.EMPTY;
    }
    return BuiltInRegistries.FLUID.getValue(id);
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
      ModComponentsFluids.setFluid(stack, BuiltInRegistries.FLUID.getKey(fluid));
      ModComponentsFluids.setFluidAmount(stack, amount);
    }
  }

  public static Component getFluidName(Fluid fluid) {
    Block block = fluid.defaultFluidState().createLegacyBlock().getBlock();
    if (block != net.minecraft.world.level.block.Blocks.AIR) {
      return block.getName();
    }
    Identifier id = BuiltInRegistries.FLUID.getKey(fluid);
    return Component.literal(id.getPath().replace('_', ' '));
  }

  @Override
  @SuppressWarnings("deprecation")
  public void appendHoverText(ItemStack stack, Item.TooltipContext level, TooltipDisplay display,
      Consumer<Component> tooltip, TooltipFlag flag) {
    Fluid fluid = getFluid(stack);
    if (fluid != Fluids.EMPTY) {
      tooltip.accept(Component.literal("< " + getFluidAmount(stack) + " mB, ")
          .append(getFluidName(fluid))
          .append(" >").withStyle(ChatFormatting.GRAY));
    } else {
      tooltip.accept(Component.translatable("item.faktocraft.empty_fluid").withStyle(ChatFormatting.GRAY));
    }
    super.appendHoverText(stack, level, display, tooltip, flag);
  }

  @Nullable
  @Override
  public ItemStackTemplate getCraftingRemainder(ItemInstance instance) {
    if (instance instanceof ItemStack stack) {
      return getFluid(stack) != Fluids.EMPTY ? new ItemStackTemplate(this) : null;
    }
    return super.getCraftingRemainder(instance);
  }
}

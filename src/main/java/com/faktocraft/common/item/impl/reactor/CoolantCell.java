package com.faktocraft.common.item.impl.reactor;

import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.fluid.ModFluids;
import com.faktocraft.common.item.base.FluidItem;
import com.faktocraft.common.item.base.FluidItemHandlerProvider;
import com.faktocraft.common.util.transfer.IFluidHandlerItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import java.util.function.Consumer;

public class CoolantCell extends FluidItem {
  private static final int BAR_COLOR = 0x4FD8FF;

  private final int sizeFactor;

  public CoolantCell(Properties properties, int sizeFactor) {
    super(properties);
    this.sizeFactor = sizeFactor;
  }

  @Override
  public int getFluidCapacity() {
    return Math.max(1, ModConfig.server().reactor_cell_coolant) * sizeFactor;
  }

  public static boolean isCoolant(FluidStack stack) {
    return !stack.isEmpty() && stack.getFluid().isSame(ModFluids.COOLANT.still());
  }

  public ItemStack makeFullStack() {
    ItemStack full = new ItemStack(this);
    setFluid(full, ModFluids.COOLANT.still(), getFluidCapacity());
    return full;
  }

  @Override
  public IFluidHandlerItem createFluidHandler(ItemStack stack) {
    return new FluidItemHandlerProvider(stack, getFluidCapacity()) {
      @Override
      public boolean isFluidValid(int tank, FluidStack resource) {
        return isCoolant(resource);
      }

      @Override
      public int fill(FluidStack resource, FluidAction action) {
        return isCoolant(resource) ? super.fill(resource, action) : 0;
      }
    };
  }

  @Nullable
  @Override
  public ItemStackTemplate getCraftingRemainder(ItemInstance instance) {
    return null;
  }

  @Override
  public boolean isBarVisible(ItemStack stack) {
    return getFluidAmount(stack) < getFluidCapacity();
  }

  @Override
  public int getBarWidth(ItemStack stack) {
    return Math.round(13.0F * getFluidAmount(stack) / (float) getFluidCapacity());
  }

  @Override
  public int getBarColor(ItemStack stack) {
    return BAR_COLOR;
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext level, TooltipDisplay display,
      Consumer<Component> tooltip, TooltipFlag flag) {
    tooltip.accept(Component.translatable("tooltip.faktocraft.coolant_cell").withStyle(ChatFormatting.GRAY));
    super.appendHoverText(stack, level, display, tooltip, flag);
  }
}

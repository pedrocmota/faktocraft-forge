package com.faktocraft.common.item.impl.reactor;

import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.fluid.ModFluids;
import com.faktocraft.common.item.base.FluidItem;
import com.faktocraft.common.item.base.FluidItemHandlerProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import java.util.List;

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
  public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
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

  @Override
  public boolean hasCraftingRemainingItem(ItemStack stack) {
    return false;
  }

  @Override
  public ItemStack getCraftingRemainingItem(ItemStack stack) {
    return ItemStack.EMPTY;
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
  public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(Component.translatable("tooltip.faktocraft.coolant_cell").withStyle(ChatFormatting.GRAY));
    super.appendHoverText(stack, level, tooltip, flag);
  }
}

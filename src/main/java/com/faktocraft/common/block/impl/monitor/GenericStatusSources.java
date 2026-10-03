package com.faktocraft.common.block.impl.monitor;

import com.faktocraft.common.util.NbtBridge;
import com.faktocraft.Faktocraft;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.util.TextComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import com.faktocraft.common.util.transfer.IFluidHandler;
import com.faktocraft.common.util.transfer.IItemHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class GenericStatusSources {

  static final String TAG_FE = "monitorFe";
  static final String TAG_FE_MAX = "monitorFeMax";
  public static final String TAG_FLUIDS = "monitorFluids";
  static final String TAG_ITEMS = "monitorItems";
  public static final String TAG_ITEM_TOTAL = "monitorItemTotal";
  static final String TAG_ITEM_SLOTS = "monitorItemSlots";
  public static final String TAG_COOK = "monitorCook";
  public static final String TAG_COOK_TOTAL = "monitorCookTotal";
  public static final String TAG_BURNING = "monitorBurning";

  private static final int MAX_TANKS = 4;
  private static final int MAX_ITEM_LINES = 4;
  private static final int PROGRESS_COLOR = 0xFFD9A441;
  private static final int BURN_COLOR = 0xFFE05A2B;

  private GenericStatusSources() {
  }

  public static void register() {
    StatusSources.register(new ForgeEnergy());
    StatusSources.register(new FluidTanks());
    StatusSources.register(new Inventory());
    StatusSources.register(new Furnace());
    StatusSources.register(new StateProperties());
  }

  private static String key(String name) {
    return "gui." + Faktocraft.MODID + ".status_monitor." + name;
  }

  private static final class ForgeEnergy implements StatusSource {

    @Override
    public void collect(ServerLevel level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
        CompoundTag out) {
      if (blockEntity == null || blockEntity instanceof FaktocraftBlockEntity) {
        return;
      }
      EnergyHandler storage = level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.Energy.BLOCK, pos,
          state, blockEntity, null);
      if (storage != null && storage.getCapacityAsInt() > 0) {
        out.putInt(TAG_FE, storage.getAmountAsInt());
        out.putInt(TAG_FE_MAX, storage.getCapacityAsInt());
      }
    }

    @Override
    public void lines(BlockState state, CompoundTag data, List<StatusLine> out) {
      int max = data.getIntOr(TAG_FE_MAX, 0);
      if (max <= 0) {
        return;
      }
      int stored = data.getIntOr(TAG_FE, 0);
      out.add(new StatusLine.Bar(Math.min(1.0F, (float) stored / max), 0xFFC03030,
          Component.literal(TextComponentUtil.getFormattedEnergyUnit(stored) + " / "
              + TextComponentUtil.getFormattedEnergyUnit(max) + " FE")));
    }
  }

  private static final class FluidTanks implements StatusSource {

    @Override
    public void collect(ServerLevel level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
        CompoundTag out) {
      if (blockEntity == null) {
        return;
      }
      IFluidHandler handler = com.faktocraft.common.util.transfer.CapabilityBridge.fluidHandler(level, pos, null);
      if (handler == null) {
        return;
      }
      ListTag tanks = new ListTag();
      for (int i = 0; i < handler.getTanks() && tanks.size() < MAX_TANKS; i++) {
        int capacity = handler.getTankCapacity(i);
        FluidStack fluid = handler.getFluidInTank(i);
        if (capacity <= 0 && fluid.isEmpty()) {
          continue;
        }
        CompoundTag tank = new CompoundTag();
        tank.putString("fluid", String.valueOf(BuiltInRegistries.FLUID.getKey(fluid.getFluid())));
        tank.putInt("amount", fluid.getAmount());
        tank.putInt("capacity", capacity);
        tanks.add(tank);
      }
      if (!tanks.isEmpty()) {
        out.put(TAG_FLUIDS, tanks);
      }
    }

    @Override
    public void lines(BlockState state, CompoundTag data, List<StatusLine> out) {
      for (Tag element : data.getListOrEmpty(TAG_FLUIDS)) {
        CompoundTag tank = (CompoundTag) element;
        int amount = tank.getIntOr("amount", 0);
        int capacity = Math.max(tank.getIntOr("capacity", 0), amount);
        Fluid fluid = BuiltInRegistries.FLUID.getValue(Identifier.tryParse(tank.getStringOr("fluid", "")));
        boolean empty = fluid == null || fluid == Fluids.EMPTY || amount <= 0;
        Component label = empty
            ? Component.literal("0 / " + capacity + " mB")
            : Component.literal(amount + " / " + capacity + " mB ")
                .append(fluid.getFluidType().getDescription());
        out.add(new StatusLine.Bar(capacity > 0 ? (float) amount / capacity : 0.0F,
            empty ? 0xFF555555 : StatusSources.fluidColor(fluid), label));
      }
    }
  }

  private static final class Inventory implements StatusSource {

    @Override
    public void collect(ServerLevel level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
        CompoundTag out) {
      if (blockEntity == null) {
        return;
      }
      IItemHandler handler = com.faktocraft.common.util.transfer.CapabilityBridge.itemHandler(level, pos, null);
      if (handler == null || handler.getSlots() == 0) {
        return;
      }
      Map<String, Integer> counts = new LinkedHashMap<>();
      int total = 0;
      for (int i = 0; i < handler.getSlots(); i++) {
        ItemStack stack = handler.getStackInSlot(i);
        if (stack.isEmpty()) {
          continue;
        }
        total += stack.getCount();
        counts.merge(String.valueOf(BuiltInRegistries.ITEM.getKey(stack.getItem())), stack.getCount(),
            Integer::sum);
      }
      out.putInt(TAG_ITEM_TOTAL, total);
      out.putInt(TAG_ITEM_SLOTS, handler.getSlots());
      ListTag items = new ListTag();
      for (Map.Entry<String, Integer> entry : counts.entrySet()) {
        if (items.size() >= MAX_ITEM_LINES) {
          break;
        }
        CompoundTag item = new CompoundTag();
        item.putString("id", entry.getKey());
        item.putInt("count", entry.getValue());
        items.add(item);
      }
      out.put(TAG_ITEMS, items);
    }

    @Override
    public void lines(BlockState state, CompoundTag data, List<StatusLine> out) {
      if (!data.contains(TAG_ITEM_SLOTS)) {
        return;
      }
      out.add(new StatusLine.Text(Component.translatable(key("items"), data.getIntOr(TAG_ITEM_TOTAL, 0),
          data.getIntOr(TAG_ITEM_SLOTS, 0)).withStyle(ChatFormatting.GRAY)));
      for (Tag element : data.getListOrEmpty(TAG_ITEMS)) {
        CompoundTag item = (CompoundTag) element;
        net.minecraft.world.item.Item found = BuiltInRegistries.ITEM.getValue(
            Identifier.tryParse(item.getStringOr("id", "")));
        if (found == null) {
          continue;
        }
        ItemStack stack = new ItemStack(found);
        out.add(new StatusLine.Item(stack, Component.literal(item.getIntOr("count", 0) + "x ")
            .append(stack.getHoverName()).withStyle(ChatFormatting.GRAY)));
      }
    }
  }

  private static final class Furnace implements StatusSource {

    @Override
    public void collect(ServerLevel level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
        CompoundTag out) {
      if (!(blockEntity instanceof AbstractFurnaceBlockEntity)) {
        return;
      }
      CompoundTag saved = blockEntity.saveWithoutMetadata(NbtBridge.registries());
      out.putInt(TAG_COOK, saved.getIntOr("cooking_time_spent", 0));
      out.putInt(TAG_COOK_TOTAL, saved.getIntOr("cooking_total_time", 0));
      out.putBoolean(TAG_BURNING, saved.getIntOr("lit_time_remaining", 0) > 0);
    }

    @Override
    public void lines(BlockState state, CompoundTag data, List<StatusLine> out) {
      if (!data.contains(TAG_COOK)) {
        return;
      }
      int total = data.getIntOr(TAG_COOK_TOTAL, 0);
      float ratio = total > 0 ? Math.min(1.0F, (float) data.getIntOr(TAG_COOK, 0) / total) : 0.0F;
      out.add(new StatusLine.Bar(ratio, PROGRESS_COLOR,
          Component.translatable(key("progress")).append(" " + Math.round(ratio * 100.0F) + "%")));
      if (data.getBooleanOr(TAG_BURNING, false)) {
        out.add(new StatusLine.Bar(1.0F, BURN_COLOR, Component.translatable(key("burning"))));
      }
    }
  }

  private static final class StateProperties implements StatusSource {

    @Override
    public void collect(ServerLevel level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
        CompoundTag out) {
    }

    @Override
    public void lines(BlockState state, CompoundTag data, List<StatusLine> out) {
      if (state.hasProperty(BlockStateProperties.LIT)) {
        boolean lit = state.getValue(BlockStateProperties.LIT);
        out.add(new StatusLine.Text(Component.translatable(key(lit ? "lit" : "unlit"))
            .withStyle(lit ? ChatFormatting.GOLD : ChatFormatting.GRAY)));
      }
      if (state.hasProperty(BlockStateProperties.POWERED)) {
        boolean powered = state.getValue(BlockStateProperties.POWERED);
        out.add(new StatusLine.Text(Component.translatable(key(powered ? "powered" : "unpowered"))
            .withStyle(powered ? ChatFormatting.RED : ChatFormatting.GRAY)));
      }
      if (state.hasProperty(BlockStateProperties.OPEN)) {
        out.add(new StatusLine.Text(com.faktocraft.integration.waila.WailaData.openClosed(
            state.getValue(BlockStateProperties.OPEN))));
      }
      addRange(state, out, "level", BlockStateProperties.LEVEL, BlockStateProperties.LEVEL_CAULDRON,
          BlockStateProperties.LEVEL_COMPOSTER, BlockStateProperties.LEVEL_HONEY);
      addRange(state, out, "age", BlockStateProperties.AGE_1, BlockStateProperties.AGE_2,
          BlockStateProperties.AGE_3, BlockStateProperties.AGE_4, BlockStateProperties.AGE_5,
          BlockStateProperties.AGE_7, BlockStateProperties.AGE_15, BlockStateProperties.AGE_25);
    }

    private static void addRange(BlockState state, List<StatusLine> out, String name, IntegerProperty... options) {
      for (IntegerProperty property : options) {
        if (state.hasProperty(property)) {
          int value = state.getValue(property);
          int max = 0;
          for (Integer possible : property.getPossibleValues()) {
            max = Math.max(max, possible);
          }
          out.add(new StatusLine.Text(Component.translatable(key(name), value, max)
              .withStyle(ChatFormatting.GRAY)));
          return;
        }
      }
    }
  }
}

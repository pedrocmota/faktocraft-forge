package com.faktocraft.common.block.impl.logistics;

import com.faktocraft.common.util.TransferUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.Nullable;

public record Endpoint(Type type, BlockPos pos, int slot) {

  public enum Type {
    INVENTORY, ASSEMBLY_IN, ASSEMBLY_OUT, TABLE_BUFFER, MACHINE_SLOT, CHASSIS, EJECTOR, DISPOSAL,
    CHASSIS_BUFFER
  }

  public static Endpoint inventory(BlockPos pos) {
    return new Endpoint(Type.INVENTORY, pos, -1);
  }

  public static Endpoint assemblyIn(BlockPos pos) {
    return new Endpoint(Type.ASSEMBLY_IN, pos, -1);
  }

  public static Endpoint assemblyOut(BlockPos pos) {
    return new Endpoint(Type.ASSEMBLY_OUT, pos, -1);
  }

  public static Endpoint tableBuffer(BlockPos pos) {
    return new Endpoint(Type.TABLE_BUFFER, pos, -1);
  }

  public static Endpoint machineSlot(BlockPos pos, int slot) {
    return new Endpoint(Type.MACHINE_SLOT, pos, slot);
  }

  public static Endpoint chassis(BlockPos pos) {
    return new Endpoint(Type.CHASSIS, pos, -1);
  }

  public static Endpoint ejector(BlockPos pos) {
    return new Endpoint(Type.EJECTOR, pos, -1);
  }

  public static Endpoint disposal(BlockPos pos) {
    return new Endpoint(Type.DISPOSAL, pos, -1);
  }

  public static Endpoint chassisBuffer(BlockPos pos) {
    return new Endpoint(Type.CHASSIS_BUFFER, pos, -1);
  }

  public boolean isLoaded(Level level) {
    return level.isLoaded(pos);
  }

  @Nullable
  private IItemHandler handler(Level level) {
    return switch (type) {
      case INVENTORY, MACHINE_SLOT -> TransferUtil.findItemHandler(level, pos, null);
      case ASSEMBLY_IN -> level.getBlockEntity(pos) instanceof BlockEntityAssemblyTable assembly
          ? new InvWrapper(assembly.getIngredients())
          : null;
      case ASSEMBLY_OUT -> level.getBlockEntity(pos) instanceof BlockEntityAssemblyTable assembly
          ? new InvWrapper(assembly.getOutput())
          : null;
      case TABLE_BUFFER -> level.getBlockEntity(pos) instanceof BlockEntityRequestTable table
          ? new InvWrapper(table.getItemStackHandler())
          : null;
      case CHASSIS_BUFFER -> level.getBlockEntity(pos) instanceof BlockEntityChassis chassis
          ? new InvWrapper(chassis.getCollectorBuffer())
          : null;
      case CHASSIS, EJECTOR, DISPOSAL -> null;
    };
  }

  public ItemStack insert(Level level, ItemStack stack, boolean simulate) {
    if (!isLoaded(level) || stack.isEmpty()) {
      return stack;
    }

    if (type == Type.DISPOSAL) {
      return level.getBlockEntity(pos) instanceof BlockEntityChassis chassis
          && chassis.hasModule(ModuleType.DISPOSAL) ? ItemStack.EMPTY : stack;
    }
    if (type == Type.EJECTOR) {
      if (!(level.getBlockEntity(pos) instanceof BlockEntityChassis chassis)
          || !chassis.hasModule(ModuleType.EJECTOR)) {
        return stack;
      }
      var facing = chassis.freeFace();
      if (facing == null) {
        return stack;
      }
      if (!simulate) {
        var target = pos.relative(facing);
        var entity = new net.minecraft.world.entity.item.ItemEntity(level,
            target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, stack.copy(),
            facing.getStepX() * 0.15, facing.getStepY() * 0.15, facing.getStepZ() * 0.15);
        level.addFreshEntity(entity);
      }
      return ItemStack.EMPTY;
    }
    if (type == Type.CHASSIS) {
      ItemStack remaining = stack.copy();
      if (level.getBlockEntity(pos) instanceof BlockEntityChassis chassis) {
        for (BlockEntityChassis.AdjacentHandler adjacent : chassis.adjacentHandlers()) {
          remaining = ItemHandlerHelper.insertItemStacked(adjacent.handler(), remaining, simulate);
          if (remaining.isEmpty()) {
            return ItemStack.EMPTY;
          }
        }
      }
      return remaining;
    }
    IItemHandler handler = handler(level);
    if (handler == null) {
      return stack;
    }
    if (type == Type.MACHINE_SLOT) {
      if (slot < 0 || slot >= handler.getSlots()) {
        return stack;
      }
      return handler.insertItem(slot, stack.copy(), simulate);
    }
    return ItemHandlerHelper.insertItemStacked(handler, stack.copy(), simulate);
  }

  public int count(Level level, ItemKey key) {
    if (!isLoaded(level)) {
      return 0;
    }
    if (type == Type.CHASSIS) {
      int total = 0;
      if (level.getBlockEntity(pos) instanceof BlockEntityChassis chassis) {
        for (BlockEntityChassis.AdjacentHandler adjacent : chassis.adjacentHandlers()) {
          total += countIn(adjacent.handler(), key);
        }
      }
      return total;
    }
    IItemHandler handler = handler(level);
    if (handler == null) {
      return 0;
    }
    if (type == Type.MACHINE_SLOT) {
      if (slot < 0 || slot >= handler.getSlots()) {
        return 0;
      }
      ItemStack inSlot = handler.getStackInSlot(slot);
      return key.matches(inSlot) ? inSlot.getCount() : 0;
    }
    return countIn(handler, key);
  }

  private static int countIn(IItemHandler handler, ItemKey key) {
    int total = 0;
    for (int i = 0; i < handler.getSlots(); i++) {
      ItemStack inSlot = handler.getStackInSlot(i);
      if (key.matches(inSlot)) {
        total += inSlot.getCount();
      }
    }
    return total;
  }

  public int extract(Level level, ItemKey key, int amount, boolean simulate) {
    if (!isLoaded(level) || amount <= 0) {
      return 0;
    }
    IItemHandler handler = handler(level);
    if (handler == null) {
      return 0;
    }
    int extracted = 0;
    int start = type == Type.MACHINE_SLOT ? Math.max(0, slot) : 0;
    int end = type == Type.MACHINE_SLOT ? Math.max(0, slot) + 1 : handler.getSlots();
    for (int i = start; i < Math.min(end, handler.getSlots()) && extracted < amount; i++) {
      ItemStack inSlot = handler.getStackInSlot(i);
      if (!key.matches(inSlot)) {
        continue;
      }
      extracted += handler.extractItem(i, amount - extracted, simulate).getCount();
    }
    return extracted;
  }

  public BlockPos routeNode() {
    return pos;
  }

  public CompoundTag save() {
    CompoundTag tag = new CompoundTag();
    tag.putByte("t", (byte) type.ordinal());
    tag.putLong("p", pos.asLong());
    tag.putInt("s", slot);
    return tag;
  }

  public static Endpoint load(CompoundTag tag) {
    return new Endpoint(Type.values()[Math.floorMod(tag.getByte("t"), Type.values().length)],
        BlockPos.of(tag.getLong("p")), tag.getInt("s"));
  }
}

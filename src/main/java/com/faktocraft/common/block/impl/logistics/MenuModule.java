package com.faktocraft.common.block.impl.logistics;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class MenuModule extends AbstractContainerMenu {

  public static final int PLAYER_INV_Y = 174;

  public static final int ACTION_BACK = 0;
  public static final int ACTION_CLEAR_LINE = 1;
  public static final int ACTION_TOGGLE_NBT = 2;
  public static final int ACTION_TOGGLE_DMG = 3;
  public static final int ACTION_COUNT_UP = 4;
  public static final int ACTION_COUNT_DOWN = 5;
  public static final int ACTION_COUNT_UP_16 = 6;
  public static final int ACTION_COUNT_DOWN_16 = 7;
  public static final int ACTION_SET_FROM_CARRIED = 8;
  public static final int ACTION_PRIORITY_UP = 10;
  public static final int ACTION_PRIORITY_DOWN = 11;
  public static final int ACTION_TOGGLE_FLAG = 12;
  public static final int ACTION_RESERVE_UP = 13;
  public static final int ACTION_RESERVE_DOWN = 14;
  public static final int ACTION_COPY_CONFIG = 16;
  public static final int ACTION_PASTE_CONFIG = 17;

  public static int encode(int action, int line) {
    return action * 16 + line;
  }

  private final BlockEntityChassis chassis;
  private final int moduleSlot;
  private final DataSlot benchMode;

  public MenuModule(int windowId, Level level, BlockPos pos, int moduleSlot, Inventory playerInventory,
      Player player) {
    super(LogisticsRegistry.MODULE_MENU, windowId);
    this.chassis = level.getBlockEntity(pos) instanceof BlockEntityChassis found ? found : null;
    this.moduleSlot = moduleSlot;

    if (!level.isClientSide() && chassis != null) {
      ItemStack module = chassis.getModules().getStackInSlot(Math.max(0, moduleSlot));
      ModuleType type = ModuleItem.typeOf(module);
      if (type != null && !ModuleSettings.isTreeCurrent(module)) {
        LogisticsItemTree.ensureBuilt(level);
        if (ModuleSettings.hasTree(module)) {
          LogisticsItemTree.repair(module, type);
        } else {
          LogisticsItemTree.migrate(module, type);
        }
        chassis.setChanged();
      }
    }

    addSlot(new Slot(chassis != null ? chassis.getModules() : new com.faktocraft.common.util.ItemStackHandler(8),
        Math.max(0, moduleSlot), 213, 8) {
      @Override
      public boolean mayPlace(ItemStack stack) {
        return false;
      }

      @Override
      public boolean mayPickup(Player taker) {
        return false;
      }
    });

    for (int row = 0; row < 3; row++) {
      for (int col = 0; col < 9; col++) {
        addSlot(new Slot(playerInventory, col + row * 9 + 9, 38 + col * 18, PLAYER_INV_Y + row * 18));
      }
    }
    for (int col = 0; col < 9; col++) {
      addSlot(new Slot(playerInventory, col, 38 + col * 18, PLAYER_INV_Y + 58));
    }

    this.benchMode = addDataSlot(chassis != null ? new DataSlot() {
      @Override
      public int get() {
        return chassis.adjacentAssembly() != null ? 1 : 0;
      }

      @Override
      public void set(int value) {
      }
    } : DataSlot.standalone());
  }

  public ItemStack getModuleStack() {
    return this.slots.get(0).getItem();
  }

  @Nullable
  public ModuleType getModuleType() {
    return ModuleItem.typeOf(getModuleStack());
  }

  public boolean isBenchMode() {
    return benchMode.get() != 0;
  }

  public BlockPos getChassisPos() {
    return chassis != null ? chassis.getBlockPos() : BlockPos.ZERO;
  }

  public int getModuleSlot() {
    return moduleSlot;
  }

  private ItemStack module() {
    return chassis != null ? chassis.getModules().getStackInSlot(moduleSlot) : ItemStack.EMPTY;
  }

  private boolean backToChassis(Player player) {
    if (!(player instanceof ServerPlayer serverPlayer)) {
      return false;
    }
    if (chassis == null || chassis.isRemoved()) {
      serverPlayer.closeContainer();
      return true;
    }
    BlockPos pos = chassis.getBlockPos();
    NetworkHooks.openScreen(serverPlayer,
        new net.minecraft.world.SimpleMenuProvider(
            (windowId, inventory, p) -> new MenuChassis(windowId, p.level(), pos, inventory, p),
            Component.translatable(chassis.getBlockState().getBlock().getDescriptionId())),
        buf -> buf.writeBlockPos(pos));
    return true;
  }

  public void applyTreeNode(String node, boolean allow) {
    ItemStack module = module();
    ModuleType type = ModuleItem.typeOf(module);
    if (module.isEmpty() || type == null || !ModuleSettings.hasTree(module) || node.isEmpty()) {
      return;
    }
    LogisticsItemTree.applyOverride(module, type, node, allow);
    chassis.setChanged();
    broadcastChanges();
  }

  public void applyTreeCount(String node, int count) {
    ItemStack module = module();
    if (module.isEmpty() || ModuleItem.typeOf(module) != ModuleType.SUPPLIER
        || !ModuleSettings.hasTree(module) || node.isEmpty()) {
      return;
    }
    LogisticsItemTree.setCount(module, node, count);
    chassis.setChanged();
    broadcastChanges();
  }

  @Override
  public boolean clickMenuButton(Player player, int id) {
    if (id < 0) {
      return false;
    }
    int action = id / 16;
    int line = id % 16;
    if (action == ACTION_BACK) {
      return backToChassis(player);
    }
    ItemStack module = module();
    if (module.isEmpty()) {
      return false;
    }
    java.util.List<ModuleSettings.FilterLine> lines = ModuleSettings.lines(module);
    ModuleSettings.FilterLine current = line < ModuleSettings.MAX_LINES && line < lines.size()
        ? lines.get(line) : null;
    switch (action) {
      case ACTION_CLEAR_LINE -> {
        if (current != null) {
          ModuleSettings.setLine(module, line, new ModuleSettings.FilterLine(ModuleSettings.LineMode.ITEM,
              ItemStack.EMPTY, "", false, false, 0));
        }
      }
      case ACTION_TOGGLE_NBT -> {
        if (current != null) {
          ModuleSettings.setLine(module, line, new ModuleSettings.FilterLine(current.mode(), current.item(),
              current.text(), !current.matchNbt(), current.matchDamage(), current.count()));
        }
      }
      case ACTION_TOGGLE_DMG -> {
        if (current != null) {
          ModuleSettings.setLine(module, line, new ModuleSettings.FilterLine(current.mode(), current.item(),
              current.text(), current.matchNbt(), !current.matchDamage(), current.count()));
        }
      }
      case ACTION_COUNT_UP, ACTION_COUNT_DOWN, ACTION_COUNT_UP_16, ACTION_COUNT_DOWN_16 -> {
        if (current != null) {
          int delta = switch (action) {
            case ACTION_COUNT_UP -> 1;
            case ACTION_COUNT_DOWN -> -1;
            case ACTION_COUNT_UP_16 -> 16;
            default -> -16;
          };
          int count = Math.max(0, Math.min(6400, current.count() + delta));
          ModuleSettings.setLine(module, line, new ModuleSettings.FilterLine(current.mode(), current.item(),
              current.text(), current.matchNbt(), current.matchDamage(), count));
        }
      }
      case ACTION_SET_FROM_CARRIED -> {
        if (current != null) {
          ItemStack carried = getCarried();
          ModuleSettings.setLine(module, line, new ModuleSettings.FilterLine(ModuleSettings.LineMode.ITEM,
              carried.isEmpty() ? ItemStack.EMPTY : carried.copyWithCount(1), "", current.matchNbt(),
              current.matchDamage(), current.count()));
        }
      }
      case ACTION_PRIORITY_UP -> ModuleSettings.setPriority(module, ModuleSettings.getPriority(module) + 1);
      case ACTION_PRIORITY_DOWN -> ModuleSettings.setPriority(module, ModuleSettings.getPriority(module) - 1);
      case ACTION_TOGGLE_FLAG -> {
        ModuleType type = ModuleItem.typeOf(module);
        String flag = null;
        if (type == ModuleType.SINK || type == ModuleType.EJECTOR || type == ModuleType.DISPOSAL) {
          flag = ModuleSettings.FLAG_OVERFLOW;
        } else if (type == ModuleType.PROVIDER) {
          flag = ModuleSettings.FLAG_EXCLUDE;
        } else if (type == ModuleType.SUPPLIER) {
          flag = ModuleSettings.FLAG_ALLOW_CRAFTS;
        }
        if (flag != null) {
          ModuleSettings.setFlag(module, flag, !ModuleSettings.getFlag(module, flag));
        }
      }
      case ACTION_RESERVE_UP -> ModuleSettings.setMinReserve(module, ModuleSettings.getMinReserve(module) + 1);
      case ACTION_RESERVE_DOWN -> ModuleSettings.setMinReserve(module, ModuleSettings.getMinReserve(module) - 1);

      case ACTION_COPY_CONFIG -> {
        ModuleType type = ModuleItem.typeOf(module);
        if (type != null) {
          ConfigClipboard.put(player, "module:" + type.id(),
              module.getTag() != null ? module.getTag().copy() : new net.minecraft.nbt.CompoundTag());
          player.displayClientMessage(Component.translatable("chat.faktocraft.config_copied"), true);
        }
      }
      case ACTION_PASTE_CONFIG -> {
        ModuleType type = ModuleItem.typeOf(module);
        net.minecraft.nbt.CompoundTag payload = type != null
            ? ConfigClipboard.get(player, "module:" + type.id())
            : null;
        if (payload == null) {
          player.displayClientMessage(Component.translatable("chat.faktocraft.config_paste_empty"), true);
          return true;
        }
        module.setTag(payload.isEmpty() ? null : payload.copy());
        player.displayClientMessage(Component.translatable("chat.faktocraft.config_pasted"), true);
      }
      default -> {
        return false;
      }
    }
    if (chassis != null) {
      chassis.setChanged();
    }
    broadcastChanges();
    return true;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    return ItemStack.EMPTY;
  }

  @Override
  public boolean stillValid(Player player) {
    return chassis != null && !chassis.isRemoved()
        && chassis.getModules().getStackInSlot(moduleSlot).getItem() instanceof ModuleItem
        && player.distanceToSqr(chassis.getBlockPos().getCenter()) <= 64.0;
  }
}

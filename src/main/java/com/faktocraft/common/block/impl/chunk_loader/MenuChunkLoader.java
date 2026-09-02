package com.faktocraft.common.block.impl.chunk_loader;

import com.faktocraft.common.container.FaktocraftMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.level.Level;
import java.util.function.ObjIntConsumer;
import java.util.function.ToIntFunction;

public class MenuChunkLoader extends FaktocraftMenu {

  public static final int BUTTON_TOGGLE = 1;
  public static final int BUTTON_MORE_CHUNKS = 2;
  public static final int BUTTON_FEWER_CHUNKS = 3;

  private final DataSlot enabledData;
  private final DataSlot chunkCountData;
  private final DataSlot statusData;
  private final DataSlot activeCountData;
  private final DataSlot maxActiveData;

  public MenuChunkLoader(int windowId, Inventory inv, BlockPos pos) {
    this(windowId, inv.player.level(), pos, inv, inv.player);
  }

  public MenuChunkLoader(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(ChunkLoaderRegistry.CHUNK_LOADER_MENU, windowId, level, pos, playerInventory, player);
    BlockEntityChunkLoader loader = getBlockEntity() instanceof BlockEntityChunkLoader be ? be : null;
    enabledData = addDataSlot(slot(loader, be -> be.isEnabledByPlayer() ? 1 : 0,
        (be, value) -> be.setEnabledByPlayer(value == 1)));
    chunkCountData = addDataSlot(slot(loader, BlockEntityChunkLoader::getChunkCount,
        BlockEntityChunkLoader::setChunkCount));
    statusData = addDataSlot(slot(loader, BlockEntityChunkLoader::getStatus,
        BlockEntityChunkLoader::setStatusClient));
    activeCountData = addDataSlot(slot(loader, BlockEntityChunkLoader::getActiveCount,
        BlockEntityChunkLoader::setActiveCountClient));
    maxActiveData = addDataSlot(slot(loader, BlockEntityChunkLoader::getMaxActive,
        BlockEntityChunkLoader::setMaxActiveClient));
    init(playerInventory);
  }

  private static DataSlot slot(BlockEntityChunkLoader loader, ToIntFunction<BlockEntityChunkLoader> getter,
      ObjIntConsumer<BlockEntityChunkLoader> setter) {
    if (loader == null) {
      return DataSlot.standalone();
    }
    return new DataSlot() {
      @Override
      public int get() {
        return getter.applyAsInt(loader);
      }

      @Override
      public void set(int value) {
        setter.accept(loader, value);
      }
    };
  }

  public boolean isEnabled() {
    return enabledData.get() == 1;
  }

  public int getChunkCount() {
    return chunkCountData.get();
  }

  public int getStatus() {
    return statusData.get();
  }

  public int getActiveCount() {
    return activeCountData.get();
  }

  public int getMaxActive() {
    return maxActiveData.get();
  }

  @Override
  public boolean clickMenuButton(Player player, int id) {
    if (getBlockEntity() instanceof BlockEntityChunkLoader loader) {
      switch (id) {
        case BUTTON_TOGGLE -> {
          loader.setEnabledByPlayer(!loader.isEnabledByPlayer());
          return true;
        }
        case BUTTON_MORE_CHUNKS -> {
          loader.setChunkCount(loader.getChunkCount() + 1);
          return true;
        }
        case BUTTON_FEWER_CHUNKS -> {
          loader.setChunkCount(loader.getChunkCount() - 1);
          return true;
        }
        default -> {
        }
      }
    }
    return super.clickMenuButton(player, id);
  }
}

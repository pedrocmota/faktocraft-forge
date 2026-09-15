package com.faktocraft.gametest.world;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.chunk_loader.BlockEntityChunkLoader;
import com.faktocraft.common.block.impl.chunk_loader.ChunkLoaderManager;
import com.faktocraft.common.block.impl.chunk_loader.ChunkLoaderRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.ArrayList;
import java.util.List;

@GameTestHolder(Faktocraft.MODID)
@PrefixGameTestTemplate(false)
public class ChunkLoaderGameTest {

  private static final String TEMPLATE = "gametest_platform";
  private static final BlockPos LOADER = new BlockPos(1, 2, 1);

  private static void place(GameTestHelper helper, BlockPos rel, Block block) {
    helper.setBlock(rel, block.defaultBlockState());
    BlockPos abs = helper.absolutePos(rel);
    block.setPlacedBy(helper.getLevel(), abs, helper.getLevel().getBlockState(abs), null, ItemStack.EMPTY);
  }

  private static BlockEntityChunkLoader loader(GameTestHelper helper, BlockPos rel) {
    if (helper.getBlockEntity(rel) instanceof BlockEntityChunkLoader be) {
      return be;
    }
    throw new IllegalStateException("no chunk loader block entity at " + rel);
  }

  private static void fillEnergy(GameTestHelper helper, BlockPos rel) {
    if (helper.getBlockEntity(rel) instanceof com.faktocraft.common.entity.block.FaktocraftBlockEntity be) {
      be.getEnergyStorage().setEnergy(be.getEnergyStorage().maxEnergy());
    }
  }

  private static GlobalPos key(GameTestHelper helper, BlockPos rel) {
    return GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(rel).immutable());
  }

  private static boolean ownChunkForced(GameTestHelper helper, BlockPos rel) {
    ForcedChunksSavedData saved = helper.getLevel().getDataStorage()
        .computeIfAbsent(ForcedChunksSavedData::load, ForcedChunksSavedData::new, ForcedChunksSavedData.FILE_ID);
    long chunk = new ChunkPos(helper.absolutePos(rel)).toLong();
    return saved.getBlockForcedChunks().getTickingChunks().values().stream().anyMatch(set -> set.contains(chunk))
        || saved.getBlockForcedChunks().getChunks().values().stream().anyMatch(set -> set.contains(chunk));
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 300)
  public static void enableForcesAndDisableReleases(GameTestHelper helper) {
    place(helper, LOADER, ChunkLoaderRegistry.CHUNK_LOADER);
    fillEnergy(helper, LOADER);
    BlockEntityChunkLoader loader = loader(helper, LOADER);
    loader.setEnabledByPlayer(true);
    GlobalPos key = key(helper, LOADER);

    helper.startSequence()
        .thenWaitUntil(() -> {
          helper.assertTrue(loader.getStatus() == BlockEntityChunkLoader.STATUS_ACTIVE,
              "status " + loader.getStatus() + ", expected ACTIVE");
          helper.assertTrue(ChunkLoaderManager.get(helper.getLevel().getServer()).isActive(key),
              "loader not registered in manager");
          helper.assertTrue(ownChunkForced(helper, LOADER), "own chunk has no forge ticket");
        })
        .thenExecute(() -> loader.setEnabledByPlayer(false))
        .thenWaitUntil(() -> {
          helper.assertTrue(loader.getStatus() == BlockEntityChunkLoader.STATUS_OFF,
              "status " + loader.getStatus() + ", expected OFF");
          helper.assertTrue(!ChunkLoaderManager.get(helper.getLevel().getServer()).isActive(key),
              "loader still registered after disable");
          helper.assertTrue(!ownChunkForced(helper, LOADER), "ticket not released after disable");
        })
        .thenSucceed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 300)
  public static void starvedLoaderReleasesChunksButKeepsSlot(GameTestHelper helper) {
    place(helper, LOADER, ChunkLoaderRegistry.CHUNK_LOADER);
    fillEnergy(helper, LOADER);
    BlockEntityChunkLoader loader = loader(helper, LOADER);
    loader.setEnabledByPlayer(true);
    GlobalPos key = key(helper, LOADER);

    helper.startSequence()
        .thenWaitUntil(() -> helper.assertTrue(loader.getStatus() == BlockEntityChunkLoader.STATUS_ACTIVE,
            "loader never became active"))
        .thenExecute(() -> loader.getEnergyStorage().setEnergy(0))
        .thenWaitUntil(() -> {
          helper.assertTrue(loader.getStatus() == BlockEntityChunkLoader.STATUS_NO_ENERGY,
              "status " + loader.getStatus() + ", expected NO_ENERGY");
          helper.assertTrue(!ownChunkForced(helper, LOADER), "starved loader kept its tickets");
          helper.assertTrue(ChunkLoaderManager.get(helper.getLevel().getServer()).isActive(key),
              "starved loader lost its slot");
        })
        .thenExecute(() -> loader.setEnabledByPlayer(false))
        .thenWaitUntil(() -> helper.assertTrue(
            !ChunkLoaderManager.get(helper.getLevel().getServer()).isActive(key), "slot not freed"))
        .thenSucceed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 100)
  public static void managerEnforcesSlotLimit(GameTestHelper helper) {
    ChunkLoaderManager manager = ChunkLoaderManager.get(helper.getLevel().getServer());
    List<GlobalPos> mine = new ArrayList<>();
    int i = 0;
    while (i < 1000) {
      GlobalPos fake = GlobalPos.of(helper.getLevel().dimension(), new BlockPos(900_000 + i++, 300, 900_000));
      if (!manager.tryActivate(fake)) {
        break;
      }
      mine.add(fake);
    }
    GlobalPos blocked = GlobalPos.of(helper.getLevel().dimension(), new BlockPos(900_000, 300, 900_001));
    boolean rejected = !manager.tryActivate(blocked);
    boolean idempotent = mine.isEmpty() || manager.tryActivate(mine.get(0));
    if (!mine.isEmpty()) {
      manager.deactivate(mine.get(0));
    }
    boolean acceptedAfterFree = mine.isEmpty() || manager.tryActivate(blocked);

    manager.deactivate(blocked);
    for (GlobalPos fake : mine) {
      manager.deactivate(fake);
    }

    if (i >= 1000) {
      helper.fail("manager accepted 1000 activations, limit not enforced");
      return;
    }
    if (!rejected) {
      helper.fail("activation beyond the slot limit was accepted");
      return;
    }
    if (!idempotent) {
      helper.fail("re-activating an already active loader was rejected");
      return;
    }
    if (!acceptedAfterFree) {
      helper.fail("freed slot could not be reused");
      return;
    }
    helper.succeed();
  }
}

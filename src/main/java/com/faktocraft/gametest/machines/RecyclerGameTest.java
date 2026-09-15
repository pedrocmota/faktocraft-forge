package com.faktocraft.gametest.machines;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.machines.recycler.BlockEntityRecycler;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.registries.machines.M2Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Faktocraft.MODID)
@PrefixGameTestTemplate(false)
public class RecyclerGameTest {

  private static final String TEMPLATE = "gametest_platform";
  private static final BlockPos POS = new BlockPos(1, 1, 1);

  private static BlockEntityRecycler place(GameTestHelper helper) {
    helper.setBlock(POS, M2Registry.RECYCLER.defaultBlockState());
    if (!(helper.getBlockEntity(POS) instanceof BlockEntityRecycler recycler)) {
      throw new GameTestAssertException("no recycler block entity");
    }
    recycler.setRedstoneOnly(false);
    recycler.getBatteryStackHandler().setStackInSlot(0, new ItemStack(ModItems.BASIC_CAPACITOR));
    recycler.getEnergyStorage().setEnergy(recycler.getEnergyStorage().maxEnergy());
    return recycler;
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 400)
  public static void recyclerTurnsNuclearWasteIntoPlentyOfScrap(GameTestHelper helper) {
    BlockEntityRecycler recycler = place(helper);
    recycler.getItemStackHandler().setStackInSlot(BlockEntityRecycler.INPUT_SLOT,
        new ItemStack(ModItems.NUCLEAR_WASTE));
    helper.succeedWhen(() -> {
      recycler.getEnergyStorage().setEnergy(recycler.getEnergyStorage().maxEnergy());
      ItemStack output = recycler.getItemStackHandler().getStackInSlot(BlockEntityRecycler.OUTPUT_SLOT);
      if (!output.is(ModItems.SCRAP) || output.getCount() < 6) {
        helper.fail("expected at least 6 scrap, got " + output);
      }
    });
  }
}

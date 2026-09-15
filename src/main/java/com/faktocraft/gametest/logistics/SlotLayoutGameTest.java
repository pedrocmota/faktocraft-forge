package com.faktocraft.gametest.logistics;

import com.faktocraft.Faktocraft;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Faktocraft.MODID)
@PrefixGameTestTemplate(false)
public class SlotLayoutGameTest {

  @GameTest(template = "gametest_platform", timeoutTicks = 20)
  public static void slotYCanBeMovedByReflection(GameTestHelper helper) {
    Slot slot = new Slot(new SimpleContainer(1), 0, 10, 20);
    ObfuscationReflectionHelper.setPrivateValue(Slot.class, slot, 77, "f_40221_");
    if (slot.y != 77 || slot.x != 10) {
      helper.fail("slot position after reflection is " + slot.x + "," + slot.y);
    }
    helper.succeed();
  }
}

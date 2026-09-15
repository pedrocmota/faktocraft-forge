package com.faktocraft.gametest.nuclear;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.item.impl.armor.HazmatArmorItem;
import com.faktocraft.common.radiation.RadiationExposure;
import com.faktocraft.common.radiation.RadiationManager;
import com.faktocraft.common.registries.ModBlocks;
import com.faktocraft.common.registries.ModEffects;
import com.faktocraft.common.registries.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.ArrayList;
import java.util.List;

@GameTestHolder(Faktocraft.MODID)
@PrefixGameTestTemplate(false)
public class RadiationGameTest {

  private static final String TEMPLATE = "gametest_platform";
  private static final BlockPos CHEST = new BlockPos(1, 1, 1);
  private static final BlockPos PIG = new BlockPos(5, 1, 1);
  private static final BlockPos WALL = new BlockPos(2, 1, 1);

  private static void placeHotChest(GameTestHelper helper) {
    placeChest(helper, new ItemStack(ModItems.ENRICHED_URANIUM_DUST, 64));
    if (helper.getBlockEntity(CHEST) instanceof ChestBlockEntity chest) {
      chest.setItem(1, new ItemStack(ModItems.ENRICHED_URANIUM_DUST, 64));
    }
  }

  private static void placeChest(GameTestHelper helper, ItemStack content) {
    helper.setBlock(CHEST, Blocks.CHEST.defaultBlockState());
    if (!(helper.getBlockEntity(CHEST) instanceof ChestBlockEntity chest)) {
      throw new GameTestAssertException("no chest block entity");
    }
    chest.setItem(0, content);
  }

  private static Pig spawnPig(GameTestHelper helper) {
    return helper.spawn(EntityType.PIG, PIG);
  }

  private static void pulse(GameTestHelper helper) {
    RadiationManager.pulse(helper.getLevel(), List.of(helper.absolutePos(PIG)));
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 40)
  public static void radiationHitsNearbyCreature(GameTestHelper helper) {
    placeHotChest(helper);
    Pig pig = spawnPig(helper);
    helper.runAfterDelay(2, () -> {
      pulse(helper);
      if (!pig.hasEffect(ModEffects.RADIATION)) {
        helper.fail("pig next to enriched uranium chest has no radiation effect");
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 40)
  public static void radiationBlockedByLead(GameTestHelper helper) {
    placeHotChest(helper);
    for (BlockPos wall : new BlockPos[] { WALL, WALL.above(), WALL.east(), WALL.east().above(),
        WALL.east(2), WALL.east(2).above() }) {
      helper.setBlock(wall, ModBlocks.LEAD_BLOCK.defaultBlockState());
    }
    Pig pig = spawnPig(helper);
    helper.runAfterDelay(2, () -> {
      pulse(helper);
      if (pig.hasEffect(ModEffects.RADIATION)) {
        helper.fail("lead wall did not block the radiation");
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 40)
  public static void radiationSparesHazmatSuit(GameTestHelper helper) {
    placeHotChest(helper);
    Pig pig = spawnPig(helper);
    pig.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.HAZMAT_HELMET));
    pig.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.HAZMAT_CHESTPLATE));
    pig.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.HAZMAT_LEGGINGS));
    pig.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.HAZMAT_BOOTS));
    if (!HazmatArmorItem.isFullSuit(pig)) {
      throw new GameTestAssertException("pig is not wearing the full suit");
    }
    helper.runAfterDelay(2, () -> {
      pulse(helper);
      if (pig.hasEffect(ModEffects.RADIATION)) {
        helper.fail("full hazmat suit did not grant immunity");
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 40)
  public static void lowDoseHurtsOnlyAfterExposure(GameTestHelper helper) {
    placeChest(helper, new ItemStack(ModItems.ENRICHED_URANIUM_DUST, 8));
    BlockPos near = new BlockPos(3, 1, 1);
    Pig pig = helper.spawn(EntityType.PIG, near);
    float full = pig.getHealth();
    helper.runAfterDelay(2, () -> {
      List<BlockPos> centers = List.of(helper.absolutePos(near));
      for (int i = 0; i < 5; i++) {
        RadiationManager.pulse(helper.getLevel(), centers);
      }
      if (pig.getHealth() < full) {
        helper.fail("a weak dose hurt right away");
      }
      if (!pig.hasEffect(ModEffects.RADIATION)) {
        helper.fail("no warning effect while the dose builds up");
      }
      for (int i = 0; i < 70; i++) {
        RadiationManager.pulse(helper.getLevel(), centers);
      }
      if (pig.getHealth() >= full) {
        helper.fail("a weak dose never hurt after " + RadiationExposure.get(pig) + " rad*s");
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 80)
  public static void highDoseHurtsHardButNeverOneShots(GameTestHelper helper) {
    placeHotChest(helper);
    if (helper.getBlockEntity(CHEST) instanceof ChestBlockEntity chest) {
      for (int slot = 2; slot < 8; slot++) {
        chest.setItem(slot, new ItemStack(ModItems.ENRICHED_URANIUM_DUST, 64));
      }
    }
    BlockPos next = new BlockPos(2, 1, 1);
    Pig pig = helper.spawn(EntityType.PIG, next);
    float full = pig.getHealth();
    List<BlockPos> centers = List.of(helper.absolutePos(next));
    helper.runAfterDelay(2, () -> {
      RadiationManager.pulse(helper.getLevel(), centers);
      if (!pig.isAlive() || pig.getHealth() != 1.0F) {
        helper.fail("first pulse should leave half a heart, health=" + pig.getHealth() + " full=" + full);
      }
      helper.runAfterDelay(25, () -> {
        RadiationManager.pulse(helper.getLevel(), centers);
        if (pig.isAlive()) {
          helper.fail("staying in a deadly dose did not kill");
        }
        helper.succeed();
      });
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 40, batch = "radiationRecover")
  public static void radiationExposureRecovers(GameTestHelper helper) {
    Pig pig = spawnPig(helper);
    RadiationManager.expose(pig, 10.0F);
    helper.runAfterDelay(2, () -> {
      pulse(helper);
      float after = RadiationExposure.get(pig);
      if (after >= 10.0F || after <= 0.0F) {
        helper.fail("exposure did not recover: " + after);
      }
      RadiationManager.expose(pig, 200.0F);
      for (int i = 0; i < 10; i++) {
        pulse(helper);
      }
      if (RadiationExposure.get(pig) > 10.0F) {
        helper.fail("a heavy exposure should fade within ten seconds, still " + RadiationExposure.get(pig));
      }
      if (pig.getHealth() < pig.getMaxHealth()) {
        helper.fail("recovering below the sickness limit should not hurt");
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 40, batch = "radiationRejoin")
  public static void radiationExposureKeepsDecayingAfterRejoin(GameTestHelper helper) {
    Pig pig = EntityType.PIG.create(helper.getLevel());
    if (pig == null) {
      helper.fail("could not create a pig");
      return;
    }
    Vec3 at = Vec3.atBottomCenterOf(helper.absolutePos(PIG));
    pig.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
    RadiationExposure.set(pig, 10.0F);
    helper.getLevel().addFreshEntity(pig);
    helper.runAfterDelay(2, () -> {
      pulse(helper);
      float after = RadiationExposure.get(pig);
      if (after >= 10.0F) {
        helper.fail("exposure carried in the entity data froze after joining the level: " + after);
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 40)
  public static void geigerReadingSeesChestAndPocket(GameTestHelper helper) {
    placeHotChest(helper);
    Pig pig = spawnPig(helper);
    helper.runAfterDelay(2, () -> {
      RadiationManager.Reading reading = RadiationManager.measure(helper.getLevel(), pig);
      if (reading.ambient() <= 0.0F || reading.strongest() == null
          || !reading.strongest().equals(helper.absolutePos(CHEST))) {
        helper.fail("geiger reading did not point at the chest: " + reading);
      }
      if (reading.carried() != 0.0F) {
        helper.fail("pig carries nothing but reading says " + reading.carried());
      }
      pig.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.URANIUM_DUST, 3));
      RadiationManager.Reading pocket = RadiationManager.measure(helper.getLevel(), pig);
      if (Math.abs(pocket.carried() - 0.375F) > 0.001F) {
        helper.fail("carried dose should be 0.375 (3 dust behind the pocket factor) but is " + pocket.carried());
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public static void uraniumOreVeinRadiatesFaintly(GameTestHelper helper) {
    List<BlockPos> vein = new ArrayList<>();
    for (int x = 1; x <= 2; x++) {
      for (int y = 1; y <= 2; y++) {
        for (int z = 1; z <= 2; z++) {
          BlockPos pos = new BlockPos(x, y, z);
          vein.add(helper.absolutePos(pos));
          helper.setBlock(pos, ModBlocks.URANIUM_ORE.defaultBlockState());
        }
      }
    }
    Pig pig = spawnPig(helper);
    RadiationManager.Reading reading = RadiationManager.measure(helper.getLevel(), pig);
    if (reading.ambient() <= 0.0F || reading.strongest() == null || !vein.contains(reading.strongest())) {
      helper.fail("uranium ore vein was not measured: " + reading);
    }
    if (reading.ambient() >= 1.0F) {
      helper.fail("eight ores should be faint, not " + reading.ambient());
    }
    for (BlockPos pos : vein) {
      helper.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }
    RadiationManager.Reading mined = RadiationManager.measure(helper.getLevel(), pig);
    if (mined.ambient() >= reading.ambient() || (mined.strongest() != null && vein.contains(mined.strongest()))) {
      helper.fail("mined vein still radiates through the cache: " + mined);
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public static void radiationDoseFallsWithDistanceAndShielding(GameTestHelper helper) {
    BlockPos source = helper.absolutePos(CHEST);
    Vec3 near = Vec3.atCenterOf(helper.absolutePos(CHEST.east(2)));
    Vec3 far = Vec3.atCenterOf(helper.absolutePos(CHEST.east(6)));
    float nearDose = RadiationManager.doseAt(helper.getLevel(), source, 100.0F, near);
    float farDose = RadiationManager.doseAt(helper.getLevel(), source, 100.0F, far);
    if (nearDose <= farDose) {
      helper.fail("dose did not fall with distance: near=" + nearDose + " far=" + farDose);
    }
    helper.setBlock(CHEST.east(1), ModBlocks.LEAD_BLOCK.defaultBlockState());
    float shielded = RadiationManager.doseAt(helper.getLevel(), source, 100.0F, near);
    if (shielded >= nearDose) {
      helper.fail("lead block did not attenuate: open=" + nearDose + " shielded=" + shielded);
    }
    helper.succeed();
  }
}

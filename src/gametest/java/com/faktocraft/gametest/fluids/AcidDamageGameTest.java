package com.faktocraft.gametest.fluids;

import com.faktocraft.common.fluid.ModFluids;
import com.faktocraft.gametest.GameTest;
import com.faktocraft.gametest.MockPlayers;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.level.block.Blocks;

public class AcidDamageGameTest {

  private static final String TEMPLATE = "gametest_platform";

  private static final BlockPos POOL_MIN = new BlockPos(1, 1, 1);
  private static final BlockPos POOL_MAX = new BlockPos(3, 3, 3);
  private static final BlockPos SPAWN = new BlockPos(2, 3, 2);
  private static final BlockPos PLAYER_SPAWN = new BlockPos(2, 1, 2);

  private static final int MAX_FIRST_HIT_TICK = 3;
  private static final int WAIT_TICKS = 40;

  private static void buildPool(GameTestHelper helper) {
    for (int x = POOL_MIN.getX() - 1; x <= POOL_MAX.getX() + 1; x++) {
      for (int z = POOL_MIN.getZ() - 1; z <= POOL_MAX.getZ() + 1; z++) {
        for (int y = POOL_MIN.getY(); y <= POOL_MAX.getY() + 1; y++) {
          boolean inside = x >= POOL_MIN.getX() && x <= POOL_MAX.getX()
              && z >= POOL_MIN.getZ() && z <= POOL_MAX.getZ() && y <= POOL_MAX.getY();
          helper.setBlock(new BlockPos(x, y, z),
              inside ? ModFluids.SULFURIC_ACID.block().defaultBlockState() : Blocks.STONE.defaultBlockState());
        }
      }
    }
  }

  private static ServerPlayer survivalPlayer(GameTestHelper helper, double x, double y, double z) {
    return MockPlayers.survival(helper, x, y, z).player();
  }

  private static void assertHurtOnContact(GameTestHelper helper, LivingEntity entity, Runnable eachTick,
      Runnable cleanup, String name) {
    float full = entity.getHealth();
    int[] firstHit = { -1 };
    int[] ticks = { 0 };
    helper.onEachTick(() -> {
      ticks[0]++;
      eachTick.run();
      if (firstHit[0] < 0 && (!entity.isAlive() || entity.getHealth() < full)) {
        firstHit[0] = ticks[0];
      }
    });
    helper.runAfterDelay(WAIT_TICKS, () -> {
      cleanup.run();
      if (firstHit[0] < 0) {
        helper.fail(name + " took no acid damage after " + WAIT_TICKS + " ticks");
        return;
      }
      if (firstHit[0] > MAX_FIRST_HIT_TICK) {
        helper.fail(name + " was first hurt only at tick " + firstHit[0]);
        return;
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 80)
  public static void acidHurtsOnContact(GameTestHelper helper) {
    buildPool(helper);
    Pig pig = helper.spawn(EntityTypes.PIG, SPAWN);
    assertHurtOnContact(helper, pig, () -> {
    }, () -> {
    }, "pig");
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 80)
  public static void acidHurtsStandingPlayerOnContact(GameTestHelper helper) {
    buildPool(helper);
    BlockPos abs = helper.absolutePos(PLAYER_SPAWN);
    ServerPlayer player = survivalPlayer(helper, abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5);
    assertHurtOnContact(helper, player, () -> {
    },
        () -> helper.getLevel().getServer().getPlayerList().remove(player), "standing player");
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 80)
  public static void acidHurtsWalkingPlayerOnContact(GameTestHelper helper) {
    buildPool(helper);
    BlockPos abs = helper.absolutePos(PLAYER_SPAWN);
    double baseX = abs.getX() + 0.5;
    double baseZ = abs.getZ() + 0.5;
    ServerPlayer player = survivalPlayer(helper, baseX, abs.getY(), baseZ);
    int[] step = { 0 };
    assertHurtOnContact(helper, player, () -> {
      step[0]++;
      double dx = step[0] % 2 == 0 ? 0.1 : -0.1;
      player.connection.handleMovePlayer(
          new ServerboundMovePlayerPacket.Pos(baseX + dx, abs.getY(), baseZ, true, false));
      player.connection.handleClientTickEnd(ServerboundClientTickEndPacket.INSTANCE);
    }, () -> helper.getLevel().getServer().getPlayerList().remove(player), "walking player");
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 120)
  public static void acidKeepsHurting(GameTestHelper helper) {
    buildPool(helper);
    Pig pig = helper.spawn(EntityTypes.PIG, SPAWN);
    helper.runAfterDelay(60, () -> {
      if (pig.isAlive()) {
        helper.fail("pig still alive with " + pig.getHealth() + " health after 60 ticks in acid");
        return;
      }
      helper.succeed();
    });
  }
}

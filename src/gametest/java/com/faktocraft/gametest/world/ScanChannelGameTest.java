package com.faktocraft.gametest.world;

import com.faktocraft.common.block.impl.machines.geo_scanner.BlockEntityGeoScanner;
import com.faktocraft.common.block.impl.machines.geo_scanner.GeoScannerRegistry;
import com.faktocraft.common.network.packet.PacketGeoScannerState;
import com.faktocraft.common.network.packet.PacketProspectorState;
import com.faktocraft.common.scan.ScanChannel;
import com.faktocraft.common.scan.ScanChannels;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import com.faktocraft.gametest.GameTest;
import com.faktocraft.gametest.TestUtil;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public class ScanChannelGameTest {

  private static final String TEMPLATE = "gametest_platform";
  private static final int SHARED_CODE = 424_242;
  private static final int OTHER_CODE = 131_313;

  private static BlockEntityGeoScanner place(GameTestHelper helper, BlockPos rel, int code) {
    helper.setBlock(rel, GeoScannerRegistry.GEO_SCANNER.defaultBlockState());
    if (!(TestUtil.blockEntity(helper, rel) instanceof BlockEntityGeoScanner scanner)) {
      throw TestUtil.assertion(helper, "no geo scanner at " + rel.toShortString());
    }
    scanner.setCode(code);
    return scanner;
  }

  private static CompoundTag fakeScan(ServerLevel level) {
    CompoundTag entries = new CompoundTag();
    entries.putInt("minecraft:iron_ore", 7);
    CompoundTag scan = new CompoundTag();
    scan.putLong("t", level.getGameTime());
    scan.put("entries", entries);
    return scan;
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public static void scanPacketsSurviveLargeScans(GameTestHelper helper) {
    CompoundTag scans = new CompoundTag();
    for (int cx = -14; cx <= 14; cx++) {
      for (int cz = -14; cz <= 14; cz++) {
        CompoundTag entries = new CompoundTag();
        for (int i = 0; i < 30; i++) {
          entries.putInt("minecraft:deepslate_diamond_ore_" + i, i + cx + cz);
        }
        CompoundTag scan = new CompoundTag();
        scan.putLong("t", 1234L);
        scan.put("entries", entries);
        scans.put(ScanChannel.localKey(cx, cz), scan);
      }
    }
    FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
    PacketProspectorState.encode(new PacketProspectorState(7, 3, scans), buf);
    PacketProspectorState prospector = PacketProspectorState.decode(buf);
    if (prospector.scans().keySet().size() != scans.keySet().size()) {
      helper.fail("prospector packet lost scans: " + prospector.scans().keySet().size());
    }
    FriendlyByteBuf buf2 = new FriendlyByteBuf(Unpooled.buffer());
    PacketGeoScannerState state = new PacketGeoScannerState(BlockPos.ZERO, true, 3, false, 0, 1, 841, false,
        0, 0, 0, false, 0, 0, 7, scans);
    PacketGeoScannerState.encode(state, buf2);
    PacketGeoScannerState decoded = PacketGeoScannerState.decode(buf2);
    if (decoded.scans() == null || decoded.scans().keySet().size() != scans.keySet().size()
        || !decoded.scans().getCompoundOrEmpty(ScanChannel.localKey(3, -2)).getCompoundOrEmpty("entries")
            .contains("minecraft:deepslate_diamond_ore_29")) {
      helper.fail("geo scanner packet did not carry all the scans");
    }
    if (buf2.writerIndex() > 1_000_000) {
      helper.fail("scanner packet is too large for a dedicated server: " + buf2.writerIndex() + " bytes");
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public static void geoScannerAreaIsRound(GameTestHelper helper) {
    BlockEntityGeoScanner scanner = place(helper, new BlockPos(2, 1, 2), SHARED_CODE);
    ChunkPos center = scanner.centerChunk();
    scanner.setManualTarget(center.x() + BlockEntityGeoScanner.RADIUS, center.z() + BlockEntityGeoScanner.RADIUS);
    if (scanner.isManualPending()) {
      helper.fail("the square corner is 20 chunks away and should be out of range");
    }
    scanner.setManualTarget(center.x() + BlockEntityGeoScanner.RADIUS, center.z());
    if (!scanner.isManualPending()) {
      helper.fail("a chunk exactly 14 chunks east should be in range");
    }
    int expected = 0;
    for (int dx = -BlockEntityGeoScanner.RADIUS; dx <= BlockEntityGeoScanner.RADIUS; dx++) {
      for (int dz = -BlockEntityGeoScanner.RADIUS; dz <= BlockEntityGeoScanner.RADIUS; dz++) {
        if (dx * dx + dz * dz <= BlockEntityGeoScanner.RADIUS * BlockEntityGeoScanner.RADIUS) {
          expected++;
        }
      }
    }
    if (BlockEntityGeoScanner.TOTAL_CHUNKS != expected || expected >= 841) {
      helper.fail("total chunks should be the disc, got " + BlockEntityGeoScanner.TOTAL_CHUNKS);
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public static void scanChannelSharedByPassword(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    BlockEntityGeoScanner first = place(helper, new BlockPos(1, 1, 1), SHARED_CODE);
    BlockEntityGeoScanner second = place(helper, new BlockPos(3, 1, 1), SHARED_CODE);
    BlockEntityGeoScanner other = place(helper, new BlockPos(5, 1, 1), OTHER_CODE);
    ChunkPos center = first.centerChunk();

    ScanChannels.get(level).channel(SHARED_CODE).put(level, center.x(), center.z(), fakeScan(level));

    if (!first.hasScan(center.x(), center.z())) {
      helper.fail("scanner does not see the scan stored on its own password");
    }
    if (!second.hasScan(center.x(), center.z())) {
      helper.fail("second scanner with the same password does not share the scan");
    }
    if (other.hasScan(center.x(), center.z())) {
      helper.fail("scanner with another password sees a foreign scan");
    }
    second.setCode(OTHER_CODE);
    if (second.hasScan(center.x(), center.z())) {
      helper.fail("changing the password kept the old channel data");
    }
    if (second.codeText().length() != ScanChannels.CODE_DIGITS) {
      helper.fail("password text is not " + ScanChannels.CODE_DIGITS + " digits");
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public static void scanChannelDefaultPassword(GameTestHelper helper) {
    helper.setBlock(new BlockPos(1, 1, 1), GeoScannerRegistry.GEO_SCANNER.defaultBlockState());
    if (!(TestUtil.blockEntity(helper, new BlockPos(1, 1, 1)) instanceof BlockEntityGeoScanner scanner)) {
      throw TestUtil.assertion(helper, "no geo scanner");
    }
    if (scanner.getCode() != ScanChannels.DEFAULT_CODE) {
      helper.fail("new scanner did not start on the default password");
    }
    scanner.setCode(ScanChannels.MAX_CODE + 1);
    if (scanner.getCode() != ScanChannels.DEFAULT_CODE) {
      helper.fail("invalid password was not reset to the default");
    }
    helper.succeed();
  }
}

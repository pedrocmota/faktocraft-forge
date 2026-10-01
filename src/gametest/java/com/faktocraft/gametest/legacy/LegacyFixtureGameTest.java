package com.faktocraft.gametest.legacy;

import com.faktocraft.common.util.NbtBridge;
import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.chunk_loader.BlockEntityChunkLoader;
import com.faktocraft.common.block.impl.chunk_loader.ChunkLoaderManager;
import com.faktocraft.common.block.impl.chunk_loader.ChunkLoaderRegistry;
import com.faktocraft.common.block.impl.logistics.BlockEntityChassis;
import com.faktocraft.common.block.impl.logistics.BlockEntityRecipePipe;
import com.faktocraft.common.block.impl.logistics.BlockEntityRequestTable;
import com.faktocraft.common.block.impl.logistics.LogisticsRegistry;
import com.faktocraft.common.block.impl.logistics.ModuleSettings;
import com.faktocraft.common.block.impl.machines.geo_scanner.BlockEntityGeoScanner;
import com.faktocraft.common.block.impl.machines.geo_scanner.GeoScannerRegistry;
import com.faktocraft.common.block.impl.monitor.BlockEntityStatusMonitor;
import com.faktocraft.common.block.impl.monitor.BlockStatusMonitor;
import com.faktocraft.common.block.impl.monitor.MonitorCardItem;
import com.faktocraft.common.block.impl.monitor.MonitorRegistry;
import com.faktocraft.common.block.impl.pipe.BlockEntityEnderTank;
import com.faktocraft.common.block.impl.pipe.BlockEntityFluidPipe;
import com.faktocraft.common.block.impl.pipe.BlockEntityTank;
import com.faktocraft.common.block.impl.pipe.EnderTankChannels;
import com.faktocraft.common.block.impl.teleport_anchor.BlockEntityTeleportAnchor;
import com.faktocraft.common.cover.CoverSupport;
import com.faktocraft.common.cover.DrillOps;
import com.faktocraft.common.cover.ICoverHost;
import com.faktocraft.common.energy.provider.EnergyCore;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.item.base.FluidItem;
import com.faktocraft.common.item.impl.tools.ToolboxMenu;
import com.faktocraft.common.fluid.ModFluids;
import com.faktocraft.common.radiation.RadiationSources;
import com.faktocraft.common.registries.ModBlocks;
import com.faktocraft.common.registries.ModComponents;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.registries.PipeRegistry;
import com.faktocraft.common.registries.machines.M1Registry;
import com.faktocraft.common.registries.machines.M2Registry;
import com.faktocraft.common.scan.ScanChannels;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import com.faktocraft.gametest.GameTest;
import com.faktocraft.gametest.TestUtil;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LegacyFixtureGameTest {
  private static final String TEMPLATE = "gametest_platform";

  static final int ENDER_CODE = 424_242;
  static final int SCAN_CODE = 131_313;
  static final int BATTERY_ENERGY = 12_345;
  static final int CELL_MB = 1000;
  static final int HAZMAT_COLOR = 0x00FF00;
  static final int TANK_MB = 12_000;
  static final int PIPE_MB = 100;
  static final int ENDER_MB = 3000;
  static final int ANCHOR_ENERGY = 40_000;
  static final int PICKAXE_DAMAGE = 100;
  static final String PICKAXE_NAME = "Picareta do Pedro";
  static final int SULFUR_DUST_COUNT = 32;
  static final int SULFUR_ORE_COUNT = 5;
  static final int DEEPSLATE_SULFUR_ORE_COUNT = 3;
  static final int SCAN_IRON = 7;
  static final int SCAN_SULFUR = 12;
  static final int SCAN_DEEPSLATE_SULFUR = 4;
  static final String RECIPE_TAG_SULFUR = "forge:dusts/sulfur";
  static final String RECIPE_TAG_IRON = "forge:ingots/iron";
  static final String RECIPE_BIND_BLOCK = "minecraft:furnace";
  static final int TREE_COUNT = 16;
  static final float AFTERMATH_STRENGTH = 5.0F;

  private final Map<String, JsonObject> entries = new LinkedHashMap<>();
  private final List<ChunkPos> forced = new ArrayList<>();
  private BlockPos base;

  private static BlockPos at(BlockPos base, int dx, int dz) {
    return base.offset(dx, 0, dz);
  }

  private static Block legacyBlock(String id) {
    return BuiltInRegistries.BLOCK.getOptional(Identifier.parse(id)).orElseThrow(
        () -> new IllegalStateException("the legacy fixture can only be built by a build that still has " + id));
  }

  private static Item legacyItem(String id) {
    return BuiltInRegistries.ITEM.getOptional(Identifier.parse(id)).orElseThrow(
        () -> new IllegalStateException("the legacy fixture can only be built by a build that still has " + id));
  }

  private static <T> T be(ServerLevel level, BlockPos pos, Class<T> type) {
    BlockEntity blockEntity = level.getBlockEntity(pos);
    if (!type.isInstance(blockEntity)) {
      throw TestUtil.assertion("no " + type.getSimpleName() + " at " + pos.toShortString() + ", found "
          + blockEntity);
    }
    return type.cast(blockEntity);
  }

  private static void place(ServerLevel level, BlockPos pos, BlockState state) {
    level.setBlock(pos, state, Block.UPDATE_ALL);
    state.getBlock().setPlacedBy(level, pos, state, null, ItemStack.EMPTY);
  }

  private static void place(ServerLevel level, BlockPos pos, Block block) {
    place(level, pos, block.defaultBlockState());
  }

  private static String id(Block block) {
    return String.valueOf(BuiltInRegistries.BLOCK.getKey(block));
  }

  private JsonObject entry(String key, BlockPos pos, String kind) {
    JsonObject entry = new JsonObject();
    entry.add("pos", LegacyFixture.pos(pos));
    entry.addProperty("kind", kind);
    entries.put(key, entry);
    return entry;
  }

  private void force(ServerLevel level, ChunkPos chunk) {
    level.setChunkForced(chunk.x(), chunk.z(), true);
    forced.add(chunk);
  }

  private void unforceAll(ServerLevel level) {
    for (ChunkPos chunk : forced) {
      level.setChunkForced(chunk.x(), chunk.z(), false);
    }
    forced.clear();
  }

  private static ItemStack chargedBattery() {
    ItemStack stack = new ItemStack(ModItems.MEDIUM_BATTERY);
    ModComponents.setEnergy(stack, BATTERY_ENERGY);
    return stack;
  }

  private static ItemStack oilCell() {
    ItemStack stack = new ItemStack(ModItems.FLUID_CELL);
    FluidItem.setFluid(stack, ModFluids.OIL.still(), CELL_MB);
    return stack;
  }

  private static ItemStack coolantCell() {
    ItemStack stack = new ItemStack(ModItems.MEDIUM_COOLANT_CELL);
    FluidItem.setFluid(stack, ModFluids.COOLANT.still(), CELL_MB);
    return stack;
  }

  private static ItemStack dyedHazmat() {
    ItemStack stack = new ItemStack(ModItems.HAZMAT_CHESTPLATE);
    stack.set(DataComponents.DYED_COLOR, new DyedItemColor(HAZMAT_COLOR));
    return stack;
  }

  private static ItemStack toolboxWithWrench() {
    ItemStack stack = new ItemStack(ModItems.TOOLBOX);
    com.faktocraft.common.util.transfer.LegacyItemStackHandler handler =
        new com.faktocraft.common.util.transfer.LegacyItemStackHandler(ToolboxMenu.SIZE);
    handler.setStackInSlot(0, new ItemStack(ModItems.WRENCH));
    NbtBridge.updateCustomData(stack, tag -> tag.put("Items", handler.serializeNBT()));
    return stack;
  }

  private static ItemStack enchantedPickaxe(ServerLevel level) {
    ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
    stack.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY), 3);
    stack.setDamageValue(PICKAXE_DAMAGE);
    stack.set(DataComponents.CUSTOM_NAME, Component.literal(PICKAXE_NAME));
    return stack;
  }

  private static ItemStack monitorCard(ServerLevel level, BlockPos target) {
    ItemStack stack = new ItemStack(MonitorRegistry.MONITOR_CARD);
    MonitorCardItem.copy(stack, level, target);
    return stack;
  }

  private static ItemStack[] sampleItems(ServerLevel level, BlockPos monitorTarget) {
    return new ItemStack[] {
        chargedBattery(),
        oilCell(),
        coolantCell(),
        dyedHazmat(),
        toolboxWithWrench(),
        new ItemStack(ModItems.SULFUR_DUST, SULFUR_DUST_COUNT),
        new ItemStack(legacyItem(LegacyFixture.SULFUR_ORE_ID), SULFUR_ORE_COUNT),
        new ItemStack(legacyItem(LegacyFixture.DEEPSLATE_SULFUR_ORE_ID), DEEPSLATE_SULFUR_ORE_COUNT),
        monitorCard(level, monitorTarget),
    };
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 1200)
  public void buildLegacyFixture(GameTestHelper helper) {
    if (!LegacyFixture.MODE_BUILD.equals(LegacyFixture.mode())) {
      helper.succeed();
      return;
    }
    ServerLevel level = helper.getLevel();
    ChunkPos baseChunk = new ChunkPos(LegacyFixture.BASE_X >> 4, LegacyFixture.BASE_Z >> 4);
    for (int dx = -1; dx <= 1; dx++) {
      for (int dz = -1; dz <= 1; dz++) {
        force(level, new ChunkPos(baseChunk.x() + dx, baseChunk.z() + dz));
      }
    }
    ChunkPos loaderChunk = new ChunkPos((LegacyFixture.BASE_X + LegacyFixture.LOADER_DX) >> 4,
        (LegacyFixture.BASE_Z + LegacyFixture.LOADER_DZ) >> 4);
    force(level, loaderChunk);

    int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING, LegacyFixture.BASE_X, LegacyFixture.BASE_Z);
    base = new BlockPos(LegacyFixture.BASE_X, surface, LegacyFixture.BASE_Z);
    Faktocraft.LOGGER.info("[LegacyFixture] building at {} (chunk {})", base.toShortString(), baseChunk);

    buildMachines(level);
    buildEnergyNetwork(level);
    buildContainers(level);
    buildFluids(level);
    buildScanner(level);
    buildLogistics(level);
    buildCovers(level);
    buildOres(level);
    buildAnchor(level);
    buildMonitor(level);
    buildRadiation(level);
    buildChunkLoader(level);

    BlockPos loaderPos = LegacyFixture.pos(entries.get("chunk_loader"));
    BlockPos monitorPos = LegacyFixture.pos(entries.get("monitor"));
    helper.startSequence()
        .thenWaitUntil(() -> {
          BlockEntityChunkLoader loader = be(level, loaderPos, BlockEntityChunkLoader.class);
          if (loader.getStatus() != BlockEntityChunkLoader.STATUS_ACTIVE) {
            throw TestUtil.assertion(helper, "chunk loader status " + loader.getStatus() + ", waiting for ACTIVE");
          }
          BlockEntityStatusMonitor monitor = be(level, monitorPos, BlockEntityStatusMonitor.class);
          if (monitor.status() != BlockEntityStatusMonitor.STATUS_OK) {
            throw TestUtil.assertion(helper, "monitor status " + monitor.status() + ", waiting for OK");
          }
        })
        .thenExecuteAfter(100, () -> {
          record(level);
          unforceAll(level);
          level.getDataStorage().saveAndJoin();
          Faktocraft.LOGGER.info("[LegacyFixture] manifest written with {} entries to {}", entries.size(),
              LegacyFixture.MANIFEST.toAbsolutePath());
        })
        .thenSucceed();
  }

  private void buildMachines(ServerLevel level) {
    BlockPos idle = at(base, 0, 0);
    place(level, idle, M2Registry.ELECTRIC_FURNACE);
    FaktocraftBlockEntity furnace = be(level, idle, FaktocraftBlockEntity.class);
    furnace.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.RAW_IRON, 64));
    furnace.getItemStackHandler().setStackInSlot(1, enchantedPickaxe(level));
    furnace.getEnergyStorage().setEnergy(0);
    JsonObject entry = entry("furnace_idle", idle, "machine");
    entry.addProperty("input_item", "minecraft:raw_iron");
    entry.addProperty("input_count", 64);
    entry.addProperty("output_item", "minecraft:diamond_pickaxe");
    entry.addProperty("output_damage", PICKAXE_DAMAGE);
    entry.addProperty("output_efficiency", 3);
    entry.addProperty("output_name", PICKAXE_NAME);

    BlockPos energized = at(base, 3, 0);
    place(level, energized, M2Registry.ELECTRIC_FURNACE);
    FaktocraftBlockEntity stored = be(level, energized, FaktocraftBlockEntity.class);
    stored.getBatteryStackHandler().setStackInSlot(0, new ItemStack(ModItems.INTERMEDIATE_CAPACITOR));
    stored.getEnergyStorage().setEnergy(stored.getEnergyStorage().maxEnergy() / 2);
    entry("furnace_energy", energized, "machine");
  }

  private void buildEnergyNetwork(ServerLevel level) {
    BlockPos mfe = at(base, 0, 3);
    place(level, mfe, M1Registry.MFE);
    FaktocraftBlockEntity bank = be(level, mfe, FaktocraftBlockEntity.class);
    bank.getEnergyStorage().setEnergy(bank.getEnergyStorage().maxEnergy());
    entry("mfe", mfe, "machine");
    for (int i = 1; i <= 3; i++) {
      place(level, at(base, i, 3), ModBlocks.GOLD_CABLE_INSULATED);
    }
    entry("cable", at(base, 2, 3), "cable");
  }

  private void buildContainers(ServerLevel level) {
    BlockPos monitorTarget = at(base, 3, 0);
    BlockPos chestPos = at(base, 0, 6);
    level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
    ChestBlockEntity chest = be(level, chestPos, ChestBlockEntity.class);
    ItemStack[] items = sampleItems(level, monitorTarget);
    for (int i = 0; i < items.length; i++) {
      chest.setItem(i, items[i]);
    }
    itemsEntry(entry("chest", chestPos, "vanilla_container"), monitorTarget);

    BlockPos tablePos = at(base, 3, 6);
    place(level, tablePos, LogisticsRegistry.REQUEST_TABLE);
    BlockEntityRequestTable table = be(level, tablePos, BlockEntityRequestTable.class);
    ItemStack[] again = sampleItems(level, monitorTarget);
    for (int i = 0; i < again.length; i++) {
      table.getItemStackHandler().setStackInSlot(i, again[i]);
    }
    itemsEntry(entry("request_table", tablePos, "mod_container"), monitorTarget);
  }

  private static void itemsEntry(JsonObject entry, BlockPos monitorTarget) {
    entry.addProperty("battery_energy", BATTERY_ENERGY);
    entry.addProperty("cell_fluid", "faktocraft:oil");
    entry.addProperty("cell_mb", CELL_MB);
    entry.addProperty("coolant_fluid", "faktocraft:coolant");
    entry.addProperty("coolant_mb", CELL_MB);
    entry.addProperty("hazmat_color", HAZMAT_COLOR);
    entry.addProperty("toolbox_item", "faktocraft:wrench");
    entry.addProperty("sulfur_dust_count", SULFUR_DUST_COUNT);
    entry.addProperty("sulfur_ore_item", LegacyFixture.SULFUR_ORE_ID);
    entry.addProperty("sulfur_ore_item_after_port", "minecraft:stone");
    entry.addProperty("sulfur_ore_count", SULFUR_ORE_COUNT);
    entry.addProperty("deepslate_sulfur_ore_item", LegacyFixture.DEEPSLATE_SULFUR_ORE_ID);
    entry.addProperty("deepslate_sulfur_ore_item_after_port", "minecraft:deepslate");
    entry.addProperty("deepslate_sulfur_ore_count", DEEPSLATE_SULFUR_ORE_COUNT);
    entry.add("monitor_card_target", LegacyFixture.pos(monitorTarget));
  }

  private void buildFluids(ServerLevel level) {
    BlockPos tankPos = at(base, 6, 6);
    place(level, tankPos, PipeRegistry.TANK);
    BlockEntityTank tank = be(level, tankPos, BlockEntityTank.class);
    tank.tank.fillFluid(new FluidStack(ModFluids.OIL.still(), TANK_MB), TANK_MB, false);
    JsonObject tankEntry = entry("tank", tankPos, "tank");
    tankEntry.addProperty("fluid", "faktocraft:oil");
    tankEntry.addProperty("mb", TANK_MB);

    BlockPos pipePos = at(base, 8, 6);
    place(level, pipePos, PipeRegistry.FLUID_STONE_PIPE);
    BlockEntityFluidPipe pipe = be(level, pipePos, BlockEntityFluidPipe.class);
    pipe.tank.fillFluid(new FluidStack(Fluids.WATER, PIPE_MB), PIPE_MB, false);
    JsonObject pipeEntry = entry("fluid_pipe", pipePos, "fluid_pipe");
    pipeEntry.addProperty("fluid", "minecraft:water");
    pipeEntry.addProperty("mb", PIPE_MB);

    BlockPos enderPos = at(base, 10, 6);
    place(level, enderPos, PipeRegistry.ENDER_TANK);
    BlockEntityEnderTank ender = be(level, enderPos, BlockEntityEnderTank.class);
    ender.setCode(ENDER_CODE);
    EnderTankChannels.get(level).channel(ENDER_CODE).fillFluid(new FluidStack(Fluids.LAVA, ENDER_MB), ENDER_MB,
        false);
    JsonObject enderEntry = entry("ender_tank", enderPos, "ender_tank");
    enderEntry.addProperty("code", ENDER_CODE);
    enderEntry.addProperty("fluid", "minecraft:lava");
    enderEntry.addProperty("mb", ENDER_MB);
  }

  private void buildScanner(ServerLevel level) {
    BlockPos pos = at(base, 0, 9);
    place(level, pos, GeoScannerRegistry.GEO_SCANNER);
    BlockEntityGeoScanner scanner = be(level, pos, BlockEntityGeoScanner.class);
    scanner.setCode(SCAN_CODE);
    ChunkPos center = scanner.centerChunk();
    CompoundTag counts = new CompoundTag();
    counts.putInt("minecraft:iron_ore", SCAN_IRON);
    counts.putInt(LegacyFixture.SULFUR_ORE_ID, SCAN_SULFUR);
    counts.putInt(LegacyFixture.DEEPSLATE_SULFUR_ORE_ID, SCAN_DEEPSLATE_SULFUR);
    CompoundTag scan = new CompoundTag();
    scan.putLong("t", level.getGameTime());
    scan.put("entries", counts);
    ScanChannels.get(level).channel(SCAN_CODE).put(level, center.x(), center.z(), scan);
    JsonObject entry = entry("geo_scanner", pos, "geo_scanner");
    entry.addProperty("code", SCAN_CODE);
    entry.addProperty("chunk_x", center.x());
    entry.addProperty("chunk_z", center.z());
    entry.addProperty("iron_ore", SCAN_IRON);
    entry.addProperty("sulfur_ore", SCAN_SULFUR);
    entry.addProperty("deepslate_sulfur_ore", SCAN_DEEPSLATE_SULFUR);
  }

  private void buildLogistics(ServerLevel level) {
    BlockPos chassisPos = at(base, 3, 9);
    place(level, chassisPos, LogisticsRegistry.CHASSIS_1);
    BlockEntityChassis chassis = be(level, chassisPos, BlockEntityChassis.class);
    ItemStack sink = new ItemStack(LogisticsRegistry.MODULE_SINK);
    ModuleSettings.setLine(sink, 0, new ModuleSettings.FilterLine(ModuleSettings.LineMode.ITEM,
        new ItemStack(ModItems.SULFUR_DUST), "", false, false, 0));
    Map<String, Boolean> tree = new LinkedHashMap<>();
    tree.put("i:faktocraft:sulfur_dust", Boolean.TRUE);
    tree.put("m:faktocraft", Boolean.FALSE);
    ModuleSettings.putTreeOverrides(sink, tree);
    ModuleSettings.putTreeCounts(sink, Map.of("i:faktocraft:sulfur_dust", TREE_COUNT));
    chassis.getModules().setStackInSlot(0, sink);
    JsonObject chassisEntry = entry("chassis", chassisPos, "chassis");
    chassisEntry.addProperty("module", String.valueOf(BuiltInRegistries.ITEM.getKey(LogisticsRegistry.MODULE_SINK)));
    chassisEntry.addProperty("filter_item", "faktocraft:sulfur_dust");
    chassisEntry.addProperty("tree_node", "i:faktocraft:sulfur_dust");
    chassisEntry.addProperty("tree_count", TREE_COUNT);

    BlockPos pipePos = at(base, 6, 9);
    place(level, pipePos, LogisticsRegistry.RECIPE_PIPE);
    BlockEntityRecipePipe pipe = be(level, pipePos, BlockEntityRecipePipe.class);
    int recipe = pipe.addRecipe();
    pipe.setIo(recipe, 0, new ItemStack(ModItems.SULFUR_DUST));
    pipe.setIo(recipe, 1, new ItemStack(Items.IRON_INGOT));
    pipe.bind(recipe, 0, 0, RECIPE_BIND_BLOCK, 3);
    BlockEntityRecipePipe.MachineRecipe machineRecipe = pipe.recipe(recipe);
    machineRecipe.inputs[0].tag = RECIPE_TAG_SULFUR;
    machineRecipe.inputs[1].tag = RECIPE_TAG_IRON;
    pipe.changed();
    JsonObject pipeEntry = entry("recipe_pipe", pipePos, "recipe_pipe");
    pipeEntry.addProperty("tag_0", RECIPE_TAG_SULFUR);
    pipeEntry.addProperty("tag_0_after_port", "c:dusts/sulfur");
    pipeEntry.addProperty("tag_1", RECIPE_TAG_IRON);
    pipeEntry.addProperty("tag_1_after_port", "c:ingots/iron");
    pipeEntry.addProperty("bind_block", RECIPE_BIND_BLOCK);
    pipeEntry.addProperty("bind_slot", 0);
  }

  private void buildCovers(ServerLevel level) {
    BlockPos cablePos = at(base, 9, 9);
    level.setBlock(cablePos, legacyBlock(LegacyFixture.SULFUR_ORE_ID).defaultBlockState(), Block.UPDATE_ALL);
    if (!DrillOps.drill(level, cablePos, Direction.NORTH) || !DrillOps.drill(level, cablePos, Direction.SOUTH)) {
      throw TestUtil.assertion("sulfur ore could not be drilled at " + cablePos.toShortString());
    }
    BlockState cover = be(level, cablePos, ICoverHost.class).getCover();
    if (!CoverSupport.placeInto(level, cablePos, cover, ModBlocks.COPPER_CABLE, new ItemStack(ModBlocks.COPPER_CABLE),
        null, Direction.UP)) {
      throw TestUtil.assertion("cable could not be inserted into the drilled ore");
    }
    JsonObject cableEntry = entry("covered_cable", cablePos, "cover");
    cableEntry.addProperty("block", id(ModBlocks.COPPER_CABLE));
    cableEntry.addProperty("cover", LegacyFixture.SULFUR_ORE_ID);
    cableEntry.addProperty("cover_after_port", "minecraft:stone");
    cableEntry.addProperty("holes", CoverSupport.bit(Direction.NORTH) | CoverSupport.bit(Direction.SOUTH));

    BlockPos drilledPos = at(base, 11, 9);
    level.setBlock(drilledPos, legacyBlock(LegacyFixture.SULFUR_ORE_ID).defaultBlockState(), Block.UPDATE_ALL);
    if (!DrillOps.drill(level, drilledPos, Direction.EAST) || !DrillOps.drill(level, drilledPos, Direction.WEST)) {
      throw TestUtil.assertion("second sulfur ore could not be drilled");
    }
    JsonObject drilledEntry = entry("drilled_ore", drilledPos, "cover");
    drilledEntry.addProperty("block", id(ModBlocks.DRILLED_BLOCK));
    drilledEntry.addProperty("cover", LegacyFixture.SULFUR_ORE_ID);
    drilledEntry.addProperty("cover_after_port", "minecraft:stone");
    drilledEntry.addProperty("holes", CoverSupport.bit(Direction.EAST) | CoverSupport.bit(Direction.WEST));
  }

  private void buildOres(ServerLevel level) {
    ore(level, "ore_sulfur", at(base, 0, 12), legacyBlock(LegacyFixture.SULFUR_ORE_ID), "minecraft:stone");
    ore(level, "ore_deepslate_sulfur", at(base, 2, 12), legacyBlock(LegacyFixture.DEEPSLATE_SULFUR_ORE_ID),
        "minecraft:deepslate");
    ore(level, "ore_iron_control", at(base, 4, 12), Blocks.IRON_ORE, "minecraft:iron_ore");
  }

  private void ore(ServerLevel level, String key, BlockPos pos, Block block, String afterPort) {
    level.setBlock(pos, block.defaultBlockState(), Block.UPDATE_ALL);
    JsonObject entry = entry(key, pos, "block");
    entry.addProperty("block", id(block));
    entry.addProperty("block_after_port", afterPort);
  }

  private void buildAnchor(ServerLevel level) {
    BlockPos pos = at(base, 6, 12);
    BlockPos destination = pos.east(20);
    place(level, pos, ModBlocks.TELEPORT_ANCHOR);
    BlockEntityTeleportAnchor anchor = be(level, pos, BlockEntityTeleportAnchor.class);
    anchor.setDestination(destination);
    anchor.getEnergyStorage().setEnergy(ANCHOR_ENERGY);
    JsonObject entry = entry("teleport_anchor", pos, "teleport_anchor");
    entry.add("destination", LegacyFixture.pos(destination));
  }

  private void buildMonitor(ServerLevel level) {
    BlockPos master = at(base, 6, 2);
    BlockPos target = at(base, 3, 0);
    ((BlockStatusMonitor) MonitorRegistry.STATUS_MONITOR).placePanel(level, master, Direction.NORTH);
    BlockEntityStatusMonitor monitor = be(level, master, BlockEntityStatusMonitor.class);
    monitor.setTarget(level.dimension(), target);
    JsonObject entry = entry("monitor", master, "monitor");
    entry.add("target", LegacyFixture.pos(target));
  }

  private void buildRadiation(ServerLevel level) {
    BlockPos pos = at(base, 0, 15);
    RadiationSources.get(level).addAftermath(level, pos, AFTERMATH_STRENGTH, 10_000_000L);
    JsonObject entry = entry("radiation_aftermath", pos, "saved_data");
    entry.addProperty("strength", AFTERMATH_STRENGTH);
  }

  private void buildChunkLoader(ServerLevel level) {
    BlockPos pos = base.offset(LegacyFixture.LOADER_DX, 0, LegacyFixture.LOADER_DZ);
    place(level, pos, ChunkLoaderRegistry.CHUNK_LOADER);
    BlockEntityChunkLoader loader = be(level, pos, BlockEntityChunkLoader.class);
    loader.getEnergyStorage().setEnergy(loader.getEnergyStorage().maxEnergy());
    loader.setChunkCount(BlockEntityChunkLoader.MIN_CHUNKS);
    loader.setEnabledByPlayer(true);
    JsonObject entry = entry("chunk_loader", pos, "chunk_loader");
    entry.addProperty("chunks", BlockEntityChunkLoader.MIN_CHUNKS);
  }

  private void record(ServerLevel level) {
    for (Map.Entry<String, JsonObject> item : entries.entrySet()) {
      JsonObject entry = item.getValue();
      BlockPos pos = LegacyFixture.pos(entry);
      BlockEntity blockEntity = level.getBlockEntity(pos);
      if (blockEntity == null) {
        continue;
      }
      LegacyFixture.writeNbt(item.getKey(), blockEntity.saveWithFullMetadata(NbtBridge.registries()));
      if (blockEntity instanceof FaktocraftBlockEntity be && be.getEnergyStorage() != null) {
        entry.addProperty("energy", be.getEnergyStorage().energyStored());
        entry.addProperty("max_energy", be.getEnergyStorage().maxEnergy());
      }
    }
    BlockPos cable = LegacyFixture.pos(entries.get("cable"));
    entries.get("cable").addProperty("has_network",
        EnergyCore.get(level).getNetworks().getNetwork(cable) != null);
    BlockPos loaderPos = LegacyFixture.pos(entries.get("chunk_loader"));
    entries.get("chunk_loader").addProperty("manager_active",
        ChunkLoaderManager.get(level).isActive(GlobalPos.of(level.dimension(), loaderPos)));

    LegacyFixture.writeNbt("saved_scan_channels", ScanChannels.get(level).save(new CompoundTag()));
    LegacyFixture.writeNbt("saved_ender_tanks", EnderTankChannels.get(level).save(new CompoundTag()));
    LegacyFixture.writeNbt("saved_chunk_loaders", ChunkLoaderManager.get(level).save(new CompoundTag()));
    LegacyFixture.writeNbt("saved_radiation_sources", RadiationSources.get(level).save(new CompoundTag()));
    LegacyFixture.writeNbt("saved_energy_core", EnergyCore.get(level).save(new CompoundTag()));

    JsonObject manifest = new JsonObject();
    manifest.addProperty("source", "forge-1.20.1");
    manifest.addProperty("data_version", net.minecraft.SharedConstants.getCurrentVersion().dataVersion()
        .version());
    manifest.addProperty("dimension", level.dimension().identifier().toString());
    manifest.add("base", LegacyFixture.pos(base));
    JsonArray chunks = new JsonArray();
    for (ChunkPos chunk : forced) {
      JsonArray pair = new JsonArray();
      pair.add(chunk.x());
      pair.add(chunk.z());
      chunks.add(pair);
    }
    manifest.add("chunks", chunks);
    JsonObject all = new JsonObject();
    entries.forEach(all::add);
    manifest.add("entries", all);
    LegacyFixture.writeManifest(manifest);
  }
}

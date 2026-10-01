package com.faktocraft.gametest.legacy;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.chunk_loader.BlockEntityChunkLoader;
import com.faktocraft.common.block.impl.chunk_loader.ChunkLoaderManager;
import com.faktocraft.common.block.impl.logistics.BlockEntityChassis;
import com.faktocraft.common.block.impl.logistics.BlockEntityRecipePipe;
import com.faktocraft.common.block.impl.logistics.BlockEntityRequestTable;
import com.faktocraft.common.block.impl.logistics.ModuleSettings;
import com.faktocraft.common.block.impl.machines.geo_scanner.BlockEntityGeoScanner;
import com.faktocraft.common.block.impl.monitor.BlockEntityStatusMonitor;
import com.faktocraft.common.block.impl.monitor.MonitorCardItem;
import com.faktocraft.common.block.impl.pipe.BlockEntityEnderTank;
import com.faktocraft.common.block.impl.pipe.BlockEntityFluidPipe;
import com.faktocraft.common.block.impl.pipe.BlockEntityTank;
import com.faktocraft.common.block.impl.pipe.EnderTankChannels;
import com.faktocraft.common.block.impl.teleport_anchor.BlockEntityTeleportAnchor;
import com.faktocraft.common.cover.CoverSupport;
import com.faktocraft.common.cover.ICoverHost;
import com.faktocraft.common.energy.provider.EnergyCore;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.item.base.FluidItem;
import com.faktocraft.common.item.impl.tools.ToolboxMenu;
import com.faktocraft.common.radiation.RadiationSources;
import com.faktocraft.common.registries.ModComponents;
import com.faktocraft.common.scan.ScanChannels;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@GameTestHolder(Faktocraft.MODID)
@PrefixGameTestTemplate(false)
public class LegacySaveGameTest {
  private static final String TEMPLATE = "gametest_platform";
  private static final double ENERGY_TOLERANCE = 0.05;

  private final List<String> failures = new ArrayList<>();

  private static boolean skip(GameTestHelper helper) {
    if (!LegacyFixture.MODE_VERIFY.equals(LegacyFixture.mode())) {
      helper.succeed();
      return true;
    }
    return false;
  }

  private void check(boolean ok, String message) {
    if (!ok) {
      failures.add(message);
    }
  }

  private void finish(GameTestHelper helper, String what) {
    if (failures.isEmpty()) {
      Faktocraft.LOGGER.info("[LegacySave] {} passed", what);
      helper.succeed();
      return;
    }
    StringBuilder sb = new StringBuilder(what).append(" found ").append(failures.size()).append(" problem(s):");
    for (String failure : failures) {
      sb.append("\n  - ").append(failure);
    }
    helper.fail(sb.toString());
  }

  private static String expected(JsonObject entry, String key) {
    String ported = key + "_after_port";
    return LegacyFixture.afterPort() && entry.has(ported) ? entry.get(ported).getAsString()
        : entry.get(key).getAsString();
  }

  private static String id(BlockState state) {
    return String.valueOf(ForgeRegistries.BLOCKS.getKey(state.getBlock()));
  }

  private static String id(ItemStack stack) {
    return String.valueOf(ForgeRegistries.ITEMS.getKey(stack.getItem()));
  }

  private static String id(Fluid fluid) {
    return String.valueOf(ForgeRegistries.FLUIDS.getKey(fluid));
  }

  private static boolean near(int actual, int expected) {
    return Math.abs(actual - expected) <= Math.max(1, expected * ENERGY_TOLERANCE);
  }

  private <T> T be(ServerLevel level, JsonObject entry, Class<T> type, String key) {
    BlockPos pos = LegacyFixture.pos(entry);
    BlockEntity blockEntity = level.getBlockEntity(pos);
    if (!type.isInstance(blockEntity)) {
      failures.add(key + ": no " + type.getSimpleName() + " at " + pos.toShortString() + ", found " + blockEntity
          + " in " + id(level.getBlockState(pos)));
      return null;
    }
    return type.cast(blockEntity);
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 1200)
  public void legacySaveWorldParity(GameTestHelper helper) {
    if (skip(helper)) {
      return;
    }
    JsonObject manifest = LegacyFixture.readManifest();
    JsonObject entries = manifest.getAsJsonObject("entries");
    ServerLevel level = helper.getLevel();
    Faktocraft.LOGGER.info("[LegacySave] verifying {} entries from {} (after port: {})", entries.size(),
        manifest.get("source").getAsString(), LegacyFixture.afterPort());

    JsonObject loaderEntry = entries.getAsJsonObject("chunk_loader");
    BlockPos loaderPos = LegacyFixture.pos(loaderEntry);
    ChunkPos loaderChunk = new ChunkPos(loaderPos);
    check(level.getChunkSource().hasChunk(loaderChunk.x, loaderChunk.z),
        "chunk_loader: chunk " + loaderChunk + " was not loaded by the migrated ticket at startup");
    check(ownChunkForced(level, loaderPos), "chunk_loader: no block ticket recorded for " + loaderChunk);
    check(ChunkLoaderManager.get(level).isActive(GlobalPos.of(level.dimension(), loaderPos)),
        "chunk_loader: manager lost the active slot");

    List<ChunkPos> forced = new ArrayList<>();
    for (var pair : manifest.getAsJsonArray("chunks")) {
      JsonArray xz = pair.getAsJsonArray();
      ChunkPos chunk = new ChunkPos(xz.get(0).getAsInt(), xz.get(1).getAsInt());
      level.setChunkForced(chunk.x, chunk.z, true);
      forced.add(chunk);
    }

    checkMachine(level, entries.getAsJsonObject("furnace_idle"), "furnace_idle");
    checkEnergy(level, entries.getAsJsonObject("furnace_energy"), "furnace_energy");
    checkEnergy(level, entries.getAsJsonObject("mfe"), "mfe");
    checkNetwork(level, entries.getAsJsonObject("cable"));
    checkChest(level, entries.getAsJsonObject("chest"));
    checkRequestTable(level, entries.getAsJsonObject("request_table"));
    checkTank(level, entries.getAsJsonObject("tank"));
    checkFluidPipe(level, entries.getAsJsonObject("fluid_pipe"));
    checkEnderTank(level, entries.getAsJsonObject("ender_tank"));
    checkScanner(level, entries.getAsJsonObject("geo_scanner"));
    checkChassis(level, entries.getAsJsonObject("chassis"));
    checkRecipePipe(level, entries.getAsJsonObject("recipe_pipe"));
    checkCover(level, entries.getAsJsonObject("covered_cable"), "covered_cable");
    checkCover(level, entries.getAsJsonObject("drilled_ore"), "drilled_ore");
    checkBlock(level, entries.getAsJsonObject("ore_sulfur"), "ore_sulfur");
    checkBlock(level, entries.getAsJsonObject("ore_deepslate_sulfur"), "ore_deepslate_sulfur");
    checkBlock(level, entries.getAsJsonObject("ore_iron_control"), "ore_iron_control");
    checkAnchor(level, entries.getAsJsonObject("teleport_anchor"));
    checkRadiation(level, entries.getAsJsonObject("radiation_aftermath"));

    JsonObject monitorEntry = entries.getAsJsonObject("monitor");
    helper.startSequence()
        .thenWaitUntil(() -> {
          BlockEntity loader = level.getBlockEntity(loaderPos);
          if (!(loader instanceof BlockEntityChunkLoader chunkLoader)) {
            throw new GameTestAssertException("chunk_loader: no block entity at " + loaderPos.toShortString());
          }
          if (chunkLoader.getStatus() != BlockEntityChunkLoader.STATUS_ACTIVE) {
            throw new GameTestAssertException("chunk_loader: status " + chunkLoader.getStatus()
                + ", waiting for ACTIVE");
          }
          BlockEntity monitor = level.getBlockEntity(LegacyFixture.pos(monitorEntry));
          if (!(monitor instanceof BlockEntityStatusMonitor statusMonitor)) {
            throw new GameTestAssertException("monitor: no block entity");
          }
          if (statusMonitor.status() != BlockEntityStatusMonitor.STATUS_OK) {
            throw new GameTestAssertException("monitor: status " + statusMonitor.status() + ", waiting for OK");
          }
        })
        .thenExecute(() -> {
          BlockEntityStatusMonitor monitor = be(level, monitorEntry, BlockEntityStatusMonitor.class, "monitor");
          if (monitor != null) {
            BlockPos target = LegacyFixture.pos(monitorEntry, "target");
            check(target.equals(monitor.target()), "monitor: target " + monitor.target() + ", expected "
                + target.toShortString());
          }
          for (ChunkPos chunk : forced) {
            level.setChunkForced(chunk.x, chunk.z, false);
          }
          finish(helper, "world parity");
        })
        .thenSucceed();
  }

  private static boolean ownChunkForced(ServerLevel level, BlockPos pos) {
    ForcedChunksSavedData saved = level.getDataStorage()
        .computeIfAbsent(ForcedChunksSavedData::load, ForcedChunksSavedData::new, ForcedChunksSavedData.FILE_ID);
    long chunk = new ChunkPos(pos).toLong();
    return saved.getBlockForcedChunks().getTickingChunks().values().stream().anyMatch(set -> set.contains(chunk))
        || saved.getBlockForcedChunks().getChunks().values().stream().anyMatch(set -> set.contains(chunk));
  }

  private void checkMachine(ServerLevel level, JsonObject entry, String key) {
    FaktocraftBlockEntity machine = be(level, entry, FaktocraftBlockEntity.class, key);
    if (machine == null) {
      return;
    }
    checkMachineContents(machine, entry, key);
  }

  private void checkMachineContents(FaktocraftBlockEntity machine, JsonObject entry, String key) {
    ItemStack input = machine.getItemStackHandler().getStackInSlot(0);
    check(id(input).equals(entry.get("input_item").getAsString())
        && input.getCount() == entry.get("input_count").getAsInt(),
        key + ": input slot is " + input + ", expected " + entry.get("input_count") + " "
            + entry.get("input_item").getAsString());
    ItemStack output = machine.getItemStackHandler().getStackInSlot(1);
    check(id(output).equals(entry.get("output_item").getAsString()), key + ": output slot is " + output);
    check(output.getDamageValue() == entry.get("output_damage").getAsInt(),
        key + ": output damage " + output.getDamageValue() + ", expected " + entry.get("output_damage"));
    check(output.getEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY) == entry.get("output_efficiency").getAsInt(),
        key + ": output lost its efficiency enchantment");
    check(output.getHoverName().getString().equals(entry.get("output_name").getAsString()),
        key + ": output name is '" + output.getHoverName().getString() + "'");
    check(machine.getEnergyStorage().energyStored() == entry.get("energy").getAsInt(),
        key + ": energy " + machine.getEnergyStorage().energyStored() + ", expected " + entry.get("energy"));
  }

  private void checkEnergy(ServerLevel level, JsonObject entry, String key) {
    FaktocraftBlockEntity machine = be(level, entry, FaktocraftBlockEntity.class, key);
    if (machine == null) {
      return;
    }
    checkEnergy(machine, entry, key);
  }

  private void checkEnergy(FaktocraftBlockEntity machine, JsonObject entry, String key) {
    int expected = entry.get("energy").getAsInt();
    int actual = machine.getEnergyStorage().energyStored();
    check(near(actual, expected), key + ": energy " + actual + ", expected about " + expected);
    check(machine.getEnergyStorage().maxEnergy() == entry.get("max_energy").getAsInt(),
        key + ": max energy " + machine.getEnergyStorage().maxEnergy() + ", expected " + entry.get("max_energy"));
  }

  private void checkNetwork(ServerLevel level, JsonObject entry) {
    BlockPos pos = LegacyFixture.pos(entry);
    boolean has = EnergyCore.get(level).getNetworks().getNetwork(pos) != null;
    check(has == entry.get("has_network").getAsBoolean(), "cable: network present=" + has + ", expected "
        + entry.get("has_network"));
  }

  private void checkChest(ServerLevel level, JsonObject entry) {
    ChestBlockEntity chest = be(level, entry, ChestBlockEntity.class, "chest");
    if (chest == null) {
      return;
    }
    checkItems(containerItems(chest), entry, "chest");
  }

  private void checkRequestTable(ServerLevel level, JsonObject entry) {
    BlockEntityRequestTable table = be(level, entry, BlockEntityRequestTable.class, "request_table");
    if (table == null) {
      return;
    }
    checkItems(handlerItems(table.getItemStackHandler()), entry, "request_table");
  }

  private static List<ItemStack> containerItems(Container container) {
    List<ItemStack> items = new ArrayList<>();
    for (int i = 0; i < container.getContainerSize(); i++) {
      items.add(container.getItem(i));
    }
    return items;
  }

  private static List<ItemStack> handlerItems(com.faktocraft.common.util.ItemStackHandler handler) {
    List<ItemStack> items = new ArrayList<>();
    for (int i = 0; i < handler.getSlots(); i++) {
      items.add(handler.getStackInSlot(i));
    }
    return items;
  }

  private static ItemStack find(List<ItemStack> items, String id) {
    for (ItemStack stack : items) {
      if (!stack.isEmpty() && id(stack).equals(id)) {
        return stack;
      }
    }
    return ItemStack.EMPTY;
  }

  private void checkItems(List<ItemStack> items, JsonObject entry, String key) {
    ItemStack battery = find(items, "faktocraft:medium_battery");
    check(!battery.isEmpty(), key + ": medium battery missing");
    check(ModComponents.getEnergy(battery, -1) == entry.get("battery_energy").getAsInt(),
        key + ": battery energy " + ModComponents.getEnergy(battery, -1) + ", expected " + entry.get("battery_energy"));

    ItemStack cell = find(items, "faktocraft:fluid_cell");
    check(!cell.isEmpty(), key + ": fluid cell missing");
    check(id(FluidItem.getFluid(cell)).equals(entry.get("cell_fluid").getAsString())
        && FluidItem.getFluidAmount(cell) == entry.get("cell_mb").getAsInt(),
        key + ": fluid cell holds " + FluidItem.getFluidAmount(cell) + " mB of " + id(FluidItem.getFluid(cell)));

    ItemStack coolant = find(items, "faktocraft:medium_coolant_cell");
    check(!coolant.isEmpty(), key + ": coolant cell missing");
    check(id(FluidItem.getFluid(coolant)).equals(entry.get("coolant_fluid").getAsString())
        && FluidItem.getFluidAmount(coolant) == entry.get("coolant_mb").getAsInt(),
        key + ": coolant cell holds " + FluidItem.getFluidAmount(coolant) + " mB of "
            + id(FluidItem.getFluid(coolant)));

    ItemStack hazmat = find(items, "faktocraft:hazmat_chestplate");
    check(!hazmat.isEmpty(), key + ": hazmat chestplate missing");
    int color = hazmat.getItem() instanceof DyeableLeatherItem dyeable ? dyeable.getColor(hazmat) : -1;
    check(color == entry.get("hazmat_color").getAsInt(), key + ": hazmat color " + Integer.toHexString(color));

    ItemStack toolbox = find(items, "faktocraft:toolbox");
    check(!toolbox.isEmpty(), key + ": toolbox missing");
    check(toolboxContains(toolbox, entry.get("toolbox_item").getAsString()), key + ": toolbox lost its wrench");

    ItemStack dust = find(items, "faktocraft:sulfur_dust");
    check(dust.getCount() == entry.get("sulfur_dust_count").getAsInt(), key + ": sulfur dust is " + dust);

    ItemStack ore = find(items, expected(entry, "sulfur_ore_item"));
    check(ore.getCount() == entry.get("sulfur_ore_count").getAsInt(), key + ": expected "
        + entry.get("sulfur_ore_count") + " " + expected(entry, "sulfur_ore_item") + ", found " + ore);
    ItemStack deepslate = find(items, expected(entry, "deepslate_sulfur_ore_item"));
    check(deepslate.getCount() == entry.get("deepslate_sulfur_ore_count").getAsInt(), key + ": expected "
        + entry.get("deepslate_sulfur_ore_count") + " " + expected(entry, "deepslate_sulfur_ore_item") + ", found "
        + deepslate);

    ItemStack card = find(items, "faktocraft:monitor_card");
    check(!card.isEmpty(), key + ": monitor card missing");
    BlockPos target = LegacyFixture.pos(entry, "monitor_card_target");
    check(target.equals(MonitorCardItem.targetPos(card)), key + ": monitor card target "
        + MonitorCardItem.targetPos(card) + ", expected " + target.toShortString());
  }

  private static boolean toolboxContains(ItemStack toolbox, String id) {
    if (toolbox.isEmpty() || !toolbox.hasTag() || !toolbox.getTag().contains("Items")) {
      return false;
    }
    net.minecraftforge.items.ItemStackHandler handler = new net.minecraftforge.items.ItemStackHandler(
        ToolboxMenu.SIZE);
    handler.deserializeNBT(toolbox.getTag().getCompound("Items"));
    for (int i = 0; i < handler.getSlots(); i++) {
      ItemStack stack = handler.getStackInSlot(i);
      if (!stack.isEmpty() && id(stack).equals(id)) {
        return true;
      }
    }
    return false;
  }

  private void checkTank(ServerLevel level, JsonObject entry) {
    BlockEntityTank tank = be(level, entry, BlockEntityTank.class, "tank");
    if (tank == null) {
      return;
    }
    checkFluid(tank.tank.getFluidStack(), entry, "tank");
  }

  private void checkFluidPipe(ServerLevel level, JsonObject entry) {
    BlockEntityFluidPipe pipe = be(level, entry, BlockEntityFluidPipe.class, "fluid_pipe");
    if (pipe == null) {
      return;
    }
    checkFluid(pipe.tank.getFluidStack(), entry, "fluid_pipe");
  }

  private void checkFluid(FluidStack stack, JsonObject entry, String key) {
    check(!stack.isEmpty() && id(stack.getFluid()).equals(entry.get("fluid").getAsString())
        && stack.getAmount() == entry.get("mb").getAsInt(),
        key + ": holds " + stack.getAmount() + " mB of " + (stack.isEmpty() ? "nothing" : id(stack.getFluid()))
            + ", expected " + entry.get("mb") + " mB of " + entry.get("fluid").getAsString());
  }

  private void checkEnderTank(ServerLevel level, JsonObject entry) {
    BlockEntityEnderTank tank = be(level, entry, BlockEntityEnderTank.class, "ender_tank");
    if (tank == null) {
      return;
    }
    int code = entry.get("code").getAsInt();
    String codeText = String.format(java.util.Locale.ROOT, "%0" + EnderTankChannels.CODE_DIGITS + "d", code);
    check(codeText.equals(tank.codeText()), "ender_tank: code '" + tank.codeText() + "', expected " + codeText);
    checkFluid(EnderTankChannels.get(level).channel(code).getFluidStack(), entry, "ender_tank channel " + code);
  }

  private void checkScanner(ServerLevel level, JsonObject entry) {
    BlockEntityGeoScanner scanner = be(level, entry, BlockEntityGeoScanner.class, "geo_scanner");
    if (scanner == null) {
      return;
    }
    int code = entry.get("code").getAsInt();
    int cx = entry.get("chunk_x").getAsInt();
    int cz = entry.get("chunk_z").getAsInt();
    check(scanner.getCode() == code, "geo_scanner: code " + scanner.getCode() + ", expected " + code);
    check(scanner.hasScan(cx, cz), "geo_scanner: scan for chunk " + cx + "," + cz + " is gone");
    CompoundTag scan = ScanChannels.get(level).channel(code).get(level, cx, cz);
    CompoundTag counts = scan != null ? scan.getCompound("entries") : new CompoundTag();
    check(counts.getInt("minecraft:iron_ore") == entry.get("iron_ore").getAsInt(),
        "geo_scanner: iron ore count " + counts.getInt("minecraft:iron_ore"));
    if (LegacyFixture.afterPort()) {
      check(!counts.contains(LegacyFixture.SULFUR_ORE_ID) && !counts.contains(LegacyFixture.DEEPSLATE_SULFUR_ORE_ID),
          "geo_scanner: removed ore ids still present in the scan: " + counts.getAllKeys());
    } else {
      check(counts.getInt(LegacyFixture.SULFUR_ORE_ID) == entry.get("sulfur_ore").getAsInt(),
          "geo_scanner: sulfur ore count " + counts.getInt(LegacyFixture.SULFUR_ORE_ID));
    }
  }

  private void checkChassis(ServerLevel level, JsonObject entry) {
    BlockEntityChassis chassis = be(level, entry, BlockEntityChassis.class, "chassis");
    if (chassis == null) {
      return;
    }
    checkChassisModule(chassis.getModules().getStackInSlot(0), entry, "chassis");
  }

  private void checkChassisModule(ItemStack module, JsonObject entry, String key) {
    check(!module.isEmpty(), key + ": module slot 0 is empty");
    if (module.isEmpty()) {
      return;
    }
    List<ModuleSettings.FilterLine> lines = ModuleSettings.lines(module);
    check(!lines.isEmpty() && id(lines.get(0).item()).equals(entry.get("filter_item").getAsString()),
        key + ": filter line 0 is " + (lines.isEmpty() ? "missing" : lines.get(0).item().toString()));
    Map<String, Boolean> tree = ModuleSettings.treeOverrides(module);
    String node = entry.get("tree_node").getAsString();
    check(Boolean.TRUE.equals(tree.get(node)), key + ": tree override " + node + " is " + tree.get(node));
    Map<String, Integer> counts = ModuleSettings.treeCounts(module);
    check(counts.getOrDefault(node, -1) == entry.get("tree_count").getAsInt(),
        key + ": tree count " + counts.get(node) + ", expected " + entry.get("tree_count"));
  }

  private void checkRecipePipe(ServerLevel level, JsonObject entry) {
    BlockEntityRecipePipe pipe = be(level, entry, BlockEntityRecipePipe.class, "recipe_pipe");
    if (pipe == null) {
      return;
    }
    checkRecipePipe(pipe, entry, "recipe_pipe");
  }

  private void checkRecipePipe(BlockEntityRecipePipe pipe, JsonObject entry, String key) {
    check(pipe.recipeCount() == 1, key + ": " + pipe.recipeCount() + " recipes, expected 1");
    BlockEntityRecipePipe.MachineRecipe recipe = pipe.recipe(0);
    if (recipe == null) {
      return;
    }
    check(expected(entry, "tag_0").equals(recipe.inputs[0].tag), key + ": input 0 tag '" + recipe.inputs[0].tag
        + "', expected '" + expected(entry, "tag_0") + "'");
    check(expected(entry, "tag_1").equals(recipe.inputs[1].tag), key + ": input 1 tag '" + recipe.inputs[1].tag
        + "', expected '" + expected(entry, "tag_1") + "'");
    check(entry.get("bind_block").getAsString().equals(recipe.inputs[0].bindBlock)
        && recipe.inputs[0].bindSlot == entry.get("bind_slot").getAsInt(),
        key + ": input 0 bound to " + recipe.inputs[0].bindBlock + " slot " + recipe.inputs[0].bindSlot);
  }

  private void checkCover(ServerLevel level, JsonObject entry, String key) {
    BlockPos pos = LegacyFixture.pos(entry);
    BlockState state = level.getBlockState(pos);
    check(id(state).equals(entry.get("block").getAsString()), key + ": block is " + id(state) + ", expected "
        + entry.get("block").getAsString());
    check(CoverSupport.isCovered(state) || id(state).equals("faktocraft:drilled_block"), key + ": not covered");
    ICoverHost host = be(level, entry, ICoverHost.class, key);
    if (host == null) {
      return;
    }
    BlockState cover = host.getCover();
    String coverId = cover != null ? id(cover) : "null";
    check(coverId.equals(expected(entry, "cover")), key + ": cover is " + coverId + ", expected "
        + expected(entry, "cover"));
    check(host.getCoverHoles() == entry.get("holes").getAsInt(), key + ": holes " + host.getCoverHoles()
        + ", expected " + entry.get("holes"));
  }

  private void checkBlock(ServerLevel level, JsonObject entry, String key) {
    BlockPos pos = LegacyFixture.pos(entry);
    String actual = id(level.getBlockState(pos));
    check(actual.equals(expected(entry, "block")), key + ": block at " + pos.toShortString() + " is " + actual
        + ", expected " + expected(entry, "block"));
  }

  private void checkAnchor(ServerLevel level, JsonObject entry) {
    BlockEntityTeleportAnchor anchor = be(level, entry, BlockEntityTeleportAnchor.class, "teleport_anchor");
    if (anchor == null) {
      return;
    }
    BlockPos destination = LegacyFixture.pos(entry, "destination");
    check(destination.equals(anchor.getDestination()), "teleport_anchor: destination " + anchor.getDestination()
        + ", expected " + destination.toShortString());
    checkEnergy(anchor, entry, "teleport_anchor");
  }

  private void checkRadiation(ServerLevel level, JsonObject entry) {
    long pos = LegacyFixture.pos(entry).asLong();
    float strength = entry.get("strength").getAsFloat();
    boolean found = RadiationSources.get(level).aftermath(level).stream()
        .anyMatch(aftermath -> aftermath.pos() == pos && aftermath.strength() == strength);
    check(found, "radiation_aftermath: no aftermath of strength " + strength + " at "
        + LegacyFixture.pos(entry).toShortString());
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 100)
  public void legacySaveNbtParity(GameTestHelper helper) {
    if (skip(helper)) {
      return;
    }
    JsonObject entries = LegacyFixture.readManifest().getAsJsonObject("entries");
    ServerLevel level = helper.getLevel();

    loadInto(level, entries, "furnace_idle", FaktocraftBlockEntity.class,
        (be, entry) -> checkMachineContents(be, entry, "nbt furnace_idle"));
    loadInto(level, entries, "furnace_energy", FaktocraftBlockEntity.class,
        (be, entry) -> checkEnergy(be, entry, "nbt furnace_energy"));
    loadInto(level, entries, "mfe", FaktocraftBlockEntity.class, (be, entry) -> checkEnergy(be, entry, "nbt mfe"));
    loadInto(level, entries, "request_table", BlockEntityRequestTable.class,
        (be, entry) -> checkItems(handlerItems(be.getItemStackHandler()), entry, "nbt request_table"));
    loadInto(level, entries, "tank", BlockEntityTank.class,
        (be, entry) -> checkFluid(be.tank.getFluidStack(), entry, "nbt tank"));
    loadInto(level, entries, "fluid_pipe", BlockEntityFluidPipe.class,
        (be, entry) -> checkFluid(be.tank.getFluidStack(), entry, "nbt fluid_pipe"));
    loadInto(level, entries, "ender_tank", BlockEntityEnderTank.class,
        (be, entry) -> check(be.hasCode() && be.codeText().endsWith(String.valueOf(entry.get("code").getAsInt())),
            "nbt ender_tank: code '" + be.codeText() + "'"));
    loadInto(level, entries, "geo_scanner", BlockEntityGeoScanner.class,
        (be, entry) -> check(be.getCode() == entry.get("code").getAsInt(), "nbt geo_scanner: code " + be.getCode()));
    loadInto(level, entries, "chassis", BlockEntityChassis.class,
        (be, entry) -> checkChassisModule(be.getModules().getStackInSlot(0), entry, "nbt chassis"));
    loadInto(level, entries, "recipe_pipe", BlockEntityRecipePipe.class,
        (be, entry) -> checkRecipePipe(be, entry, "nbt recipe_pipe"));
    loadInto(level, entries, "covered_cable", BlockEntity.class, (be, entry) -> {
      String cover = be instanceof ICoverHost host && host.getCover() != null ? id(host.getCover()) : "null";
      check(cover.equals(expected(entry, "cover")), "nbt covered_cable: cover " + cover + ", expected "
          + expected(entry, "cover"));
    });
    loadInto(level, entries, "teleport_anchor", BlockEntityTeleportAnchor.class, (be, entry) -> {
      check(LegacyFixture.pos(entry, "destination").equals(be.getDestination()),
          "nbt teleport_anchor: destination " + be.getDestination());
      checkEnergy(be, entry, "nbt teleport_anchor");
    });
    loadInto(level, entries, "chunk_loader", BlockEntityChunkLoader.class,
        (be, entry) -> check(be.isEnabledByPlayer() && be.getChunkCount() == entry.get("chunks").getAsInt(),
            "nbt chunk_loader: enabled=" + be.isEnabledByPlayer() + " chunks=" + be.getChunkCount()));

    finish(helper, "nbt parity");
  }

  private interface Check<T> {
    void run(T blockEntity, JsonObject entry);
  }

  private <T> void loadInto(ServerLevel level, JsonObject entries, String key, Class<T> type, Check<T> check) {
    if (!LegacyFixture.hasNbt(key)) {
      failures.add("nbt " + key + ": dump missing");
      return;
    }
    JsonObject entry = entries.getAsJsonObject(key);
    CompoundTag tag = LegacyFixture.readNbt(key);
    BlockPos pos = LegacyFixture.pos(entry);
    BlockState state = level.getBlockState(pos);
    BlockEntity blockEntity = BlockEntity.loadStatic(pos, state, tag);
    if (blockEntity == null) {
      failures.add("nbt " + key + ": block entity type " + tag.getString("id") + " could not be created for "
          + id(state));
      return;
    }
    blockEntity.setLevel(level);
    if (!type.isInstance(blockEntity)) {
      failures.add("nbt " + key + ": loaded " + blockEntity.getClass().getSimpleName() + ", expected "
          + type.getSimpleName());
      return;
    }
    check.run(type.cast(blockEntity), entry);
  }
}

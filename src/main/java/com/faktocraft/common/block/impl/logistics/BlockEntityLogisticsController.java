package com.faktocraft.common.block.impl.logistics;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.energy.interfaces.IEnergyBlock;
import com.faktocraft.common.entity.block.IndRebBlockEntity;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BlockEntityLogisticsController extends IndRebBlockEntity implements IEnergyBlock {

  private LogisticsGraph graph;
  private boolean graphDirty = true;
  private int graphCooldown;
  private final TaskLedger ledger = new TaskLedger(this);
  private final List<PendingPlan> pendingPlans = new ArrayList<>();
  private final List<String> configErrors = new ArrayList<>();

  private record PendingPlan(CompletableFuture<LogisticsPlanner.Plan> future, ItemKey target, int quantity,
      Endpoint dest, BlockPos destNode, @Nullable ServerPlayer player, boolean system) {
  }

  public BlockEntityLogisticsController(BlockPos pos, BlockState state) {
    super(LogisticsRegistry.LOGISTICS_CONTROLLER_BLOCK_ENTITY, pos, state);

    createEnergyStorage(0, ModConfig.server().logistics_controller_capacity, EnergyType.RECEIVE, EnergyTier.MEDIUM);
    initBatterySlots();
  }

  public void markGraphDirty() {
    graphDirty = true;
  }

  @Nullable
  public LogisticsGraph graphIfPresent() {
    return graph;
  }

  @Nullable
  public LogisticsGraph graph() {
    if (level != null && !level.isClientSide() && (graph == null || graphDirty) && graphCooldown <= 0) {
      graph = LogisticsGraph.build(level, worldPosition);
      graphDirty = false;
      graphCooldown = 10;
    }
    return graph;
  }

  public boolean hasCoreConflict() {
    LogisticsGraph current = graph();
    return current != null && current.hasCoreConflict();
  }

  public boolean networkOnline() {
    return !hasCoreConflict() && getEnergyStorage().energyStored() > 0;
  }

  public TaskLedger getLedger() {
    return ledger;
  }

  private long guiSnapshotTick = Long.MIN_VALUE;
  @org.jetbrains.annotations.Nullable
  private GuiSnapshot guiSnapshotCache;

  public record GuiSnapshot(java.util.Map<ItemKey, Integer> stock,
      List<LogisticsPlanner.CraftDecl> decls, List<String> errors,
      java.util.Set<ItemKey> known,
      java.util.Set<ItemKey> producible) {
  }

  public GuiSnapshot guiSnapshot(net.minecraft.world.level.Level level, LogisticsGraph currentGraph) {
    if (guiSnapshotCache == null || level.getGameTime() != guiSnapshotTick) {
      guiSnapshotTick = level.getGameTime();
      java.util.Map<ItemKey, Integer> stock =
          BlockEntityChassis.stockSnapshot(level, currentGraph, ledger);
      List<LogisticsPlanner.CraftDecl> decls = collectDecls(currentGraph);
      guiSnapshotCache = new GuiSnapshot(stock, decls, List.copyOf(configErrors),
          LogisticsPlanner.craftableSet(stock, decls), LogisticsPlanner.producibleSet(stock, decls));
    }
    return guiSnapshotCache;
  }

  public List<String> getConfigErrors() {
    return configErrors;
  }

  public void consumeEnergy(int amount) {
    if (amount > 0) {
      int taken = getEnergyStorage().consumeEnergy(amount, false);
      getEnergyStorage().updateConsumed(taken);
    }
  }

  @Override
  public void tickWork(BlockState state) {
    if (level == null || level.isClientSide()) {
      return;
    }
    if (graphCooldown > 0) {
      graphCooldown--;
    }
    LogisticsGraph current = graph();
    pollPlans(current);
    if (current == null || current.hasCoreConflict() || getEnergyStorage().energyStored() <= 0) {
      return;
    }
    setActive(true);
    ledger.tick(level, current);
  }

  public void submitRequest(ItemKey target, int quantity, Endpoint dest, BlockPos destNode, boolean allowCrafts,
      @Nullable ServerPlayer player, boolean system) {
    LogisticsGraph current = graph();
    if (current == null || level == null) {
      return;
    }
    quantity = Math.max(1, Math.min(quantity, 64 * 64));
    if (current.hasCoreConflict()) {
      recordFailure(player, target, quantity, "conflict", "", destNode);
      return;
    }
    if (getEnergyStorage().energyStored() <= 0) {
      recordFailure(player, target, quantity, "no_energy_short", "", destNode);
      return;
    }
    Map<ItemKey, Integer> stock = BlockEntityChassis.stockSnapshot(level, current, ledger);
    List<LogisticsPlanner.CraftDecl> decls = allowCrafts ? collectDecls(current) : List.of();
    LogisticsPlanner.PlanRequest request = new LogisticsPlanner.PlanRequest(target, quantity, stock, decls);
    CompletableFuture<LogisticsPlanner.Plan> future = LogisticsEngine.submit(() -> LogisticsPlanner.plan(request));
    pendingPlans.add(new PendingPlan(future, target, quantity, dest, destNode, player, system));
  }

  public String originLabel(BlockPos pos) {
    return level != null
        ? level.getBlockState(pos).getBlock().getName().getString()
        : "";
  }

  public List<TaskLedger.TaskSummary> pendingSummaries() {
    List<TaskLedger.TaskSummary> result = new ArrayList<>();
    for (PendingPlan pending : pendingPlans) {
      result.add(new TaskLedger.TaskSummary(0, pending.target().stack(), pending.quantity(), "pending", "",
          pending.system(), pending.destNode().asLong(), originLabel(pending.destNode()), List.of()));
    }
    return result;
  }

  private void pollPlans(@Nullable LogisticsGraph current) {
    if (pendingPlans.isEmpty()) {
      return;
    }
    Iterator<PendingPlan> iterator = pendingPlans.iterator();
    while (iterator.hasNext()) {
      PendingPlan pending = iterator.next();
      if (!pending.future().isDone()) {
        continue;
      }
      iterator.remove();
      LogisticsPlanner.Plan plan;
      try {
        plan = pending.future().join();
      } catch (Exception exception) {
        Faktocraft.LOGGER.error("Logistics planning failed", exception);
        recordFailure(pending.player(), pending.target(), pending.quantity(), "internal", "",
            pending.destNode());
        continue;
      }
      applyPlan(current, pending, plan);
    }
  }

  private void applyPlan(@Nullable LogisticsGraph current, PendingPlan pending, LogisticsPlanner.Plan plan) {
    if (level == null || current == null) {
      return;
    }
    if (!plan.success()) {
      String detail = "missing".equals(plan.errorKey()) && plan.missingItem() != null
          ? plan.missingCount() + "× " + plan.missingItem().stack().getHoverName().getString()
          : "";
      recordFailure(pending.player(), pending.target(), pending.quantity(), plan.errorKey(), detail,
          pending.destNode());
      return;
    }

    Map<ItemKey, Integer> stock = BlockEntityChassis.stockSnapshot(level, current, ledger);
    for (Map.Entry<ItemKey, Integer> entry : plan.withdrawals().entrySet()) {
      if (stock.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
        recordFailure(pending.player(), pending.target(), pending.quantity(), "stock_changed", "",
            pending.destNode());
        return;
      }
    }
    int perItem = Math.max(0, ModConfig.server().logistics_energy_per_item);
    int perCraft = Math.max(0, ModConfig.server().logistics_energy_per_craft);
    int cost = plan.movedItems() * perItem + plan.totalCrafts() * perCraft;
    if (getEnergyStorage().energyStored() < cost) {
      recordFailure(pending.player(), pending.target(), pending.quantity(), "no_energy", String.valueOf(cost),
          pending.destNode());
      return;
    }
    consumeEnergy(cost);
    ledger.reserve(plan.withdrawals());

    TaskLedger.RequestJob job = new TaskLedger.RequestJob();
    job.id = ledger.nextId();
    job.target = pending.target().stack();
    job.quantity = pending.quantity();
    job.system = pending.system();
    job.originPos = pending.destNode().asLong();
    job.originLabel = originLabel(pending.destNode());
    job.directQuantity = Math.min(pending.quantity(), plan.directFromStock());
    if (job.directQuantity > 0) {
      job.stockToDeliver.put(pending.target(), job.directQuantity);
    }
    job.dest = pending.dest();
    job.destNode = pending.destNode();
    job.withdrawalsRemaining = new HashMap<>(plan.withdrawals());
    for (LogisticsPlanner.PlanStep step : plan.steps()) {
      TaskLedger.StepData data = new TaskLedger.StepData();
      data.result = step.decl().result().stack();
      data.resultCount = step.decl().resultCount();
      data.times = step.times();
      data.surplus = step.surplus();
      data.timeout = step.decl().timeout();
      data.ingredients = step.ingredients();
      data.stations = step.stations();
      data.machine = step.decl().machine();
      job.steps.add(data);
    }
    ledger.startStage(level, job);
    ledger.addJob(job);
  }

  public List<LogisticsPlanner.CraftDecl> collectDecls(LogisticsGraph current) {
    configErrors.clear();
    List<LogisticsPlanner.CraftDecl> decls = new ArrayList<>();
    if (level == null) {
      return decls;
    }
    for (BlockPos pos : current.craftPipeList()) {
      if (level.getBlockEntity(pos) instanceof BlockEntityCraftPipe pipe) {
        collectBenchDecls(pipe, pos, decls);
      } else if (level.getBlockEntity(pos) instanceof BlockEntityRecipePipe pipe) {
        collectMachineDecls(pipe, pos, decls);
      }
    }
    return decls;
  }

  private void collectBenchDecls(BlockEntityCraftPipe pipe, BlockPos pos,
      List<LogisticsPlanner.CraftDecl> decls) {
    if (level == null || pipe.recipeCount() == 0) {
      return;
    }

    BlockEntityAssemblyTable assembly = pipe.adjacentAssembly();
    if (assembly == null) {
      configErrors.add("no_assembly:" + posText(pos));
      return;
    }
    for (int index = 0; index < pipe.recipeCount(); index++) {
      CraftingRecipe recipe = pipe.patternRecipe(index);
      if (recipe == null) {
        continue;
      }
      ItemStack result = recipe.getResultItem(level.registryAccess());
      List<LogisticsPlanner.ItemChoice> counts = pipe.patternIngredients(index);
      List<Endpoint> ends = new ArrayList<>();
      Endpoint input = Endpoint.assemblyIn(assembly.getBlockPos());
      for (int i = 0; i < counts.size(); i++) {
        ends.add(input);
      }
      if (counts.isEmpty()) {
        continue;
      }
      decls.add(new LogisticsPlanner.CraftDecl(pos, 0, ItemKey.of(result), result.getCount(), counts, ends,
          Endpoint.assemblyOut(assembly.getBlockPos()), 0, false, pipe.timeoutTicks()));
    }
  }

  private void collectMachineDecls(BlockEntityRecipePipe pipe, BlockPos pos,
      List<LogisticsPlanner.CraftDecl> decls) {
    if (level == null || pipe.recipeCount() == 0) {
      return;
    }
    BlockPos docked = pipe.dockedPos();
    if (docked == null) {
      configErrors.add("no_machine:" + posText(pos));
      return;
    }
    for (int index = 0; index < pipe.recipeCount(); index++) {
      BlockEntityRecipePipe.MachineRecipe recipe = pipe.recipe(index);
      if (recipe == null) {
        continue;
      }
      List<LogisticsPlanner.ItemChoice> counts = new ArrayList<>();
      List<Endpoint> ends = new ArrayList<>();
      boolean valid = true;
      for (BlockEntityRecipePipe.Io input : recipe.inputs) {
        if (input.isEmpty() || input.count <= 0) {
          continue;
        }
        Endpoint end = pinOrDock(input, docked);
        if (end == null) {
          valid = false;
          break;
        }

        counts.add(new LogisticsPlanner.ItemChoice(BlockEntityRecipePipe.options(input), input.count));
        ends.add(end);
      }
      if (!valid || counts.isEmpty()) {
        continue;
      }

      for (BlockEntityRecipePipe.Io output : recipe.outputs) {
        if (output.isEmpty() || output.count <= 0) {
          continue;
        }
        Endpoint outputEnd = pinOrDock(output, docked);
        if (outputEnd == null) {
          continue;
        }
        decls.add(new LogisticsPlanner.CraftDecl(pos, 0, ItemKey.of(output.stack), Math.max(1, output.count),
            counts, ends, outputEnd, 0, true, pipe.timeoutTicks()));
      }
    }
  }

  @Nullable
  private Endpoint pinOrDock(BlockEntityRecipePipe.Io io, BlockPos docked) {
    if (io.bindSlot < 0) {
      return Endpoint.inventory(docked);
    }
    if (level == null || !level.isLoaded(docked)) {
      return null;
    }
    String blockId = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(level.getBlockState(docked).getBlock(
        )).toString();
    net.minecraftforge.items.IItemHandler handler = com.faktocraft.common.util.TransferUtil
        .findItemHandler(level, docked, null);
    if (!blockId.equals(io.bindBlock) || handler == null || handler.getSlots() != io.bindSlots
        || io.bindSlot >= handler.getSlots()) {
      configErrors.add("invalid_bind:" + posText(docked));
      return null;
    }
    return Endpoint.machineSlot(docked, io.bindSlot);
  }

  private static String posText(BlockPos pos) {
    return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
  }

  private void recordFailure(@Nullable ServerPlayer player, ItemKey target, int quantity, String errorKey,
      String detail, BlockPos origin) {
    if (player == null) {
      return;
    }
    ledger.addRecord(target.stack(), quantity, "error." + errorKey, detail, origin, originLabel(origin));
    player.displayClientMessage(
        Component.translatable("logistics." + Faktocraft.MODID + ".state.error." + errorKey, detail), true);
  }

  @Override
  public boolean canReceiveEnergyDir(@Nullable Direction side) {
    return true;
  }

  @Override
  public void onLoad() {
    super.onLoad();
    if (level != null && !level.isClientSide()) {
      LogisticsCores.register(this);
    }
  }

  @Override
  public void onChunkUnloaded() {
    super.onChunkUnloaded();
    LogisticsCores.unregister(this);
  }

  @Override
  public void setRemoved() {
    super.setRemoved();
    LogisticsCores.unregister(this);
  }

  @Override
  public void preRemoveSideEffects(BlockPos pos, BlockState state) {
    if (level != null && !level.isClientSide()) {
      ledger.dropCustody(level, pos);
      LogisticsCores.markDirtyNear(level, pos);
    }
    super.preRemoveSideEffects(pos, state);
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    tag.put("ledger", ledger.save());
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    if (tag.contains("ledger")) {
      ledger.load(tag.getCompound("ledger"));
    }
  }
}

package com.faktocraft.common.block.impl.logistics;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class LogisticsPlanner {

  public static final int MAX_DEPTH = 16;
  public static final int MAX_CRAFTS = 4096;

  private static final int MAX_WORK = MAX_CRAFTS * 8;

  public record ItemCount(ItemKey item, int count) {
  }

  public record ItemChoice(List<ItemKey> options, int count) {

    public static ItemChoice of(ItemKey item, int count) {
      return new ItemChoice(List.of(item), count);
    }

    public ItemKey declared() {
      return options.get(0);
    }
  }

  public record CraftDecl(BlockPos chassisPos, int moduleSlot, ItemKey result, int resultCount,
      List<ItemChoice> ingredients, List<Endpoint> ingredientEnds, Endpoint outputEnd, int priority,
      boolean machine, int timeout) {
  }

  public record PlanRequest(ItemKey target, int quantity, Map<ItemKey, Integer> stock, List<CraftDecl> decls) {
  }

  public record StationChoice(BlockPos node, Endpoint output, List<Endpoint> ingredientEnds) {
  }

  public record PlanStep(CraftDecl decl, int times, int surplus, List<ItemCount> ingredients,
      List<StationChoice> stations) {
  }

  public record Plan(boolean success, Map<ItemKey, Integer> withdrawals, List<PlanStep> steps, int movedItems,
      int totalCrafts, int directFromStock, @Nullable ItemKey missingItem, int missingCount,
      @Nullable String errorKey) {

    public static Plan failure(String errorKey, @Nullable ItemKey missingItem, int missingCount) {
      return new Plan(false, Map.of(), List.of(), 0, 0, 0, missingItem, missingCount, errorKey);
    }
  }

  private static final class State {
    final Map<ItemKey, Integer> stock;
    final Map<ItemKey, List<CraftDecl>> declIndex;
    final Map<ItemKey, Integer> produced = new HashMap<>();
    final Map<ItemKey, Integer> withdrawals = new HashMap<>();
    final List<PlanStep> steps = new ArrayList<>();
    final ArrayDeque<ItemKey> visiting = new ArrayDeque<>();
    int totalCrafts;
    int work;
    boolean exhausted;
    @Nullable
    ItemKey missingItem;
    int missingCount;
    boolean cycle;

    State(Map<ItemKey, Integer> stock, Map<ItemKey, List<CraftDecl>> declIndex) {
      this.stock = new HashMap<>(stock);
      this.declIndex = declIndex;
    }
  }

  private record Snapshot(Map<ItemKey, Integer> stock, Map<ItemKey, Integer> produced,
      Map<ItemKey, Integer> withdrawals, int totalCrafts, int stepsSize, boolean cycle) {

    static Snapshot of(State state) {
      return new Snapshot(new HashMap<>(state.stock), new HashMap<>(state.produced),
          new HashMap<>(state.withdrawals), state.totalCrafts, state.steps.size(), state.cycle);
    }

    void restore(State state) {
      state.stock.clear();
      state.stock.putAll(stock);
      state.produced.clear();
      state.produced.putAll(produced);
      state.withdrawals.clear();
      state.withdrawals.putAll(withdrawals);
      state.totalCrafts = totalCrafts;
      state.steps.subList(stepsSize, state.steps.size()).clear();
      state.cycle = cycle;
      state.missingItem = null;
      state.missingCount = 0;
    }
  }

  private LogisticsPlanner() {
  }

  public static Map<ItemKey, List<CraftDecl>> index(List<CraftDecl> decls) {
    Map<ItemKey, List<CraftDecl>> declIndex = new HashMap<>();
    for (CraftDecl decl : decls) {
      declIndex.computeIfAbsent(decl.result(), item -> new ArrayList<>()).add(decl);
    }
    declIndex.values().forEach(list -> list.sort(Comparator.comparingInt(decl -> -decl.priority())));
    return declIndex;
  }

  @Nullable
  private static int[] matchIngredients(CraftDecl decl, CraftDecl other) {
    List<ItemChoice> mine = decl.ingredients();
    List<ItemChoice> theirs = other.ingredients();
    if (decl.resultCount() != other.resultCount() || mine.size() != theirs.size()) {
      return null;
    }
    int[] mapping = new int[mine.size()];
    boolean[] used = new boolean[theirs.size()];
    for (int i = 0; i < mine.size(); i++) {
      ItemChoice want = mine.get(i);
      int found = -1;
      for (int j = 0; j < theirs.size(); j++) {
        ItemChoice candidate = theirs.get(j);
        if (!used[j] && candidate.count() == want.count()
            && candidate.options().equals(want.options())) {
          found = j;
          break;
        }
      }
      if (found < 0) {
        return null;
      }
      used[found] = true;
      mapping[i] = found;
    }
    return mapping;
  }

  private static List<StationChoice> stationsFor(State state, CraftDecl decl, List<Integer> origins,
      List<Endpoint> ends) {
    List<StationChoice> stations = new ArrayList<>();
    stations.add(new StationChoice(decl.chassisPos(), decl.outputEnd(), ends));
    for (CraftDecl other : state.declIndex.getOrDefault(decl.result(), List.of())) {
      if (other == decl) {
        continue;
      }
      int[] mapping = matchIngredients(decl, other);
      if (mapping == null) {
        continue;
      }
      List<Endpoint> otherEnds = new ArrayList<>(origins.size());
      for (int origin : origins) {
        otherEnds.add(other.ingredientEnds().get(mapping[origin]));
      }
      stations.add(new StationChoice(other.chassisPos(), other.outputEnd(), otherEnds));
    }
    return stations;
  }

  public static ItemKey blockingIngredient(ItemKey item, Map<ItemKey, List<CraftDecl>> declIndex, Set<ItemKey> known) {
    return blockingIngredient(item, declIndex, known, new HashSet<>(), 0);
  }

  private static ItemKey blockingIngredient(ItemKey item, Map<ItemKey, List<CraftDecl>> declIndex, Set<ItemKey> known,
      Set<ItemKey> visiting, int depth) {
    List<CraftDecl> options = declIndex.get(item);
    CraftDecl decl = options == null || options.isEmpty() ? null : options.get(0);
    if (decl == null || depth >= MAX_DEPTH || !visiting.add(item)) {
      return item;
    }
    for (ItemChoice ingredient : decl.ingredients()) {
      if (!anyKnown(known, ingredient)) {
        return blockingIngredient(ingredient.declared(), declIndex, known, visiting, depth + 1);
      }
    }
    return item;
  }

  public static Plan plan(PlanRequest request) {
    Map<ItemKey, List<CraftDecl>> declIndex = index(request.decls());
    State state = new State(request.stock(), declIndex);
    int direct = Math.min(request.quantity(), request.stock().getOrDefault(request.target(), 0));
    if (!resolve(state, request.target(), request.quantity(), 0)) {
      if (state.cycle) {
        return Plan.failure("cycle", state.missingItem, state.missingCount);
      }
      if (state.exhausted || state.totalCrafts > MAX_CRAFTS) {
        return Plan.failure("too_complex", null, 0);
      }
      return Plan.failure("missing", state.missingItem, state.missingCount);
    }
    int moved = state.withdrawals.values().stream().mapToInt(Integer::intValue).sum();
    for (PlanStep step : state.steps) {
      for (ItemCount ingredient : step.ingredients()) {
        moved += ingredient.count();
      }
    }
    moved += request.quantity();

    Map<ItemKey, Integer> lastStep = new HashMap<>();
    for (int i = 0; i < state.steps.size(); i++) {
      lastStep.put(state.steps.get(i).decl().result(), i);
    }
    state.produced.forEach((item, extra) -> {
      Integer index = lastStep.get(item);
      if (index != null && extra > 0) {
        PlanStep old = state.steps.get(index);
        state.steps.set(index, new PlanStep(old.decl(), old.times(), extra, old.ingredients(),
            old.stations()));
      }
    });
    return new Plan(true, state.withdrawals, state.steps, moved, state.totalCrafts, direct, null, 0, null);
  }

  private static boolean resolve(State state, ItemKey item, int quantity, int depth) {
    int fromProduced = Math.min(quantity, state.produced.getOrDefault(item, 0));
    if (fromProduced > 0) {
      state.produced.merge(item, -fromProduced, Integer::sum);
      quantity -= fromProduced;
    }
    int fromStock = Math.min(quantity, state.stock.getOrDefault(item, 0));
    if (fromStock > 0) {
      state.stock.merge(item, -fromStock, Integer::sum);
      state.withdrawals.merge(item, fromStock, Integer::sum);
      quantity -= fromStock;
    }
    if (quantity <= 0) {
      return true;
    }
    if (state.visiting.contains(item)) {
      state.cycle = true;
      state.missingItem = item;
      state.missingCount = quantity;
      return false;
    }
    List<CraftDecl> options = state.declIndex.get(item);
    CraftDecl decl = options == null || options.isEmpty() ? null : options.get(0);
    if (decl == null || depth >= MAX_DEPTH) {
      state.missingItem = item;
      state.missingCount = quantity;
      return false;
    }
    int outputCount = Math.max(1, decl.resultCount());
    int times = (quantity + outputCount - 1) / outputCount;
    state.totalCrafts += times;
    state.work += times;
    if (state.work > MAX_WORK) {
      state.exhausted = true;
      return false;
    }
    if (state.totalCrafts > MAX_CRAFTS) {
      return false;
    }
    state.visiting.push(item);
    List<ItemCount> ingredients = new ArrayList<>();
    List<Endpoint> ends = new ArrayList<>();

    List<Integer> origins = new ArrayList<>();
    for (int i = 0; i < decl.ingredients().size(); i++) {
      ItemChoice ingredient = decl.ingredients().get(i);
      Endpoint end = decl.ingredientEnds().get(i);
      int before = ingredients.size();
      if (!resolveChoice(state, ingredient, ingredient.count() * times, depth + 1, ingredients, ends, end)) {
        state.visiting.pop();
        return false;
      }
      for (int k = before; k < ingredients.size(); k++) {
        origins.add(i);
      }
    }
    state.visiting.pop();
    state.steps.add(new PlanStep(decl, times, 0, ingredients, stationsFor(state, decl, origins, ends)));
    int extra = times * outputCount - quantity;
    if (extra > 0) {
      state.produced.merge(item, extra, Integer::sum);
    }
    return true;
  }

  private static boolean resolveChoice(State state, ItemChoice choice, int quantity, int depth,
      List<ItemCount> out, List<Endpoint> outEnds, Endpoint end) {
    int remaining = quantity;
    if (choice.options().size() > 1) {
      for (ItemKey option : preference(state, choice)) {
        if (remaining <= 0) {
          break;
        }
        int take = Math.min(remaining, available(state, option));

        if (take <= 0 || !resolve(state, option, take, depth)) {
          continue;
        }
        out.add(new ItemCount(option, take));
        outEnds.add(end);
        remaining -= take;
      }
    }
    if (remaining <= 0) {
      return true;
    }

    List<ItemKey> candidates = craftCandidates(state, choice);
    ItemKey firstMissing = null;
    int firstMissingCount = 0;
    boolean firstCycle = false;
    for (int i = 0; i < candidates.size(); i++) {
      ItemKey candidate = candidates.get(i);
      Snapshot snapshot = candidates.size() > 1 ? Snapshot.of(state) : null;
      if (resolve(state, candidate, remaining, depth)) {
        out.add(new ItemCount(candidate, remaining));
        outEnds.add(end);
        return true;
      }
      if (state.exhausted || snapshot == null) {
        return false;
      }

      if (i == 0) {
        firstMissing = state.missingItem;
        firstMissingCount = state.missingCount;
        firstCycle = state.cycle;
      }
      snapshot.restore(state);
    }
    state.missingItem = firstMissing != null ? firstMissing : choice.declared();
    state.missingCount = firstMissing != null ? firstMissingCount : remaining;
    state.cycle = firstCycle;
    return false;
  }

  private static List<ItemKey> preference(State state, ItemChoice choice) {
    List<ItemKey> options = new ArrayList<>(choice.options());
    options.sort(Comparator.comparingInt(item -> -available(state, item)));
    ItemKey declared = choice.declared();
    if (available(state, declared) > 0 && options.remove(declared)) {
      options.add(0, declared);
    }
    return options;
  }

  private static List<ItemKey> craftCandidates(State state, ItemChoice choice) {
    List<ItemKey> result = new ArrayList<>();
    for (ItemKey option : choice.options()) {
      if (state.declIndex.containsKey(option)) {
        result.add(option);
      }
    }
    if (result.isEmpty()) {
      result.add(choice.declared());
    }
    return result;
  }

  private static int available(State state, ItemKey item) {
    return state.produced.getOrDefault(item, 0) + state.stock.getOrDefault(item, 0);
  }

  private static boolean anyKnown(Set<ItemKey> known, ItemChoice choice) {
    for (ItemKey option : choice.options()) {
      if (known.contains(option)) {
        return true;
      }
    }
    return false;
  }

  public static Set<ItemKey> producibleSet(Map<ItemKey, Integer> stock, List<CraftDecl> decls) {
    Set<ItemKey> known = craftableSet(stock, decls);
    Set<ItemKey> producible = new HashSet<>();
    for (CraftDecl decl : decls) {
      boolean satisfiable = true;
      for (ItemChoice ingredient : decl.ingredients()) {
        if (!anyKnown(known, ingredient)) {
          satisfiable = false;
          break;
        }
      }
      if (satisfiable) {
        producible.add(decl.result());
      }
    }
    return producible;
  }

  public static Set<ItemKey> craftableSet(Map<ItemKey, Integer> stock, List<CraftDecl> decls) {
    Set<ItemKey> known = new HashSet<>(stock.keySet());
    int count = decls.size();
    int[] remaining = new int[count];
    boolean[][] satisfied = new boolean[count][];
    Map<ItemKey, List<int[]>> waiting = new HashMap<>();
    ArrayDeque<ItemKey> queue = new ArrayDeque<>();
    for (int d = 0; d < count; d++) {
      List<ItemChoice> ingredients = decls.get(d).ingredients();
      remaining[d] = ingredients.size();
      satisfied[d] = new boolean[ingredients.size()];
      for (int c = 0; c < ingredients.size(); c++) {
        if (anyKnown(known, ingredients.get(c))) {
          satisfied[d][c] = true;
          remaining[d]--;
        } else {
          for (ItemKey option : ingredients.get(c).options()) {
            waiting.computeIfAbsent(option, key -> new ArrayList<>()).add(new int[] { d, c });
          }
        }
      }
      if (remaining[d] == 0 && known.add(decls.get(d).result())) {
        queue.add(decls.get(d).result());
      }
    }
    while (!queue.isEmpty()) {
      for (int[] ref : waiting.getOrDefault(queue.poll(), List.of())) {
        if (satisfied[ref[0]][ref[1]]) {
          continue;
        }
        satisfied[ref[0]][ref[1]] = true;
        if (--remaining[ref[0]] == 0 && known.add(decls.get(ref[0]).result())) {
          queue.add(decls.get(ref[0]).result());
        }
      }
    }
    return known;
  }
}

package com.faktocraft.common.recipe;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.Optional;

public class RecipeChanceResult {

  public static final RecipeChanceResult EMPTY = new RecipeChanceResult(List.of());

  private final List<ChanceResult> results;
  private final int totalWeight;

  public RecipeChanceResult(List<ChanceResult> results) {
    this.results = List.copyOf(results);
    int total = 0;
    for (ChanceResult result : this.results) {
      total += (int) result.chance();
    }
    this.totalWeight = total;
  }

  public static RecipeChanceResult of(Optional<ChanceResult> bonusResult) {
    return bonusResult.map(result -> new RecipeChanceResult(List.of(result))).orElse(EMPTY);
  }

  public static RecipeChanceResult of(List<ChanceResult> results) {
    return results.isEmpty() ? EMPTY : new RecipeChanceResult(results);
  }

  public List<ChanceResult> getResults() {
    return results;
  }

  public Optional<ChanceResult> firstResult() {
    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
  }

  public ItemStack rollResult(RandomSource random) {
    if (results.isEmpty()) {
      return ItemStack.EMPTY;
    }

    float position = random.nextInt(Math.max(totalWeight, 100));
    if (position >= totalWeight) {
      return ItemStack.EMPTY;
    }

    for (ChanceResult result : results) {
      if (position < result.chance()) {
        return result.stack();
      }
      position -= result.chance();
    }

    throw new IllegalStateException("Should never get here!");
  }
}

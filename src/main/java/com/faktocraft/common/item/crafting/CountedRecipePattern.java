package com.faktocraft.common.item.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class CountedRecipePattern {

  private static final int MAX_SIZE = 3;
  public static final char EMPTY_SLOT = ' ';

  private final int width;
  private final int height;
  private final List<Optional<CountedIngredient>> ingredients;

  public CountedRecipePattern(int width, int height, List<Optional<CountedIngredient>> ingredients) {
    this.width = width;
    this.height = height;
    this.ingredients = List.copyOf(ingredients);
  }

  public static CountedRecipePattern fromJson(JsonObject json) {
    Map<Character, CountedIngredient> key = keyFromJson(GsonHelper.getAsJsonObject(json, "key"));
    List<String> pattern = patternFromJson(json);
    return unpack(key, pattern);
  }

  private static Map<Character, CountedIngredient> keyFromJson(JsonObject json) {
    Map<Character, CountedIngredient> key = new java.util.HashMap<>();
    for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
      String symbol = entry.getKey();
      if (symbol.length() != 1) {
        throw new JsonSyntaxException(
            "Invalid key entry: '" + symbol + "' is an invalid symbol (must be 1 character only).");
      }
      if (" ".equals(symbol)) {
        throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
      }
      key.put(symbol.charAt(0), CountedIngredient.fromJson(entry.getValue()));
    }
    return key;
  }

  private static List<String> patternFromJson(JsonObject json) {
    List<String> pattern = new ArrayList<>();
    for (JsonElement element : GsonHelper.getAsJsonArray(json, "pattern")) {
      pattern.add(GsonHelper.convertToString(element, "pattern"));
    }
    if (pattern.size() > MAX_SIZE) {
      throw new JsonSyntaxException("Invalid pattern: too many rows, " + MAX_SIZE + " is maximum");
    }
    if (pattern.isEmpty()) {
      throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
    }
    int firstLength = pattern.get(0).length();
    for (String line : pattern) {
      if (line.length() > MAX_SIZE) {
        throw new JsonSyntaxException("Invalid pattern: too many columns, " + MAX_SIZE + " is maximum");
      }
      if (firstLength != line.length()) {
        throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
      }
    }
    return pattern;
  }

  public static CountedRecipePattern of(Map<Character, CountedIngredient> key, String... pattern) {
    return of(key, List.of(pattern));
  }

  public static CountedRecipePattern of(Map<Character, CountedIngredient> key, List<String> pattern) {
    return unpack(key, pattern);
  }

  private static CountedRecipePattern unpack(Map<Character, CountedIngredient> key, List<String> pattern) {
    String[] shrunkPattern = shrink(pattern);
    int width = shrunkPattern.length == 0 ? 0 : shrunkPattern[0].length();
    int height = shrunkPattern.length;
    List<Optional<CountedIngredient>> ingredients = new ArrayList<>(width * height);
    Set<Character> unusedSymbols = new HashSet<>(key.keySet());

    for (String line : shrunkPattern) {
      for (int x = 0; x < line.length(); x++) {
        char symbol = line.charAt(x);
        Optional<CountedIngredient> ingredient;
        if (symbol == EMPTY_SLOT) {
          ingredient = Optional.empty();
        } else {
          CountedIngredient ingredientForSymbol = key.get(symbol);
          if (ingredientForSymbol == null) {
            throw new JsonSyntaxException(
                "Pattern references symbol '" + symbol + "' but it's not defined in the key");
          }
          ingredient = Optional.of(ingredientForSymbol);
        }

        unusedSymbols.remove(symbol);
        ingredients.add(ingredient);
      }
    }

    if (!unusedSymbols.isEmpty()) {
      throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + unusedSymbols);
    }
    return new CountedRecipePattern(width, height, ingredients);
  }

  static String[] shrink(List<String> pattern) {
    int left = Integer.MAX_VALUE;
    int right = 0;
    int top = 0;
    int bottom = 0;

    for (int i = 0; i < pattern.size(); i++) {
      String line = pattern.get(i);
      left = Math.min(left, firstNonEmpty(line));
      int lastNonSpace = lastNonEmpty(line);
      right = Math.max(right, lastNonSpace);
      if (lastNonSpace < 0) {
        if (top == i) {
          top++;
        }
        bottom++;
      } else {
        bottom = 0;
      }
    }

    if (pattern.size() == bottom) {
      return new String[0];
    }

    String[] result = new String[pattern.size() - bottom - top];
    for (int line = 0; line < result.length; line++) {
      result[line] = pattern.get(line + top).substring(left, right + 1);
    }
    return result;
  }

  private static int firstNonEmpty(String line) {
    int index = 0;
    while (index < line.length() && line.charAt(index) == ' ') {
      index++;
    }
    return index;
  }

  private static int lastNonEmpty(String line) {
    int index = line.length() - 1;
    while (index >= 0 && line.charAt(index) == ' ') {
      index--;
    }
    return index;
  }

  public void toNetwork(FriendlyByteBuf buf) {
    buf.writeVarInt(width);
    buf.writeVarInt(height);
    for (Optional<CountedIngredient> cell : ingredients) {
      buf.writeBoolean(cell.isPresent());
      if (cell.isPresent()) {
        cell.get().toNetwork(buf);
      }
    }
  }

  public static CountedRecipePattern fromNetwork(FriendlyByteBuf buf) {
    int width = buf.readVarInt();
    int height = buf.readVarInt();
    List<Optional<CountedIngredient>> ingredients = new ArrayList<>(width * height);
    for (int i = 0; i < width * height; i++) {
      ingredients.add(buf.readBoolean() ? Optional.of(CountedIngredient.fromNetwork(buf)) : Optional.empty());
    }
    return new CountedRecipePattern(width, height, ingredients);
  }

  public boolean matches(CraftingContainer container) {
    for (int x = 0; x <= container.getWidth() - this.width; x++) {
      for (int y = 0; y <= container.getHeight() - this.height; y++) {
        if (matches(container, x, y, true) || matches(container, x, y, false)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean matches(CraftingContainer container, int startX, int startY, boolean xFlip) {
    for (int x = 0; x < container.getWidth(); x++) {
      for (int y = 0; y < container.getHeight(); y++) {
        int patternX = x - startX;
        int patternY = y - startY;
        Optional<CountedIngredient> expected = Optional.empty();
        if (patternX >= 0 && patternY >= 0 && patternX < this.width && patternY < this.height) {
          expected = cell(patternX, patternY, xFlip);
        }
        ItemStack actual = container.getItem(x + y * container.getWidth());
        if (!testCell(expected, actual)) {
          return false;
        }
      }
    }
    return true;
  }

  private Optional<CountedIngredient> cell(int x, int y, boolean xFlip) {
    return xFlip
        ? this.ingredients.get(this.width - x - 1 + y * this.width)
        : this.ingredients.get(x + y * this.width);
  }

  private static boolean testCell(Optional<CountedIngredient> expected, ItemStack actual) {
    return expected.map(ci -> ci.test(actual)).orElseGet(actual::isEmpty);
  }

  public void consumeExtra(CraftingContainer container) {
    for (int startX = 0; startX <= container.getWidth() - this.width; startX++) {
      for (int startY = 0; startY <= container.getHeight() - this.height; startY++) {
        boolean xFlip;
        if (matches(container, startX, startY, true)) {
          xFlip = true;
        } else if (matches(container, startX, startY, false)) {
          xFlip = false;
        } else {
          continue;
        }

        for (int y = 0; y < this.height; y++) {
          for (int x = 0; x < this.width; x++) {
            Optional<CountedIngredient> expected = this.cell(x, y, xFlip);
            if (expected.isPresent() && expected.get().count() > 1) {
              int gridX = startX + x;
              int gridY = startY + y;
              container.getItem(gridX + gridY * container.getWidth()).shrink(expected.get().count() - 1);
            }
          }
        }
        return;
      }
    }
  }

  public int width() {
    return this.width;
  }

  public int height() {
    return this.height;
  }

  public NonNullList<Ingredient> plainIngredients() {
    NonNullList<Ingredient> list = NonNullList.withSize(ingredients.size(), Ingredient.EMPTY);
    for (int i = 0; i < ingredients.size(); i++) {
      Optional<CountedIngredient> cell = ingredients.get(i);
      if (cell.isPresent()) {
        list.set(i, cell.get().ingredient());
      }
    }
    return list;
  }
}

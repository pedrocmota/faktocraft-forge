package com.faktocraft.common.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public final class CountedRecipePattern {
  private static final int MAX_SIZE = 3;
  public static final char EMPTY_SLOT = ' ';

  public record Data(Map<Character, CountedIngredient> key, List<String> pattern) {
    private static final Codec<List<String>> PATTERN_CODEC = Codec.STRING.listOf().comapFlatMap(pattern -> {
      if (pattern.size() > MAX_SIZE) {
        return DataResult.error(() -> "Invalid pattern: too many rows, " + MAX_SIZE + " is maximum");
      }
      if (pattern.isEmpty()) {
        return DataResult.error(() -> "Invalid pattern: empty pattern not allowed");
      }
      int firstLength = pattern.get(0).length();
      for (String line : pattern) {
        if (line.length() > MAX_SIZE) {
          return DataResult.error(() -> "Invalid pattern: too many columns, " + MAX_SIZE + " is maximum");
        }
        if (firstLength != line.length()) {
          return DataResult.error(() -> "Invalid pattern: each row must be the same width");
        }
      }
      return DataResult.success(pattern);
    }, Function.identity());

    private static final Codec<Character> SYMBOL_CODEC = Codec.STRING.comapFlatMap(symbol -> {
      if (symbol.length() != 1) {
        return DataResult.error(
            () -> "Invalid key entry: '" + symbol + "' is an invalid symbol (must be 1 character only).");
      }
      if (" ".equals(symbol)) {
        return DataResult.error(() -> "Invalid key entry: ' ' is a reserved symbol.");
      }
      return DataResult.success(symbol.charAt(0));
    }, String::valueOf);

    public static final MapCodec<Data> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        ExtraCodecs.strictUnboundedMap(SYMBOL_CODEC, CountedIngredient.CODEC).fieldOf("key").forGetter(Data::key),
        PATTERN_CODEC.fieldOf("pattern").forGetter(Data::pattern))
        .apply(i, Data::new));
  }

  public static final MapCodec<CountedRecipePattern> MAP_CODEC = Data.MAP_CODEC.flatXmap(
      CountedRecipePattern::unpack,
      pattern -> pattern.data.map(DataResult::success)
          .orElseGet(() -> DataResult.error(() -> "Cannot encode unpacked recipe")));

  public static final StreamCodec<RegistryFriendlyByteBuf, CountedRecipePattern> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT, CountedRecipePattern::width,
      ByteBufCodecs.VAR_INT, CountedRecipePattern::height,
      ByteBufCodecs.<RegistryFriendlyByteBuf, CountedIngredient>optional(CountedIngredient.STREAM_CODEC)
          .apply(ByteBufCodecs.list()),
      pattern -> pattern.ingredients,
      CountedRecipePattern::new);

  private final int width;
  private final int height;
  private final List<Optional<CountedIngredient>> ingredients;
  private final Optional<Data> data;

  public CountedRecipePattern(int width, int height, List<Optional<CountedIngredient>> ingredients) {
    this(width, height, ingredients, Optional.empty());
  }

  private CountedRecipePattern(int width, int height, List<Optional<CountedIngredient>> ingredients,
      Optional<Data> data) {
    this.width = width;
    this.height = height;
    this.ingredients = List.copyOf(ingredients);
    this.data = data;
  }

  public static CountedRecipePattern of(Map<Character, CountedIngredient> key, String... pattern) {
    return of(key, List.of(pattern));
  }

  public static CountedRecipePattern of(Map<Character, CountedIngredient> key, List<String> pattern) {
    return unpack(new Data(key, pattern)).getOrThrow(IllegalArgumentException::new);
  }

  private static DataResult<CountedRecipePattern> unpack(Data data) {
    String[] shrunkPattern = shrink(data.pattern());
    int width = shrunkPattern.length == 0 ? 0 : shrunkPattern[0].length();
    int height = shrunkPattern.length;
    List<Optional<CountedIngredient>> ingredients = new ArrayList<>(width * height);
    Set<Character> unusedSymbols = new HashSet<>(data.key().keySet());

    for (String line : shrunkPattern) {
      for (int x = 0; x < line.length(); x++) {
        char symbol = line.charAt(x);
        Optional<CountedIngredient> ingredient;
        if (symbol == EMPTY_SLOT) {
          ingredient = Optional.empty();
        } else {
          CountedIngredient ingredientForSymbol = data.key().get(symbol);
          if (ingredientForSymbol == null) {
            return DataResult.error(
                () -> "Pattern references symbol '" + symbol + "' but it's not defined in the key");
          }
          ingredient = Optional.of(ingredientForSymbol);
        }

        unusedSymbols.remove(symbol);
        ingredients.add(ingredient);
      }
    }

    if (!unusedSymbols.isEmpty()) {
      return DataResult.error(() -> "Key defines symbols that aren't used in pattern: " + unusedSymbols);
    }
    return DataResult.success(new CountedRecipePattern(width, height, ingredients, Optional.of(data)));
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

  public void toNetwork(RegistryFriendlyByteBuf buf) {
    STREAM_CODEC.encode(buf, this);
  }

  public static CountedRecipePattern fromNetwork(RegistryFriendlyByteBuf buf) {
    return STREAM_CODEC.decode(buf);
  }

  public boolean matches(CraftingInput input) {
    for (int x = 0; x <= input.width() - this.width; x++) {
      for (int y = 0; y <= input.height() - this.height; y++) {
        if (matches(input, x, y, true) || matches(input, x, y, false)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean matches(CraftingInput input, int startX, int startY, boolean xFlip) {
    for (int x = 0; x < input.width(); x++) {
      for (int y = 0; y < input.height(); y++) {
        int patternX = x - startX;
        int patternY = y - startY;
        Optional<CountedIngredient> expected = Optional.empty();
        if (patternX >= 0 && patternY >= 0 && patternX < this.width && patternY < this.height) {
          expected = cell(patternX, patternY, xFlip);
        }
        ItemStack actual = input.getItem(x, y);
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

  public void consumeExtra(CraftingInput input) {
    for (int startX = 0; startX <= input.width() - this.width; startX++) {
      for (int startY = 0; startY <= input.height() - this.height; startY++) {
        boolean xFlip;
        if (matches(input, startX, startY, true)) {
          xFlip = true;
        } else if (matches(input, startX, startY, false)) {
          xFlip = false;
        } else {
          continue;
        }

        for (int y = 0; y < this.height; y++) {
          for (int x = 0; x < this.width; x++) {
            Optional<CountedIngredient> expected = this.cell(x, y, xFlip);
            if (expected.isPresent() && expected.get().count() > 1) {
              input.getItem(startX + x, startY + y).shrink(expected.get().count() - 1);
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

  public List<Optional<CountedIngredient>> ingredients() {
    return this.ingredients;
  }

  public List<Optional<Ingredient>> plainIngredients() {
    List<Optional<Ingredient>> list = new ArrayList<>(ingredients.size());
    for (Optional<CountedIngredient> cell : ingredients) {
      list.add(cell.map(CountedIngredient::ingredient));
    }
    return List.copyOf(list);
  }
}

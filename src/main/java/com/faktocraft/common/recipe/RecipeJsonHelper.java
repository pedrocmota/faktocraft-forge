package com.faktocraft.common.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class RecipeJsonHelper {
  private static final String TYPE = "type";
  private static final String NEOFORGE_TYPE = "neoforge:ingredient_type";
  private static final String COUNT = "count";

  private RecipeJsonHelper() {
  }

  public record CountedIngredient(Ingredient ingredient, int count) {
  }

  public static final Codec<CountedIngredient> COUNTED_INGREDIENT = new Codec<>() {
    @Override
    public <T> DataResult<Pair<CountedIngredient, T>> decode(DynamicOps<T> ops, T input) {
      if (ops.getStringValue(input).result().isPresent()) {
        return Ingredient.CODEC.decode(ops, input)
            .map(pair -> pair.mapFirst(ingredient -> new CountedIngredient(ingredient, 1)));
      }
      if (ops.getMap(input).result().isEmpty()) {
        return DataResult.error(() -> "Ingredient must be a string or an object: " + input);
      }
      Dynamic<T> dynamic = new Dynamic<>(ops, input);
      int count = dynamic.get(COUNT).asInt(1);
      Optional<String> tag = dynamic.get("tag").asString().result();
      Optional<String> item = dynamic.get("item").asString().result();
      DataResult<Ingredient> ingredient;
      if (dynamic.get(TYPE).result().isPresent() || dynamic.get(NEOFORGE_TYPE).result().isPresent()) {
        Dynamic<T> custom = dynamic.remove(COUNT);
        Optional<Dynamic<T>> type = custom.get(TYPE).result();
        if (type.isPresent() && custom.get(NEOFORGE_TYPE).result().isEmpty()) {
          custom = custom.remove(TYPE).set(NEOFORGE_TYPE, type.get());
        }
        ingredient = Ingredient.CODEC.parse(custom);
      } else if (tag.isPresent()) {
        ingredient = Ingredient.CODEC.parse(ops, ops.createString("#" + tag.get()));
      } else if (item.isPresent()) {
        ingredient = Ingredient.CODEC.parse(ops, ops.createString(item.get()));
      } else {
        return DataResult.error(() -> "Ingredient must have 'item' or 'tag': " + input);
      }
      return ingredient.map(value -> Pair.of(new CountedIngredient(value, count), ops.empty()));
    }

    @Override
    public <T> DataResult<T> encode(CountedIngredient value, DynamicOps<T> ops, T prefix) {
      DataResult<T> encoded = Ingredient.CODEC.encodeStart(ops, value.ingredient());
      if (value.count() == 1) {
        return encoded;
      }
      return encoded.flatMap(element -> {
        Optional<String> string = ops.getStringValue(element).result();
        if (string.isPresent()) {
          Map<T, T> fields = new HashMap<>();
          String id = string.get();
          if (id.startsWith("#")) {
            fields.put(ops.createString("tag"), ops.createString(id.substring(1)));
          } else {
            fields.put(ops.createString("item"), ops.createString(id));
          }
          fields.put(ops.createString(COUNT), ops.createInt(value.count()));
          return ops.mergeToMap(ops.emptyMap(), fields);
        }
        return ops.mergeToMap(element, ops.createString(COUNT), ops.createInt(value.count()));
      });
    }

    @Override
    public String toString() {
      return "CountedIngredient";
    }
  };

  public static final Codec<Ingredient> INGREDIENT = COUNTED_INGREDIENT.xmap(CountedIngredient::ingredient,
      ingredient -> new CountedIngredient(ingredient, 1));

  private record ResultJson(Optional<String> item, Optional<String> id, int count) {
  }

  private static final MapCodec<ResultJson> RESULT_JSON = RecordCodecBuilder.mapCodec(i -> i.group(
      Codec.STRING.optionalFieldOf("item").forGetter(ResultJson::item),
      Codec.STRING.optionalFieldOf("id").forGetter(ResultJson::id),
      Codec.INT.optionalFieldOf(COUNT, 1).forGetter(ResultJson::count))
      .apply(i, ResultJson::new));

  public static final MapCodec<ItemStackTemplate> RESULT_MAP = RESULT_JSON.flatXmap(
      json -> json.item().or(json::id)
          .map(id -> itemResult(id).map(item -> new ItemStackTemplate(item, json.count())))
          .orElseGet(() -> DataResult.error(() -> "Result must have 'item' or 'id'")),
      template -> DataResult.success(new ResultJson(
          Optional.of(BuiltInRegistries.ITEM.getKey(template.item().value()).toString()), Optional.empty(),
          template.count())));

  public static final Codec<ItemStackTemplate> RESULT = Codec.either(Codec.STRING, RESULT_MAP.codec()).flatXmap(
      either -> either.map(id -> itemResult(id).map(ItemStackTemplate::new), DataResult::success),
      template -> DataResult.success(Either.right(template)));

  public static final StreamCodec<RegistryFriendlyByteBuf, Optional<ItemStackTemplate>> OPTIONAL_RESULT_STREAM_CODEC =
      ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC);

  public static final Codec<ChanceResult> CHANCE_RESULT = RecordCodecBuilder.create(i -> i.group(
      RESULT_MAP.forGetter(ChanceResult::result),
      Codec.FLOAT.optionalFieldOf("chance", 100.0F).forGetter(ChanceResult::chance))
      .apply(i, ChanceResult::new));

  public static final Codec<Identifier> FLUID_ID = Identifier.CODEC.validate(
      id -> BuiltInRegistries.FLUID.containsKey(id)
          ? DataResult.success(id)
          : DataResult.error(() -> "Unknown fluid: " + id));

  public static final Codec<FluidIngredientData> FLUID = RecordCodecBuilder.create(i -> i.group(
      FLUID_ID.fieldOf("fluid").forGetter(FluidIngredientData::fluidId),
      Codec.INT.optionalFieldOf("amount", 1).forGetter(FluidIngredientData::amountMb))
      .apply(i, FluidIngredientData::new));

  private static <A> A parse(Codec<A> codec, JsonElement json, String what) {
    if (json == null || json.isJsonNull()) {
      throw new JsonSyntaxException(what + " cannot be null");
    }
    return codec.parse(JsonOps.INSTANCE, json).getOrThrow(JsonSyntaxException::new);
  }

  public static CountedIngredient counted(JsonElement json) {
    return parse(COUNTED_INGREDIENT, json, "Ingredient");
  }

  public static Ingredient ingredient(JsonElement json) {
    return counted(json).ingredient();
  }

  public static ItemStackTemplate template(JsonElement json) {
    return parse(RESULT, json, "Result");
  }

  public static ItemStack result(JsonElement json) {
    return template(json).create();
  }

  public static FluidIngredientData fluid(JsonElement json) {
    return parse(FLUID, json, "Fluid");
  }

  public static DataResult<Item> itemResult(String id) {
    Identifier rl = Identifier.tryParse(id);
    if (rl == null) {
      return DataResult.error(() -> "Invalid item id: " + id);
    }
    return BuiltInRegistries.ITEM.getOptional(rl)
        .map(DataResult::success)
        .orElseGet(() -> DataResult.error(() -> "Unknown item: " + id));
  }

  public static Item item(String id) {
    return itemResult(id).getOrThrow(JsonSyntaxException::new);
  }

  public static FluidStack fluid(Identifier id, int amount) {
    Fluid fluid = BuiltInRegistries.FLUID.getOptional(id)
        .orElseThrow(() -> new JsonSyntaxException("Unknown fluid: " + id));
    return new FluidStack(fluid, amount);
  }
}

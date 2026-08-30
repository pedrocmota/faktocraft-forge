package com.faktocraft.common.block.impl.logistics;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockEntityRecipePipe extends BlockEntityDockingPipe {

  public static final int MAX_RECIPES = 4096;
  public static final int MAX_INPUTS = 12;
  public static final int MAX_OUTPUTS = 4;
  public static final int MIN_INPUTS = 3;
  public static final int MIN_OUTPUTS = 1;

  private static final int MAX_TAG_ITEMS = 64;

  public static int outputId(int index) {
    return -1 - index;
  }

  public static boolean isOutputId(int io) {
    return io < 0;
  }

  public static int outputIndex(int io) {
    return -1 - io;
  }

  public static final class Io {

    public ItemStack stack = ItemStack.EMPTY;
    public int count;

    public String tag = "";
    public int bindSlot = -1;
    public String bindBlock = "";
    public int bindSlots;

    public boolean isEmpty() {
      return stack.isEmpty();
    }

    void clear() {
      stack = ItemStack.EMPTY;
      count = 0;
      tag = "";
      clearBinding();
    }

    void clearBinding() {
      bindSlot = -1;
      bindBlock = "";
      bindSlots = 0;
    }

    CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      if (!stack.isEmpty()) {
        tag.put("item", stack.save(new CompoundTag()));
        tag.putInt("cnt", count);
      }
      if (!this.tag.isEmpty()) {
        tag.putString("tag", this.tag);
      }
      if (bindSlot >= 0) {
        tag.putInt("slot", bindSlot);
        tag.putString("block", bindBlock);
        tag.putInt("slots", bindSlots);
      }
      return tag;
    }

    static Io load(CompoundTag tag) {
      Io io = new Io();
      io.stack = ItemStack.of(tag.getCompound("item"));
      io.count = tag.getInt("cnt");
      io.tag = tag.getString("tag");
      io.bindSlot = tag.contains("slot") ? tag.getInt("slot") : -1;
      io.bindBlock = tag.getString("block");
      io.bindSlots = tag.getInt("slots");
      return io;
    }
  }

  public static final class MachineRecipe {

    public final Io[] inputs = new Io[MAX_INPUTS];
    public final Io[] outputs = new Io[MAX_OUTPUTS];

    MachineRecipe() {
      for (int i = 0; i < MAX_INPUTS; i++) {
        inputs[i] = new Io();
      }
      for (int i = 0; i < MAX_OUTPUTS; i++) {
        outputs[i] = new Io();
      }
    }

    public Io io(int ioId) {
      return isOutputId(ioId) ? outputs[outputIndex(ioId)] : inputs[ioId];
    }

    public boolean isEmpty() {
      for (Io io : inputs) {
        if (!io.isEmpty()) {
          return false;
        }
      }
      for (Io io : outputs) {
        if (!io.isEmpty()) {
          return false;
        }
      }
      return true;
    }

    CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      ListTag inputList = new ListTag();
      for (Io io : inputs) {
        inputList.add(io.save());
      }
      tag.put("in", inputList);
      ListTag outputList = new ListTag();
      for (Io io : outputs) {
        outputList.add(io.save());
      }
      tag.put("out", outputList);
      return tag;
    }

    static MachineRecipe load(CompoundTag tag) {
      MachineRecipe recipe = new MachineRecipe();
      ListTag inputList = tag.getList("in", Tag.TAG_COMPOUND);
      for (int i = 0; i < Math.min(inputList.size(), MAX_INPUTS); i++) {
        recipe.inputs[i] = Io.load(inputList.getCompound(i));
      }
      ListTag outputList = tag.getList("out", Tag.TAG_COMPOUND);
      for (int i = 0; i < Math.min(outputList.size(), MAX_OUTPUTS); i++) {
        recipe.outputs[i] = Io.load(outputList.getCompound(i));
      }
      return recipe;
    }
  }

  private final List<MachineRecipe> recipes = new ArrayList<>();

  public BlockEntityRecipePipe(BlockPos pos, BlockState state) {
    super(LogisticsRegistry.RECIPE_PIPE_BLOCK_ENTITY, pos, state);
  }

  @Override
  protected boolean canDockTo(BlockState state) {
    return !(state.getBlock() instanceof BlockAssemblyTable);
  }

  public int recipeCount() {
    return recipes.size();
  }

  @Nullable
  public MachineRecipe recipe(int index) {
    return index >= 0 && index < recipes.size() ? recipes.get(index) : null;
  }

  public int addRecipe() {
    for (int i = 0; i < recipes.size(); i++) {
      if (recipes.get(i).isEmpty()) {
        return i;
      }
    }
    if (recipes.size() >= MAX_RECIPES) {
      return -1;
    }
    recipes.add(new MachineRecipe());
    changed();
    return recipes.size() - 1;
  }

  public void removeRecipe(int index) {
    if (index >= 0 && index < recipes.size()) {
      recipes.remove(index);
      changed();
    }
  }

  public void moveRecipe(int from, int to) {
    if (from < 0 || from >= recipes.size() || to < 0 || to > recipes.size() || from == to) {
      return;
    }
    MachineRecipe moved = recipes.remove(from);

    recipes.add(Math.min(to > from ? to - 1 : to, recipes.size()), moved);
    changed();
  }

  public void setIo(int recipe, int ioId, ItemStack stack) {
    MachineRecipe entry = recipe(recipe);
    if (entry == null) {
      return;
    }
    Io io = entry.io(ioId);
    if (stack.isEmpty()) {
      io.clear();
    } else {
      io.stack = stack.copyWithCount(1);
      io.count = Math.max(1, io.count);
      io.tag = "";
      io.clearBinding();
    }
    changed();
  }

  public void setIo(int recipe, int ioId, ItemStack stack, int count) {
    setIo(recipe, ioId, stack);
    MachineRecipe entry = recipe(recipe);
    if (entry != null && !stack.isEmpty()) {
      entry.io(ioId).count = Math.max(1, Math.min(6400, count));
      changed();
    }
  }

  public void adjustCount(int recipe, int ioId, int delta) {
    MachineRecipe entry = recipe(recipe);
    if (entry == null) {
      return;
    }
    Io io = entry.io(ioId);
    if (!io.isEmpty()) {
      io.count = Math.max(1, Math.min(6400, io.count + delta));
      changed();
    }
  }

  public void cycleTag(int recipe, int ioId) {
    MachineRecipe entry = recipe(recipe);
    if (entry == null || isOutputId(ioId)) {
      return;
    }
    Io io = entry.io(ioId);
    if (io.isEmpty()) {
      return;
    }
    List<String> tags = io.stack.getTags().map(key -> key.location().toString()).sorted().toList();
    int next = io.tag.isEmpty() ? 0 : tags.indexOf(io.tag) + 1;
    io.tag = next >= 0 && next < tags.size() ? tags.get(next) : "";
    changed();
  }

  public static List<ItemKey> options(Io io) {
    List<ItemKey> options = new ArrayList<>();
    if (io.isEmpty()) {
      return options;
    }
    options.add(ItemKey.of(io.stack));
    if (io.tag.isEmpty() || !ResourceLocation.isValidResourceLocation(io.tag)) {
      return options;
    }
    TagKey<Item> key = TagKey.create(Registries.ITEM, new ResourceLocation(io.tag));
    for (Item tagItem : net.minecraftforge.registries.ForgeRegistries.ITEMS.tags().getTag(key)) {
      ItemKey option = ItemKey.of(tagItem);
      if (options.size() < MAX_TAG_ITEMS && !options.contains(option)) {
        options.add(option);
      }
    }
    return options;
  }

  public void bind(int recipe, int ioId, int slot, String blockId, int slotCount) {
    MachineRecipe entry = recipe(recipe);
    if (entry == null) {
      return;
    }
    Io io = entry.io(ioId);
    if (slot < 0) {
      io.clearBinding();
    } else {
      io.bindSlot = slot;
      io.bindBlock = blockId;
      io.bindSlots = slotCount;
    }
    changed();
  }

  public void changed() {
    setChanged();
    sync();
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    ListTag list = new ListTag();
    for (MachineRecipe recipe : recipes) {
      list.add(recipe.save());
    }
    tag.put("recipes", list);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    recipes.clear();
    ListTag list = tag.getList("recipes", Tag.TAG_COMPOUND);
    for (int i = 0; i < Math.min(list.size(), MAX_RECIPES); i++) {
      recipes.add(MachineRecipe.load(list.getCompound(i)));
    }
  }
}

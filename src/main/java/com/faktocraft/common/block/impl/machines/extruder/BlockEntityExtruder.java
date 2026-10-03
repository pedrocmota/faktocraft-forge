package com.faktocraft.common.block.impl.machines.extruder;

import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.energy.interfaces.IEnergyBlock;
import com.faktocraft.common.entity.block.BlockEntityProgress;
import com.faktocraft.common.entity.block.FluidStorage;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.entity.slot.FaktocraftSlot;
import com.faktocraft.common.entity.slot.SlotBattery;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.enums.GuiSlotType;
import com.faktocraft.common.enums.InventorySlotType;
import com.faktocraft.common.enums.UpgradeType;
import com.faktocraft.common.interfaces.entity.IElectricSlot;
import com.faktocraft.common.interfaces.entity.IExpCollector;
import com.faktocraft.common.interfaces.entity.IMachineActions;
import com.faktocraft.common.interfaces.entity.ISupportUpgrades;
import com.faktocraft.common.interfaces.entity.ITileSound;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketExperience;
import com.faktocraft.common.recipe.impl.FluidExtrudingRecipe;
import com.faktocraft.common.registries.ModRecipeType;
import com.faktocraft.common.registries.ModSounds;
import com.faktocraft.common.registries.machines.M3Registry;
import com.faktocraft.common.util.EnergyCosts;
import com.faktocraft.common.util.RecipeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import com.faktocraft.common.util.transfer.Capability;
import com.faktocraft.common.util.transfer.ForgeCapabilities;
import com.faktocraft.common.util.transfer.LazyOptional;
import com.faktocraft.common.util.transfer.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BlockEntityExtruder extends FaktocraftBlockEntity
    implements IEnergyBlock, ITileSound, IExpCollector, ISupportUpgrades, IMachineActions.IRecipeSwitcher {

  public static final int INPUT_SLOT = 0;
  public static final int OUTPUT_SLOT = 1;

  protected int cachedWater = 0;
  protected int cachedLava = 0;

  public final FluidStorage waterStorage = new FluidStorage(8000, v -> v.getFluid() == Fluids.WATER);
  public final FluidStorage lavaStorage = new FluidStorage(8000, v -> v.getFluid() == Fluids.LAVA);
  public final BlockEntityProgress progress = new BlockEntityProgress();

  protected int recipeIndex = 0;
  @Nullable
  protected List<RecipeHolder<FluidExtrudingRecipe>> recipes;
  @Nullable
  protected RecipeHolder<FluidExtrudingRecipe> recipe;
  @Nullable
  private String pendingRecipeId;

  private final LazyOptional<IFluidHandler> fluidPortsCap = LazyOptional.of(
      () -> new com.faktocraft.common.block.impl.machines.MachineFluidPorts(
          java.util.List.of(waterStorage, lavaStorage), java.util.List.of()));

  public BlockEntityExtruder(BlockPos pos, BlockState state) {
    super(M3Registry.EXTRUDER_BLOCK_ENTITY, pos, state);
    createEnergyStorage(0, ModConfig.server().extruder_energy_capacity, EnergyType.RECEIVE, EnergyTier.LOW);
    initBatterySlots();
    waterStorage.setChangeListener(this::setChanged);
    lavaStorage.setChangeListener(this::setChanged);
  }

  @Override
  public ArrayList<FaktocraftSlot> addInventorySlot(ArrayList<FaktocraftSlot> slots) {
    slots.add(new FaktocraftSlot(INPUT_SLOT, 80, 59, InventorySlotType.DISABLED, GuiSlotType.NORMAL, 79, 58));
    slots.add(new FaktocraftSlot(OUTPUT_SLOT, 121, 35, InventorySlotType.OUTPUT, GuiSlotType.LARGE, 116, 30));
    return super.addInventorySlot(slots);
  }

  @Override
  public ArrayList<IElectricSlot> addBatterySlot(ArrayList<IElectricSlot> slots) {
    slots.add(new SlotBattery(0, 152, 62, false));
    return super.addBatterySlot(slots);
  }

  @Override
  public SoundEvent getSoundEvent() {
    return ModSounds.EXTRUDER;
  }

  @Override
  public boolean isItemValidForSlot(int slot, ItemStack stack) {
    return false;
  }

  public void initRecipes() {
    if (level instanceof ServerLevel serverLevel) {
      recipes = RecipeUtil.getAllRecipeHoldersFor(serverLevel, ModRecipeType.FLUID_EXTRUDING).stream()
          .sorted(Comparator.comparing(holder -> holder.id().identifier()))
          .toList();
    }
  }

  public void setRecipe(int index) {
    if (recipes == null || recipes.isEmpty()) {
      return;
    }
    selectRecipe(index);
    progress.setBoth(-1);
  }

  private void selectRecipe(int index) {
    if (recipes == null || recipes.isEmpty()) {
      return;
    }
    index = Math.max(0, Math.min(index, recipes.size() - 1));
    this.recipe = recipes.get(index);
    this.recipeIndex = index;
    getItemStackHandler().setStackInSlot(INPUT_SLOT, recipe.value().getResultItem());
  }

  private int indexOfRecipe(@Nullable String id) {
    if (id == null || recipes == null) {
      return -1;
    }
    for (int i = 0; i < recipes.size(); i++) {
      if (recipes.get(i).id().identifier().toString().equals(id)) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public void changeRecipe(boolean next) {
    if (recipes == null) {
      initRecipes();
    }
    if (recipes != null && !recipes.isEmpty()) {
      int newIndex = recipeIndex + (next ? 1 : -1);
      if (newIndex > recipes.size() - 1) {
        newIndex = 0;
      }
      if (newIndex < 0) {
        newIndex = recipes.size() - 1;
      }

      setRecipe(newIndex);

      progress.setBoth(-1);
      progress.clearChanged();
      setActive(false);
      updateBlockState();
    }
  }

  @Override
  public void tickWork(BlockState state) {
    boolean active = false;
    getEnergyStorage().updateConsumed(0);

    if (recipes == null) {
      initRecipes();
    }

    if (cachedWater != waterStorage.getFluidAmount() || cachedLava != lavaStorage.getFluidAmount()) {
      this.cachedWater = waterStorage.getFluidAmount();
      this.cachedLava = lavaStorage.getFluidAmount();
      updateBlockState();
    }

    if (this.recipe == null && recipes != null && !recipes.isEmpty()) {
      int savedIndex = indexOfRecipe(pendingRecipeId);
      pendingRecipeId = null;
      if (savedIndex >= 0) {
        selectRecipe(savedIndex);
      } else {
        setRecipe(0);
      }
      updateBlockState();
    }

    final ItemStack outputStack = getItemStackHandler().getStackInSlot(OUTPUT_SLOT);

    if (recipe != null) {
      FluidExtrudingRecipe currentRecipe = recipe.value();
      ItemStack resultItem = currentRecipe.getResultItem();

      progress.rescaleMax(getSpeedFactor() * currentRecipe.getDuration());
      int energyCost = EnergyCosts.perTick(currentRecipe.getPowerCost(), getEnergyUsageFactor());

      boolean outputFits = outputStack.isEmpty()
          || (outputStack.getItem() == resultItem.getItem()
              && resultItem.getCount() + outputStack.getCount() <= outputStack.getMaxStackSize());

      if (getEnergyStorage().consumeEnergy(energyCost, true) == energyCost &&
          outputFits &&
          !waterStorage.isEmpty() &&
          !lavaStorage.isEmpty() &&
          waterStorage.takeFluid(currentRecipe.getWaterCost(), true) == currentRecipe.getWaterCost() &&
          lavaStorage.takeFluid(currentRecipe.getLavaCost(), true) == currentRecipe.getLavaCost()) {
        if (progress.getProgress() == -1) {
          progress.setData(0, currentRecipe.getDuration());
        }

        getEnergyStorage().consumeEnergy(energyCost, false);
        getEnergyStorage().updateConsumed(energyCost);
        progress.incProgress(1);
        active = true;
      }

      if (progress.getProgress() > 0 && progress.getProgress() >= progress.getProgressMax()) {
        progress.setBoth(-1);
        addRecipeUsed(recipe);

        if (currentRecipe.getWaterCost() > 0) {
          waterStorage.takeFluid(currentRecipe.getWaterCost(), false);
        }
        if (currentRecipe.getLavaCost() > 0) {
          lavaStorage.takeFluid(currentRecipe.getLavaCost(), false);
        }

        if (outputStack.isEmpty()) {
          getItemStackHandler().setStackInSlot(OUTPUT_SLOT, resultItem.copy());
        } else {
          getItemStackHandler().getStackInSlot(OUTPUT_SLOT).grow(resultItem.getCount());
        }
      }
    }

    setActive(active);
    if (progress.changed()) {
      progress.clearChanged();
      updateBlockState();
    }
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    tag.putInt("cachedWater", cachedWater);
    tag.putInt("cachedLava", cachedLava);
    CompoundTag waterTag = new CompoundTag();
    waterStorage.save(waterTag);
    tag.put("water_storage", waterTag);
    CompoundTag lavaTag = new CompoundTag();
    lavaStorage.save(lavaTag);
    tag.put("lava_storage", lavaTag);
    tag.putBoolean("active", activeState);
    CompoundTag progressTag = new CompoundTag();
    progress.save(progressTag);
    tag.put("progress", progressTag);
    if (recipe != null) {
      tag.putString("recipe", recipe.id().identifier().toString());
    } else if (pendingRecipeId != null) {
      tag.putString("recipe", pendingRecipeId);
    }
    super.saveAdditional(tag);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    this.cachedWater = tag.getIntOr("cachedWater", 0);
    this.cachedLava = tag.getIntOr("cachedLava", 0);
    if (tag.contains("water_storage")) {
      waterStorage.load(tag.getCompoundOrEmpty("water_storage"));
    }
    if (tag.contains("lava_storage")) {
      lavaStorage.load(tag.getCompoundOrEmpty("lava_storage"));
    }
    this.activeState = tag.getBooleanOr("active", false);
    if (tag.contains("progress")) {
      progress.load(tag.getCompoundOrEmpty("progress"));
    }
    this.pendingRecipeId = tag.contains("recipe") ? tag.getStringOr("recipe", "") : null;
  }

  @Override
  public float getExperience(Recipe<?> recipe) {
    return ((FluidExtrudingRecipe) recipe).getExperience();
  }

  @Override
  public Runnable collectExp() {
    BlockPos pos = getBlockPos();
    return () -> ModNetworking.sendToServer(new PacketExperience(pos));
  }

  @Override
  public boolean canReceiveEnergyDir(@Nullable Direction side) {
    return true;
  }

  @Override
  public List<UpgradeType> getSupportedUpgrades() {
    return List.of(UpgradeType.OVERCLOCKER, UpgradeType.EFFICIENCY);
  }

  @NotNull
  @Override
  public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
    if (cap == ForgeCapabilities.FLUID_HANDLER) {
      if (isFrontSide(side)) {
        return LazyOptional.empty();
      }
      return fluidPortsCap.cast();
    }
    return super.getCapability(cap, side);
  }

  @Override
  public void setRemoved() {
    super.setRemoved();
    fluidPortsCap.invalidate();
  }

  @Override
  public java.util.List<com.faktocraft.common.entity.block.FluidStorage> getGuiTanks() {
    return java.util.List.of(lavaStorage, waterStorage);
  }
}

package com.faktocraft.common.block.impl.machines.alloy_smelter;

import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.energy.interfaces.IEnergyBlock;
import com.faktocraft.common.entity.block.BlockEntityProgress;
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
import com.faktocraft.common.interfaces.entity.ITileSound;
import com.faktocraft.common.interfaces.entity.ISupportUpgrades;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketExperience;
import com.faktocraft.common.recipe.MachineRecipeInput;
import com.faktocraft.common.recipe.impl.AlloySmeltingRecipe;
import com.faktocraft.common.registries.ModRecipeType;
import com.faktocraft.common.registries.ModSounds;
import com.faktocraft.common.registries.machines.M3Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BlockEntityAlloySmelter extends FaktocraftBlockEntity
    implements IEnergyBlock, IExpCollector, ISupportUpgrades, ITileSound {

  public static final int INPUT_SLOT_0 = 0;
  public static final int INPUT_SLOT_1 = 1;
  public static final int INPUT_SLOT_2 = 2;
  public static final int OUTPUT_SLOT = 3;

  public final BlockEntityProgress progress = new BlockEntityProgress();
  public final BlockEntityProgress heatLevel = new BlockEntityProgress(0, 100);

  private ItemStack resultStack = ItemStack.EMPTY;
  @Nullable
  protected AlloySmeltingRecipe recipe;
  private int energyCostPerTick = 0;
  private int duration = 0;

  private ItemStack cachedInputStack0 = ItemStack.EMPTY;
  private ItemStack cachedInputStack1 = ItemStack.EMPTY;
  private ItemStack cachedInputStack2 = ItemStack.EMPTY;
  private ItemStack cachedOutput = ItemStack.EMPTY;
  private boolean cachedWork;

  public BlockEntityAlloySmelter(BlockPos pos, BlockState state) {
    super(M3Registry.ALLOY_SMELTER_BLOCK_ENTITY, pos, state);
    createEnergyStorage(0, ModConfig.server().alloy_smelter_energy_capacity, EnergyType.RECEIVE, EnergyTier.HIGH);
    initBatterySlots();
  }

  @Override
  public ArrayList<FaktocraftSlot> addInventorySlot(ArrayList<FaktocraftSlot> slots) {
    slots.add(new FaktocraftSlot(INPUT_SLOT_0, 16, 33, InventorySlotType.INPUT, GuiSlotType.NORMAL, 15, 32));
    slots.add(new FaktocraftSlot(INPUT_SLOT_1, 37, 21, InventorySlotType.INPUT, GuiSlotType.NORMAL, 36, 20));
    slots.add(new FaktocraftSlot(INPUT_SLOT_2, 58, 33, InventorySlotType.INPUT, GuiSlotType.NORMAL, 57, 32));
    slots.add(new FaktocraftSlot(OUTPUT_SLOT, 118, 33, InventorySlotType.OUTPUT, GuiSlotType.LARGE, 113, 28));
    return super.addInventorySlot(slots);
  }

  @Override
  public ArrayList<IElectricSlot> addBatterySlot(ArrayList<IElectricSlot> slots) {
    slots.add(new SlotBattery(0, 152, 62, false));
    return super.addBatterySlot(slots);
  }

  protected Optional<AlloySmeltingRecipe> getRecipe(ItemStack... input) {
    if (!(level instanceof ServerLevel serverLevel)) {
      return Optional.empty();
    }
    return serverLevel.getRecipeManager().getRecipeFor(ModRecipeType.ALLOY_SMELTING, MachineRecipeInput.of(input),
        level);
  }

  private boolean isValidInput(final ItemStack... stack) {
    if (level != null && level.isClientSide()) {
      return true;
    }
    return getRecipe(stack).isPresent();
  }

  private boolean canWork(ItemStack inputStack0, ItemStack inputStack1, ItemStack inputStack2, ItemStack outputStack,
      ItemStack resultStack) {
    return getRecipe(inputStack0, inputStack1, inputStack2).isPresent()
        && (outputStack.isEmpty()
            || (resultStack.getCount() + outputStack.getCount() <= outputStack.getMaxStackSize()
                && resultStack.getItem() == outputStack.getItem()));
  }

  private static boolean stackChanged(ItemStack cached, ItemStack current) {
    return !ItemStack.isSameItemSameTags(cached, current) || cached.getCount() != current.getCount();
  }

  @Override
  public void tickWork(BlockState state) {
    boolean active = false;
    getEnergyStorage().updateConsumed(0);
    int heatCost = ModConfig.server().alloy_smelter_energy_heat_cost;

    final ItemStack inputStack0 = getItemStackHandler().getStackInSlot(INPUT_SLOT_0);
    final ItemStack inputStack1 = getItemStackHandler().getStackInSlot(INPUT_SLOT_1);
    final ItemStack inputStack2 = getItemStackHandler().getStackInSlot(INPUT_SLOT_2);
    final ItemStack outputStack = getItemStackHandler().getStackInSlot(OUTPUT_SLOT);

    if (stackChanged(cachedInputStack0, inputStack0) || stackChanged(cachedInputStack1, inputStack1)
        || stackChanged(cachedInputStack2, inputStack2) || stackChanged(cachedOutput, outputStack)) {
      cachedInputStack0 = inputStack0.copy();
      cachedInputStack1 = inputStack1.copy();
      cachedInputStack2 = inputStack2.copy();
      cachedOutput = outputStack.copy();

      AlloySmeltingRecipe oldRecipe = recipe;
      recipe = getRecipe(inputStack0, inputStack1, inputStack2).orElse(null);
      if (recipe != oldRecipe) {

        progress.setBoth(-1);
      }
      if (recipe != null && level != null) {
        resultStack = recipe.assemble(MachineRecipeInput.of(inputStack0, inputStack1, inputStack2),
            level.registryAccess());
        energyCostPerTick = recipe.getPowerCost();
        duration = recipe.getDuration();
      }

      cachedWork = false;
    }

    if (recipe != null && (cachedWork || canWork(inputStack0, inputStack1, inputStack2, outputStack, resultStack))) {
      cachedWork = true;

      if (progress.getProgress() == -1) {
        progress.setData(0, duration);
      }

      progress.rescaleMax(getSpeedFactor() * duration);
      int energyCost = (int) (energyCostPerTick * getEnergyUsageFactor());

      if (getEnergyStorage().consumeEnergy(energyCost, true) == energyCost
          && progress.getProgress() <= progress.getProgressMax()) {
        active = true;
        progress.incProgress(1 + (heatLevel.getPercentProgress() / 100f));
        getEnergyStorage().consumeEnergy(energyCost, false);
        getEnergyStorage().updateConsumed(energyCost);
      }

      if (progress.getProgress() >= progress.getProgressMax()) {
        int[] consume = recipe.slotConsumption(
            MachineRecipeInput.of(inputStack0, inputStack1, inputStack2));
        if (consume == null) {
          progress.setBoth(-1);
        } else {
          if (outputStack.isEmpty()) {
            getItemStackHandler().setStackInSlot(OUTPUT_SLOT, resultStack.copy());
          } else {
            outputStack.grow(resultStack.getCount());
            getItemStackHandler().setStackInSlot(OUTPUT_SLOT, outputStack.copy());
          }

          ItemStack[] inputs = {inputStack0, inputStack1, inputStack2};
          int[] inputSlots = {INPUT_SLOT_0, INPUT_SLOT_1, INPUT_SLOT_2};
          for (int i = 0; i < inputs.length; i++) {
            if (consume[i] > 0) {
              inputs[i].shrink(consume[i]);
              getItemStackHandler().setStackInSlot(inputSlots[i], inputs[i].copy());
            }
          }

          addRecipeUsed(recipe);
          progress.setBoth(-1);
        }
      }
    } else {
      progress.setBoth(-1);
    }

    if ((getRedstonePower() > 0 && getEnergyStorage().consumeEnergy(heatCost, true) >= heatCost) || active) {
      if (heatLevel.getProgress() < 100 && tickCounter == 20) {
        heatLevel.incProgress(0.2f);
        if (!active) {
          getEnergyStorage().consumeEnergy(heatCost, false);
        }
      }
    } else {
      if (heatLevel.getProgress() > 0 && tickCounter == 20) {
        heatLevel.decProgress(Math.min(heatLevel.getProgress(), 1));
      }
    }

    setActive(active);
    if (heatLevel.changed() || progress.changed()) {
      heatLevel.clearChanged();
      progress.clearChanged();
      updateBlockState();
    }
  }

  @Override
  public boolean isItemValidForSlot(int slot, ItemStack stack) {
    if (slot == INPUT_SLOT_0 || slot == INPUT_SLOT_1 || slot == INPUT_SLOT_2) {
      return isValidInput(stack);
    }
    return false;
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    tag.putBoolean("active", activeState);
    CompoundTag progressTag = new CompoundTag();
    progress.save(progressTag);
    tag.put("progress", progressTag);
    CompoundTag heatLevelTag = new CompoundTag();
    heatLevel.save(heatLevelTag);
    tag.put("heatLevel", heatLevelTag);
    super.saveAdditional(tag);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    this.activeState = tag.getBoolean("active");
    if (tag.contains("progress")) {
      progress.load(tag.getCompound("progress"));
    }
    if (tag.contains("heatLevel")) {
      heatLevel.load(tag.getCompound("heatLevel"));
    }
  }

  @Override
  public float getExperience(Recipe<?> recipe) {
    return ((AlloySmeltingRecipe) recipe).getExperience();
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

  @Override
  public SoundEvent getSoundEvent() {
    return ModSounds.ALLOY_SMELTER;
  }
}

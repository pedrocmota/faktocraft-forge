package com.faktocraft.common.block.impl.machines.scanner;

import com.faktocraft.common.capabilities.scan_result.ScannerResult;
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
import com.faktocraft.common.enums.ScannerMode;
import com.faktocraft.common.interfaces.entity.IElectricSlot;
import com.faktocraft.common.interfaces.entity.IExpCollector;
import com.faktocraft.common.interfaces.entity.IMachineActions;
import com.faktocraft.common.interfaces.entity.ITileSound;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketExperience;
import com.faktocraft.common.network.packet.PacketScannerCleanScan;
import com.faktocraft.common.network.packet.PacketScannerSaveScan;
import com.faktocraft.common.recipe.MachineRecipeInput;
import com.faktocraft.common.recipe.impl.ScannerRecipe;
import com.faktocraft.common.registries.machines.M4Registry;
import com.faktocraft.common.registries.ModComponentsFluids;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.registries.ModRecipeType;
import com.faktocraft.common.registries.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Optional;

public class BlockEntityScanner extends FaktocraftBlockEntity
    implements IEnergyBlock, IExpCollector, ITileSound, IMachineActions.IScannerActions,
    com.faktocraft.common.interfaces.entity.ISupportUpgrades {

  public static final int INPUT_SLOT = 0;
  public static final int MEMORY_SLOT = 1;

  public final BlockEntityProgress progress = new BlockEntityProgress();
  protected ScannerMode mode = ScannerMode.NO_POWER;
  protected int currentModeTick = 0;
  @Nullable
  protected ScannerRecipe recipe;
  protected ScannerResult result = ScannerResult.EMPTY;

  private boolean pendingInputChange = false;

  public BlockEntityScanner(BlockPos pos, BlockState state) {
    super(M4Registry.SCANNER_BE, pos, state);
    createEnergyStorage(0, ModConfig.server().scanner_energy_capacity, EnergyType.RECEIVE, EnergyTier.VERY_HIGH);
    initBatterySlots();
  }

  @Override
  public boolean inputSlotChanged(int slotId, ItemStack oldStack, ItemStack newStack) {
    if (slotId != INPUT_SLOT) {
      return false;
    }

    boolean validChange = false;
    ScannerRecipe oldRecipe = recipe;

    if (oldStack.getItem() != newStack.getItem()) {
      if (!newStack.isEmpty()) {
        Optional<ScannerRecipe> optionalRecipe = getRecipe(newStack);
        if (optionalRecipe.isPresent()) {
          recipe = optionalRecipe.get();
          ItemStack stack = new ItemStack(getItemStackHandler().getStackInSlot(INPUT_SLOT).getItem());
          result = new ScannerResult(stack, recipe.getMatterCost(), recipe.getEnergyCost());
        } else {
          recipe = null;
        }
      } else {
        recipe = null;
      }

      validChange = true;
    } else {
      if (recipe != null) {
        recipe = null;
        validChange = true;
      }
    }

    if (oldRecipe == null && recipe != null) {
      validChange = false;
    }

    if (validChange && mode.getId() < 4) {
      result = ScannerResult.EMPTY;
    }

    if (validChange) {
      pendingInputChange = true;
    }

    return validChange;
  }

  @Override
  public void tickWork(BlockState state) {
    boolean active = false;
    boolean validChange = checkInputSlotChange(INPUT_SLOT) || pendingInputChange;
    pendingInputChange = false;

    if (validChange) {
      progress.setBoth(-1);
    }

    if (mode.getId() < 4) {
      if (getEnergyStorage().energyStored() > 0) {
        if (recipe != null) {
          ScannerRecipe scannerRecipe = recipe;
          if (progress.getProgress() == -1) {
            progress.setData(0, getSpeedFactor() * scannerRecipe.getDuration());
          }

          int powerCost = (int) (scannerRecipe.getPowerCost() * getEnergyUsageFactor());
          if (getEnergyStorage().consumeEnergy(powerCost, true) >= powerCost) {
            active = true;
            progress.incProgress(1);
            getEnergyStorage().consumeEnergy(powerCost, false);
            getEnergyStorage().updateConsumed(powerCost);
          }

          if (progress.getProgress() >= progress.getProgressMax()) {
            mode = ScannerMode.RESULT;
            addRecipeUsed(recipe);
            progress.setBoth(-1);
          } else {
            shouldUpdateState = true;
            mode = ScannerMode.PROGRESS;
          }
        } else {
          progress.setBoth(-1);
          mode = ScannerMode.IDLE;
        }
      } else {
        shouldUpdateState = true;
        mode = ScannerMode.NO_POWER;
      }
    } else {
      if (mode.getId() > 4) {
        currentModeTick++;
        if (currentModeTick >= 60) {
          currentModeTick = 0;
          mode = ScannerMode.RESULT;
          shouldUpdateState = true;
        }
      }
    }

    if (progress.changed()) {
      progress.clearChanged();
      shouldUpdateState = true;
    }

    setActive(active);
  }

  @Override
  public ArrayList<FaktocraftSlot> addInventorySlot(ArrayList<FaktocraftSlot> slots) {
    slots.add(new FaktocraftSlot(INPUT_SLOT, 30, 29, InventorySlotType.INPUT, GuiSlotType.LARGE, 25, 24));
    slots.add(new FaktocraftSlot(MEMORY_SLOT, 130, 55, InventorySlotType.INPUT, GuiSlotType.NORMAL, 129, 54));
    return super.addInventorySlot(slots);
  }

  @Override
  public ArrayList<IElectricSlot> addBatterySlot(ArrayList<IElectricSlot> slots) {
    slots.add(new SlotBattery(0, 152, 62, false));
    return super.addBatterySlot(slots);
  }

  @Override
  public boolean canReceiveEnergyDir(@Nullable Direction side) {
    return true;
  }

  @Override
  public SoundEvent getSoundEvent() {
    return ModSounds.SCANNER;
  }

  @Override
  public java.util.List<com.faktocraft.common.enums.UpgradeType> getSupportedUpgrades() {
    return java.util.List.of(com.faktocraft.common.enums.UpgradeType.OVERCLOCKER,
        com.faktocraft.common.enums.UpgradeType.EFFICIENCY);
  }

  @Override
  public boolean isItemValidForSlot(int slot, ItemStack stack) {
    if (slot == INPUT_SLOT) {
      return isValidInput(stack);
    }
    if (slot == MEMORY_SLOT) {
      return stack.getItem() == ModItems.MEMORY_CARD;
    }
    return false;
  }

  @Override
  public int getCustomSlotLimit(int slot) {
    return 1;
  }

  protected Optional<ScannerRecipe> getRecipe(ItemStack input) {
    if (level == null) {
      return Optional.empty();
    }
    return level.getRecipeManager().getRecipeFor(ModRecipeType.SCANNER, MachineRecipeInput.of(input), level);
  }

  public boolean isScannable(ItemStack stack) {
    return !stack.isEmpty() && getRecipe(stack).isPresent();
  }

  protected boolean isValidInput(ItemStack stack) {
    if (stack.isEmpty()) {
      return false;
    }
    if (mode.getId() >= 4) {
      return false;
    }
    return getRecipe(stack).isPresent();
  }

  @Override
  public float getExperience(Recipe<?> recipe) {
    return recipe instanceof ScannerRecipe scannerRecipe ? scannerRecipe.getExperience() : 0;
  }

  @Override
  public Runnable collectExp() {
    BlockPos pos = getBlockPos();
    return () -> ModNetworking.sendToServer(new PacketExperience(pos));
  }

  public Runnable clientClickCleanScan() {
    BlockPos pos = getBlockPos();
    return () -> ModNetworking.sendToServer(new PacketScannerCleanScan(pos));
  }

  public Runnable clientClickSaveScan() {
    BlockPos pos = getBlockPos();
    return () -> ModNetworking.sendToServer(new PacketScannerSaveScan(pos));
  }

  @Override
  public void cleanScan() {
    result = ScannerResult.EMPTY;

    recipe = null;
    mode = getEnergyStorage().energyStored() > 0 ? ScannerMode.IDLE : ScannerMode.NO_POWER;
    updateBlockState();
  }

  @Override
  public void saveScan() {
    if (result.isEmpty()) {
      return;
    }

    final ItemStack memoryStack = getItemStackHandler().getStackInSlot(MEMORY_SLOT);

    ScannerResult cardResult = ModComponentsFluids.getScannerResult(memoryStack);
    boolean cardEmpty = cardResult == null || cardResult.isEmpty();

    if (!memoryStack.isEmpty() && memoryStack.getItem() == ModItems.MEMORY_CARD && cardEmpty) {
      ModComponentsFluids.setScannerResult(memoryStack, result);
      mode = getEnergyStorage().energyStored() > 0 ? ScannerMode.IDLE : ScannerMode.NO_POWER;
      result = ScannerResult.EMPTY;
      recipe = null;
    } else {
      mode = ScannerMode.TRANSFER_NO_STORAGE;
    }

    updateBlockState();
  }

  public ScannerMode getMode() {
    return mode;
  }

  public ScannerResult getResult() {
    return result;
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    CompoundTag progressTag = new CompoundTag();
    progress.save(progressTag);
    tag.put("progress", progressTag);

    CompoundTag resultTag = new CompoundTag();
    result.save(resultTag);
    tag.put("result", resultTag);

    tag.putInt("mode", mode.getId());
    tag.putInt("currentModeTick", currentModeTick);
    tag.putBoolean("recipeLoaded", recipe != null);
    super.saveAdditional(tag);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    if (tag.contains("progress")) {
      progress.load(tag.getCompound("progress"));
    }
    this.result = tag.contains("result") ? ScannerResult.load(tag.getCompound("result")) : ScannerResult.EMPTY;
    this.mode = ScannerMode.getModeFromId(tag.contains("mode") ? tag.getInt("mode") : ScannerMode.NO_POWER.getId());
    this.currentModeTick = tag.getInt("currentModeTick");

    if (!tag.getBoolean("recipeLoaded")) {
      seedInputCache(INPUT_SLOT);
    }
  }
}

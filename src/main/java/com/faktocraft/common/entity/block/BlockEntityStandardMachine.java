package com.faktocraft.common.entity.block;

import com.faktocraft.common.energy.interfaces.IEnergyBlock;
import com.faktocraft.common.entity.slot.IndRebSlot;
import com.faktocraft.common.entity.slot.SlotBattery;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.enums.GuiSlotType;
import com.faktocraft.common.enums.InventorySlotType;
import com.faktocraft.common.enums.UpgradeType;
import com.faktocraft.common.interfaces.entity.IElectricSlot;
import com.faktocraft.common.interfaces.entity.IExpCollector;
import com.faktocraft.common.interfaces.entity.ISupportUpgrades;
import com.faktocraft.common.interfaces.entity.ITileSound;
import com.faktocraft.common.interfaces.receipe.IChanceRecipe;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketExperience;
import com.faktocraft.common.util.StackHandlerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class BlockEntityStandardMachine extends IndRebBlockEntity
    implements IEnergyBlock, ITileSound, IExpCollector, ISupportUpgrades {

  public static final int INPUT_SLOT = 0;
  public static final int OUTPUT_SLOT = 1;
  public static final int BONUS_SLOT = 2;

  public final BlockEntityProgress progress = new BlockEntityProgress();

  protected ItemStack cachedInputItem = ItemStack.EMPTY;
  @Nullable
  protected IChanceRecipe cachedRecipe;
  protected ItemStack cachedResult = ItemStack.EMPTY;
  protected ItemStack rolledChance = ItemStack.EMPTY;
  protected boolean recipeResolved = false;

  public BlockEntityStandardMachine(BlockEntityType<?> type, BlockPos pos, BlockState state, int energyCapacity) {
    super(type, pos, state);
    createEnergyStorage(0, energyCapacity, EnergyType.RECEIVE, EnergyTier.LOW);
    initBatterySlots();
  }

  @Override
  public ArrayList<IndRebSlot> addInventorySlot(ArrayList<IndRebSlot> slots) {
    slots.add(new IndRebSlot(INPUT_SLOT, 48, 35, InventorySlotType.INPUT, GuiSlotType.NORMAL, 47, 34));
    slots.add(new IndRebSlot(OUTPUT_SLOT, 108, 24, InventorySlotType.OUTPUT, GuiSlotType.LARGE, 103, 19));
    slots.add(new IndRebSlot(BONUS_SLOT, 108, 50, InventorySlotType.BONUS, GuiSlotType.NORMAL, 107, 49));
    return slots;
  }

  @Override
  public ArrayList<IElectricSlot> addBatterySlot(ArrayList<IElectricSlot> slots) {
    slots.add(new SlotBattery(0, 152, 62, false));
    return slots;
  }

  @Override
  public boolean canReceiveEnergyDir(@Nullable net.minecraft.core.Direction side) {
    return true;
  }

  @Override
  public List<UpgradeType> getSupportedUpgrades() {
    return List.of(UpgradeType.OVERCLOCKER, UpgradeType.EFFICIENCY);
  }

  protected abstract Optional<? extends IChanceRecipe> getRecipe(ItemStack input);

  protected ItemStack getRecipeResult(ItemStack input) {
    return cachedRecipe != null && level != null
        ? cachedRecipe.assemble(new SimpleContainer(input), level.registryAccess())
        : ItemStack.EMPTY;
  }

  @Override
  public void tickWork(BlockState state) {
    if (level == null || level.isClientSide()) {
      return;
    }
    boolean active = false;

    ItemStack inputStack = itemStackHandler.getStackInSlot(INPUT_SLOT);

    if (!ItemStack.isSameItemSameTags(cachedInputItem, inputStack) || !recipeResolved) {
      cachedInputItem = inputStack.copy();
      cachedRecipe = inputStack.isEmpty() ? null : getRecipe(inputStack).orElse(null);
      recipeResolved = true;
      if (cachedRecipe != null) {
        cachedResult = getRecipeResult(inputStack);
        IChanceRecipe recipe = cachedRecipe;
        rolledChance = recipe.rollChanceResult(level.getRandom());
        progress.setData(0, getSpeedFactor() * recipe.getDuration());
      } else {
        cachedResult = ItemStack.EMPTY;
        rolledChance = ItemStack.EMPTY;
        progress.setBoth(-1);
      }
    }

    if (cachedRecipe != null && (!cachedResult.isEmpty() || !cachedRecipe.getChanceResults().isEmpty())) {
      IChanceRecipe recipe = cachedRecipe;
      ItemStack outputStack = itemStackHandler.getStackInSlot(OUTPUT_SLOT);
      ItemStack bonusStack = itemStackHandler.getStackInSlot(BONUS_SLOT);

      boolean outputFits = cachedResult.isEmpty() || outputStack.isEmpty()
          || (ItemStack.isSameItemSameTags(outputStack, cachedResult)
              && outputStack.getCount() + cachedResult.getCount() <= outputStack.getMaxStackSize());
      boolean bonusFits = rolledChance.isEmpty() || bonusStack.isEmpty()
          || (ItemStack.isSameItemSameTags(bonusStack, rolledChance)
              && bonusStack.getCount() + rolledChance.getCount() <= bonusStack.getMaxStackSize());
      boolean enoughInput = inputStack.getCount() >= recipe.getIngredientCount();

      if (outputFits && bonusFits && enoughInput) {
        if (progress.getProgressMax() < 0) {
          progress.setData(0, getSpeedFactor() * recipe.getDuration());
        }
        int energyCost = (int) (recipe.getPowerCost() * getEnergyUsageFactor());
        if (getEnergyStorage().consumeEnergy(energyCost, true) == energyCost) {
          active = true;
          progress.incProgress(1);
          getEnergyStorage().consumeEnergy(energyCost, false);
          getEnergyStorage().updateConsumed(energyCost);

          if (progress.getProgress() >= progress.getProgressMax()) {
            if (!cachedResult.isEmpty()) {
              StackHandlerHelper.incMachineOutputStack(itemStackHandler, OUTPUT_SLOT, cachedResult.copy());
            }
            if (!rolledChance.isEmpty()) {
              StackHandlerHelper.incMachineOutputStack(itemStackHandler, BONUS_SLOT, rolledChance.copy());
            }
            StackHandlerHelper.shrinkInputStack(itemStackHandler, INPUT_SLOT, recipe.getIngredientCount());
            addRecipeUsed(cachedRecipe);
            rolledChance = recipe.rollChanceResult(level.getRandom());
            progress.setData(0, getSpeedFactor() * recipe.getDuration());
            recipeResolved = false;
          }
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
  public float getExperience(Recipe<?> recipe) {
    if (recipe instanceof IChanceRecipe chanceRecipe) {
      return chanceRecipe.getExperience();
    }
    if (recipe instanceof com.faktocraft.common.interfaces.receipe.IBaseRecipe<?> baseRecipe) {
      return baseRecipe.getExperience();
    }
    return 0;
  }

  @Override
  public Runnable collectExp() {
    BlockPos pos = getBlockPos();
    return () -> ModNetworking.sendToServer(new PacketExperience(pos));
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    tag.putBoolean("active", activeState);
    CompoundTag progressTag = new CompoundTag();
    progress.save(progressTag);
    tag.put("progress", progressTag);
    super.saveAdditional(tag);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    activeState = tag.contains("active") ? tag.getBoolean("active") : false;
    if (tag.contains("progress")) {
      progress.load(tag.getCompound("progress"));
    }
  }
}

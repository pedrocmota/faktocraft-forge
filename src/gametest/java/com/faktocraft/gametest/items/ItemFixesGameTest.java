package com.faktocraft.gametest.items;

import com.faktocraft.common.item.base.FluidItem;
import com.faktocraft.common.registries.ModComponentsFluids;
import com.faktocraft.common.registries.ModDataComponents;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.util.RecipeUtil;
import com.faktocraft.gametest.GameTest;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;

public class ItemFixesGameTest {

  private static final String TEMPLATE = "gametest_platform";
  private static final Identifier COOLANT = Identifier.fromNamespaceAndPath("faktocraft", "coolant");

  private static Holder<Enchantment> enchantment(GameTestHelper helper, ResourceKey<Enchantment> key) {
    return helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
  }

  private static boolean craftedCellHolds(GameTestHelper helper, String path, int amount) {
    Optional<RecipeHolder<?>> holder = RecipeUtil.byKey(helper.getLevel(),
        Identifier.fromNamespaceAndPath("faktocraft", path));
    if (holder.isEmpty() || !(holder.get().value() instanceof CraftingRecipe recipe)) {
      helper.fail("recipe " + path + " not found");
      return false;
    }
    ItemStack result = recipe.assemble(CraftingInput.EMPTY);
    if (!COOLANT.equals(ModComponentsFluids.getFluid(result)) || FluidItem.getFluidAmount(result) != amount) {
      helper.fail(path + " result holds " + ModComponentsFluids.getFluid(result) + " x"
          + FluidItem.getFluidAmount(result) + ", expected coolant x" + amount);
      return false;
    }
    return true;
  }

  @GameTest(template = TEMPLATE)
  public static void coolantCellRecipesFillTheCells(GameTestHelper helper) {
    if (craftedCellHolds(helper, "item/reactor/medium_coolant_cell", 3000)
        && craftedCellHolds(helper, "item/reactor/large_coolant_cell", 6000)) {
      helper.succeed();
    }
  }

  @GameTest(template = TEMPLATE)
  public static void bronzeRepairsWithBronzeIngots(GameTestHelper helper) {
    ItemStack ingot = new ItemStack(ModItems.BRONZE_INGOT);
    if (!new ItemStack(ModItems.BRONZE_HELMET).isValidRepairItem(ingot)) {
      helper.fail("bronze helmet does not accept bronze ingots for repair");
      return;
    }
    if (!new ItemStack(ModItems.BRONZE_SWORD).isValidRepairItem(ingot)) {
      helper.fail("bronze sword does not accept bronze ingots for repair");
      return;
    }
    if (new ItemStack(ModItems.BRONZE_SWORD).isValidRepairItem(new ItemStack(ModItems.NANO_HELMET))) {
      helper.fail("bronze sword accepted an unrelated item for repair");
      return;
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE)
  public static void bronzeArmorEnchantsAndElectricArmorDoesNot(GameTestHelper helper) {
    Holder<Enchantment> protection = enchantment(helper, Enchantments.PROTECTION);
    Holder<Enchantment> unbreaking = enchantment(helper, Enchantments.UNBREAKING);
    Holder<Enchantment> thorns = enchantment(helper, Enchantments.THORNS);
    ItemStack bronze = new ItemStack(ModItems.BRONZE_CHESTPLATE);
    ItemStack nano = new ItemStack(ModItems.NANO_CHESTPLATE);
    if (!bronze.isEnchantable() || !bronze.supportsEnchantment(protection) || !bronze.isPrimaryItemFor(protection)
        || !bronze.supportsEnchantment(unbreaking) || !bronze.supportsEnchantment(thorns)) {
      helper.fail("bronze chestplate is not enchantable like 1.20.1");
      return;
    }
    if (nano.isEnchantable() || nano.supportsEnchantment(protection) || nano.supportsEnchantment(unbreaking)) {
      helper.fail("electric armor became enchantable");
      return;
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE)
  public static void legacyCellMigratesOnInventoryTick(GameTestHelper helper) {
    ItemStack legacy = new ItemStack(ModItems.FLUID_CELL);
    CompoundTag data = new CompoundTag();
    data.putString("fluid", COOLANT.toString());
    data.putInt("fluid_amount", 1000);
    legacy.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    ItemStack fresh = new ItemStack(ModItems.FLUID_CELL);
    FluidItem.setFluid(fresh, BuiltInRegistries.FLUID.getValue(COOLANT), 1000);
    if (legacy.has(ModDataComponents.FLUID) || FluidItem.getFluidAmount(legacy) != 1000) {
      helper.fail("legacy cell setup is wrong: " + legacy.getComponents());
      return;
    }
    if (ItemStack.isSameItemSameComponents(legacy, fresh)) {
      helper.fail("legacy and fresh cells already compared equal before migration");
      return;
    }
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    legacy.getItem().inventoryTick(legacy, helper.getLevel(), player, null);
    if (!legacy.has(ModDataComponents.FLUID) || FluidItem.getFluidAmount(legacy) != 1000) {
      helper.fail("legacy cell was not migrated to the fluid component: " + legacy.getComponents());
      return;
    }
    CustomData left = legacy.get(DataComponents.CUSTOM_DATA);
    if (left != null && (left.contains("fluid") || left.contains("fluid_amount"))) {
      helper.fail("legacy keys were left in custom_data: " + left);
      return;
    }
    if (!ItemStack.isSameItemSameComponents(legacy, fresh)) {
      helper.fail("migrated cell still differs from a fresh cell: " + legacy.getComponents() + " vs "
          + fresh.getComponents());
      return;
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE)
  public static void bronzeIngotIsInTheCommonTag(GameTestHelper helper) {
    Holder<net.minecraft.world.item.Item> holder = BuiltInRegistries.ITEM.wrapAsHolder(ModItems.BRONZE_INGOT);
    if (!holder.is(com.faktocraft.common.registries.ModTags.commonItemTag("ingots/bronze"))) {
      helper.fail("bronze ingot is not in the tag used by the bronze tool and armor materials");
      return;
    }
    helper.succeed();
  }
}

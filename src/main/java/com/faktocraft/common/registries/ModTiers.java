package com.faktocraft.common.registries;

import net.minecraftforge.common.util.Lazy;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import java.util.function.Supplier;

public class ModTiers {

  public static final Tier BRONZE = new ModTier(2, 280, 7.0F, 2.0F, 14,
      () -> Ingredient.of(ModTags.commonItemTag("ingots/bronze")));

  public static final Tier DIAMOND_TOOL = new ModTier(3, 1561, 8.4F, 3.0F, 10,
      () -> Ingredient.of(Items.DIAMOND));

  public static final Tier IRIDIUM_TOOL = new ModTier(4, 3046, 10.8F, 4.0F, 15,
      () -> Ingredient.of(ModTags.itemTag("repairs_iridium")));

  private static final class ModTier implements Tier {

    private final int level;
    private final int uses;
    private final float speed;
    private final float attackDamageBonus;
    private final int enchantmentValue;
    private final Lazy<Ingredient> repairIngredient;

    private ModTier(int level, int uses, float speed, float attackDamageBonus, int enchantmentValue,
        Supplier<Ingredient> repairIngredient) {
      this.level = level;
      this.uses = uses;
      this.speed = speed;
      this.attackDamageBonus = attackDamageBonus;
      this.enchantmentValue = enchantmentValue;
      this.repairIngredient = Lazy.of(repairIngredient);
    }

    @Override
    public int getUses() {
      return uses;
    }

    @Override
    public float getSpeed() {
      return speed;
    }

    @Override
    public float getAttackDamageBonus() {
      return attackDamageBonus;
    }

    @Override
    public int getLevel() {
      return level;
    }

    @Override
    public int getEnchantmentValue() {
      return enchantmentValue;
    }

    @Override
    public Ingredient getRepairIngredient() {
      return repairIngredient.get();
    }
  }
}

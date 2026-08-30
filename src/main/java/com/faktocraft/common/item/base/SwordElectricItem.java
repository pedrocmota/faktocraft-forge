package com.faktocraft.common.item.base;

import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SwordElectricItem extends ElectricItem {

  private final Tier material;
  private final float attackDamage;
  private final float attackSpeed;
  private final Multimap<Attribute, AttributeModifier> defaultModifiers;

  public SwordElectricItem(Tier material, int damage, float speed, Properties properties,
      int energyStored, int maxEnergy, EnergyType energyType, EnergyTier energyTier) {
    super(properties, energyStored, maxEnergy, energyType, energyTier);
    this.material = material;
    this.attackDamage = damage + material.getAttackDamageBonus();
    this.attackSpeed = speed;
    this.defaultModifiers = createSwordAttributes(this.attackDamage, this.attackSpeed);
  }

  public static Multimap<Attribute, AttributeModifier> createSwordAttributes(float totalAttackDamage,
      float attackSpeed) {
    return ImmutableMultimap.<Attribute, AttributeModifier>builder()
        .put(Attributes.ATTACK_DAMAGE,
            new AttributeModifier(Item.BASE_ATTACK_DAMAGE_UUID, "Weapon modifier",
                totalAttackDamage, AttributeModifier.Operation.ADDITION))
        .put(Attributes.ATTACK_SPEED,
            new AttributeModifier(Item.BASE_ATTACK_SPEED_UUID, "Weapon modifier",
                attackSpeed, AttributeModifier.Operation.ADDITION))
        .build();
  }

  public float getDamage() {
    return attackDamage;
  }

  public float getAttackSpeed() {
    return attackSpeed;
  }

  @Override
  public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
    return slot == EquipmentSlot.MAINHAND ? defaultModifiers : ImmutableMultimap.of();
  }

  @Override
  public boolean isEnchantable(ItemStack stack) {
    return true;
  }

  @Override
  public int getEnchantmentValue() {
    return material.getEnchantmentValue();
  }

  @Override
  public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
    return !player.isCreative();
  }

  @Override
  public float getDestroySpeed(ItemStack stack, BlockState state) {
    if (state.is(Blocks.COBWEB)) {
      return 15.0F;
    }
    return state.is(BlockTags.SWORD_EFFICIENT) ? 1.5F : 1.0F;
  }

  @Override
  public boolean isCorrectToolForDrops(BlockState state) {
    return state.is(Blocks.COBWEB);
  }
}

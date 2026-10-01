package com.faktocraft.common.item.base;

import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;

public class SwordElectricItem extends ElectricItem {
  private final ToolMaterial material;
  private final float attackDamage;
  private final float attackSpeed;
  private final ItemAttributeModifiers defaultModifiers;

  public SwordElectricItem(ToolMaterial material, int damage, float speed, Properties properties,
      int energyStored, int maxEnergy, EnergyType energyType, EnergyTier energyTier) {
    super(swordProperties(properties, material), energyStored, maxEnergy, energyType, energyTier);
    this.material = material;
    this.attackDamage = damage + material.attackDamageBonus();
    this.attackSpeed = speed;
    this.defaultModifiers = createSwordAttributes(this.attackDamage, this.attackSpeed);
  }

  private static Properties swordProperties(Properties properties, ToolMaterial material) {
    HolderGetter<Block> blocks = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
    return properties.enchantable(material.enchantmentValue())
        .component(DataComponents.TOOL, new Tool(List.of(
            Tool.Rule.minesAndDrops(HolderSet.direct(BuiltInRegistries.BLOCK.wrapAsHolder(Blocks.COBWEB)), 15.0F),
            Tool.Rule.overrideSpeed(blocks.getOrThrow(BlockTags.SWORD_INSTANTLY_MINES), Float.MAX_VALUE),
            Tool.Rule.overrideSpeed(blocks.getOrThrow(BlockTags.SWORD_EFFICIENT), 1.5F)), 1.0F, 0, false))
        .component(DataComponents.WEAPON, new Weapon(0));
  }

  public static ItemAttributeModifiers createSwordAttributes(float totalAttackDamage, float attackSpeed) {
    return ItemAttributeModifiers.builder()
        .add(Attributes.ATTACK_DAMAGE,
            new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, totalAttackDamage,
                AttributeModifier.Operation.ADD_VALUE),
            EquipmentSlotGroup.MAINHAND)
        .add(Attributes.ATTACK_SPEED,
            new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE),
            EquipmentSlotGroup.MAINHAND)
        .build();
  }

  public ToolMaterial getMaterial() {
    return material;
  }

  public float getDamage() {
    return attackDamage;
  }

  public float getAttackSpeed() {
    return attackSpeed;
  }

  @Override
  public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
    return defaultModifiers;
  }

  @Override
  public boolean canDestroyBlock(ItemStack stack, BlockState state, Level level, BlockPos pos, LivingEntity user) {
    return !(user instanceof Player player && player.isCreative());
  }

  @Override
  public float getDestroySpeed(ItemStack stack, BlockState state) {
    if (state.is(Blocks.COBWEB)) {
      return 15.0F;
    }
    return state.is(BlockTags.SWORD_EFFICIENT) ? 1.5F : 1.0F;
  }

  @Override
  public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
    return state.is(Blocks.COBWEB);
  }
}

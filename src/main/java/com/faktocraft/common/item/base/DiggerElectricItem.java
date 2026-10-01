package com.faktocraft.common.item.base;

import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.fml.DistExecutor;
import java.util.List;

public class DiggerElectricItem extends ElectricItem {

  private final Tier material;
  private final List<TagKey<Block>> mineableTags;
  private final float disableBlockingSeconds;
  private final Multimap<Attribute, AttributeModifier> defaultModifiers;

  public DiggerElectricItem(Tier material, float attackDamage, float attackSpeed,
      List<TagKey<Block>> mineableTags,
      Properties properties, int energyStored, int maxEnergy, EnergyType energyType, EnergyTier energyTier) {
    this(material, attackDamage, attackSpeed, 0.0F, mineableTags, properties, energyStored, maxEnergy, energyType,
        energyTier);
  }

  public DiggerElectricItem(Tier material, float attackDamage, float attackSpeed, float disableBlockingSeconds,
      List<TagKey<Block>> mineableTags, Properties properties,
      int energyStored, int maxEnergy, EnergyType energyType, EnergyTier energyTier) {
    super(properties, energyStored, maxEnergy, energyType, energyTier);
    this.material = material;
    this.mineableTags = List.copyOf(mineableTags);
    this.disableBlockingSeconds = disableBlockingSeconds;
    this.defaultModifiers = ImmutableMultimap.<Attribute, AttributeModifier>builder()
        .put(Attributes.ATTACK_DAMAGE,
            new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier",
                attackDamage + material.getAttackDamageBonus(), AttributeModifier.Operation.ADDITION))
        .put(Attributes.ATTACK_SPEED,
            new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier",
                attackSpeed, AttributeModifier.Operation.ADDITION))
        .build();
  }

  public int getHurtEnergyCost() {
    return 100;
  }

  public int getMineCost() {
    return 50;
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
  public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
    return enchantment.category == EnchantmentCategory.DIGGER;
  }

  protected float getBaseDestroySpeed(BlockState state) {
    for (TagKey<Block> tag : mineableTags) {
      if (state.is(tag)) {
        return material.getSpeed();
      }
    }
    return 1.0F;
  }

  protected float efficiencyScale() {
    return 1.0F;
  }

  public static int efficiencyBonus(ItemStack stack) {
    int level = stack.getEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY);
    return level > 0 ? level * level + 1 : 0;
  }

  @Override
  public float getDestroySpeed(ItemStack stack, BlockState state) {
    float speed = getBaseDestroySpeed(state);
    if (speed > 1.0F) {
      if (getEnergy(stack).consumeEnergy(getMineCost(), true) < getMineCost()) {
        return 1.0F;
      }
      return speed + (efficiencyScale() - 1.0F) * efficiencyBonus(stack);
    }
    return speed;
  }

  @Override
  public boolean isCorrectToolForDrops(BlockState state) {
    for (TagKey<Block> tag : mineableTags) {
      if (state.is(tag)) {
        return TierSortingRegistry.isCorrectTierForDrops(material, state);
      }
    }
    return false;
  }

  @Override
  public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
    getEnergy(stack).consumeEnergy(getHurtEnergyCost(), false);
    return true;
  }

  @Override
  public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity owner) {
    if (!level.isClientSide() && state.getDestroySpeed(level, pos) != 0.0F) {
      getEnergy(stack).consumeEnergy(getMineCost(), false);
    }
    return true;
  }

  @Override
  public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
    return disableBlockingSeconds > 0;
  }

  public boolean animatesWhileWorking() {
    return false;
  }

  protected Tier tier() {
    return material;
  }

  public boolean minesVeins() {
    return false;
  }

  public TagKey<Block> veinFamily() {
    return com.faktocraft.common.item.impl.tools.VeinMining.ORES;
  }

  protected String veinTooltipKey() {
    return "tooltip.faktocraft.vein_mining";
  }

  @Override
  public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level level,
      List<net.minecraft.network.chat.Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
    super.appendHoverText(stack, level, tooltip, flag);
    if (minesVeins() && com.faktocraft.common.config.BasicConfig.veinMiningEnabled()) {
      tooltip.add(net.minecraft.network.chat.Component.translatable(veinTooltipKey(),
          com.faktocraft.common.item.impl.tools.VeinMining.keyName(),
          com.faktocraft.common.config.BasicConfig.veinMiningMaxBlocks(),
          com.faktocraft.common.config.BasicConfig.veinMiningEnergyMultiplier())
          .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
  }

  @Override
  public void initializeClient(
      java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
    if (!animatesWhileWorking()) {
      return;
    }
    consumer.accept(new net.minecraftforge.client.extensions.common.IClientItemExtensions() {
      @Override
      public boolean applyForgeHandTransform(com.mojang.blaze3d.vertex.PoseStack poseStack,
          net.minecraft.client.player.LocalPlayer player, net.minecraft.world.entity.HumanoidArm arm,
          ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
        return com.faktocraft.client.render.ToolAimAnimation.apply(poseStack, player, arm, itemInHand,
            partialTick, equipProcess);
      }
    });
  }

  @Override
  public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
    if (!animatesWhileWorking() || !entity.level().isClientSide()) {
      return false;
    }
    DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
        () -> () -> com.faktocraft.client.ToolWorkAnimation.markSwing(entity));
    return true;
  }
}

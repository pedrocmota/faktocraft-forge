package com.faktocraft.common.item.base;

import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.List;

public class DiggerElectricItem extends ElectricItem {
  private final ToolMaterial material;
  private final List<TagKey<Block>> mineableTags;
  private final float disableBlockingSeconds;

  public DiggerElectricItem(ToolMaterial material, float attackDamage, float attackSpeed,
      List<TagKey<Block>> mineableTags,
      Properties properties, int energyStored, int maxEnergy, EnergyType energyType, EnergyTier energyTier) {
    this(material, attackDamage, attackSpeed, 0.0F, mineableTags, properties, energyStored, maxEnergy, energyType,
        energyTier);
  }

  public DiggerElectricItem(ToolMaterial material, float attackDamage, float attackSpeed,
      float disableBlockingSeconds, List<TagKey<Block>> mineableTags, Properties properties,
      int energyStored, int maxEnergy, EnergyType energyType, EnergyTier energyTier) {
    super(toolProperties(properties, material, attackDamage, attackSpeed, disableBlockingSeconds, mineableTags),
        energyStored, maxEnergy, energyType, energyTier);
    this.material = material;
    this.mineableTags = List.copyOf(mineableTags);
    this.disableBlockingSeconds = disableBlockingSeconds;
  }

  private static Properties toolProperties(Properties properties, ToolMaterial material, float attackDamage,
      float attackSpeed, float disableBlockingSeconds, List<TagKey<Block>> mineableTags) {
    HolderGetter<Block> blocks = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
    List<Tool.Rule> rules = new ArrayList<>();
    rules.add(Tool.Rule.deniesDrops(blocks.getOrThrow(material.incorrectBlocksForDrops())));
    for (TagKey<Block> tag : mineableTags) {
      rules.add(Tool.Rule.minesAndDrops(blocks.getOrThrow(tag), material.speed()));
    }
    return properties.enchantable(material.enchantmentValue())
        .component(DataComponents.TOOL, new Tool(List.copyOf(rules), 1.0F, 0, true))
        .attributes(ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(BASE_ATTACK_DAMAGE_ID, attackDamage + material.attackDamageBonus(),
                    AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED,
                new AttributeModifier(BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND)
            .build())
        .component(DataComponents.WEAPON, new Weapon(0, disableBlockingSeconds));
  }

  public ToolMaterial getMaterial() {
    return material;
  }

  public int getHurtEnergyCost() {
    return 100;
  }

  public int getMineCost() {
    return 50;
  }

  protected float getBaseDestroySpeed(BlockState state) {
    for (TagKey<Block> tag : mineableTags) {
      if (state.is(tag)) {
        return material.speed();
      }
    }
    return 1.0F;
  }

  protected float efficiencyScale() {
    return 1.0F;
  }

  public static int efficiencyBonus(ItemStack stack) {
    for (var entry : stack.getTagEnchantments().entrySet()) {
      if (entry.getKey().is(Enchantments.EFFICIENCY)) {
        int level = entry.getIntValue();
        return level * level + 1;
      }
    }
    return 0;
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
  public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
    for (TagKey<Block> tag : mineableTags) {
      if (state.is(tag)) {
        return !state.is(material.incorrectBlocksForDrops());
      }
    }
    return false;
  }

  @Override
  public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
    getEnergy(stack).consumeEnergy(getHurtEnergyCost(), false);
  }

  @Override
  public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity owner) {
    if (!level.isClientSide() && state.getDestroySpeed(level, pos) != 0.0F) {
      getEnergy(stack).consumeEnergy(getMineCost(), false);
    }
    return true;
  }

  public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
    return disableBlockingSeconds > 0;
  }

  public boolean animatesWhileWorking() {
    return false;
  }

  protected ToolMaterial tier() {
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
  public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context,
      net.minecraft.world.item.component.TooltipDisplay display,
      java.util.function.Consumer<net.minecraft.network.chat.Component> tooltip,
      net.minecraft.world.item.TooltipFlag flag) {
    super.appendHoverText(stack, context, display, tooltip, flag);
    if (minesVeins() && com.faktocraft.common.config.BasicConfig.veinMiningEnabled()) {
      tooltip.accept(net.minecraft.network.chat.Component.translatable(veinTooltipKey(),
          com.faktocraft.common.item.impl.tools.VeinMining.keyName(),
          com.faktocraft.common.config.BasicConfig.veinMiningMaxBlocks(),
          com.faktocraft.common.config.BasicConfig.veinMiningEnergyMultiplier())
          .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
  }

  @Override
  public boolean onEntitySwing(ItemStack stack, LivingEntity entity, net.minecraft.world.InteractionHand hand) {
    if (!animatesWhileWorking() || !entity.level().isClientSide()) {
      return false;
    }
    if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient()) {
      com.faktocraft.client.ToolWorkAnimation.markSwing(entity);
    }
    return true;
  }
}

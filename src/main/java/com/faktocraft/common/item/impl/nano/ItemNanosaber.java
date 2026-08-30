package com.faktocraft.common.item.impl.nano;

import com.faktocraft.common.energy.interfaces.IEnergy;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.item.base.SwordElectricItem;
import com.faktocraft.common.registries.ModComponents;
import com.google.common.collect.Multimap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;

public class ItemNanosaber extends SwordElectricItem {

  private static final float ACTIVE_DAMAGE = 19.0F;

  public ItemNanosaber(Properties properties) {
    super(Tiers.DIAMOND, 1, -3.0F, properties, 0, 160000, EnergyType.RECEIVE, EnergyTier.HIGH);
  }

  @Override
  public void inventoryTick(ItemStack stack, Level level, Entity owner, int slotId, boolean isSelected) {
    if (level.isClientSide()) {
      return;
    }
    if (level.getGameTime() % 20 == 0) {
      IEnergy energy = getEnergy(stack);
      if (isActive(stack)) {
        energy.consumeEnergy(200, false);
      }
      if (energy.energyStored() == 0 && isActive(stack)) {
        setActive(stack, false);
        level.playSound(null, owner.getX(), owner.getY(), owner.getZ(),
            com.faktocraft.common.registries.ModSounds.NANO_SABER_RETRACT,
            net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 1.0F);
      }
    }
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    if (hand == InteractionHand.MAIN_HAND) {
      ItemStack stack = player.getItemInHand(hand);
      if (getEnergy(stack).energyStored() > 0) {
        boolean nowActive = !isActive(stack);
        setActive(stack, nowActive);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            nowActive ? com.faktocraft.common.registries.ModSounds.NANO_SABER_IGNITE
                : com.faktocraft.common.registries.ModSounds.NANO_SABER_RETRACT,
            net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 1.0F);
        return InteractionResultHolder.consume(stack);
      }
    }
    return super.use(level, player, hand);
  }

  public static boolean isActive(ItemStack stack) {
    return ModComponents.getActive(stack, false);
  }

  private void setActive(ItemStack stack, boolean active) {
    ModComponents.setActive(stack, active);
  }

  @Override
  public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
    if (slot == EquipmentSlot.MAINHAND) {
      return createSwordAttributes(isActive(stack) ? ACTIVE_DAMAGE : getDamage(), getAttackSpeed());
    }
    return super.getAttributeModifiers(slot, stack);
  }
}

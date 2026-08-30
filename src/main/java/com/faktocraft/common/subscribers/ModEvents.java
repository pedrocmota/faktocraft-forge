package com.faktocraft.common.subscribers;

import com.faktocraft.IndReb;
import com.faktocraft.common.energy.interfaces.IEnergy;
import com.faktocraft.common.interfaces.item.IElectricItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = IndReb.MODID)
public class ModEvents {

  @SubscribeEvent
  public static void onLevelTick(TickEvent.LevelTickEvent event) {
    if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) {
      return;
    }
    if (level.getGameTime() % 20 != 0) {
      return;
    }
    for (ServerPlayer player : level.players()) {
      com.faktocraft.common.util.NightVisionHandler.check(player);
    }
  }

  @SubscribeEvent
  public static void onLevelUnload(net.minecraftforge.event.level.LevelEvent.Unload event) {
    if (event.getLevel() instanceof net.minecraft.world.level.Level level && level.isClientSide()) {
      com.faktocraft.common.energy.provider.EnergyCore.removeClientCore(level);
    }
  }

  @SubscribeEvent
  public static void onLivingHurt(LivingHurtEvent event) {
    if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
      return;
    }
    for (ItemStack stack : com.faktocraft.common.util.NightVisionHandler.armorItems(player)) {
      if (!stack.isEmpty() && stack.getItem() instanceof IElectricItem electricItem) {
        IEnergy energy = electricItem.getEnergy(stack);
        if (energy != null) {
          energy.consumeEnergy((int) event.getAmount() * 500, false);
          electricItem.tickElectric(stack);
        }
      }
    }
  }

  @SubscribeEvent
  public static void onLivingFall(LivingFallEvent event) {
    if (event.getEntity() instanceof Player player
        && (com.faktocraft.common.item.impl.armor.HazmatArmorItem.hasBoots(player)
            || player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET)
                .getItem() instanceof com.faktocraft.common.item.impl.nano.ItemNanoArmor)) {
      event.setDistance(event.getDistance() / 3.3F);
    }
  }

  @SubscribeEvent
  public static void onLivingAttack(net.minecraftforge.event.entity.living.LivingAttackEvent event) {
    if (event.getEntity().level().isClientSide()
        || !event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
      return;
    }
    var living = event.getEntity();
    if (com.faktocraft.common.item.impl.armor.HazmatArmorItem.isFullSuit(living)) {
      event.setCanceled(true);
      if (living.getRandom().nextBoolean()) {
        com.faktocraft.common.item.impl.armor.HazmatArmorItem.wearFullSuit(living, 1);
      }
      return;
    }
    if (com.faktocraft.common.item.impl.nano.ItemNanoArmor.tryUseFullSuit(living,
        com.faktocraft.common.item.impl.nano.ItemNanoArmor.FIRE_COST)) {
      event.setCanceled(true);
      living.clearFire();
    }
  }
}

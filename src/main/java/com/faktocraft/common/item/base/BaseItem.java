package com.faktocraft.common.item.base;

import com.faktocraft.common.util.NbtBridge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class BaseItem extends Item {

  public BaseItem(Properties properties) {
    super(properties);
  }

  @Override
  public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
    NbtBridge.migrateLegacyComponents(stack);
  }
}

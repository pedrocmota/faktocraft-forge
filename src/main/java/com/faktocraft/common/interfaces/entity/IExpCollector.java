package com.faktocraft.common.interfaces.entity;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;

public interface IExpCollector {

  default boolean hasExpButton() {
    return true;
  }

  float getExperience(Recipe<?> recipe);

  float getStoredExperience();

  void collectExp(Player player);

  Runnable collectExp();
}

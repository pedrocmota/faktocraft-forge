package com.faktocraft.common.item.impl;

import com.faktocraft.common.item.base.BaseItem;
import com.faktocraft.common.recipe.impl.ScrapBoxRecipe;
import com.faktocraft.common.registries.ModRecipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

public class ScrapBox extends BaseItem {

  public ScrapBox(Properties properties) {
    super(properties);
  }

  @Override
  public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
    return 1800;
  }

  public static ItemStack openScrap(Level level) {
    return ScrapBoxRecipe.rollDrop(
        level.getRecipeManager().getAllRecipesFor(ModRecipeType.SCRAP_BOX), level.getRandom());
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    if (!level.isClientSide()) {
      ItemStack drop = openScrap(level);
      if (!drop.isEmpty()) {
        ItemEntity item = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), drop);
        level.addFreshEntity(item);
      }
      player.getItemInHand(hand).shrink(1);
    }
    return InteractionResultHolder.success(player.getItemInHand(hand));
  }

  private static final DispenseItemBehavior SCRAP_BOX_DISPENSE_BEHAVIOR = (BlockSource source, ItemStack stack) -> {
    Direction face = source.getBlockState().getValue(DispenserBlock.FACING);
    Level level = source.getLevel();
    BlockPos pos = source.getPos().relative(face);

    ItemStack dropStack = openScrap(level);
    if (!dropStack.isEmpty()) {
      ItemEntity item = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), dropStack);
      level.addFreshEntity(item);
    }
    level.playSound(null, pos, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS, 1.0F, 1.0F);

    stack.shrink(1);
    source.getLevel().gameEvent(null, GameEvent.ENTITY_PLACE, source.getPos());
    return stack;
  };

  public static void registerDispenseBehavior(ScrapBox item) {
    DispenserBlock.registerBehavior(item, SCRAP_BOX_DISPENSE_BEHAVIOR);
  }
}

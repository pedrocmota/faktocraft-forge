package com.faktocraft.common.item.impl.tools;

import com.faktocraft.common.energy.interfaces.IEnergy;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.item.base.DiggerElectricItem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ElectricHoe extends DiggerElectricItem {

  protected static final Map<Block, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>> TILLABLES = Map.of(
      Blocks.GRASS_BLOCK, Pair.of(ElectricHoe::onlyIfAirAbove, changeIntoState(Blocks.FARMLAND.defaultBlockState())),
      Blocks.DIRT_PATH, Pair.of(ElectricHoe::onlyIfAirAbove, changeIntoState(Blocks.FARMLAND.defaultBlockState())),
      Blocks.DIRT, Pair.of(ElectricHoe::onlyIfAirAbove, changeIntoState(Blocks.FARMLAND.defaultBlockState())),
      Blocks.COARSE_DIRT, Pair.of(ElectricHoe::onlyIfAirAbove, changeIntoState(Blocks.DIRT.defaultBlockState())),
      Blocks.ROOTED_DIRT,
      Pair.of(context -> true, changeIntoStateAndDropItem(Blocks.DIRT.defaultBlockState(), Items.HANGING_ROOTS)));

  private final int energyCostMine;
  private final int energyCostHurt;
  private final int energyCostTill;

  public ElectricHoe(Tier material, float attackDamage, float attackSpeed, Properties properties,
      int energyStored, int maxEnergy, int energyCostMine, int energyCostHurt, int energyCostTill,
      EnergyType energyType, EnergyTier energyTier) {
    super(material, attackDamage, attackSpeed, List.of(BlockTags.MINEABLE_WITH_HOE),
        properties, energyStored, maxEnergy, energyType, energyTier);
    this.energyCostMine = energyCostMine;
    this.energyCostHurt = energyCostHurt;
    this.energyCostTill = energyCostTill;
  }

  @Override
  public int getHurtEnergyCost() {
    return energyCostHurt;
  }

  @Override
  public int getMineCost() {
    return energyCostMine;
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    ItemStack stack = context.getItemInHand();
    IEnergy energy = getEnergy(stack);
    if (energy.energyStored() <= 1) {
      return InteractionResult.PASS;
    }

    Level level = context.getLevel();
    BlockPos pos = context.getClickedPos();
    Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> pair = TILLABLES.get(level.getBlockState(pos).getBlock());
    if (pair == null) {
      return InteractionResult.PASS;
    }

    if (pair.getFirst().test(context)) {
      Player player = context.getPlayer();
      level.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
      if (!level.isClientSide()) {
        pair.getSecond().accept(context);
        if (player != null) {
          energy.consumeEnergy(energyCostTill, false);
        }
      }
      return InteractionResult.SUCCESS;
    }
    return InteractionResult.PASS;
  }

  public static Consumer<UseOnContext> changeIntoState(BlockState state) {
    return context -> {
      context.getLevel().setBlock(context.getClickedPos(), state, 11);
      context.getLevel().gameEvent(GameEvent.BLOCK_CHANGE, context.getClickedPos(),
          GameEvent.Context.of(context.getPlayer(), state));
    };
  }

  public static Consumer<UseOnContext> changeIntoStateAndDropItem(BlockState state, ItemLike drop) {
    return context -> {
      context.getLevel().setBlock(context.getClickedPos(), state, 11);
      context.getLevel().gameEvent(GameEvent.BLOCK_CHANGE, context.getClickedPos(),
          GameEvent.Context.of(context.getPlayer(), state));
      Block.popResourceFromFace(context.getLevel(), context.getClickedPos(), context.getClickedFace(),
          new ItemStack(drop));
    };
  }

  public static boolean onlyIfAirAbove(UseOnContext context) {
    return context.getClickedFace() != Direction.DOWN
        && context.getLevel().getBlockState(context.getClickedPos().above()).isAir();
  }
}

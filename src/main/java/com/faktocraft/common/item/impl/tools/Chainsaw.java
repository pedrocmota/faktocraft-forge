package com.faktocraft.common.item.impl.tools;

import net.neoforged.neoforge.common.DataMapHooks;
import com.google.common.collect.ImmutableMap;
import com.faktocraft.common.energy.interfaces.IEnergy;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.item.base.DiggerElectricItem;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Chainsaw extends DiggerElectricItem {

  private static final Map<Block, Block> STRIPPABLES = new ImmutableMap.Builder<Block, Block>()
      .put(Blocks.OAK_WOOD, Blocks.STRIPPED_OAK_WOOD)
      .put(Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG)
      .put(Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_WOOD)
      .put(Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG)
      .put(Blocks.ACACIA_WOOD, Blocks.STRIPPED_ACACIA_WOOD)
      .put(Blocks.ACACIA_LOG, Blocks.STRIPPED_ACACIA_LOG)
      .put(Blocks.CHERRY_WOOD, Blocks.STRIPPED_CHERRY_WOOD)
      .put(Blocks.CHERRY_LOG, Blocks.STRIPPED_CHERRY_LOG)
      .put(Blocks.BIRCH_WOOD, Blocks.STRIPPED_BIRCH_WOOD)
      .put(Blocks.BIRCH_LOG, Blocks.STRIPPED_BIRCH_LOG)
      .put(Blocks.JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_WOOD)
      .put(Blocks.JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_LOG)
      .put(Blocks.SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_WOOD)
      .put(Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG)
      .put(Blocks.WARPED_STEM, Blocks.STRIPPED_WARPED_STEM)
      .put(Blocks.WARPED_HYPHAE, Blocks.STRIPPED_WARPED_HYPHAE)
      .put(Blocks.CRIMSON_STEM, Blocks.STRIPPED_CRIMSON_STEM)
      .put(Blocks.CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_HYPHAE)
      .put(Blocks.MANGROVE_WOOD, Blocks.STRIPPED_MANGROVE_WOOD)
      .put(Blocks.MANGROVE_LOG, Blocks.STRIPPED_MANGROVE_LOG)
      .put(Blocks.BAMBOO_BLOCK, Blocks.STRIPPED_BAMBOO_BLOCK)
      .build();

  private final int energyCostMine;
  private final int energyCostHurt;
  private final float efficiencyScale;

  public Chainsaw(ToolMaterial material, float efficiencyScale, float attackDamage, float attackSpeed,
      Properties properties, int energyStored, int maxEnergy, int energyCostMine, int energyCostHurt,
      EnergyType energyType, EnergyTier energyTier) {
    super(material, attackDamage, attackSpeed, 5.0F, List.of(BlockTags.MINEABLE_WITH_AXE),
        properties, energyStored, maxEnergy, energyType, energyTier);
    this.energyCostMine = energyCostMine;
    this.energyCostHurt = energyCostHurt;
    this.efficiencyScale = efficiencyScale;
  }

  @Override
  protected float efficiencyScale() {
    return efficiencyScale;
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
  public boolean animatesWhileWorking() {
    return true;
  }

  @Override
  public boolean minesVeins() {
    return tier() == com.faktocraft.common.registries.ModTiers.IRIDIUM_TOOL;
  }

  @Override
  public net.minecraft.tags.TagKey<Block> veinFamily() {
    return VeinMining.LOGS;
  }

  @Override
  protected String veinTooltipKey() {
    return "tooltip.faktocraft.vein_chopping";
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    ItemStack stack = context.getItemInHand();
    IEnergy energy = getEnergy(stack);
    if (energy.energyStored() < 20) {
      return InteractionResult.PASS;
    }

    Level level = context.getLevel();
    BlockPos pos = context.getClickedPos();
    Player player = context.getPlayer();
    BlockState state = level.getBlockState(pos);

    Optional<BlockState> modified = Optional.empty();

    Block stripped = STRIPPABLES.get(state.getBlock());
    if (stripped != null) {
      BlockState strippedState = stripped.defaultBlockState();
      if (state.hasProperty(RotatedPillarBlock.AXIS) && strippedState.hasProperty(RotatedPillarBlock.AXIS)) {
        strippedState = strippedState.setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
      }
      level.playSound(player, pos, SoundEvents.AXE_STRIP.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
      modified = Optional.of(strippedState);
    } else {
      Optional<BlockState> scraped = WeatheringCopper.getPrevious(state);
      if (scraped.isPresent()) {
        level.playSound(player, pos, SoundEvents.AXE_SCRAPE.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
        level.levelEvent(player, 3005, pos, 0);
        modified = scraped;
      } else {
        Optional<BlockState> waxedOff = Optional.ofNullable(DataMapHooks.getBlockUnwaxed(state.getBlock()))
            .map(block -> block.withPropertiesOf(state));
        if (waxedOff.isPresent()) {
          level.playSound(player, pos, SoundEvents.AXE_WAX_OFF.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
          level.levelEvent(player, 3004, pos, 0);
          modified = waxedOff;
        }
      }
    }

    if (modified.isPresent()) {
      if (player instanceof ServerPlayer serverPlayer) {
        CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
      }
      level.setBlock(pos, modified.get(), 11);
      level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, modified.get()));
      if (player != null) {
        energy.consumeEnergy(20, false);
      }
      return InteractionResult.SUCCESS;
    }

    return InteractionResult.PASS;
  }
}

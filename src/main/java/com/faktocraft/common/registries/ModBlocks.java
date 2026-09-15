package com.faktocraft.common.registries;

import com.faktocraft.common.block.BlockOre;
import com.faktocraft.common.block.BlockResource;
import com.faktocraft.common.block.BlockRubberCarpet;
import com.faktocraft.common.block.impl.machines.nuclear_reactor.BlockAdvancedMachineCasing;
import com.faktocraft.common.block.impl.machines.nuclear_reactor.BlockNuclearReactor;
import com.faktocraft.common.block.impl.nuke.BlockNuke;
import com.faktocraft.common.block.BlockSheet;
import com.faktocraft.common.block.impl.BlockIronFence;
import com.faktocraft.common.block.impl.BlockIronScaffolding;
import com.faktocraft.common.block.impl.cable.BlockCable;
import com.faktocraft.common.block.impl.cf.BlockReinforcedGlass;
import com.faktocraft.common.block.impl.cf.BlockReinforcedStone;
import com.faktocraft.common.block.impl.cf.BlockReinforcedStoneDoor;
import com.faktocraft.common.block.impl.cf.BlockReinforcedStoneSlab;
import com.faktocraft.common.block.impl.cf.BlockReinforcedStoneStairs;
import com.faktocraft.common.block.impl.luminator.BlockLuminator;
import com.faktocraft.common.block.impl.rubber_wood.RubberLeaves;
import com.faktocraft.common.block.impl.rubber_wood.RubberLog;
import com.faktocraft.common.block.impl.rubber_wood.RubberPlanks;
import com.faktocraft.common.block.impl.rubber_wood.RubberSapling;
import com.faktocraft.common.block.impl.rubber_wood.RubberSlab;
import com.faktocraft.common.block.impl.rubber_wood.RubberStairs;
import com.faktocraft.common.block.impl.rubber_wood.RubberWood;
import com.faktocraft.common.tier.CableTier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

public class ModBlocks {

  private static final Map<Block, String> NAMES = new IdentityHashMap<>();

  public static final Block TIN_ORE = register("tin_ore", BlockOre::new, BlockOre.oreProperties());
  public static final Block DEEPSLATE_TIN_ORE = register("deepslate_tin_ore", BlockOre::new, BlockOre.oreProperties());
  public static final Block LEAD_ORE = register("lead_ore", BlockOre::new, BlockOre.oreProperties());
  public static final Block DEEPSLATE_LEAD_ORE = register("deepslate_lead_ore", BlockOre::new,
      BlockOre.oreProperties());
  public static final Block DEEPSLATE_URANIUM_ORE = register("deepslate_uranium_ore", BlockOre::new,
      BlockOre.oreProperties());
  public static final Block URANIUM_ORE = register("uranium_ore", BlockOre::new, BlockOre.oreProperties());
  public static final Block SILVER_ORE = register("silver_ore", BlockOre::new, BlockOre.oreProperties());
  public static final Block SULFUR_ORE = register("sulfur_ore", BlockOre::new, BlockOre.oreProperties());
  public static final Block DEEPSLATE_SULFUR_ORE = register("deepslate_sulfur_ore", BlockOre::new,
      BlockOre.oreProperties());
  public static final Block DEEPSLATE_SILVER_ORE = register("deepslate_silver_ore", BlockOre::new,
      BlockOre.oreProperties());
  public static final Block LITHIUM_ORE = register("lithium_ore", BlockOre::new, BlockOre.oreProperties());
  public static final Block DEEPSLATE_LITHIUM_ORE = register("deepslate_lithium_ore", BlockOre::new,
      BlockOre.oreProperties());
  public static final Block IRIDIUM_ORE = register("iridium_ore",
      com.faktocraft.common.block.BlockIridiumOre::new, BlockOre.oreProperties());
  public static final Block DEEPSLATE_IRIDIUM_ORE = register("deepslate_iridium_ore",
      com.faktocraft.common.block.BlockIridiumOre::new,
      BlockOre.oreProperties());

  public static final Block TIN_CABLE = registerCable("tin_cable", 0.127F, CableTier.TIN_CABLE);
  public static final Block TIN_CABLE_INSULATED = registerCable("tin_cable_insulated", 0.189F,
      CableTier.TIN_CABLE_INSULATED);
  public static final Block COPPER_CABLE = registerCable("copper_cable", 0.127F, CableTier.COPPER_CABLE);
  public static final Block COPPER_CABLE_INSULATED = registerCable("copper_cable_insulated", 0.189F,
      CableTier.COPPER_CABLE_INSULATED);
  public static final Block GOLD_CABLE = registerCable("gold_cable", 0.127F, CableTier.GOLD_CABLE);
  public static final Block GOLD_CABLE_INSULATED = registerCable("gold_cable_insulated", 0.189F,
      CableTier.GOLD_CABLE_INSULATED);
  public static final Block HV_CABLE = registerCable("hv_cable", 0.189F, CableTier.HV_CABLE);
  public static final Block HV_CABLE_INSULATED = registerCable("hv_cable_insulated", 0.313F,
      CableTier.HV_CABLE_INSULATED);
  public static final Block GLASS_FIBRE_CABLE = registerCable("glass_fibre_cable", 0.127F, CableTier.GLASS_FIBRE_CABLE);
  public static final Block HANDLE_GUARD = register("handle_guard",
      com.faktocraft.common.block.impl.BlockHandleGuard::new,
      net.minecraft.world.level.block.state.BlockBehaviour.Properties.of());
  public static final Block CIRCUIT_BREAKER = register("circuit_breaker",
      com.faktocraft.common.block.impl.cable.BlockBreaker::new,
      net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
          .mapColor(net.minecraft.world.level.material.MapColor.METAL)
          .strength(2.0F, 8.0F)
          .sound(net.minecraft.world.level.block.SoundType.METAL)
          .requiresCorrectToolForDrops());

  public static final Block TIN_BLOCK = register("tin_block", BlockResource::new,
      BlockResource.resourceProperties(1F, 3F));
  public static final Block SILVER_BLOCK = register("silver_block", BlockResource::new,
      BlockResource.resourceProperties(1F, 3F));
  public static final Block STEEL_BLOCK = register("steel_block", BlockResource::new,
      BlockResource.resourceProperties(1F, 3F));
  public static final Block BRONZE_BLOCK = register("bronze_block", BlockResource::new,
      BlockResource.resourceProperties(1F, 3F));
  public static final Block LEAD_BLOCK = register("lead_block", BlockResource::new,
      BlockResource.resourceProperties(1F, 3F));
  public static final Block BASIC_MACHINE_CASING = register("basic_machine_casing", BlockResource::new,
      BlockResource.resourceProperties(1F, 3F));
  public static final Block ADVANCED_MACHINE_CASING = register("advanced_machine_casing",
      BlockAdvancedMachineCasing::new, BlockResource.resourceProperties(1F, 3F));
  public static final Block NUCLEAR_REACTOR = register("nuclear_reactor", BlockNuclearReactor::new,
      BlockNuclearReactor.reactorProperties());
  public static final Block NUKE = register("nuke", BlockNuke::new, BlockNuke.nukeProperties());

  public static final Block PLASTIC_BLOCK = register("plastic_block", BlockResource::new,
      BlockResource.resourceProperties(1F, 3F));

  public static final Block RESIN_SHEET = register("resin_sheet", BlockSheet::new, BlockSheet.sheetProperties(0.7F));
  public static final Block RUBBER_CARPET = register("rubber_carpet", BlockRubberCarpet::new,
      BlockRubberCarpet.carpetProperties());
  public static final Block RUBBER_BLOCK = register("rubber_block", Block::new,
      BlockRubberCarpet.blockProperties());

  public static final Block TELEPORT_ANCHOR = register("teleport_anchor",
      com.faktocraft.common.block.impl.teleport_anchor.BlockTeleportAnchor::new,
      com.faktocraft.common.block.BlockMachine.machineProperties());
  public static final Block DRILLED_BLOCK = register("drilled_block",
      com.faktocraft.common.cover.BlockDrilled::new, com.faktocraft.common.cover.BlockDrilled.drilledProperties());
  public static final Block DIMENSIONAL_TELEPORT_ANCHOR = register("dimensional_teleport_anchor",
      properties -> new com.faktocraft.common.block.impl.teleport_anchor.BlockTeleportAnchor(properties, true),
      com.faktocraft.common.block.BlockMachine.machineProperties());

  public static final Block RUBBER_LOG = register("rubber_log", RubberLog::new, RubberLog.logProperties());
  public static final Block RUBBER_WOOD = register("rubber_wood", RubberWood::new, RubberWood.woodProperties());
  public static final Block RUBBER_LEAVES = register("rubber_leaves", RubberLeaves::new,
      RubberLeaves.leavesProperties());
  public static final Block RUBBER_PLANKS = register("rubber_planks", RubberPlanks::new,
      RubberPlanks.planksProperties());
  public static final Block DRIED_RUBBER_LOG = register("dried_rubber_log",
      net.minecraft.world.level.block.RotatedPillarBlock::new, RubberLog.logProperties());
  public static final Block RUBBER_SAPLING = register("rubber_sapling", RubberSapling::new,
      RubberSapling.saplingProperties());
  public static final Block RUBBER_STAIRS = register("rubber_stairs", RubberStairs::new,
      RubberStairs.stairsProperties());
  public static final Block RUBBER_SLAB = register("rubber_slab", RubberSlab::new, RubberSlab.slabProperties());

  public static final Block REINFORCED_GLASS = register("reinforced_glass", BlockReinforcedGlass::new,
      BlockReinforcedGlass.reinforcedGlassProperties());
  public static final Block REINFORCED_STONE = register("reinforced_stone", BlockReinforcedStone::new,
      BlockReinforcedStone.reinforcedStoneProperties());
  public static final Block REINFORCED_STONE_SLAB = register("reinforced_stone_slab", BlockReinforcedStoneSlab::new,
      BlockReinforcedStoneSlab.reinforcedStoneSlabProperties());
  public static final Block REINFORCED_STONE_STAIRS = register("reinforced_stone_stairs",
      BlockReinforcedStoneStairs::new, BlockReinforcedStoneStairs.reinforcedStoneStairsProperties());
  public static final Block REINFORCED_STONE_DOOR = register("reinforced_stone_door",
      BlockReinforcedStoneDoor::new, BlockReinforcedStoneDoor.reinforcedStoneDoorProperties());

  public static final Block IRON_SCAFFOLDING = register("iron_scaffolding", BlockIronScaffolding::new,
      BlockIronScaffolding.scaffoldingProperties());
  public static final Block IRON_FENCE = register("iron_fence", BlockIronFence::new, BlockIronFence.fenceProperties());

  public static final Block LUMINATOR = register("luminator", BlockLuminator::new,
      BlockLuminator.luminatorProperties());

  public static String nameOf(Block block) {
    String name = NAMES.get(block);
    if (name == null) {
      throw new IllegalArgumentException("Block was not registered through ModBlocks.register: " + block);
    }
    return name;
  }

  public static Block register(String name, Function<BlockBehaviour.Properties, Block> factory,
      BlockBehaviour.Properties properties) {
    Block block = factory.apply(properties);
    NAMES.put(block, name);
    return RegistrationHandler.block(name, block);
  }

  private static Block registerCable(String name, float apothem, CableTier tier) {
    return register(name, p -> new BlockCable(apothem, tier, p), tier.createProperties());
  }

  public static void register() {
  }
}

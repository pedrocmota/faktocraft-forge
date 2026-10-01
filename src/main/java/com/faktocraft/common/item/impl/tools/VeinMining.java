package com.faktocraft.common.item.impl.tools;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.config.BasicConfig;
import com.faktocraft.common.energy.interfaces.IEnergy;
import com.faktocraft.common.item.base.DiggerElectricItem;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.level.BlockEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class VeinMining {

  public static final TagKey<Block> ORES = BlockTags.create(new ResourceLocation(Faktocraft.MODID, "vein_ores"));
  public static final TagKey<Block> LOGS = BlockTags.create(new ResourceLocation(Faktocraft.MODID, "vein_logs"));
  private static final String ORE_GROUP_NAMESPACE = "forge";
  private static final String ORE_GROUP_PREFIX = "ores/";
  private static final float MINE_EXHAUSTION = 0.005F;

  private static final Set<UUID> ACTIVE = new HashSet<>();
  private static boolean breaking;

  private VeinMining() {
  }

  public static String keyName() {
    return net.minecraftforge.fml.loading.FMLEnvironment.dist.isClient()
        ? com.faktocraft.client.VeinMiningKey.keyName()
        : "Alt";
  }

  public static void setActive(Player player, boolean active) {
    if (active) {
      ACTIVE.add(player.getUUID());
    } else {
      ACTIVE.remove(player.getUUID());
    }
  }

  public static void forget(Player player) {
    ACTIVE.remove(player.getUUID());
  }

  public static boolean isActive(Player player) {
    return ACTIVE.contains(player.getUUID());
  }

  public static void onBreak(BlockEvent.BreakEvent event) {
    if (breaking || !BasicConfig.veinMiningEnabled()) {
      return;
    }
    if (!(event.getPlayer() instanceof ServerPlayer player) || !(event.getLevel() instanceof ServerLevel level)) {
      return;
    }
    if (!isActive(player)) {
      return;
    }
    ItemStack stack = player.getMainHandItem();
    if (!(stack.getItem() instanceof DiggerElectricItem tool) || !tool.minesVeins()) {
      return;
    }
    BlockState origin = event.getState();
    TagKey<Block> family = tool.veinFamily();
    if (!origin.is(family) || !tool.isCorrectToolForDrops(origin)) {
      return;
    }
    List<BlockPos> vein = collect(level, event.getPos(), origin, family, BasicConfig.veinMiningMaxBlocks());
    event.setCanceled(true);
    breaking = true;
    try {
      breakVein(player, level, stack, tool, event.getPos(), event.getExpToDrop(), vein);
    } finally {
      breaking = false;
    }
  }

  private static void breakVein(ServerPlayer player, ServerLevel level, ItemStack stack, DiggerElectricItem tool,
      BlockPos originPos, int originExp, List<BlockPos> vein) {
    boolean creative = player.isCreative();
    IEnergy energy = tool.getEnergy(stack);
    int base = tool.getMineCost();
    int extra = Math.max(0, BasicConfig.veinMiningEnergyMultiplier() - 1) * base;
    GameType gameMode = player.gameMode.getGameModeForPlayer();
    breakOne(player, level, stack, originPos, originExp, creative);
    for (BlockPos pos : vein) {
      if (!creative && energy.consumeEnergy(base + extra, true) < base + extra) {
        return;
      }
      int exp = ForgeHooks.onBlockBreakEvent(level, gameMode, player, pos);
      if (exp < 0) {
        continue;
      }
      if (breakOne(player, level, stack, pos, exp, creative) && !creative) {
        energy.consumeEnergy(extra, false);
      }
    }
  }

  private static boolean breakOne(ServerPlayer player, ServerLevel level, ItemStack stack, BlockPos pos, int exp,
      boolean creative) {
    BlockState state = level.getBlockState(pos);
    if (state.isAir() || player.blockActionRestricted(level, pos, player.gameMode.getGameModeForPlayer())) {
      return false;
    }
    Block block = state.getBlock();
    BlockEntity blockEntity = level.getBlockEntity(pos);
    List<ItemStack> drops = creative ? List.of() : Block.getDrops(state, level, pos, blockEntity, player, stack);
    block.playerWillDestroy(level, pos, state, player);
    if (!level.removeBlock(pos, false)) {
      return false;
    }
    block.destroy(level, pos, state);
    if (creative) {
      return true;
    }
    stack.mineBlock(level, state, pos, player);
    player.awardStat(Stats.BLOCK_MINED.get(block));
    player.causeFoodExhaustion(MINE_EXHAUSTION);
    for (ItemStack drop : drops) {
      if (!player.getInventory().add(drop)) {
        player.drop(drop, false);
      }
    }
    if (exp > 0) {
      player.giveExperiencePoints(exp);
    }
    return true;
  }

  public static List<BlockPos> collect(LevelAccessor level, BlockPos origin, BlockState state, TagKey<Block> family,
      int limit) {
    List<BlockPos> found = new ArrayList<>();
    int extras = limit - 1;
    if (extras <= 0) {
      return found;
    }
    Set<TagKey<Block>> groups = oreGroups(state);
    Set<BlockPos> seen = new HashSet<>();
    ArrayDeque<BlockPos> queue = new ArrayDeque<>();
    seen.add(origin);
    queue.add(origin);
    while (!queue.isEmpty() && found.size() < extras) {
      BlockPos current = queue.poll();
      for (int dx = -1; dx <= 1 && found.size() < extras; dx++) {
        for (int dy = -1; dy <= 1 && found.size() < extras; dy++) {
          for (int dz = -1; dz <= 1 && found.size() < extras; dz++) {
            if (dx == 0 && dy == 0 && dz == 0) {
              continue;
            }
            BlockPos next = current.offset(dx, dy, dz);
            if (!seen.add(next) || !level.isAreaLoaded(next, 0)) {
              continue;
            }
            if (!matches(state, groups, level.getBlockState(next), family)) {
              continue;
            }
            found.add(next);
            queue.add(next);
          }
        }
      }
    }
    return found;
  }

  private static Set<TagKey<Block>> oreGroups(BlockState state) {
    return state.getTags()
        .filter(tag -> tag.location().getNamespace().equals(ORE_GROUP_NAMESPACE)
            && tag.location().getPath().startsWith(ORE_GROUP_PREFIX))
        .collect(Collectors.toSet());
  }

  private static boolean matches(BlockState origin, Set<TagKey<Block>> groups, BlockState other,
      TagKey<Block> family) {
    if (!other.is(family)) {
      return false;
    }
    if (other.is(origin.getBlock())) {
      return true;
    }
    return !groups.isEmpty() && other.getTags().anyMatch(groups::contains);
  }
}

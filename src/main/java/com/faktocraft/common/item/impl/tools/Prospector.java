package com.faktocraft.common.item.impl.tools;

import com.faktocraft.IndReb;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.item.base.ElectricItem;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class Prospector extends ElectricItem {

  public static final int SCAN_COST = 10000;
  public static final float FALSE_NEGATIVE_CHANCE = 0.08F;
  public static final int MAX_SAVED_CHUNKS = 96;
  public static final int SCAN_DURATION_TICKS = 160;
  public static final String TAG_SCANS = "Scans";
  public static final String TAG_JOB = "ScanJob";

  public Prospector(Properties properties) {
    super(properties.stacksTo(1), 0, 400000, EnergyType.RECEIVE, EnergyTier.HIGH);
  }

  public static String chunkKey(Level level, int chunkX, int chunkZ) {
    return level.dimension().location() + "|" + chunkX + "|" + chunkZ;
  }

  @Nullable
  public static CompoundTag getScan(ItemStack stack, String key) {
    if (stack.hasTag() && stack.getTag().contains(TAG_SCANS)) {
      CompoundTag scans = stack.getTag().getCompound(TAG_SCANS);
      if (scans.contains(key)) {
        return scans.getCompound(key);
      }
    }
    return null;
  }

  @Nullable
  public static CompoundTag getJob(ItemStack stack) {
    return stack.hasTag() && stack.getTag().contains(TAG_JOB) ? stack.getTag().getCompound(TAG_JOB) : null;
  }

  @Override
  public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity owner, int slotId,
      boolean isSelected) {
    super.inventoryTick(stack, level, owner, slotId, isSelected);
    if (level.isClientSide() || !(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
      return;
    }
    CompoundTag job = getJob(stack);
    if (job == null) {
      return;
    }
    int remaining = job.getInt("remaining") - 1;
    if (remaining > 0) {
      job.putInt("remaining", remaining);
      stack.getTag().put(TAG_JOB, job);
      return;
    }
    finishScan(serverLevel, stack, job);
    stack.getTag().remove(TAG_JOB);
    if (owner != null) {
      serverLevel.playSound(null, owner.blockPosition(), net.minecraft.sounds.SoundEvents.BEACON_POWER_SELECT,
          net.minecraft.sounds.SoundSource.PLAYERS, 0.6F, 1.7F);
    }
  }

  public static CompoundTag computeScanEntries(net.minecraft.server.level.ServerLevel level, int chunkX, int chunkZ) {
    net.minecraft.world.level.chunk.LevelChunk chunk = level.getChunk(chunkX, chunkZ);
    java.util.Map<String, Integer> found = new java.util.HashMap<>();
    net.minecraft.core.BlockPos.MutableBlockPos cursor = new net.minecraft.core.BlockPos.MutableBlockPos();
    int baseX = chunk.getPos().getMinBlockX();
    int baseZ = chunk.getPos().getMinBlockZ();
    for (int x = 0; x < 16; x++) {
      for (int z = 0; z < 16; z++) {
        for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
          cursor.set(baseX + x, y, baseZ + z);
          var state = chunk.getBlockState(cursor);
          if (state.isAir()) {
            continue;
          }
          if (state.is(net.minecraftforge.common.Tags.Blocks.ORES)) {
            found.merge(net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString(),
                1, Integer::sum);
          } else if (state.is(com.faktocraft.common.fluid.ModFluids.OIL.block())) {
            found.merge(y >= 30 ? "faktocraft:oil_lake" : "faktocraft:oil_underground", 1, Integer::sum);
          }
        }
      }
    }
    Integer undergroundOil = found.remove("faktocraft:oil_underground");
    if (undergroundOil != null) {
      found.put(undergroundOil >= 600 ? "faktocraft:oil_giant" : "faktocraft:oil_pocket", undergroundOil);
    }
    CompoundTag entries = new CompoundTag();
    var random = level.getRandom();
    for (java.util.Map.Entry<String, Integer> entry : found.entrySet()) {
      if (random.nextFloat() < FALSE_NEGATIVE_CHANCE) {
        continue;
      }
      entries.putInt(entry.getKey(),
          Math.max(1, Math.round(entry.getValue() * (0.75F + random.nextFloat() * 0.5F))));
    }
    return entries;
  }

  private static void finishScan(net.minecraft.server.level.ServerLevel level, ItemStack stack, CompoundTag job) {
    int chunkX = job.getInt("cx");
    int chunkZ = job.getInt("cz");
    if (!level.dimension().location().toString().equals(job.getString("dim"))) {
      return;
    }
    CompoundTag scan = new CompoundTag();
    scan.putLong("t", level.getGameTime());
    scan.put("entries", computeScanEntries(level, chunkX, chunkZ));
    CompoundTag scans = stack.getOrCreateTag().getCompound(TAG_SCANS);
    scans.put(chunkKey(level, chunkX, chunkZ), scan);
    while (scans.getAllKeys().size() > MAX_SAVED_CHUNKS) {
      String oldest = null;
      long oldestTime = Long.MAX_VALUE;
      for (String key : scans.getAllKeys()) {
        long t = scans.getCompound(key).getLong("t");
        if (t < oldestTime) {
          oldestTime = t;
          oldest = key;
        }
      }
      scans.remove(oldest);
    }
    stack.getOrCreateTag().put(TAG_SCANS, scans);
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    if (level.isClientSide()) {
      net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,
          () -> com.faktocraft.client.ProspectorClient::openScreen);
    }
    return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(Component.translatable("tooltip." + IndReb.MODID + ".prospector")
        .withStyle(ChatFormatting.GRAY));
    tooltip.add(Component.translatable("tooltip." + IndReb.MODID + ".prospector_accuracy")
        .withStyle(ChatFormatting.DARK_GRAY));
    super.appendHoverText(stack, level, tooltip, flag);
  }
}

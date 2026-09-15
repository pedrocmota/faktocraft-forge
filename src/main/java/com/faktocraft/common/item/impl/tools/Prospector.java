package com.faktocraft.common.item.impl.tools;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.item.base.ElectricItem;
import com.faktocraft.common.scan.ScanChannel;
import com.faktocraft.common.scan.ScanChannels;
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
  public static final int SCAN_DURATION_TICKS = 160;
  public static final int VIEW_RADIUS = 3;
  public static final String TAG_SCANS = "Scans";
  public static final String TAG_JOB = "ScanJob";
  public static final String TAG_CODE = "ScanCode";

  public Prospector(Properties properties) {
    super(properties.stacksTo(1), 0, 400000, EnergyType.RECEIVE, EnergyTier.HIGH);
  }

  public static ItemStack held(Player player) {
    if (player.getMainHandItem().getItem() instanceof Prospector) {
      return player.getMainHandItem();
    }
    if (player.getOffhandItem().getItem() instanceof Prospector) {
      return player.getOffhandItem();
    }
    return ItemStack.EMPTY;
  }

  public static int getCode(ItemStack stack) {
    if (stack.hasTag() && stack.getTag().contains(TAG_CODE)) {
      return ScanChannels.sanitize(stack.getTag().getInt(TAG_CODE));
    }
    return ScanChannels.DEFAULT_CODE;
  }

  public static void setCode(ItemStack stack, int code) {
    stack.getOrCreateTag().putInt(TAG_CODE, ScanChannels.sanitize(code));
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
    importLegacyScans(serverLevel, stack);
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

  private static void importLegacyScans(net.minecraft.server.level.ServerLevel level, ItemStack stack) {
    if (!stack.hasTag() || !stack.getTag().contains(TAG_SCANS)) {
      return;
    }
    CompoundTag legacy = stack.getTag().getCompound(TAG_SCANS);
    if (!legacy.isEmpty()) {
      ScanChannels.get(level).channel(getCode(stack)).importKeyed(legacy);
    }
    stack.getTag().remove(TAG_SCANS);
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
    ScanChannel channel = ScanChannels.get(level).channel(getCode(stack));
    channel.put(level, chunkX, chunkZ, scan);
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
    tooltip.add(Component.translatable("tooltip." + Faktocraft.MODID + ".prospector")
        .withStyle(ChatFormatting.GRAY));
    tooltip.add(Component.translatable("tooltip." + Faktocraft.MODID + ".prospector_accuracy")
        .withStyle(ChatFormatting.DARK_GRAY));
    tooltip.add(Component.translatable("tooltip." + Faktocraft.MODID + ".scan_code",
        ScanChannels.codeText(getCode(stack))).withStyle(ChatFormatting.LIGHT_PURPLE));
    super.appendHoverText(stack, level, tooltip, flag);
  }
}

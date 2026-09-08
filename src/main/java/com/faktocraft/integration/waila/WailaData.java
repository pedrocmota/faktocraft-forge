package com.faktocraft.integration.waila;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.cable.BlockEntityCable;
import com.faktocraft.common.block.impl.pipe.IExtractorPipe;
import com.faktocraft.common.energy.impl.BasicEnergyStorage;
import com.faktocraft.common.energy.provider.EnergyNetwork;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.util.TextComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class WailaData {

  public static final String TAG_ENERGY = "faktocraftEnergy";
  public static final String TAG_MAX_ENERGY = "faktocraftMaxEnergy";
  public static final String TAG_TIER = "faktocraftTier";
  public static final String TAG_TIERS = "faktocraftTiers";
  public static final String TAG_UNDERVOLTAGE = "faktocraftUndervolt";
  public static final String TAG_REDSTONE_OFF = "faktocraftRedstoneOff";
  public static final String TAG_GENERATOR = "faktocraftGenerator";
  public static final String TAG_FLOWING = "faktocraftFlowing";
  public static final String TAG_CABLE_TIER = "faktocraftCableTier";

  public static final int BAR_COLOR = 0xFF000000 | (76 << 16) | (178 << 8) | 13;

  private WailaData() {
  }

  private static String key(String name) {
    return "top." + Faktocraft.MODID + "." + name;
  }

  public static void writeEnergy(@Nullable BlockEntity blockEntity, CompoundTag tag) {
    if (blockEntity instanceof IExtractorPipe pipe) {
      BasicEnergyStorage energy = pipe.extractor().energy();
      writeStorage(tag, energy);
      return;
    }
    if (blockEntity instanceof FaktocraftBlockEntity entity && entity.hasEnergy()) {
      BasicEnergyStorage energy = entity.getEnergyStorage();
      if (energy == null) {
        return;
      }
      writeStorage(tag, energy);
      if (energy.energyType() == EnergyType.EXTRACT) {
        tag.putBoolean(TAG_GENERATOR, true);
      }
      if (entity.isUndervoltage()) {
        tag.putBoolean(TAG_UNDERVOLTAGE, true);
      }
      if (entity.isRedstoneBlocked()) {
        tag.putBoolean(TAG_REDSTONE_OFF, true);
      }
    }
  }

  private static void writeStorage(CompoundTag tag, BasicEnergyStorage energy) {
    tag.putInt(TAG_ENERGY, energy.energyStored());
    tag.putInt(TAG_MAX_ENERGY, energy.maxEnergy());
    tag.putInt(TAG_TIER, energy.energyTier().getLvl());
    tag.putIntArray(TAG_TIERS, energy.acceptedTiers().stream()
        .mapToInt(EnergyTier::getLvl)
        .sorted()
        .toArray());
  }

  public static void writeCable(@Nullable BlockEntity blockEntity, CompoundTag tag) {
    if (blockEntity instanceof BlockEntityCable cable) {
      EnergyNetwork network = cable.getNetwork();
      if (network != null) {
        EnergyTier flowing = network.getEnergyFlowing();
        tag.putInt(TAG_FLOWING, flowing != null ? flowing.getLvl() : -1);
        tag.putInt(TAG_CABLE_TIER, network.getEnergyTier().getLvl());
      }
    }
  }

  public static int maxEnergy(CompoundTag data) {
    return data.contains(TAG_MAX_ENERGY) ? data.getInt(TAG_MAX_ENERGY) : -1;
  }

  public static float ratio(CompoundTag data) {
    int max = maxEnergy(data);
    return max <= 0 ? 0.0F : Math.min(1.0F, (float) data.getInt(TAG_ENERGY) / max);
  }

  public static Component barText(CompoundTag data) {
    return Component.literal(TextComponentUtil.getFormattedEnergyUnit(data.getInt(TAG_ENERGY))
        + " / " + TextComponentUtil.getFormattedEnergyUnit(maxEnergy(data)) + " IE");
  }

  @Nullable
  public static Component tierLine(CompoundTag data) {
    int[] levels = data.contains(TAG_TIERS)
        ? data.getIntArray(TAG_TIERS)
        : new int[] { data.contains(TAG_TIER) ? data.getInt(TAG_TIER) : 1 };
    if (levels.length == 0) {
      return null;
    }
    MutableComponent tiers = Component.empty();
    for (int i = 0; i < levels.length; i++) {
      if (i > 0) {
        tiers.append(Component.literal(", ").withStyle(ChatFormatting.DARK_GRAY));
      }
      EnergyTier tier = EnergyTier.getTierFromLvl(levels[i]);
      tiers.append(Component.translatable(tier.getLang().getTranslationKey()).withStyle(tier.getColor()));
    }
    return Component.translatable(key("energy_tier"), tiers).withStyle(ChatFormatting.DARK_GRAY);
  }

  public static List<Component> energyLines(CompoundTag data) {
    List<Component> lines = new ArrayList<>();
    if (maxEnergy(data) <= 0) {
      if (data.contains(TAG_TIER)) {
        Component tier = tierLine(data);
        if (tier != null) {
          lines.add(tier);
        }
        lines.add(Component.translatable("gui." + Faktocraft.MODID + ".capacitor_required")
            .withStyle(ChatFormatting.RED));
      }
      return lines;
    }
    Component tier = tierLine(data);
    if (tier != null) {
      lines.add(tier);
    }
    if (data.getBoolean(TAG_REDSTONE_OFF)) {
      lines.add(Component.translatable(key("redstone_off")).withStyle(ChatFormatting.GOLD));
    } else if (data.getInt(TAG_ENERGY) <= 0) {
      if (data.getBoolean(TAG_GENERATOR)) {
        lines.add(Component.translatable(key("buffer_empty")).withStyle(ChatFormatting.GRAY));
      } else {
        lines.add(Component.translatable(key("no_energy")).withStyle(ChatFormatting.RED));
      }
    } else if (data.getBoolean(TAG_UNDERVOLTAGE)) {
      lines.add(Component.translatable(key("undervoltage")).withStyle(ChatFormatting.YELLOW));
    }
    return lines;
  }

  public static List<Component> cableLines(CompoundTag data) {
    List<Component> lines = new ArrayList<>();
    int cableTierLvl = data.contains(TAG_CABLE_TIER) ? data.getInt(TAG_CABLE_TIER) : -1;
    if (cableTierLvl < 0) {
      return lines;
    }
    int flowingLvl = data.contains(TAG_FLOWING) ? data.getInt(TAG_FLOWING) : -1;
    Component flowingComponent;
    if (flowingLvl >= 0) {
      EnergyTier flowing = EnergyTier.getTierFromLvl(flowingLvl);
      flowingComponent = Component.translatable(flowing.getLang().getTranslationKey()).withStyle(flowing.getColor());
    } else {
      flowingComponent = Component.literal("-");
    }
    lines.add(Component.translatable(key("current_voltage"), flowingComponent).withStyle(ChatFormatting.DARK_GRAY));
    EnergyTier cableTier = EnergyTier.getTierFromLvl(cableTierLvl);
    lines.add(Component.translatable(key("energy_tier"),
        Component.translatable(cableTier.getLang().getTranslationKey()).withStyle(cableTier.getColor()))
        .withStyle(ChatFormatting.DARK_GRAY));
    return lines;
  }

  public static Component onOff(boolean on) {
    return on
        ? Component.translatable(key("state_on")).withStyle(ChatFormatting.GREEN)
        : Component.translatable(key("state_off")).withStyle(ChatFormatting.RED);
  }

  public static Component openClosed(boolean open) {
    return open
        ? Component.translatable(key("state_open")).withStyle(ChatFormatting.GREEN)
        : Component.translatable(key("state_closed")).withStyle(ChatFormatting.RED);
  }

  public static Component breakerLine(boolean on) {
    return Component.translatable(key("breaker"), onOff(on)).withStyle(ChatFormatting.GRAY);
  }

  public static Component valveLine(boolean open) {
    return Component.translatable(key("valve"), openClosed(open)).withStyle(ChatFormatting.GRAY);
  }
}

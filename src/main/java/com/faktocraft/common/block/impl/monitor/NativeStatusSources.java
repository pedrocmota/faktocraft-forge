package com.faktocraft.common.block.impl.monitor;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.cable.BlockBreaker;
import com.faktocraft.common.block.impl.machines.nuclear_reactor.BlockEntityNuclearReactor;
import com.faktocraft.common.block.impl.pipe.IValveHolder;
import com.faktocraft.common.block.impl.pipe.PipeValve;
import com.faktocraft.common.interfaces.block.IGenerationInfo;
import com.faktocraft.integration.waila.WailaData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public final class NativeStatusSources {

  static final String TAG_REACTOR_HEAT = "monitorReactorHeat";
  static final String TAG_REACTOR_OUTPUT = "monitorReactorOutput";
  static final String TAG_REACTOR_STATUS = "monitorReactorStatus";
  static final String TAG_VALVE_PRESENT = "monitorValve";
  static final String TAG_VALVE_OPEN = "monitorValveOpen";

  private NativeStatusSources() {
  }

  public static void register() {
    StatusSources.register(new Energy());
    StatusSources.register(new Cable());
    StatusSources.register(new Reactor());
    StatusSources.register(new Breaker());
    StatusSources.register(new Valve());
  }

  private static String key(String name) {
    return "top." + Faktocraft.MODID + "." + name;
  }

  static int heatColor(float heat) {
    float clamped = Math.max(0.0F, Math.min(1.0F, heat));
    int red = (int) (80 + 175 * clamped);
    int green = (int) (200 * (1.0F - clamped) + 40);
    return 0xFF000000 | (red << 16) | (green << 8) | 0x20;
  }

  private static final class Energy implements StatusSource {

    @Override
    public void collect(ServerLevel level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
        CompoundTag out) {
      WailaData.writeEnergy(blockEntity, out);
    }

    @Override
    public void lines(BlockState state, CompoundTag data, List<StatusLine> out) {
      if (WailaData.maxEnergy(data) > 0) {
        out.add(new StatusLine.Bar(WailaData.ratio(data), WailaData.BAR_COLOR, WailaData.barText(data)));
      }
      for (Component line : WailaData.energyLines(data)) {
        out.add(new StatusLine.Text(line));
      }
    }
  }

  private static final class Cable implements StatusSource {

    @Override
    public void collect(ServerLevel level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
        CompoundTag out) {
      WailaData.writeCable(blockEntity, out);
    }

    @Override
    public void lines(BlockState state, CompoundTag data, List<StatusLine> out) {
      for (Component line : WailaData.cableLines(data)) {
        out.add(new StatusLine.Text(line));
      }
    }
  }

  private static final class Reactor implements StatusSource {

    @Override
    public void collect(ServerLevel level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
        CompoundTag out) {
      if (blockEntity instanceof BlockEntityNuclearReactor reactor) {
        out.putFloat(TAG_REACTOR_HEAT, reactor.heatRatio());
        out.putInt(TAG_REACTOR_OUTPUT, reactor.getOutputPerTick());
        out.putInt(TAG_REACTOR_STATUS, reactor.getStatus());
      }
    }

    @Override
    public void lines(BlockState state, CompoundTag data, List<StatusLine> out) {
      if (!data.contains(TAG_REACTOR_STATUS)) {
        return;
      }
      float heat = data.getFloatOr(TAG_REACTOR_HEAT, 0.0F);
      int status = data.getIntOr(TAG_REACTOR_STATUS, 0);
      out.add(new StatusLine.Bar(heat, heatColor(heat),
          Component.translatable(key("reactor_heat"), Math.round(heat * 100.0F))));
      out.add(new StatusLine.Text(Component.translatable(BlockEntityNuclearReactor.statusKey(status))
          .withStyle(status == 0 ? ChatFormatting.GREEN : ChatFormatting.GOLD)));
      out.add(new StatusLine.Text(Component.translatable(key("reactor_output"),
          IGenerationInfo.rate(data.getIntOr(TAG_REACTOR_OUTPUT, 0))).withStyle(ChatFormatting.GRAY)));
    }
  }

  private static final class Breaker implements StatusSource {

    @Override
    public void collect(ServerLevel level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
        CompoundTag out) {
    }

    @Override
    public void lines(BlockState state, CompoundTag data, List<StatusLine> out) {
      if (state.hasProperty(BlockBreaker.ON)) {
        out.add(new StatusLine.Text(WailaData.breakerLine(state.getValue(BlockBreaker.ON))));
      }
    }
  }

  private static final class Valve implements StatusSource {

    @Override
    public void collect(ServerLevel level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
        CompoundTag out) {
      if (blockEntity instanceof IValveHolder holder) {
        PipeValve valve = holder.getValve();
        if (valve.isPresent()) {
          out.putBoolean(TAG_VALVE_PRESENT, true);
          out.putBoolean(TAG_VALVE_OPEN, valve.isOpen());
        }
      }
    }

    @Override
    public void lines(BlockState state, CompoundTag data, List<StatusLine> out) {
      if (data.getBooleanOr(TAG_VALVE_PRESENT, false)) {
        out.add(new StatusLine.Text(WailaData.valveLine(data.getBooleanOr(TAG_VALVE_OPEN, false))));
      }
    }
  }
}

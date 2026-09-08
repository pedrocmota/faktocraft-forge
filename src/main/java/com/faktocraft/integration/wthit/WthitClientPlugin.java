package com.faktocraft.integration.wthit;

import com.faktocraft.common.block.FaktocraftEntityBlock;
import com.faktocraft.common.block.impl.cable.BlockBreaker;
import com.faktocraft.common.block.impl.cable.BlockCable;
import com.faktocraft.common.block.impl.machines.distillery.BlockDistillery;
import com.faktocraft.common.block.impl.machines.distillery.BlockDistilleryTower;
import com.faktocraft.common.block.impl.pipe.BlockFluidExtractorPipe;
import com.faktocraft.common.block.impl.pipe.BlockFluidPipe;
import com.faktocraft.common.block.impl.pipe.IValveHolder;
import com.faktocraft.common.block.impl.pipe.PipeValve;
import com.faktocraft.integration.waila.WailaData;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITargetRedirector;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaClientPlugin;
import mcp.mobius.waila.api.component.BarComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class WthitClientPlugin implements IWailaClientPlugin {

  private static final IBlockComponentProvider ENERGY = new IBlockComponentProvider() {
    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
      CompoundTag data = accessor.getData().raw();
      if (WailaData.maxEnergy(data) > 0) {
        tooltip.addLine(new BarComponent(WailaData.ratio(data), WailaData.BAR_COLOR, WailaData.barText(data)));
      }
      for (Component line : WailaData.energyLines(data)) {
        tooltip.addLine(line);
      }
    }
  };

  private static final IBlockComponentProvider CABLE = new IBlockComponentProvider() {
    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
      for (Component line : WailaData.cableLines(accessor.getData().raw())) {
        tooltip.addLine(line);
      }
    }
  };

  private static final IBlockComponentProvider BREAKER = new IBlockComponentProvider() {
    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
      BlockState state = accessor.getBlockState();
      if (state.hasProperty(BlockBreaker.ON)) {
        tooltip.addLine(WailaData.breakerLine(state.getValue(BlockBreaker.ON)));
      }
    }
  };

  private static final IBlockComponentProvider VALVE = new IBlockComponentProvider() {
    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
      if (accessor.getBlockEntity() instanceof IValveHolder holder) {
        PipeValve valve = holder.getValve();
        if (valve.isPresent()) {
          tooltip.addLine(WailaData.valveLine(valve.isOpen()));
        }
      }
    }
  };

  private static final IBlockComponentProvider TOWER = new IBlockComponentProvider() {
    @Override
    public ITargetRedirector.Result redirect(ITargetRedirector redirect, IBlockAccessor accessor,
        IPluginConfig config) {
      for (int i = 1; i <= BlockDistillery.TOWER_HEIGHT; i++) {
        BlockPos basePos = accessor.getPosition().below(i);
        if (accessor.getLevel().getBlockState(basePos).getBlock() instanceof BlockDistillery) {
          BlockHitResult hit = accessor.getBlockHitResult();
          return redirect.to(new BlockHitResult(hit.getLocation(), hit.getDirection(), basePos, hit.isInside()));
        }
      }
      return null;
    }
  };

  @Override
  public void register(IClientRegistrar registrar) {
    registrar.body(ENERGY, FaktocraftEntityBlock.class);
    registrar.body(ENERGY, BlockFluidExtractorPipe.class);
    registrar.body(CABLE, BlockCable.class);
    registrar.body(BREAKER, BlockBreaker.class);
    registrar.body(VALVE, BlockFluidPipe.class);
    registrar.redirect(TOWER, BlockDistilleryTower.class);
  }
}

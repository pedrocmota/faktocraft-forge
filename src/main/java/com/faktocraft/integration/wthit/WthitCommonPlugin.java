package com.faktocraft.integration.wthit;

import com.faktocraft.common.block.impl.cable.BlockEntityCable;
import com.faktocraft.common.block.impl.pipe.BlockEntityFluidExtractorPipe;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.registries.ModBlocks;
import com.faktocraft.common.registries.PipeRegistry;
import com.faktocraft.integration.waila.WailaData;
import mcp.mobius.waila.api.ICommonRegistrar;
import mcp.mobius.waila.api.IDataProvider;
import mcp.mobius.waila.api.IDataWriter;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerAccessor;
import mcp.mobius.waila.api.IWailaCommonPlugin;
import net.minecraft.world.level.block.entity.BlockEntity;

public class WthitCommonPlugin implements IWailaCommonPlugin {

  private static final IDataProvider<BlockEntity> ENERGY = new IDataProvider<>() {
    @Override
    public void appendData(IDataWriter data, IServerAccessor<BlockEntity> accessor, IPluginConfig config) {
      WailaData.writeEnergy(accessor.getTarget(), data.raw());
    }
  };

  private static final IDataProvider<BlockEntity> CABLE = new IDataProvider<>() {
    @Override
    public void appendData(IDataWriter data, IServerAccessor<BlockEntity> accessor, IPluginConfig config) {
      WailaData.writeCable(accessor.getTarget(), data.raw());
    }
  };

  @Override
  public void register(ICommonRegistrar registrar) {
    registrar.blockData(ENERGY, FaktocraftBlockEntity.class);
    registrar.blockData(ENERGY, BlockEntityFluidExtractorPipe.class);
    registrar.blockData(CABLE, BlockEntityCable.class);
    registrar.blacklist(PipeRegistry.PUMP_TUBE_BLOCK, ModBlocks.HANDLE_GUARD);
  }
}

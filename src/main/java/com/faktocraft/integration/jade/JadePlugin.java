package com.faktocraft.integration.jade;

import com.faktocraft.common.block.IndRebEntityBlock;
import com.faktocraft.common.block.impl.cable.BlockCable;
import com.faktocraft.common.block.impl.cable.BlockEntityCable;
import com.faktocraft.common.entity.block.IndRebBlockEntity;
import com.faktocraft.integration.jade.provider.JadeCableDataProvider;
import com.faktocraft.integration.jade.provider.JadeCableProvider;
import com.faktocraft.integration.jade.provider.JadeEnergyDataProvider;
import com.faktocraft.integration.jade.provider.JadeEnergyProvider;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {

  @Override
  public void register(IWailaCommonRegistration registration) {
    registration.registerBlockDataProvider(JadeEnergyDataProvider.INSTANCE, IndRebBlockEntity.class);
    registration.registerBlockDataProvider(JadeEnergyDataProvider.INSTANCE,
        com.faktocraft.common.block.impl.pipe.BlockEntityFluidExtractorPipe.class);
    registration.registerBlockDataProvider(JadeCableDataProvider.INSTANCE, BlockEntityCable.class);
  }

  @Override
  public void registerClient(IWailaClientRegistration registration) {
    registration.registerBlockComponent(JadeEnergyProvider.INSTANCE, IndRebEntityBlock.class);
    registration.registerBlockComponent(JadeEnergyProvider.INSTANCE,
        com.faktocraft.common.block.impl.pipe.BlockFluidExtractorPipe.class);
    registration.registerBlockComponent(JadeCableProvider.INSTANCE, BlockCable.class);
    registration.registerBlockComponent(com.faktocraft.integration.jade.provider.JadeBreakerProvider.INSTANCE,
        com.faktocraft.common.block.impl.cable.BlockBreaker.class);
    registration.registerBlockComponent(com.faktocraft.integration.jade.provider.JadeValveProvider.INSTANCE,
        com.faktocraft.common.block.impl.pipe.BlockFluidPipe.class);
    registration.hideTarget(com.faktocraft.common.registries.PipeRegistry.PUMP_TUBE_BLOCK);
    registration.hideTarget(com.faktocraft.common.registries.ModBlocks.HANDLE_GUARD);
    registration.addRayTraceCallback((hit, accessor, original) -> {
      if (accessor instanceof snownee.jade.api.BlockAccessor block
          && block.getBlock() instanceof com.faktocraft.common.block.impl.machines.distillery.BlockDistilleryTower) {
        var level = block.getLevel();
        for (int i = 1; i <= com.faktocraft.common.block.impl.machines.distillery.BlockDistillery.TOWER_HEIGHT; i++) {
          net.minecraft.core.BlockPos basePos = block.getPosition().below(i);
          var baseState = level.getBlockState(basePos);
          if (baseState
              .getBlock() instanceof com.faktocraft.common.block.impl.machines.distillery.BlockDistillery) {
            return registration.blockAccessor().from(block)
                .blockState(baseState)
                .blockEntity(() -> level.getBlockEntity(basePos))
                .hit(block.getHitResult().withPosition(basePos))
                .build();
          }
        }
      }
      return accessor;
    });
  }
}

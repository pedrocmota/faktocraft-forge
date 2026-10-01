package com.faktocraft.integration.jade;

import com.faktocraft.common.block.FaktocraftEntityBlock;
import com.faktocraft.common.block.impl.cable.BlockCable;
import com.faktocraft.common.block.impl.cable.BlockEntityCable;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.integration.jade.provider.JadeCableDataProvider;
import com.faktocraft.integration.jade.provider.JadeCableProvider;
import com.faktocraft.integration.jade.provider.JadeEnergyDataProvider;
import com.faktocraft.integration.jade.provider.JadeEnergyProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {
  private static ResourceKey<Block> key(Block block) {
    return ResourceKey.create(Registries.BLOCK, BuiltInRegistries.BLOCK.getKey(block));
  }

  @Override
  public void register(IWailaCommonRegistration registration) {
    registration.registerBlockDataProvider(JadeEnergyDataProvider.INSTANCE, FaktocraftBlockEntity.class);
    registration.registerBlockDataProvider(JadeEnergyDataProvider.INSTANCE,
        com.faktocraft.common.block.impl.pipe.BlockEntityFluidExtractorPipe.class);
    registration.registerBlockDataProvider(JadeCableDataProvider.INSTANCE, BlockEntityCable.class);
    registration.registerItemStorage(com.faktocraft.integration.jade.provider.JadeItemStorageProvider.INSTANCE,
        FaktocraftBlockEntity.class);
    registration.registerBlockDataProvider(
        com.faktocraft.integration.jade.provider.JadeReactorDataProvider.INSTANCE,
        com.faktocraft.common.block.impl.machines.nuclear_reactor.BlockEntityNuclearReactor.class);
    registration.blockOperations().hide(key(com.faktocraft.common.registries.PipeRegistry.PUMP_TUBE_BLOCK));
    registration.blockOperations().hide(key(com.faktocraft.common.registries.ModBlocks.HANDLE_GUARD));
  }

  @Override
  public void registerClient(IWailaClientRegistration registration) {
    registration.registerBlockComponent(JadeEnergyProvider.INSTANCE, FaktocraftEntityBlock.class);
    registration.registerBlockComponent(JadeEnergyProvider.INSTANCE,
        com.faktocraft.common.block.impl.pipe.BlockFluidExtractorPipe.class);
    registration.registerBlockComponent(JadeCableProvider.INSTANCE, BlockCable.class);
    registration.registerItemStorageClient(com.faktocraft.integration.jade.provider.JadeItemStorageProvider.INSTANCE);
    registration.registerBlockComponent(JadeEnergyProvider.INSTANCE,
        com.faktocraft.common.block.impl.machines.nuclear_reactor.BlockNuclearReactor.class);
    registration.registerBlockComponent(com.faktocraft.integration.jade.provider.JadeReactorProvider.INSTANCE,
        com.faktocraft.common.block.impl.machines.nuclear_reactor.BlockNuclearReactor.class);
    registration.registerBlockComponent(com.faktocraft.integration.jade.provider.JadeBreakerProvider.INSTANCE,
        com.faktocraft.common.block.impl.cable.BlockBreaker.class);
    registration.registerBlockComponent(com.faktocraft.integration.jade.provider.JadeValveProvider.INSTANCE,
        com.faktocraft.common.block.impl.pipe.BlockFluidPipe.class);
    registration.addRayTraceCallback((hit, accessor, original) -> {
      if (accessor instanceof snownee.jade.api.BlockAccessor part
          && part.getBlock() instanceof com.faktocraft.common.block.impl.machines.nuclear_reactor.BlockNuclearReactor) {
        net.minecraft.core.BlockPos core = com.faktocraft.common.block.impl.machines.nuclear_reactor.BlockNuclearReactor
            .corePos(part.getBlockState(), part.getPosition());
        if (core != null && !core.equals(part.getPosition())) {
          var level = part.getLevel();
          return registration.blockAccessor().from(part)
              .blockState(level.getBlockState(core))
              .blockEntity(() -> level.getBlockEntity(core))
              .hit(part.getHitResult().withPosition(core))
              .build();
        }
      }
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

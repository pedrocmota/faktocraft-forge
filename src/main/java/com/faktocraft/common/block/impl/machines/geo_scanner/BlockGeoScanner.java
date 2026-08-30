package com.faktocraft.common.block.impl.machines.geo_scanner;

import com.faktocraft.common.block.BlockElectricMachine;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketGeoScannerState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockGeoScanner extends BlockElectricMachine {

  public BlockGeoScanner(Properties properties) {
    super(EnergyTier.HIGH, properties);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BlockEntityGeoScanner(pos, state);
  }

  @Override
  public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
      BlockHitResult hitResult) {
    if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer
        && level.getBlockEntity(pos) instanceof BlockEntityGeoScanner scanner) {
      ModNetworking.sendToPlayer(serverPlayer, PacketGeoScannerState.of(scanner, true, true));
    }
    return InteractionResult.SUCCESS;
  }
}

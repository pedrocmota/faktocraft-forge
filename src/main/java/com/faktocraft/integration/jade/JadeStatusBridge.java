package com.faktocraft.integration.jade;

import com.faktocraft.common.block.impl.monitor.StatusBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.impl.BlockAccessorImpl;
import snownee.jade.impl.WailaCommonRegistration;
import java.util.List;

public final class JadeStatusBridge implements StatusBridge {

  private JadeStatusBridge() {
  }

  public static StatusBridge create() {
    return new JadeStatusBridge();
  }

  private static final String TAG_JADE = "jade";

  static boolean collectedBy(CompoundTag data) {
    return data.getBoolean(TAG_JADE);
  }

  static BlockHitResult hit(BlockPos pos) {
    return new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
  }

  @Override
  public void collect(ServerLevel level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
      CompoundTag out) {
    out.putBoolean(TAG_JADE, true);
    out.putInt("x", pos.getX());
    out.putInt("y", pos.getY());
    out.putInt("z", pos.getZ());
    if (blockEntity == null) {
      return;
    }
    out.putString("id", String.valueOf(ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(blockEntity.getType())));
    List<IServerDataProvider<BlockAccessor>> providers = WailaCommonRegistration.INSTANCE
        .getBlockNBTProviders(blockEntity);
    if (providers.isEmpty()) {
      return;
    }
    BlockAccessor accessor = new BlockAccessorImpl.Builder().level(level)
        .player(FakePlayerFactory.getMinecraft(level)).serverData(out).serverConnected(true).showDetails(false)
        .hit(hit(pos)).blockState(state).blockEntity(() -> blockEntity).build();
    CompoundTag target = accessor.getServerData();
    for (IServerDataProvider<BlockAccessor> provider : providers) {
      try {
        provider.appendServerData(target, accessor);
      } catch (RuntimeException ignored) {
        continue;
      }
    }
    if (target != out) {
      out.merge(target);
    }
  }
}

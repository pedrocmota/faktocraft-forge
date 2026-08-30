package com.faktocraft.common.block.impl.charge_pad;

import com.faktocraft.common.block.IndRebEntityBlock;
import com.faktocraft.common.interfaces.block.IHasMenu;
import com.faktocraft.common.interfaces.block.IStateActive;
import com.faktocraft.common.interfaces.block.IStateFacing;
import com.faktocraft.common.tier.ChargePadTier;
import com.faktocraft.common.util.BlockStateHelper;
import com.faktocraft.common.util.wrench.WrenchHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

public class BlockChargePad extends IndRebEntityBlock implements IStateFacing, IHasMenu, IStateActive {

  private final ChargePadTier chargePadTier;

  public BlockChargePad(ChargePadTier chargePadTier, Properties properties) {
    super(properties);
    this.chargePadTier = chargePadTier;
    WrenchHelper.registerAction(this).add(WrenchHelper.rotationAction());
  }

  public static BlockBehaviour.Properties woodenProperties() {
    return BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1F, 3F).sound(SoundType.WOOD);
  }

  public static BlockBehaviour.Properties metalProperties() {
    return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5F, 3F).sound(SoundType.METAL);
  }

  public ChargePadTier getChargePadTier() {
    return chargePadTier;
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BlockEntityChargePad(pos, state);
  }

  @Override
  public MenuChargePad getMenu(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    return new MenuChargePad(windowId, level, pos, playerInventory, player);
  }

  @Override
  public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
    if (state.getValue(BlockStateHelper.activeProperty)) {
      for (int i = 0; i < 20; i++) {
        double x = pos.getX() + 0.1 + random.nextDouble() * 0.9;
        double y = pos.getY() + 1 + 0.3 + random.nextDouble() * 1.4;
        double z = pos.getZ() + 0.1 + random.nextDouble() * 0.9;
        level.addParticle(ParticleTypes.DOLPHIN, x, y, z, 0, 100D, 0);
      }
    }
  }
}

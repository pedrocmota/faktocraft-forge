package com.faktocraft.common.block.impl.cable;

import com.faktocraft.common.block.VoxelBlock;
import com.faktocraft.common.energy.EnergyLookup;
import com.faktocraft.common.energy.provider.EnergyCore;
import com.faktocraft.common.tier.CableTier;
import com.faktocraft.common.util.wrench.WrenchHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockCable extends VoxelBlock implements EntityBlock {

  private final CableTier cableTier;

  public BlockCable(float apothem, CableTier cableTier, Properties properties) {
    super(properties, apothem);
    this.cableTier = cableTier;
    WrenchHelper.registerAction(this);
  }

  public CableTier getCableTier() {
    return cableTier;
  }

  private static final int SHOCK_INTERVAL_TICKS = 20;

  private int shockRadius() {
    return switch (cableTier.getEnergyTier()) {
      case LOW -> 1;
      case MEDIUM -> 2;
      case HIGH -> 3;
      case VERY_HIGH -> 4;
      case ULTRA -> 5;
    };
  }

  private float shockDamage() {
    return switch (cableTier.getEnergyTier()) {
      case LOW -> 1.0F;
      case MEDIUM -> 2.0F;
      case HIGH -> 4.0F;
      case VERY_HIGH -> 6.0F;
      case ULTRA -> 8.0F;
    };
  }

  private boolean canShock() {
    return waterloggable() && !cableTier.isInsulated() && shockRadius() > 0;
  }

  @Nullable
  @Override
  public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(
      Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
    if (level.isClientSide() || !canShock()) {
      return null;
    }
    return (tickLevel, pos, tickState, be) -> shockTick((ServerLevel) tickLevel, pos, tickState);
  }

  private void shockTick(ServerLevel level, BlockPos pos, BlockState state) {
    if (!state.getValue(WATERLOGGED)
        || (level.getGameTime() + pos.asLong()) % SHOCK_INTERVAL_TICKS != 0) {
      return;
    }
    var network = EnergyCore.get(level).getNetworks().getNetwork(pos);
    if (network == null || network.getEnergyFlowing() == null) {
      return;
    }
    int radius = shockRadius();
    level.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.3, 0.3, 0.3, 0.05);
    var victims = level.getEntitiesOfClass(LivingEntity.class,
        new net.minecraft.world.phys.AABB(pos).inflate(radius),
        entity -> entity.isInWater() && entity.isAlive()
            && !(entity instanceof net.minecraft.world.entity.player.Player player
                && (player.isCreative() || player.isSpectator())));
    if (victims.isEmpty()) {
      return;
    }
    long now = level.getGameTime();
    boolean shocked = false;
    for (LivingEntity victim : victims) {
      var data = victim.getPersistentData();
      if (now - data.getLong("faktocraftShockTick") < SHOCK_INTERVAL_TICKS) {
        continue;
      }
      data.putLong("faktocraftShockTick", now);
      victim.hurt(level.damageSources().lightningBolt(), shockDamage());
      level.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
          victim.getX(), victim.getY(0.5), victim.getZ(), 8, 0.2, 0.4, 0.2, 0.1);
      shocked = true;
    }
    if (shocked) {
      level.playSound(null, pos, net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_IMPACT,
          net.minecraft.sounds.SoundSource.BLOCKS, 0.35F, 1.9F);
    }
  }

  @Override
  public void appendHoverText(net.minecraft.world.item.ItemStack stack,
      @Nullable net.minecraft.world.level.BlockGetter blockGetter,
      java.util.List<net.minecraft.network.chat.Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
    var tier = cableTier.getEnergyTier();
    tooltip.add(net.minecraft.network.chat.Component.translatable("tooltip.faktocraft.max_voltage",
        tier.getLang().getTranslationComponent().withStyle(tier.getColor()))
        .withStyle(net.minecraft.ChatFormatting.GRAY)
        .append(net.minecraft.network.chat.Component.literal(
            " (" + com.faktocraft.common.util.TextComponentUtil.getFormattedLong(tier.getBasicTransfer()) + " IE/t)")
            .withStyle(net.minecraft.ChatFormatting.DARK_GRAY)));
    super.appendHoverText(stack, blockGetter, tooltip, flag);
  }

  @Override
  protected boolean canConnect(LevelAccessor level, BlockPos pos, Direction direction) {
    BlockPos relative = pos.relative(direction);
    BlockState state = level.getBlockState(relative);
    if (state.getBlock() instanceof BlockCable) {
      return true;
    }
    if (level instanceof Level realLevel) {
      if (!EnergyLookup.isPresent(realLevel, relative, direction.getOpposite())) {
        return false;
      }
      return !(realLevel.getBlockEntity(relative) instanceof com.faktocraft.common.energy.ICableSideFilter filter)
          || filter.acceptsCableFrom(direction.getOpposite());
    }
    return false;
  }

  @Override
  public void entityInside(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.Entity entity) {

    boolean ultra = cableTier == CableTier.GLASS_FIBRE_CABLE;
    if (level.isClientSide() || (cableTier.isInsulated() && !ultra) || !(entity instanceof LivingEntity living)) {
      return;
    }
    if (com.faktocraft.common.item.impl.armor.HazmatArmorItem.hasBoots(living)) {
      return;
    }
    if (com.faktocraft.common.item.impl.nano.ItemNanoArmor.tryUseShockProtection(living)) {
      return;
    }
    if (!(level.getBlockEntity(pos) instanceof BlockEntityCable cable)) {
      return;
    }
    var network = cable.getNetwork();
    if (ultra) {
      float fill = network != null && network.maxEnergy() > 0
          ? (float) network.energyStored() / network.maxEnergy()
          : 0.0F;
      boolean lethal = fill >= 0.9F;
      living.hurt(com.faktocraft.common.registries.ModDamageTypes.ultraShock(level),
          lethal ? 10000.0F : 6.0F);
      return;
    }
    var current = network != null ? network.getEnergyFlowing() : null;
    if (current == null) {
      return;
    }
    float damage = switch (current) {
      case LOW -> 1.0F;
      case MEDIUM -> 3.0F;
      case HIGH -> 6.0F;
      default -> 1000.0F;
    };
    living.hurt(com.faktocraft.common.registries.ModDamageTypes.electricShock(level), damage);
  }

  @Override
  public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    super.setPlacedBy(level, pos, state, placer, stack);
    if (!level.isClientSide()) {
      EnergyCore.get(level).getNetworks().onPlaced(pos, state, cableTier.getEnergyTier());
    }
  }

  @Override
  protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
    super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    EnergyCore.get(level).getNetworks().onRemove(pos);
  }

  @SuppressWarnings("deprecation")
  @Override
  public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos fromPos,
      boolean movedByPiston) {
    super.neighborChanged(state, level, pos, neighborBlock, fromPos, movedByPiston);
    if (!level.isClientSide()) {
      EnergyCore.get(level).getNetworks().neighborChanged(pos, fromPos);
    }
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BlockEntityCable(pos, state);
  }
}

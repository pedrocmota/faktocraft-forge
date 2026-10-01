package com.faktocraft.common.fluid;

import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public abstract class BaseFluid extends BaseFlowingFluid {

  protected BaseFluid(Properties properties) {
    super(properties);
  }

  public static class Flowing extends BaseFluid {
    public Flowing(Properties properties) {
      super(properties);
      registerDefaultState(getStateDefinition().any().setValue(LEVEL, 7));
    }

    @Override
    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
      super.createFluidStateDefinition(builder);
      builder.add(LEVEL);
    }

    @Override
    public int getAmount(FluidState fluidState) {
      return fluidState.getValue(LEVEL);
    }

    @Override
    public boolean isSource(FluidState fluidState) {
      return false;
    }
  }

  public static class Source extends BaseFluid {
    public Source(Properties properties) {
      super(properties);
    }

    @Override
    public int getAmount(FluidState fluidState) {
      return 8;
    }

    @Override
    public boolean isSource(FluidState fluidState) {
      return true;
    }
  }
}

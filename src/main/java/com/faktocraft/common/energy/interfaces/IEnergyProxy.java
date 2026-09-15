package com.faktocraft.common.energy.interfaces;

import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import org.jetbrains.annotations.Nullable;

public interface IEnergyProxy {

  @Nullable
  FaktocraftBlockEntity energyOwner();
}

package com.faktocraft.common.block.impl.pipe;

public interface IValveHolder {

  PipeValve getValve();

  void setValve(PipeValve valve);

  boolean isValveRedstoneOnly();

  void setValveRedstoneOnly(boolean redstoneOnly);
}

package com.faktocraft.common.interfaces.entity;

public class IMachineActions {

  public interface IRecipeSwitcher {
    void changeRecipe(boolean next);
  }

  public interface IModeSwitcher {
    void changeMode();
  }

  public interface ITransformerActions {
    void updateMode();
  }

  public interface IScannerActions {
    void cleanScan();

    void saveScan();
  }

  public interface IReplicatorActions {
    void stopRun();

    void singleRun();

    void repeatRun();
  }
}

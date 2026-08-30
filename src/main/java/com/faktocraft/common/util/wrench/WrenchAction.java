package com.faktocraft.common.util.wrench;

import com.faktocraft.common.interfaces.wrench.IWrenchAction;
import java.util.ArrayList;
import java.util.List;

public class WrenchAction {

  private final List<IWrenchAction> actions = new ArrayList<>();

  public WrenchAction add(IWrenchAction action) {
    actions.add(action);
    return this;
  }

  public List<IWrenchAction> getActions() {
    return actions;
  }
}

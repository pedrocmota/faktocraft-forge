package com.faktocraft.common.screen.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import java.util.function.Predicate;

public class FilteredEditBox extends EditBox {
  private Predicate<String> filter = text -> true;

  public FilteredEditBox(Font font, int x, int y, int width, int height, Component narration) {
    super(font, x, y, width, height, narration);
  }

  public void setFilter(Predicate<String> filter) {
    this.filter = filter;
  }

  @Override
  public void insertText(String input) {
    String text = StringUtil.filterText(input);
    if (!text.isEmpty() && !filter.test(text)) {
      return;
    }
    super.insertText(input);
  }

  @Override
  public void setValue(String value) {
    if (!value.isEmpty() && !filter.test(value)) {
      return;
    }
    super.setValue(value);
  }
}

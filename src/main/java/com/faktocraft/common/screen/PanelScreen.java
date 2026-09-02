package com.faktocraft.common.screen;

import com.faktocraft.common.container.FaktocraftMenu;
import com.faktocraft.common.screen.button.GuiExpButton;
import com.faktocraft.common.screen.button.GuiInfoButton;
import com.faktocraft.common.screen.widgets.GuiElement;
import com.faktocraft.common.screen.widgets.GuiUpgrades;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import java.util.ArrayList;
import java.util.List;

public class PanelScreen<T extends FaktocraftMenu> extends BaseScreen<T> {

  private final List<AbstractWidget> component;

  public PanelScreen(T container, Inventory inventory, Component component) {
    super(container, inventory, component);
    this.component = new ArrayList<>();
  }

  public PanelScreen(T container, Inventory inventory, Component component, int imageWidth, int imageHeight) {
    super(container, inventory, component, imageWidth, imageHeight);
    this.component = new ArrayList<>();
  }

  @Override
  protected void init() {
    clearComponent();
    super.init();
  }

  protected void clearComponent() {
    component.clear();
  }

  protected void addRenderableComponent(AbstractWidget widget) {
    component.add(widget);
    addRenderableWidget(widget);
  }

  protected void addRenderableOnlyComponent(AbstractWidget widget) {
    component.add(widget);
    addRenderableOnly(widget);
  }

  protected void drawComponents(boolean draw) {
    for (AbstractWidget widget : component) {
      widget.visible = draw;
    }
  }

  @Override
  protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
    super.renderTooltip(graphics, mouseX, mouseY);
    for (AbstractWidget widget : component) {
      if (widget instanceof GuiElement guiElement) {
        guiElement.renderWidgetToolTip(this, graphics, mouseX, mouseY);
      }
    }
  }

  public List<Rect2i> getAreas() {
    List<Rect2i> extraAreas = new ArrayList<>();

    for (AbstractWidget widget : component) {
      if (widget instanceof GuiUpgrades guiUpgrades) {
        extraAreas
            .add(new Rect2i(guiUpgrades.getX(), guiUpgrades.getY(), guiUpgrades.getWidth(), guiUpgrades.getHeight()));
      }

      if (widget instanceof GuiExpButton guiExpButton) {
        extraAreas.add(
            new Rect2i(guiExpButton.getX(), guiExpButton.getY(), guiExpButton.getWidth(), guiExpButton.getHeight()));
      }

      if (widget instanceof GuiInfoButton guiInfoButton) {
        extraAreas.add(new Rect2i(guiInfoButton.getX(), guiInfoButton.getY(), guiInfoButton.getWidth(),
            guiInfoButton.getHeight()));
      }
    }

    return extraAreas;
  }
}

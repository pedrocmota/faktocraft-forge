package com.faktocraft.common.screen.button;

import net.minecraft.client.input.MouseButtonEvent;
import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.common.enums.GuiSprite;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.screen.widgets.GuiElement;
import com.faktocraft.common.util.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class GuiButton extends GuiElement {

  private final GuiSprite buttonSprite;

  protected final Runnable onLeftClick;
  protected final Runnable onRightClick;

  public GuiButton(IGuiWrapper wrapper, int leftOffset, int topOffset, GuiSprite buttonSprite,
      @Nullable Runnable onLeftClick, @Nullable Runnable onRightClick) {
    super(wrapper, buttonSprite.getWidth(), buttonSprite.getHeight(), leftOffset, topOffset);
    this.buttonSprite = buttonSprite;
    this.onLeftClick = onLeftClick;
    this.onRightClick = onRightClick;
  }

  @Override
  public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
    double mouseX = event.x();
    double mouseY = event.y();
    int button = GuiUtil.legacyButton(event);
    if (this.active && this.visible && clicked(mouseX, mouseY)) {
      if (button == 0) {
        return onLeftClick();
      } else if (button == 1) {
        return onRightClick();
      }
    }
    return false;
  }

  protected boolean clicked(double mouseX, double mouseY) {
    return mouseX >= getX() && mouseY >= getY() && mouseX < getX() + this.width && mouseY < getY() + this.height;
  }

  protected boolean onLeftClick() {
    if (onLeftClick != null) {
      onLeftClick.run();
      playDownSound(Minecraft.getInstance().getSoundManager());
      return true;
    }

    return false;
  }

  protected boolean onRightClick() {
    if (onRightClick != null) {
      onRightClick.run();
      return true;
    }

    return false;
  }

  @Override
  protected void renderBg(GuiGraphicsExtractor graphics, Minecraft minecraft, int mouseX, int mouseY) {
    if (this.isHovered) {
      blit(graphics, getLeftOffset(), getTopOffset(), buttonSprite.getActiveOffsetLeft(),
          buttonSprite.getActiveOffsetTop(), buttonSprite.getActiveWidth(), buttonSprite.getActiveHeight());
    } else {
      blit(graphics, getLeftOffset(), getTopOffset(), buttonSprite.getOffsetLeft(), buttonSprite.getOffsetTop(),
          buttonSprite.getWidth(), buttonSprite.getHeight());
    }

    super.renderBg(graphics, minecraft, mouseX, mouseY);
  }

  @Override
  public Identifier getResourceLocation() {
    return Constants.BUTTONS;
  }
}

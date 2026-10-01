package com.faktocraft.integration.rei;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.util.GuiUtil;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;
import java.util.ArrayList;
import java.util.List;
import static com.faktocraft.common.util.Constants.JEI;
import static com.faktocraft.common.util.Constants.PROCESS;

public class MachineCategory implements DisplayCategory<MachineDisplay> {

  public interface Layout {
    void build(MachineCategory category, MachineDisplay display, Point origin, List<Widget> out);
  }

  public static final int TEXT_COLOR = 0x7E7E7E;
  public static final float TEXT_SCALE = 0.75F;
  private static final int PADDING = 4;
  private static final int ENERGY_TICKS = 200;
  private static final int FIRE_TICKS = 100;

  private final CategoryIdentifier<MachineDisplay> id;
  private final Component title;
  private final Renderer icon;
  private final Identifier texture;
  private final int u;
  private final int v;
  private final int width;
  private final int height;
  private final Layout layout;

  public MachineCategory(CategoryIdentifier<MachineDisplay> id, String name, ItemLike icon, Identifier texture,
      int u, int v, int width, int height, Layout layout) {
    this.id = id;
    this.title = Component.translatable("jei." + Faktocraft.MODID + "." + name);
    this.icon = EntryStacks.of(icon);
    this.texture = texture;
    this.u = u;
    this.v = v;
    this.width = width;
    this.height = height;
    this.layout = layout;
  }

  @Override
  public CategoryIdentifier<MachineDisplay> getCategoryIdentifier() {
    return id;
  }

  @Override
  public Component getTitle() {
    return title;
  }

  @Override
  public Renderer getIcon() {
    return icon;
  }

  @Override
  public int getDisplayHeight() {
    return height + PADDING * 2;
  }

  @Override
  public int getDisplayWidth(MachineDisplay display) {
    return width + PADDING * 2;
  }

  public int halfX() {
    return width / 2;
  }

  @Override
  public List<Widget> setupDisplay(MachineDisplay display, Rectangle bounds) {
    List<Widget> out = new ArrayList<>();
    out.add(Widgets.createRecipeBase(bounds));
    Point origin = new Point(bounds.x + (bounds.width - width) / 2, bounds.y + (bounds.height - height) / 2);
    out.add(Widgets.createTexturedWidget(texture, origin.x, origin.y, u, v, width, height));
    layout.build(this, display, origin, out);
    return out;
  }

  public void slot(List<Widget> out, Point o, int x, int y, EntryIngredient ingredient, boolean input) {
    var slot = Widgets.createSlot(new Point(o.x + x, o.y + y)).entries(ingredient).disableBackground();
    out.add(input ? slot.markInput() : slot.markOutput());
  }

  public void tank(List<Widget> out, Point o, int x, int y, EntryIngredient ingredient, boolean input) {
    var slot = Widgets.createSlot(new Rectangle(o.x + x - 1, o.y + y - 1, 10, 31)).entries(ingredient)
        .disableBackground();
    out.add(input ? slot.markInput() : slot.markOutput());
  }

  public void frame(List<Widget> out, Point o, int x, int y, Identifier tex, int fu, int fv, int fw, int fh) {
    out.add(Widgets.createTexturedWidget(tex, o.x + x, o.y + y, fu, fv, fw, fh));
  }

  public void progress(List<Widget> out, Point o, int x, int y, int pu, int pv, int pw, int ph, int ticks) {
    int cycle = Math.max(1, ticks) * 50;
    out.add(Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) -> {
      float ratio = (System.currentTimeMillis() % cycle) / (float) cycle;
      int filled = Math.round(pw * ratio);
      if (filled > 0) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, PROCESS, o.x + x, o.y + y, pu, pv, filled, ph, 256, 256);
      }
    }));
  }

  private static void rising(List<Widget> out, Identifier tex, int x, int y, int tu, int tv, int tw, int th,
      int ticks) {
    int cycle = ticks * 50;
    out.add(Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) -> {
      float ratio = (System.currentTimeMillis() % cycle) / (float) cycle;
      int filled = Math.round(th * ratio);
      if (filled > 0) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, tex, x, y + th - filled, tu, tv + th - filled, tw, filled,
            256, 256);
      }
    }));
  }

  public void energy(List<Widget> out, Point o, int x, int y) {
    rising(out, JEI, o.x + x, o.y + y, 249, 0, 7, 37, ENERGY_TICKS);
  }

  public void fire(List<Widget> out, Point o, int x, int y) {
    rising(out, PROCESS, o.x + x, o.y + y, 67, 0, 16, 16, FIRE_TICKS);
  }

  public void text(List<Widget> out, Point o, int x, int y, String text, float scale, int color) {
    out.add(Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) -> GuiUtil.renderScaled(graphics, text,
        o.x + x, o.y + y, scale, color, false)));
  }

  public void text(List<Widget> out, Point o, int x, int y, String text) {
    text(out, o, x, y, text, TEXT_SCALE, TEXT_COLOR);
  }

  public void experience(List<Widget> out, Point o, int x, int y, MachineDisplay display) {
    if (display.info().experience() > 0) {
      text(out, o, x, y, display.info().experience() + " XP");
    }
  }

  public void power(List<Widget> out, Point o, int x, int y, MachineDisplay display) {
    text(out, o, x, y, display.info().powerCost() + " IE/T");
  }
}

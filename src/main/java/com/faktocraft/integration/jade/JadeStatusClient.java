package com.faktocraft.integration.jade;

import com.faktocraft.Faktocraft;
import com.faktocraft.client.render.StatusClientBridge;
import com.faktocraft.client.render.StatusMonitorContent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.ui.ResizeableElement;
import snownee.jade.api.ui.TextElement;
import snownee.jade.api.ui.TooltipAnimation;
import snownee.jade.gui.JadeLinearLayout;
import snownee.jade.impl.BlockAccessorImpl;
import snownee.jade.impl.Tooltip;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.ui.BoxElementImpl;
import snownee.jade.impl.ui.JadeUIInternal;
import snownee.jade.impl.ui.ProgressElement;
import snownee.jade.impl.ui.TextElementImpl;
import snownee.jade.overlay.OverlayRenderer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public final class JadeStatusClient implements StatusClientBridge {
  private static final int ICON_GAP = 4;
  private static final int NO_MOUSE = -10000;
  private static final Set<String> REPORTED = new HashSet<>();
  @Nullable
  private static final Field TEXT_FIELD = textField();

  private JadeStatusClient() {
  }

  public static StatusClientBridge create() {
    return new JadeStatusClient();
  }

  private record Built(BoxElementImpl box, @Nullable Element icon) {
  }

  private static final class TrackedElement extends ResizeableElement {
    private final ResizeableElement inner;

    private TrackedElement(ResizeableElement inner) {
      this.inner = inner;
      width = inner.getWidth();
      height = inner.getHeight();
      setFlexGrow(inner.getFlexGrow());
    }

    @Override
    public Identifier getTag() {
      return inner.getTag();
    }

    @Override
    public UnaryOperator<LayoutSettings> getSettings() {
      return inner.getSettings();
    }

    @Override
    public JadeLinearLayout.Align getAlignSelf() {
      return inner.getAlignSelf();
    }

    @Override
    public void setX(int x) {
      super.setX(x);
      inner.setX(x);
    }

    @Override
    public void setY(int y) {
      super.setY(y);
      inner.setY(y);
    }

    @Override
    public void setFreeSpace(int freeWidth, int freeHeight) {
      inner.setFreeSpace(freeWidth, freeHeight);
      width = inner.getWidth();
      height = inner.getHeight();
    }

    @Override
    public void updateSize() {
      inner.updateSize();
      width = inner.getWidth();
      height = inner.getHeight();
    }

    @Override
    public Component getNarration() {
      return inner.getNarration();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
      graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), StatusMonitorContent.TRACK_COLOR);
      inner.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }
  }

  @Nullable
  private static Field textField() {
    try {
      Field field = TextElementImpl.class.getDeclaredField("text");
      field.setAccessible(true);
      return field;
    } catch (ReflectiveOperationException | RuntimeException e) {
      Faktocraft.LOGGER.warn("Status monitor cannot wrap Jade text lines: {}", e.toString());
      return null;
    }
  }

  @Nullable
  private static Component textOf(TextElementImpl element) {
    if (TEXT_FIELD == null) {
      return null;
    }
    try {
      return (Component) TEXT_FIELD.get(element);
    } catch (ReflectiveOperationException | RuntimeException e) {
      return null;
    }
  }

  private static Component toComponent(FormattedText text) {
    MutableComponent result = Component.empty();
    text.visit((style, string) -> {
      result.append(Component.literal(string).withStyle(style));
      return Optional.empty();
    }, Style.EMPTY);
    return result;
  }

  private static void report(String uid, RuntimeException e) {
    if (REPORTED.add(uid)) {
      Faktocraft.LOGGER.warn("Jade provider {} failed on the status monitor: {}", uid, e.toString());
    }
  }

  private static void wrapText(Tooltip tooltip, int available) {
    Font font = Minecraft.getInstance().font;
    for (int i = 0; i < tooltip.lines.size(); i++) {
      List<LayoutElement> elements = tooltip.lines.get(i).elements();
      if (elements.size() != 1 || !(elements.get(0) instanceof TextElementImpl text)
          || text.getWidth() <= available) {
        continue;
      }
      Component component = textOf(text);
      if (component == null) {
        continue;
      }
      List<FormattedText> pieces = font.getSplitter().splitLines(component, available, Style.EMPTY);
      if (pieces.size() <= 1) {
        continue;
      }
      tooltip.lines.remove(i);
      for (int j = 0; j < pieces.size(); j++) {
        tooltip.add(i + j, JadeUI.text(toComponent(pieces.get(j))));
      }
      i += pieces.size() - 1;
    }
  }

  private static void retagBars(Tooltip tooltip, BlockPos pos) {
    String prefix = "monitor/" + Long.toUnsignedString(pos.asLong(), 36) + "/";
    for (Tooltip.Line line : tooltip.lines) {
      for (LayoutElement element : line.elements()) {
        if (element instanceof ProgressElement bar) {
          Identifier tag = bar.getTag();
          String path = tag == null ? "bar" : tag.getNamespace() + "/" + tag.getPath();
          bar.tag(Identifier.fromNamespaceAndPath(Faktocraft.MODID, prefix + path));
        }
      }
    }
  }

  private static void trackBars(Tooltip tooltip) {
    for (Tooltip.Line line : tooltip.lines) {
      List<LayoutElement> elements = line.elements();
      for (int i = 0; i < elements.size(); i++) {
        if (elements.get(i) instanceof ProgressElement bar) {
          try {
            elements.set(i, new TrackedElement(bar));
          } catch (UnsupportedOperationException ignored) {
            return;
          }
        }
      }
    }
  }

  private static void fitWidth(BoxElementImpl box, Tooltip tooltip, int available) {
    boolean changed = false;
    for (Tooltip.Line line : tooltip.lines) {
      List<LayoutElement> elements = line.elements();
      if (elements.isEmpty()) {
        continue;
      }
      int minX = Integer.MAX_VALUE;
      int maxX = Integer.MIN_VALUE;
      int textWidth = 0;
      List<TextElement> texts = new ArrayList<>();
      for (LayoutElement element : elements) {
        minX = Math.min(minX, element.getX());
        maxX = Math.max(maxX, element.getX() + element.getWidth());
        if (element instanceof TextElement text) {
          texts.add(text);
          textWidth += element.getWidth();
        }
      }
      int lineWidth = maxX - minX;
      if (lineWidth <= available || texts.isEmpty() || textWidth <= 0) {
        continue;
      }
      float scale = (available - (lineWidth - textWidth)) / (float) textWidth;
      if (scale <= 0.0F || scale >= 1.0F) {
        continue;
      }
      for (TextElement text : texts) {
        text.scale(scale);
      }
      changed = true;
    }
    if (changed) {
      box.updateSize();
    }
  }

  private static void fitHeight(BoxElementImpl box, Tooltip tooltip, int height) {
    while (box.getHeight() > height && !tooltip.lines.isEmpty()) {
      tooltip.lines.remove(tooltip.lines.size() - 1);
      box.updateSize();
    }
  }

  @Override
  @Nullable
  public Object build(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
      CompoundTag data) {
    Player player = Minecraft.getInstance().player;
    if (player == null || !JadeStatusBridge.collectedBy(data)) {
      return null;
    }
    BlockAccessor accessor = new BlockAccessorImpl.Builder().level(level).player(player).serverData(data)
        .serverConnected(true).showDetails(false).hit(JadeStatusBridge.hit(pos)).blockState(state)
        .blockEntity(() -> blockEntity).build();
    IPluginConfig config = IWailaConfig.get().plugin();
    boolean accessibility = IWailaConfig.get().accessibility().getEnableAccessibilityPlugin();
    Predicate<IComponentProvider<? extends Accessor<?>>> enabled =
        provider -> (accessibility || !JadeIds.isAccess(provider.getUid())) && config.get(provider);
    Tooltip tooltip = new Tooltip();
    List<IComponentProvider<BlockAccessor>> providers = WailaClientRegistration.instance()
        .getBlockProviders(state.getBlock(), enabled);
    for (IComponentProvider<BlockAccessor> provider : providers) {
      try {
        JadeUIInternal.setContextUid(provider.getUid());
        provider.appendTooltip(tooltip, accessor, config);
      } catch (RuntimeException e) {
        report(String.valueOf(provider.getUid()), e);
      } finally {
        JadeUIInternal.setContextUid(null);
      }
    }
    if (tooltip.isEmpty()) {
      return null;
    }
    Element icon = JadeUI.item(new ItemStack(state.getBlock()));
    for (IComponentProvider<BlockAccessor> provider : WailaClientRegistration.instance()
        .getBlockIconProviders(state.getBlock(), enabled)) {
      try {
        Element custom = provider.getIcon(accessor, config, icon);
        if (custom != null && !JadeUI.isEmptyElement(custom)) {
          icon = custom;
        }
      } catch (RuntimeException e) {
        report(String.valueOf(provider.getUid()), e);
      }
    }
    if (JadeUI.isEmptyElement(icon)) {
      icon = null;
    }
    int iconWidth = icon != null ? icon.getWidth() + ICON_GAP : 0;
    int available = StatusMonitorContent.CONTENT_W - iconWidth;
    wrapText(tooltip, available);
    retagBars(tooltip, pos);
    trackBars(tooltip);
    BoxStyle style = BoxStyle.transparent().copy();
    Arrays.fill(style.padding, 0);
    style.borderWidth = 0;
    BoxElementImpl box = new BoxElementImpl(tooltip, style);
    fitWidth(box, tooltip, available);
    fitHeight(box, tooltip, StatusMonitorContent.CONTENT_H);
    box.setFreeSpace(available, box.getHeight());
    box.setX(iconWidth);
    box.setY(0);
    return new Built(box, icon);
  }

  @Override
  public boolean render(Object built, GuiGraphicsExtractor graphics, int width, int height) {
    if (!(built instanceof Built entry)) {
      return false;
    }
    TooltipAnimation animation = OverlayRenderer.animation;
    float alpha = animation.alpha;
    float showHideAlpha = animation.showHideAlpha;
    animation.alpha = 1.0F;
    animation.showHideAlpha = 1.0F;
    try {
      Element icon = entry.icon();
      if (icon != null) {
        icon.setX(0);
        icon.setY(0);
        icon.extractRenderState(graphics, NO_MOUSE, NO_MOUSE, 1.0F);
      }
      entry.box().extractRenderState(graphics, NO_MOUSE, NO_MOUSE, 1.0F);
    } finally {
      animation.alpha = alpha;
      animation.showHideAlpha = showHideAlpha;
    }
    return true;
  }
}

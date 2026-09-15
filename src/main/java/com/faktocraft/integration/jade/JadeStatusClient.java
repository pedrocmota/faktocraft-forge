package com.faktocraft.integration.jade;

import com.faktocraft.Faktocraft;
import com.faktocraft.client.render.StatusClientBridge;
import com.faktocraft.client.render.StatusMonitorContent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.impl.BlockAccessorImpl;
import snownee.jade.impl.Tooltip;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.ui.ProgressElement;
import snownee.jade.impl.ui.TextElement;
import snownee.jade.overlay.OverlayRenderer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class JadeStatusClient implements StatusClientBridge {

  private static final int ICON_GAP = 4;
  private static final int BOX_MARGIN = 2;
  private static final Set<String> REPORTED = new HashSet<>();

  private JadeStatusClient() {
  }

  public static StatusClientBridge create() {
    return new JadeStatusClient();
  }

  private record Built(Tooltip tooltip, @Nullable IElement icon) {
  }

  private static final class TrackedElement extends Element {
    private final IElement inner;

    private TrackedElement(IElement inner) {
      this.inner = inner;
    }

    @Override
    public Vec2 getSize() {
      return inner.getSize();
    }

    @Override
    public Vec2 getCachedSize() {
      return inner.getCachedSize();
    }

    @Override
    public Vec2 getTranslation() {
      return inner.getTranslation();
    }

    @Override
    public IElement.Align getAlignment() {
      return inner.getAlignment();
    }

    @Override
    public ResourceLocation getTag() {
      return inner.getTag();
    }

    @Override
    public String getMessage() {
      return inner.getMessage();
    }

    @Override
    public void render(GuiGraphics graphics, float x, float y, float maxX, float maxY) {
      Vec2 size = inner.getCachedSize();
      graphics.fill(Math.round(x), Math.round(y), Math.round(maxX), Math.round(y + size.y - BOX_MARGIN),
          StatusMonitorContent.TRACK_COLOR);
      inner.render(graphics, x, y, maxX, maxY);
    }
  }

  private static void report(String uid, RuntimeException e) {
    if (REPORTED.add(uid)) {
      Faktocraft.LOGGER.warn("Jade provider {} failed on the status monitor: {}", uid, e.toString());
    }
  }

  private static void wrapText(Tooltip tooltip, int available) {
    Font font = Minecraft.getInstance().font;
    for (int i = 0; i < tooltip.lines.size(); i++) {
      Tooltip.Line line = tooltip.lines.get(i);
      List<IElement> left = line.getAlignedElements(IElement.Align.LEFT);
      if (left.size() != 1 || !line.getAlignedElements(IElement.Align.RIGHT).isEmpty()
          || !(left.get(0) instanceof TextElement text) || text.getSize().x <= available) {
        continue;
      }
      List<FormattedText> pieces = font.getSplitter().splitLines(text.text, available, Style.EMPTY);
      if (pieces.size() <= 1) {
        continue;
      }
      tooltip.lines.remove(i);
      for (int j = 0; j < pieces.size(); j++) {
        tooltip.add(i + j, new TextElement(pieces.get(j)));
      }
      i += pieces.size() - 1;
    }
  }

  private static void trackBars(Tooltip tooltip) {
    for (Tooltip.Line line : tooltip.lines) {
      for (IElement.Align align : IElement.Align.values()) {
        List<IElement> elements = line.getAlignedElements(align);
        for (int i = 0; i < elements.size(); i++) {
          if (elements.get(i) instanceof ProgressElement) {
            try {
              elements.set(i, new TrackedElement(elements.get(i)));
            } catch (UnsupportedOperationException ignored) {
              return;
            }
          }
        }
      }
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
    Tooltip tooltip = new Tooltip();
    List<IBlockComponentProvider> providers = WailaClientRegistration.INSTANCE
        .getBlockProviders(state.getBlock(), provider -> PluginConfig.INSTANCE.get(provider));
    for (IBlockComponentProvider provider : providers) {
      try {
        provider.appendTooltip(tooltip, accessor, PluginConfig.INSTANCE);
      } catch (RuntimeException e) {
        report(String.valueOf(provider.getUid()), e);
      }
    }
    if (tooltip.lines.isEmpty()) {
      return null;
    }
    trackBars(tooltip);
    IElement icon = IElementHelper.get().item(new ItemStack(state.getBlock()));
    for (IBlockComponentProvider provider : WailaClientRegistration.INSTANCE
        .getBlockIconProviders(state.getBlock(), provider -> PluginConfig.INSTANCE.get(provider))) {
      try {
        IElement custom = provider.getIcon(accessor, PluginConfig.INSTANCE, icon);
        if (custom != null) {
          icon = custom;
        }
      } catch (RuntimeException e) {
        report(String.valueOf(provider.getUid()), e);
      }
    }
    float iconWidth = icon != null ? icon.getSize().x + ICON_GAP : 0.0F;
    wrapText(tooltip, Math.round(StatusMonitorContent.CONTENT_W - iconWidth));
    return new Built(tooltip, icon);
  }

  @Override
  public boolean render(Object built, GuiGraphics graphics, int width, int height) {
    if (!(built instanceof Built entry)) {
      return false;
    }
    float alpha = OverlayRenderer.alpha;
    OverlayRenderer.alpha = 1.0F;
    try {
      float x = 0.0F;
      if (entry.icon() != null) {
        Vec2 iconSize = entry.icon().getSize();
        entry.icon().render(graphics, 0.0F, 0.0F, iconSize.x, iconSize.y);
        x = iconSize.x + ICON_GAP;
      }
      float y = 0.0F;
      for (Tooltip.Line line : entry.tooltip().lines) {
        Vec2 size = line.getSize();
        float available = width - x;
        float scale = size.x > available && size.x > 0.0F ? available / size.x : 1.0F;
        if (y + size.y * scale > height) {
          break;
        }
        if (scale < 1.0F) {
          graphics.pose().pushPose();
          graphics.pose().translate(x, y, 0.0F);
          graphics.pose().scale(scale, scale, 1.0F);
          line.render(graphics, 0.0F, 0.0F, size.x, size.y);
          graphics.pose().popPose();
        } else {
          line.render(graphics, x, y, width, y + size.y);
        }
        y += size.y * scale;
      }
    } finally {
      OverlayRenderer.alpha = alpha;
    }
    return true;
  }
}

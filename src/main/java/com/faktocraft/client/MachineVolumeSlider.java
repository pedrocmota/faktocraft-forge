package com.faktocraft.client;

import com.faktocraft.Faktocraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = Faktocraft.MODID, value = Dist.CLIENT)
public final class MachineVolumeSlider {
  private static AbstractSliderButton machineSlider;

  private MachineVolumeSlider() {
  }

  @SubscribeEvent
  public static void onScreenInit(ScreenEvent.Init.Post event) {
    machineSlider = null;
    if (!(event.getScreen() instanceof SoundOptionsScreen)) {
      return;
    }
    double initial = ClientSoundConfig.machineVolume();
    machineSlider = new AbstractSliderButton(0, -1000, 150, 20, machineVolumeLabel(initial), initial) {
      @Override
      protected void updateMessage() {
        setMessage(machineVolumeLabel(this.value));
      }

      @Override
      protected void applyValue() {
        ClientSoundConfig.setMachineVolume((float) this.value);
      }
    };
    machineSlider.visible = false;
    event.addListener(machineSlider);
  }

  @SubscribeEvent
  public static void onScreenRender(ScreenEvent.Render.Pre event) {
    if (machineSlider == null || !(event.getScreen() instanceof SoundOptionsScreen screen)) {
      return;
    }
    AbstractSliderButton anchor = null;
    for (GuiEventListener child : screen.children()) {
      if (child instanceof OptionsList list) {
        for (GuiEventListener row : list.children()) {
          if (!(row instanceof ContainerEventHandler container)) {
            continue;
          }
          for (GuiEventListener w : container.children()) {
            if (w instanceof AbstractSliderButton slider
                && slider.getX() < screen.width / 2
                && (anchor == null || slider.getY() > anchor.getY())) {
              anchor = slider;
            }
          }
        }
      }
    }
    if (anchor == null || anchor.getY() <= 0) {
      machineSlider.visible = false;
      return;
    }
    machineSlider.setX(anchor.getX() + 160);
    machineSlider.setY(anchor.getY());
    machineSlider.visible = true;
  }

  private static Component machineVolumeLabel(double value) {
    String pct = value <= 0 ? I18n.get("options.off") : (int) (value * 100) + "%";
    return Component.translatable("options." + Faktocraft.MODID + ".machine_volume").append(": " + pct);
  }
}

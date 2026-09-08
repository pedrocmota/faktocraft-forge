package com.faktocraft.integration.rei;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.machines.distillery.DistilleryRegistry;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.registries.machines.M2Registry;
import com.faktocraft.common.registries.machines.M3Registry;
import com.faktocraft.common.registries.machines.M4Registry;
import com.faktocraft.common.util.TextComponentUtil;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.network.chat.Component;
import java.text.NumberFormat;
import java.util.List;
import static com.faktocraft.common.util.Constants.JEI;
import static com.faktocraft.common.util.Constants.JEI_2;
import static com.faktocraft.common.util.Constants.JEI_LARGE;
import static com.faktocraft.common.util.Constants.JEI_LARGE_2;
import static com.faktocraft.common.util.Constants.PROCESS;

public final class ReiCategories {

  public static final CategoryIdentifier<MachineDisplay> CRUSHING = id("crushing");
  public static final CategoryIdentifier<MachineDisplay> COMPRESSING = id("compressing");
  public static final CategoryIdentifier<MachineDisplay> EXTRACTING = id("extracting");
  public static final CategoryIdentifier<MachineDisplay> FLUID_EXTRUDING = id("fluid_extruding");
  public static final CategoryIdentifier<MachineDisplay> SAWING = id("sawing");
  public static final CategoryIdentifier<MachineDisplay> ALLOY_SMELTING = id("alloy_smelting");
  public static final CategoryIdentifier<MachineDisplay> CIRCUIT_ASSEMBLING = id("circuit_assembling");
  public static final CategoryIdentifier<MachineDisplay> RECYCLING = id("recycling");
  public static final CategoryIdentifier<MachineDisplay> FLUID_ENRICHING = id("fluid_enriching");
  public static final CategoryIdentifier<MachineDisplay> ORE_WASHING = id("ore_washing");
  public static final CategoryIdentifier<MachineDisplay> POLYMERIZING = id("polymerizing");
  public static final CategoryIdentifier<MachineDisplay> THERMAL_CENTRIFUGING = id("thermal_centrifuging");
  public static final CategoryIdentifier<MachineDisplay> SCANNER = id("scanner");
  public static final CategoryIdentifier<MachineDisplay> SCRAP_BOX = id("scrap_box");
  public static final CategoryIdentifier<MachineDisplay> ROLLING = id("rolling");
  public static final CategoryIdentifier<MachineDisplay> CUTTING = id("cutting");
  public static final CategoryIdentifier<MachineDisplay> EXTRUDING = id("extruding");
  public static final CategoryIdentifier<MachineDisplay> FERMENTING = id("fermenting");
  public static final CategoryIdentifier<MachineDisplay> DISTILLING = id("distilling");
  public static final CategoryIdentifier<MachineDisplay> MATTER_FABRICATING = id("matter_fabricating");

  public static final List<CategoryIdentifier<MachineDisplay>> ALL = List.of(CRUSHING, COMPRESSING, EXTRACTING,
      FLUID_EXTRUDING, SAWING, ALLOY_SMELTING, CIRCUIT_ASSEMBLING, RECYCLING, FLUID_ENRICHING, ORE_WASHING,
      POLYMERIZING, THERMAL_CENTRIFUGING, SCANNER, SCRAP_BOX, ROLLING, CUTTING, EXTRUDING, FERMENTING, DISTILLING,
      MATTER_FABRICATING);

  private static final int TANK_U = 160;
  private static final int TANK_V = 165;
  private static final int SLOT_U = 180;
  private static final int ARROW_U = 200;

  private ReiCategories() {
  }

  private static CategoryIdentifier<MachineDisplay> id(String path) {
    return CategoryIdentifier.of(Faktocraft.MODID, path);
  }

  private static MachineCategory.Layout simple(int progressV, boolean bonus, int outputY, int progressHeight) {
    return (c, d, o, out) -> {
      int halfX = c.halfX();
      c.slot(out, o, 9, 19, d.in(0), true);
      c.slot(out, o, halfX + 8, outputY, d.out(0), false);
      if (bonus && d.outputCount() > 1) {
        c.slot(out, o, halfX + 8, 29, d.out(1), false);
      }
      c.progress(out, o, halfX - 24, progressHeight == 18 ? 18 : 19, 25, progressV, 24, progressHeight,
          d.info().duration());
      c.energy(out, o, halfX + 39, 7);
      c.experience(out, o, 0, 0, d);
      c.power(out, o, 0, 48, d);
    };
  }

  private static MachineCategory.Layout triple(boolean fire) {
    return (c, d, o, out) -> {
      int halfX = c.halfX();
      int[][] spots = { { 5, 19 }, { 26, 7 }, { 47, 19 } };
      for (int i = 0; i < Math.min(3, d.inputCount()); i++) {
        c.slot(out, o, spots[i][0], spots[i][1], d.in(i), true);
      }
      c.slot(out, o, 103, 19, d.out(0), false);
      c.progress(out, o, halfX - 6, 19, 25, 0, 24, 16, d.info().duration());
      c.energy(out, o, halfX + 58, 7);
      if (fire) {
        c.fire(out, o, halfX - 50, 27);
        c.experience(out, o, 0, 0, d);
      }
      c.power(out, o, 0, 48, d);
    };
  }

  private static void stackedOutputs(MachineCategory c, MachineDisplay d, me.shedaniel.math.Point o, int x,
      List<me.shedaniel.rei.api.client.gui.widgets.Widget> out) {
    int count = d.outputCount();
    int startPos = count == 2 ? 11 : 19;
    for (int i = 0; i < count; i++) {
      c.slot(out, o, x, (i * 16 + (i == 0 ? 0 : 1)) + startPos, d.out(i), false);
    }
  }

  public static List<MachineCategory> all() {
    return List.of(
        new MachineCategory(CRUSHING, "crushing", M2Registry.CRUSHER, JEI, 0, 0, 114, 54, simple(17, true, 6, 16)),
        new MachineCategory(COMPRESSING, "compressing", M2Registry.COMPRESSOR, JEI, 0, 55, 114, 54,
            simple(34, true, 6, 16)),
        new MachineCategory(EXTRACTING, "extracting", M2Registry.EXTRACTOR, JEI, 0, 110, 114, 54,
            simple(51, true, 6, 16)),
        new MachineCategory(FLUID_EXTRUDING, "fluid_extruding", M3Registry.EXTRUDER, JEI_LARGE, 0, 55, 152, 54,
            (c, d, o, out) -> {
              c.tank(out, o, 11, 12, d.in(0), true);
              c.tank(out, o, 52, 12, d.in(1), true);
              c.slot(out, o, 103, 19, d.out(0), false);
              c.progress(out, o, c.halfX() - 6, 19, 25, 51, 24, 16, d.info().duration());
              c.energy(out, o, c.halfX() + 58, 7);
              c.experience(out, o, 0, 0, d);
              c.power(out, o, 0, 48, d);
            }),
        new MachineCategory(SAWING, "sawing", M2Registry.SAWMILL, JEI, 0, 165, 114, 54, simple(68, true, 6, 16)),
        new MachineCategory(ALLOY_SMELTING, "alloy_smelting", M3Registry.ALLOY_SMELTER, JEI_LARGE, 0, 0, 152, 54,
            triple(true)),
        new MachineCategory(CIRCUIT_ASSEMBLING, "circuit_assembling", M3Registry.CIRCUIT_ASSEMBLER, JEI_LARGE, 0, 0,
            152, 54, triple(false)),
        new MachineCategory(RECYCLING, "recycling", M2Registry.RECYCLER, JEI, 117, 0, 114, 54, (c, d, o, out) -> {
          c.slot(out, o, 9, 19, d.in(0), true);
          c.slot(out, o, c.halfX() + 8, 19, d.out(0), false);
          c.progress(out, o, c.halfX() - 24, 19, 25, 85, 24, 16, d.info().duration());
          c.energy(out, o, c.halfX() + 39, 7);
          c.power(out, o, 0, 48, d);
        }),
        new MachineCategory(FLUID_ENRICHING, "fluid_enriching", M3Registry.FLUID_ENRICHER, JEI_LARGE, 0, 165, 152,
            54, (c, d, o, out) -> {
              if (!d.info().dual()) {
                c.frame(out, o, 7, 18, JEI_LARGE, SLOT_U, TANK_V, 18, 18);
                c.frame(out, o, 27, 21, JEI_LARGE, ARROW_U, TANK_V, 15, 13);
                c.frame(out, o, 46, 8, JEI_LARGE, TANK_U, TANK_V, 16, 37);
                c.slot(out, o, 8, 19, d.in(0), true);
                c.tank(out, o, 50, 12, d.in(1), true);
              } else {
                c.frame(out, o, 7, 9, JEI_LARGE, SLOT_U, TANK_V, 18, 18);
                c.slot(out, o, 8, 10, d.in(0), true);
                if (!d.in(2).isEmpty()) {
                  c.frame(out, o, 7, 27, JEI_LARGE, SLOT_U, TANK_V, 18, 18);
                  c.slot(out, o, 8, 28, d.in(2), true);
                }
                c.frame(out, o, 33, 8, JEI_LARGE, TANK_U, TANK_V, 16, 37);
                c.tank(out, o, 37, 12, d.in(1), true);
                if (!d.in(3).isEmpty()) {
                  c.frame(out, o, 51, 8, JEI_LARGE, TANK_U, TANK_V, 16, 37);
                  c.tank(out, o, 55, 12, d.in(3), true);
                }
              }
              c.tank(out, o, 106, 12, d.out(0), false);
              c.progress(out, o, c.halfX() - 6, 19, 25, 0, 24, 16, d.info().duration());
              c.energy(out, o, c.halfX() + 58, 7);
              c.experience(out, o, 3, 1, d);
              c.power(out, o, 3, 46, d);
            }),
        new MachineCategory(ORE_WASHING, "ore_washing", M3Registry.ORE_WASHING_PLANT, JEI_LARGE_2, 0, 0, 152, 54,
            (c, d, o, out) -> {
              c.slot(out, o, 49, 19, d.in(0), true);
              c.tank(out, o, 11, 12, d.in(1), true);
              if (!d.in(2).isEmpty()) {
                c.tank(out, o, 28, 12, d.in(2), true);
              }
              stackedOutputs(c, d, o, 103, out);
              c.progress(out, o, c.halfX() - 2, 17, 20, 102, 19, 19, d.info().duration());
              c.energy(out, o, c.halfX() + 58, 7);
              c.experience(out, o, 3, 1, d);
              c.power(out, o, 3, 46, d);
            }),
        new MachineCategory(POLYMERIZING, "polymerizing", M3Registry.POLYMERIZER, JEI_LARGE_2, 0, 110, 152, 54,
            (c, d, o, out) -> {
              c.frame(out, o, 34, 8, JEI_LARGE, TANK_U, TANK_V, 16, 37);
              c.frame(out, o, 59, 18, JEI_LARGE, SLOT_U, TANK_V, 18, 18);
              c.frame(out, o, 105, 18, JEI_LARGE, SLOT_U, TANK_V, 18, 18);
              c.frame(out, o, 80, 19, PROCESS, 0, 0, 24, 16);
              c.tank(out, o, 38, 12, d.in(0), true);
              c.slot(out, o, 60, 19, d.in(1), true);
              c.slot(out, o, 106, 19, d.out(0), false);
              c.progress(out, o, 80, 19, 25, 0, 24, 16, d.info().duration());
              c.energy(out, o, c.halfX() + 58, 7);
              c.experience(out, o, 3, 1, d);
              c.power(out, o, 3, 46, d);
            }),
        new MachineCategory(THERMAL_CENTRIFUGING, "thermal_centrifuging", M3Registry.THERMAL_CENTRIFUGE, JEI, 117,
            55, 114, 54, (c, d, o, out) -> {
              c.slot(out, o, 9, 19, d.in(0), true);
              stackedOutputs(c, d, o, 65, out);
              c.progress(out, o, c.halfX() - 24, 19, 25, 0, 24, 16, d.info().duration());
              c.energy(out, o, c.halfX() + 39, 7);
              c.experience(out, o, 0, 0, d);
              c.text(out, o, 3, 39, d.info().temperature() + "C", MachineCategory.TEXT_SCALE, 0xb31313);
              c.power(out, o, 0, 48, d);
            }),
        new MachineCategory(SCANNER, "scanner", M4Registry.SCANNER, JEI_LARGE_2, 0, 55, 152, 54, (c, d, o, out) -> {
          c.slot(out, o, 23, 19, d.out(0), false);
          c.progress(out, o, c.halfX() - 76, 5, 62, 122, 61, 42, d.info().duration());
          c.energy(out, o, c.halfX() + 58, 7);
          c.experience(out, o, 0, -2, d);
          String prefix = "gui." + Faktocraft.MODID + ".scanner.";
          c.text(out, o, 67, 18, Component.translatable(prefix + "replication_cost").getString(), 0.65F, 0x00a200);
          c.text(out, o, 67, 25, Component.translatable(prefix + "matter_cost").getString() + " "
              + d.info().matterCost() + " mB", 0.65F, 0x00a200);
          c.text(out, o, 67, 32, Component.translatable(prefix + "energy_cost").getString() + " "
              + TextComponentUtil.getFormattedEnergyUnit(d.info().energyCost()) + " IE", 0.65F, 0x00a200);
          c.power(out, o, 0, 48, d);
        }),
        new MachineCategory(SCRAP_BOX, "scrap_box", ModItems.SCRAP_BOX, JEI, 0, 220, 92, 28, (c, d, o, out) -> {
          c.slot(out, o, 9, 6, d.in(0), true);
          c.slot(out, o, 65, 6, d.out(0), false);
          c.text(out, o, 33, 23, d.info().chanceText());
        }),
        new MachineCategory(ROLLING, "rolling", M3Registry.METAL_FORMER, JEI_2, 0, 55, 114, 54,
            simple(194, false, 19, 18)),
        new MachineCategory(CUTTING, "cutting", M3Registry.METAL_FORMER, JEI_2, 0, 110, 114, 54,
            simple(213, false, 19, 18)),
        new MachineCategory(EXTRUDING, "extruding", M3Registry.METAL_FORMER, JEI_2, 0, 0, 114, 54,
            simple(175, false, 19, 18)),
        new MachineCategory(FERMENTING, "fermenting", M3Registry.FERMENTER, JEI_LARGE, 0, 165, 152, 54,
            (c, d, o, out) -> {
              c.frame(out, o, 7, 18, JEI_LARGE, SLOT_U, TANK_V, 18, 18);
              c.frame(out, o, 27, 21, JEI_LARGE, ARROW_U, TANK_V, 15, 13);
              c.frame(out, o, 46, 8, JEI_LARGE, TANK_U, TANK_V, 16, 37);
              c.slot(out, o, 8, 19, d.in(0), true);
              c.tank(out, o, 50, 12, d.in(1), true);
              c.tank(out, o, 106, 12, d.out(0), false);
              c.progress(out, o, c.halfX() - 6, 19, 25, 0, 24, 16, d.info().duration());
              c.energy(out, o, c.halfX() + 58, 7);
              c.text(out, o, 3, 1, d.info().duration() / 20 + "s");
              c.power(out, o, 3, 46, d);
            }),
        new MachineCategory(DISTILLING, "distilling", DistilleryRegistry.DISTILLERY, JEI_LARGE, 0, 165, 152, 54,
            (c, d, o, out) -> {
              c.frame(out, o, 4, 8, JEI_LARGE, TANK_U, TANK_V, 16, 37);
              c.frame(out, o, 22, 8, JEI_LARGE, TANK_U, TANK_V, 16, 37);
              c.frame(out, o, 40, 8, JEI_LARGE, TANK_U, TANK_V, 16, 37);
              c.frame(out, o, 73, 35, JEI_LARGE, SLOT_U, TANK_V, 18, 18);
              c.tank(out, o, 8, 12, d.in(0), true);
              c.tank(out, o, 26, 12, d.in(1), true);
              c.tank(out, o, 44, 12, d.in(2), true);
              c.tank(out, o, 106, 12, d.out(0), false);
              c.slot(out, o, 74, 36, d.out(1), false);
              c.progress(out, o, c.halfX() - 6, 19, 25, 0, 24, 16, d.info().duration());
              c.energy(out, o, c.halfX() + 58, 7);
              c.text(out, o, 3, 1, d.info().duration() / 20 + "s");
              c.power(out, o, 3, 46, d);
              c.text(out, o, 93, 41, (int) (d.info().chance() * 100) + "%");
            }),
        new MachineCategory(MATTER_FABRICATING, "matter_fabricating", M4Registry.MATTER_FABRICATOR, JEI_LARGE, 0,
            165, 152, 54, (c, d, o, out) -> {
              c.frame(out, o, 7, 18, JEI_LARGE, SLOT_U, TANK_V, 18, 18);
              c.slot(out, o, 8, 19, d.in(0), true);
              c.tank(out, o, 106, 12, d.out(0), false);
              c.progress(out, o, c.halfX() - 6, 19, 25, 0, 24, 16, d.info().duration());
              c.energy(out, o, c.halfX() + 58, 7);
              NumberFormat format = NumberFormat.getIntegerInstance();
              c.text(out, o, 3, 1, format.format(d.info().energyCost()) + " IE");
              c.text(out, o, 3, 46, format.format(d.info().matterCost()) + " mB");
            }));
  }
}

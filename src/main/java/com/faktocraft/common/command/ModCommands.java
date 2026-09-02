package com.faktocraft.common.command;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.fluid.ModFluids;
import com.faktocraft.common.registries.RegistrationHandler;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Mod.EventBusSubscriber(modid = Faktocraft.MODID)
public final class ModCommands {

  private ModCommands() {
  }

  @SubscribeEvent
  public static void onRegisterCommands(RegisterCommandsEvent event) {
    var dispatcher = event.getDispatcher();
    dispatcher.register(Commands.literal("faktocraft")
        .requires(source -> source.hasPermission(2))
        .then(NetInfoCommand.build())
        .then(Commands.literal("netrepair")
            .executes(c -> {
              var level = c.getSource().getLevel();
              int count = com.faktocraft.common.energy.provider.EnergyCore.get(level).repairNetworks();
              c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(
                  "Redes reconstruidas. Total de redes ativas: " + count), false);
              return count;
            }))
        .then(Commands.literal("locateoil")
            .executes(c -> locate(c.getSource(), ModFluids.OIL.block(), 12))
            .then(Commands.argument("raioChunks", IntegerArgumentType.integer(1, 32))
                .executes(c -> locate(c.getSource(), ModFluids.OIL.block(),
                    IntegerArgumentType.getInteger(c, "raioChunks")))))
        .then(Commands.literal("locatepocket")
            .executes(c -> locatePocket(c.getSource(), 1000))
            .then(Commands.argument("raioChunks", IntegerArgumentType.integer(16, 5000))
                .executes(c -> locatePocket(c.getSource(),
                    IntegerArgumentType.getInteger(c, "raioChunks")))))
        .then(Commands.literal("locateore")
            .then(Commands.argument("minerio", com.mojang.brigadier.arguments.StringArgumentType.word())
                .suggests(ModCommands::suggestOres)
                .executes(c -> locateOre(c.getSource(),
                    com.mojang.brigadier.arguments.StringArgumentType.getString(c, "minerio"), 12))
                .then(Commands.argument("raioChunks", IntegerArgumentType.integer(1, 32))
                    .executes(c -> locateOre(c.getSource(),
                        com.mojang.brigadier.arguments.StringArgumentType.getString(c, "minerio"),
                        IntegerArgumentType.getInteger(c, "raioChunks")))))));
  }

  private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestOres(
      com.mojang.brigadier.context.CommandContext<CommandSourceStack> context,
      com.mojang.brigadier.suggestion.SuggestionsBuilder builder) {
    var names = net.minecraftforge.registries.ForgeRegistries.BLOCKS.tags().getTagNames()
        .filter(tag -> tag.location().getNamespace().equals("forge")
            && tag.location().getPath().startsWith("ores/"))
        .map(tag -> tag.location().getPath().substring("ores/".length()))
        .toList();
    return net.minecraft.commands.SharedSuggestionProvider.suggest(names, builder);
  }

  private static int locateOre(CommandSourceStack source, String oreName, int chunkRadius) {
    var tag = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,
        new net.minecraft.resources.ResourceLocation("forge", "ores/" + oreName));
    var tags = net.minecraftforge.registries.ForgeRegistries.BLOCKS.tags();
    if (tags == null || !tags.isKnownTagName(tag)) {
      source.sendFailure(Component.literal("forge:ores/" + oreName + " ?"));
      return 0;
    }
    return locate(source, Component.literal("#forge:ores/" + oreName),
        state -> state.is(tag), chunkRadius);
  }

  private record Hit(BlockPos pos, int count, double distSq) {
  }

  private record GiantLayer(String id, int rarity, boolean boosted) {
  }

  private static final List<GiantLayer> GIANT_LAYERS = List.of(
      new GiantLayer("giant_oil_pocket_placed", 600, false),
      new GiantLayer("giant_oil_pocket_ocean_placed", 1200, true),
      new GiantLayer("giant_oil_pocket_deep_placed", 240, true),
      new GiantLayer("giant_oil_pocket_hot_placed", 429, true));

  private static final int GIANT_MIN_Y = -44;
  private static final int GIANT_MAX_Y = -4;

  private static int locatePocket(CommandSourceStack source, int chunkRadius) {
    ServerLevel level = source.getLevel();
    var registry = level.registryAccess()
        .registryOrThrow(net.minecraft.core.registries.Registries.PLACED_FEATURE);
    var generator = level.getChunkSource().getGenerator();

    var steps = net.minecraft.world.level.biome.FeatureSorter.buildFeaturesPerStep(
        java.util.List.copyOf(generator.getBiomeSource().possibleBiomes()),
        holder -> holder.value().getGenerationSettings().features(), true);
    int stepIndex = net.minecraft.world.level.levelgen.GenerationStep.Decoration.LAKES.ordinal();
    if (steps.size() <= stepIndex) {
      source.sendFailure(Component.literal("passo LAKES ausente"));
      return 0;
    }
    var indexMapping = steps.get(stepIndex).indexMapping();

    record ResolvedLayer(net.minecraft.world.level.levelgen.placement.PlacedFeature placed,
        int featureIndex, int rarity, boolean boosted) {
    }
    List<ResolvedLayer> layers = new ArrayList<>();
    for (GiantLayer layer : GIANT_LAYERS) {
      var placed = registry.get(RegistrationHandler.id(layer.id()));
      if (placed == null) {
        source.sendFailure(Component.literal(layer.id() + " ausente do registro"));
        return 0;
      }
      layers.add(new ResolvedLayer(placed, indexMapping.applyAsInt(placed),
          layer.rarity(), layer.boosted()));
    }

    BlockPos center = BlockPos.containing(source.getPosition());
    ChunkPos centerChunk = new ChunkPos(center);
    var sampler = level.getChunkSource().randomState().sampler();
    long worldSeed = level.getSeed();

    record Predicted(BlockPos pos, boolean richBiome, double distSq) {
    }
    List<Predicted> found = new ArrayList<>();

    outer: for (int r = 0; r <= chunkRadius; r++) {
      for (int cx = centerChunk.x - r; cx <= centerChunk.x + r; cx++) {
        for (int cz = centerChunk.z - r; cz <= centerChunk.z + r; cz++) {
          if (Math.max(Math.abs(cx - centerChunk.x), Math.abs(cz - centerChunk.z)) != r) {
            continue;
          }
          for (ResolvedLayer layer : layers) {
            BlockPos p = predictAt(level, generator, sampler, worldSeed, cx, cz,
                layer.featureIndex(), stepIndex, layer.rarity(), layer.placed());
            if (p != null) {
              found.add(new Predicted(p, layer.boosted(), center.distSqr(p)));
              break;
            }
          }
          if (found.size() >= 3) {
            break outer;
          }
        }
      }
    }

    if (found.isEmpty()) {
      source.sendSuccess(() -> Component
          .translatable("command." + Faktocraft.MODID + ".locate.pocket_none", chunkRadius)
          .withStyle(ChatFormatting.YELLOW), false);
      return 0;
    }
    source.sendSuccess(() -> Component
        .translatable("command." + Faktocraft.MODID + ".locate.pocket_header", chunkRadius)
        .withStyle(ChatFormatting.GREEN), false);
    for (Predicted hit : found) {
      String posText = hit.pos().getX() + " " + hit.pos().getY() + " " + hit.pos().getZ();
      Component label = Component.translatable("command." + Faktocraft.MODID
          + (hit.richBiome() ? ".locate.pocket_rich" : ".locate.pocket_normal"));
      Component entry = Component
          .translatable("command." + Faktocraft.MODID + ".locate.pocket_entry",
              label, posText, (int) Math.sqrt(hit.distSq()))
          .withStyle(style -> style
              .withColor(ChatFormatting.AQUA)
              .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp @s " + posText))
              .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                  Component.translatable("command." + Faktocraft.MODID + ".locate.teleport"))));
      source.sendSuccess(() -> entry, false);
    }
    return found.size();
  }

  @org.jetbrains.annotations.Nullable
  private static BlockPos predictAt(ServerLevel level,
      net.minecraft.world.level.chunk.ChunkGenerator generator,
      net.minecraft.world.level.biome.Climate.Sampler sampler,
      long worldSeed, int chunkX, int chunkZ, int featureIndex, int stepIndex, int rarity,
      net.minecraft.world.level.levelgen.placement.PlacedFeature placed) {
    var random = new net.minecraft.world.level.levelgen.WorldgenRandom(
        new net.minecraft.world.level.levelgen.XoroshiroRandomSource(0L));
    long decorationSeed = random.setDecorationSeed(worldSeed, chunkX << 4, chunkZ << 4);
    random.setFeatureSeed(decorationSeed, featureIndex, stepIndex);
    if (random.nextFloat() >= 1.0F / rarity) {
      return null;
    }
    int x = (chunkX << 4) + random.nextInt(16);
    int z = (chunkZ << 4) + random.nextInt(16);
    int y = net.minecraft.util.Mth.randomBetweenInclusive(random, GIANT_MIN_Y, GIANT_MAX_Y);
    var biome = generator.getBiomeSource().getNoiseBiome(
        net.minecraft.core.QuartPos.fromBlock(x),
        net.minecraft.core.QuartPos.fromBlock(y),
        net.minecraft.core.QuartPos.fromBlock(z), sampler);
    if (!biome.value().getGenerationSettings().hasFeature(placed)) {
      return null;
    }
    return new BlockPos(x, y, z);
  }

  private static int locate(CommandSourceStack source, Block target, int chunkRadius) {
    return locate(source, target.getName(), state -> state.is(target), chunkRadius);
  }

  private static int locate(CommandSourceStack source, Component displayName,
      java.util.function.Predicate<net.minecraft.world.level.block.state.BlockState> matcher,
      int chunkRadius) {
    ServerLevel level = source.getLevel();
    BlockPos center = BlockPos.containing(source.getPosition());
    ChunkPos centerChunk = new ChunkPos(center);

    List<Hit> hits = new ArrayList<>();
    int scannedChunks = 0;
    int totalCount = 0;

    for (int cx = centerChunk.x - chunkRadius; cx <= centerChunk.x + chunkRadius; cx++) {
      for (int cz = centerChunk.z - chunkRadius; cz <= centerChunk.z + chunkRadius; cz++) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
        if (chunk == null) {
          continue;
        }
        scannedChunks++;
        BlockPos nearestInChunk = null;
        double nearestDistSq = Double.MAX_VALUE;
        int countInChunk = 0;
        LevelChunkSection[] sections = chunk.getSections();
        for (int i = 0; i < sections.length; i++) {
          LevelChunkSection section = sections[i];
          if (section.hasOnlyAir() || !section.maybeHas(matcher)) {
            continue;
          }
          int baseY = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(i));
          int baseX = SectionPos.sectionToBlockCoord(cx);
          int baseZ = SectionPos.sectionToBlockCoord(cz);
          for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
              for (int z = 0; z < 16; z++) {
                if (!matcher.test(section.getBlockState(x, y, z))) {
                  continue;
                }
                countInChunk++;
                double d = center.distSqr(new BlockPos(baseX + x, baseY + y, baseZ + z));
                if (d < nearestDistSq) {
                  nearestDistSq = d;
                  nearestInChunk = new BlockPos(baseX + x, baseY + y, baseZ + z);
                }
              }
            }
          }
        }
        if (nearestInChunk != null) {
          hits.add(new Hit(nearestInChunk, countInChunk, nearestDistSq));
          totalCount += countInChunk;
        }
      }
    }

    Component blockName = displayName;
    if (hits.isEmpty()) {
      final int scanned = scannedChunks;
      source.sendSuccess(() -> Component
          .translatable("command." + Faktocraft.MODID + ".locate.none", blockName, chunkRadius, scanned)
          .withStyle(ChatFormatting.YELLOW), false);
      return 0;
    }

    hits.sort(Comparator.comparingDouble(Hit::distSq));
    final int total = totalCount;
    source.sendSuccess(() -> Component
        .translatable("command." + Faktocraft.MODID + ".locate.header", total, blockName, chunkRadius)
        .withStyle(ChatFormatting.GREEN), false);
    for (Hit hit : hits.subList(0, Math.min(5, hits.size()))) {
      String posText = hit.pos().getX() + " " + hit.pos().getY() + " " + hit.pos().getZ();
      Component entry = Component
          .translatable("command." + Faktocraft.MODID + ".locate.entry",
              hit.count(), posText, (int) Math.sqrt(hit.distSq()))
          .withStyle(style -> style
              .withColor(ChatFormatting.AQUA)
              .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp @s " + posText))
              .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                  Component.translatable("command." + Faktocraft.MODID + ".locate.teleport"))));
      source.sendSuccess(() -> entry, false);
    }
    return hits.size();
  }
}

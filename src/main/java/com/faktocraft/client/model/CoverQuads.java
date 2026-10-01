package com.faktocraft.client.model;

import com.faktocraft.common.cover.CoverSupport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.quad.MutableQuad;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CoverQuads {
  private static final Map<Key, QuadCollection> CACHE = new ConcurrentHashMap<>();
  private static final float MIN = CoverSupport.HOLE_MIN;
  private static final float MAX = CoverSupport.HOLE_MAX;
  private static final float NUDGE = 1.0E-4F;
  private static final float EDGE_EPSILON = 1.0E-5F;
  private static final float INTERIOR_SHADE = 0.7F;
  private static final float END_SHADE = 0.55F;
  private static final float END_INSET = 0.05F;
  private static final int CELLS = 4;
  private static final float CELL = 16F / CELLS;

  private record Key(BlockState cover, int holes, int closed) {
  }

  private record Face(BakedQuad.MaterialInfo material) {
  }

  private CoverQuads() {
  }

  public static void clear() {
    CACHE.clear();
  }

  public static QuadCollection get(BlockState cover, int holes, int closed, RandomSource rand) {
    return CACHE.computeIfAbsent(new Key(cover, holes, closed), key -> build(key, rand));
  }

  public static BlockStateModel coverModel(BlockState cover) {
    return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(cover);
  }

  @Nullable
  @SuppressWarnings("deprecation")
  private static Face face(BlockState cover, Direction direction, RandomSource rand) {
    BlockStateModel model = coverModel(cover);
    rand.setSeed(42L);
    List<BlockStateModelPart> parts = new ArrayList<>();
    model.collectParts(rand, parts);
    for (BlockStateModelPart part : parts) {
      List<BakedQuad> quads = part.getQuads(direction);
      if (!quads.isEmpty()) {
        return new Face(quads.get(0).materialInfo());
      }
    }
    for (BlockStateModelPart part : parts) {
      for (BakedQuad quad : part.getQuads(null)) {
        if (quad.direction() == direction) {
          return new Face(quad.materialInfo());
        }
      }
    }
    return null;
  }

  private static boolean hasHole(int holes, Direction direction) {
    return (holes & (1 << direction.get3DDataValue())) != 0;
  }

  private static QuadCollection build(Key key, RandomSource rand) {
    QuadCollection.Builder builder = new QuadCollection.Builder();
    Face[] faces = new Face[Direction.values().length];
    for (Direction direction : Direction.values()) {
      faces[direction.get3DDataValue()] = face(key.cover(), direction, rand);
    }
    for (Direction side : Direction.values()) {
      Face face = faces[side.get3DDataValue()];
      if (face != null) {
        outerFace(builder, side, hasHole(key.holes(), side), face);
      }
    }
    List<BakedQuad> unculled = new ArrayList<>();
    cavityWalls(unculled, key.holes(), faces);
    for (Direction direction : Direction.values()) {
      Face face = faces[direction.get3DDataValue()];
      if (face != null && hasHole(key.holes(), direction) && hasHole(key.closed(), direction)) {
        endCap(unculled, direction, face);
      }
    }
    for (BakedQuad quad : unculled) {
      builder.addUnculledFace(quad);
    }
    return builder.build();
  }

  private static void outerFace(QuadCollection.Builder builder, Direction side, boolean hole, Face face) {
    if (!hole) {
      builder.addCulledFace(side, rect(side, 0, 0, 16, 16, face));
      return;
    }
    builder.addCulledFace(side, rect(side, 0, MAX, 16, 16, face));
    builder.addCulledFace(side, rect(side, 0, 0, 16, MIN, face));
    builder.addCulledFace(side, rect(side, 0, MIN, MIN, MAX, face));
    builder.addCulledFace(side, rect(side, MAX, MIN, 16, MAX, face));
  }

  private static BakedQuad rect(Direction side, float u0, float v0, float u1, float v1, Face face) {
    float plane = side.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 16F : 0F;
    Vector3f from;
    Vector3f to;
    switch (side.getAxis()) {
      case X -> {
        from = new Vector3f(plane, v0, u0);
        to = new Vector3f(plane, v1, u1);
      }
      case Y -> {
        from = new Vector3f(u0, plane, v0);
        to = new Vector3f(u1, plane, v1);
      }
      default -> {
        from = new Vector3f(u0, v0, plane);
        to = new Vector3f(u1, v1, plane);
      }
    }
    return bake(from, to, side, face, true);
  }

  private static int[] cell(Direction.Axis axis, int along, int first, int second) {
    return switch (axis) {
      case X -> new int[] { along, first, second };
      case Y -> new int[] { first, along, second };
      default -> new int[] { first, second, along };
    };
  }

  private static boolean[][][] cavity(int holes) {
    boolean[][][] cavity = new boolean[CELLS][CELLS][CELLS];
    for (Direction direction : Direction.values()) {
      if (!hasHole(holes, direction)) {
        continue;
      }
      int base = direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? CELLS / 2 : 0;
      for (int depth = 0; depth < CELLS / 2; depth++) {
        for (int first = 1; first < CELLS - 1; first++) {
          for (int second = 1; second < CELLS - 1; second++) {
            int[] cell = cell(direction.getAxis(), base + depth, first, second);
            cavity[cell[0]][cell[1]][cell[2]] = true;
          }
        }
      }
    }
    return cavity;
  }

  private static void cavityWalls(List<BakedQuad> quads, int holes, Face[] faces) {
    boolean[][][] cavity = cavity(holes);
    for (int x = 0; x < CELLS; x++) {
      for (int y = 0; y < CELLS; y++) {
        for (int z = 0; z < CELLS; z++) {
          if (!cavity[x][y][z]) {
            continue;
          }
          for (Direction normal : Direction.values()) {
            int nx = x + normal.getStepX();
            int ny = y + normal.getStepY();
            int nz = z + normal.getStepZ();
            if (nx < 0 || ny < 0 || nz < 0 || nx >= CELLS || ny >= CELLS || nz >= CELLS || cavity[nx][ny][nz]) {
              continue;
            }
            Face face = faces[normal.get3DDataValue()];
            if (face != null) {
              quads.add(interior(wall(x, y, z, normal, face)));
            }
          }
        }
      }
    }
  }

  private static BakedQuad wall(int x, int y, int z, Direction normal, Face face) {
    Vector3f from = new Vector3f(x * CELL, y * CELL, z * CELL);
    Vector3f to = new Vector3f((x + 1) * CELL, (y + 1) * CELL, (z + 1) * CELL);
    float plane = normal.getAxisDirection() == Direction.AxisDirection.POSITIVE
        ? from.get(normal.getAxis().ordinal()) + CELL
        : from.get(normal.getAxis().ordinal());
    set(from, normal.getAxis(), plane);
    set(to, normal.getAxis(), plane);
    return bake(from, to, normal.getOpposite(), face, false);
  }

  private static int shade(int color, float shade) {
    int red = (int) ((color & 0xFF) * shade);
    int green = (int) (((color >> 8) & 0xFF) * shade);
    int blue = (int) (((color >> 16) & 0xFF) * shade);
    return (color & 0xFF000000) | (blue << 16) | (green << 8) | red;
  }

  public static BakedQuad interior(BakedQuad quad) {
    return interior(quad, INTERIOR_SHADE);
  }

  private static boolean onBlockEdge(float coordinate) {
    return coordinate <= EDGE_EPSILON || coordinate >= 1F - EDGE_EPSILON;
  }

  private static BakedQuad interior(BakedQuad quad, float shade) {
    MutableQuad mutable = new MutableQuad().setFrom(quad);
    Direction facing = quad.direction();
    int index = facing.getAxis().ordinal();
    float step = facing.getAxis().choose(facing.getStepX(), facing.getStepY(), facing.getStepZ());
    for (int vertex = 0; vertex < 4; vertex++) {
      float x = mutable.x(vertex);
      float y = mutable.y(vertex);
      float z = mutable.z(vertex);
      float lift = onBlockEdge(x) || onBlockEdge(y) || onBlockEdge(z) ? 0F : step * NUDGE * (x + y + z);
      mutable.setPositionComponent(vertex, index, mutable.positionComponent(vertex, index) + lift);
      mutable.setColor(vertex, shade(mutable.color(vertex), shade));
    }
    mutable.setAmbientOcclusion(false);
    return mutable.toBakedQuad();
  }

  private static void endCap(List<BakedQuad> quads, Direction hole, Face face) {
    float plane = hole.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 16F - END_INSET : END_INSET;
    Vector3f from = new Vector3f(MIN, MIN, MIN);
    Vector3f to = new Vector3f(MAX, MAX, MAX);
    set(from, hole.getAxis(), plane);
    set(to, hole.getAxis(), plane);
    quads.add(interior(bake(from, to, hole.getOpposite(), face, false), END_SHADE));
  }

  private static void set(Vector3f vector, Direction.Axis axis, float value) {
    switch (axis) {
      case X -> vector.x = value;
      case Y -> vector.y = value;
      default -> vector.z = value;
    }
  }

  private static BakedQuad bake(Vector3f from, Vector3f to, Direction facing, Face face, boolean ambientOcclusion) {
    BakedQuad.MaterialInfo material = face.material();
    MutableQuad quad = new MutableQuad()
        .setSprite(material.sprite(), material.layer(), material.itemRenderType(), material.itemGlintRenderType(),
            material.itemGlintSpecialRenderType())
        .setCubeFace(facing, from.x / 16F, from.y / 16F, from.z / 16F, to.x / 16F, to.y / 16F, to.z / 16F)
        .bakeUvsFromPosition()
        .setTintIndex(material.tintIndex())
        .setShadeOverride(null)
        .setLightEmission(0)
        .setAmbientOcclusion(ambientOcclusion)
        .setColor(0xFFFFFFFF);
    return quad.toBakedQuad();
  }
}

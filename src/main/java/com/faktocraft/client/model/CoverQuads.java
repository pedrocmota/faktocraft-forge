package com.faktocraft.client.model;

import com.faktocraft.common.cover.CoverSupport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.ForgeFaceData;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Vector3f;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CoverQuads {

  private static final ResourceLocation NAME = new ResourceLocation("faktocraft", "cover");
  private static final FaceBakery BAKERY = new FaceBakery();
  private static final Map<Key, List<BakedQuad>> CACHE = new ConcurrentHashMap<>();
  private static final float MIN = CoverSupport.HOLE_MIN;
  private static final float MAX = CoverSupport.HOLE_MAX;
  private static final ForgeFaceData NO_AO = new ForgeFaceData(0xFFFFFFFF, 0, 0, false);
  private static final float NUDGE = 1.0E-4F;
  private static final float EDGE_EPSILON = 1.0E-5F;
  private static final float INTERIOR_SHADE = 0.7F;
  private static final float END_SHADE = 0.55F;
  private static final float END_INSET = 0.05F;
  private static final int CELLS = 4;
  private static final float CELL = 16F / CELLS;

  private record Key(BlockState cover, int holes, int closed, @Nullable Direction side, @Nullable RenderType type) {
  }

  private record Face(TextureAtlasSprite sprite, int tint) {
  }

  private CoverQuads() {
  }

  public static void clear() {
    CACHE.clear();
  }

  public static List<BakedQuad> get(BlockState cover, int holes, int closed, @Nullable Direction side,
      @Nullable RenderType type, RandomSource rand) {
    return CACHE.computeIfAbsent(new Key(cover, holes, closed, side, type), key -> build(key, rand));
  }

  @Nullable
  private static Face face(BlockState cover, Direction direction, @Nullable RenderType type, RandomSource rand) {
    BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(cover);
    rand.setSeed(42L);
    List<BakedQuad> quads = model.getQuads(cover, direction, rand, ModelData.EMPTY, type);
    if (quads.isEmpty()) {
      rand.setSeed(42L);
      quads = model.getQuads(cover, null, rand, ModelData.EMPTY, type);
      quads = quads.stream().filter(quad -> quad.getDirection() == direction).toList();
    }
    if (quads.isEmpty()) {
      return null;
    }
    BakedQuad quad = quads.get(0);
    return new Face(quad.getSprite(), quad.getTintIndex());
  }

  private static boolean hasHole(int holes, Direction direction) {
    return (holes & (1 << direction.get3DDataValue())) != 0;
  }

  private static List<BakedQuad> build(Key key, RandomSource rand) {
    List<BakedQuad> quads = new ArrayList<>();
    if (key.side() != null) {
      Face face = face(key.cover(), key.side(), key.type(), rand);
      if (face != null) {
        outerFace(quads, key.side(), hasHole(key.holes(), key.side()), face);
      }
      return quads;
    }
    Face[] faces = new Face[Direction.values().length];
    for (Direction direction : Direction.values()) {
      faces[direction.get3DDataValue()] = face(key.cover(), direction, key.type(), rand);
    }
    cavityWalls(quads, key.holes(), faces);
    for (Direction direction : Direction.values()) {
      Face face = faces[direction.get3DDataValue()];
      if (face != null && hasHole(key.holes(), direction) && hasHole(key.closed(), direction)) {
        endCap(quads, direction, face);
      }
    }
    return quads;
  }

  private static void outerFace(List<BakedQuad> quads, Direction side, boolean hole, Face face) {
    if (!hole) {
      quads.add(rect(side, 0, 0, 16, 16, face, side));
      return;
    }
    quads.add(rect(side, 0, MAX, 16, 16, face, side));
    quads.add(rect(side, 0, 0, 16, MIN, face, side));
    quads.add(rect(side, 0, MIN, MIN, MAX, face, side));
    quads.add(rect(side, MAX, MIN, 16, MAX, face, side));
  }

  private static BakedQuad rect(Direction side, float u0, float v0, float u1, float v1, Face face,
      @Nullable Direction cull) {
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
    return bake(from, to, side, face, cull, true);
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
    return bake(from, to, normal.getOpposite(), face, null, false);
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
    int[] vertices = quad.getVertices().clone();
    Direction facing = quad.getDirection();
    int index = facing.getAxis().ordinal();
    float step = facing.getAxis().choose(facing.getStepX(), facing.getStepY(), facing.getStepZ());
    for (int vertex = 0; vertex < 4; vertex++) {
      int offset = vertex * 8;
      float x = Float.intBitsToFloat(vertices[offset]);
      float y = Float.intBitsToFloat(vertices[offset + 1]);
      float z = Float.intBitsToFloat(vertices[offset + 2]);
      float lift = onBlockEdge(x) || onBlockEdge(y) || onBlockEdge(z) ? 0F : step * NUDGE * (x + y + z);
      vertices[offset + index] = Float.floatToRawIntBits(Float.intBitsToFloat(vertices[offset + index]) + lift);
      vertices[offset + 3] = shade(vertices[offset + 3], shade);
    }
    return new BakedQuad(vertices, quad.getTintIndex(), facing, quad.getSprite(), quad.isShade(), false);
  }

  private static void endCap(List<BakedQuad> quads, Direction hole, Face face) {
    float plane = hole.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 16F - END_INSET : END_INSET;
    Vector3f from = new Vector3f(MIN, MIN, MIN);
    Vector3f to = new Vector3f(MAX, MAX, MAX);
    set(from, hole.getAxis(), plane);
    set(to, hole.getAxis(), plane);
    quads.add(interior(bake(from, to, hole.getOpposite(), face, null, false), END_SHADE));
  }

  private static void set(Vector3f vector, Direction.Axis axis, float value) {
    switch (axis) {
      case X -> vector.x = value;
      case Y -> vector.y = value;
      default -> vector.z = value;
    }
  }

  private static float[] uv(Direction facing, Vector3f from, Vector3f to) {
    return switch (facing) {
      case DOWN -> new float[] { from.x, 16F - to.z, to.x, 16F - from.z };
      case UP -> new float[] { from.x, from.z, to.x, to.z };
      case NORTH -> new float[] { 16F - to.x, 16F - to.y, 16F - from.x, 16F - from.y };
      case SOUTH -> new float[] { from.x, 16F - to.y, to.x, 16F - from.y };
      case WEST -> new float[] { from.z, 16F - to.y, to.z, 16F - from.y };
      case EAST -> new float[] { 16F - to.z, 16F - to.y, 16F - from.z, 16F - from.y };
    };
  }

  private static BakedQuad bake(Vector3f from, Vector3f to, Direction facing, Face face, @Nullable Direction cull,
      boolean ambientOcclusion) {
    BlockElementFace element = new BlockElementFace(cull, face.tint(), "#cover",
        new BlockFaceUV(uv(facing, from, to), 0), ambientOcclusion ? ForgeFaceData.DEFAULT : NO_AO);
    return BAKERY.bakeQuad(from, to, element, face.sprite(), facing, BlockModelRotation.X0_Y0, null, true, NAME);
  }
}

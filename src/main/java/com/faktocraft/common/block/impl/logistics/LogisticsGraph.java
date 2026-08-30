package com.faktocraft.common.block.impl.logistics;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

public final class LogisticsGraph {

  public static final int MAX_NODES = 1024;

  public enum NodeType {
    TRUNK, CHASSIS, CRAFT_PIPE, CORE, REQUEST_TABLE, ASSEMBLY_TABLE
  }

  public record Node(BlockPos pos, NodeType type, int distance) {
  }

  private final BlockPos corePos;
  private final Map<BlockPos, Node> nodes = new HashMap<>();
  private final List<BlockPos> chassis = new ArrayList<>();
  private final List<BlockPos> craftPipes = new ArrayList<>();
  private final List<BlockPos> tables = new ArrayList<>();
  private final List<BlockPos> assemblies = new ArrayList<>();
  private boolean coreConflict;

  private LogisticsGraph(BlockPos corePos) {
    this.corePos = corePos;
  }

  public static boolean isNetworkMember(BlockState state) {
    Block block = state.getBlock();
    return block instanceof BlockStonePipe || block instanceof BlockChassis
        || block instanceof BlockDockingPipe
        || block instanceof BlockLogisticsController || block instanceof BlockRequestTable
        || block instanceof BlockAssemblyTable;
  }

  @Nullable
  private static NodeType classify(BlockState state) {
    Block block = state.getBlock();
    if (block instanceof BlockStonePipe) {
      return NodeType.TRUNK;
    }
    if (block instanceof BlockChassis) {
      return NodeType.CHASSIS;
    }
    if (block instanceof BlockDockingPipe) {
      return NodeType.CRAFT_PIPE;
    }
    if (block instanceof BlockLogisticsController) {
      return NodeType.CORE;
    }
    if (block instanceof BlockRequestTable) {
      return NodeType.REQUEST_TABLE;
    }
    if (block instanceof BlockAssemblyTable) {
      return NodeType.ASSEMBLY_TABLE;
    }
    return null;
  }

  static boolean linked(NodeType a, NodeType b) {
    if (isDock(a) || isDock(b)) {
      return true;
    }
    return a == NodeType.TRUNK && b == NodeType.TRUNK;
  }

  private static boolean isDock(NodeType type) {
    return type == NodeType.CHASSIS || type == NodeType.CRAFT_PIPE;
  }

  private static boolean traversable(NodeType type) {
    return type == NodeType.TRUNK || isDock(type);
  }

  public static LogisticsGraph build(Level level, BlockPos corePos) {
    LogisticsGraph graph = new LogisticsGraph(corePos);
    graph.nodes.put(corePos, new Node(corePos, NodeType.CORE, 0));
    ArrayDeque<BlockPos> queue = new ArrayDeque<>();
    queue.add(corePos);
    while (!queue.isEmpty() && graph.nodes.size() < MAX_NODES) {
      BlockPos current = queue.poll();
      int distance = graph.nodes.get(current).distance();
      NodeType currentType = graph.nodes.get(current).type();
      for (Direction direction : Direction.values()) {
        BlockPos next = current.relative(direction);
        if (graph.nodes.containsKey(next) || !level.isLoaded(next)) {
          continue;
        }
        NodeType type = classify(level.getBlockState(next));
        if (type == null || !linked(currentType, type)) {
          continue;
        }

        if (type == NodeType.ASSEMBLY_TABLE
            && BlockChassis.selectedInventoryDirection(level, current) != direction) {
          continue;
        }
        graph.nodes.put(next, new Node(next, type, distance + 1));
        switch (type) {
          case TRUNK -> queue.add(next);
          case CHASSIS -> {

            graph.chassis.add(next);
            queue.add(next);
          }
          case CRAFT_PIPE -> {
            graph.craftPipes.add(next);
            queue.add(next);
          }
          case REQUEST_TABLE -> graph.tables.add(next);
          case ASSEMBLY_TABLE -> graph.assemblies.add(next);
          case CORE -> graph.coreConflict = true;
        }
      }
    }
    return graph;
  }

  public BlockPos corePos() {
    return corePos;
  }

  public boolean hasCoreConflict() {
    return coreConflict;
  }

  public boolean contains(BlockPos pos) {
    return nodes.containsKey(pos);
  }

  public boolean touches(BlockPos pos) {
    if (nodes.containsKey(pos)) {
      return true;
    }
    for (Direction direction : Direction.values()) {
      if (nodes.containsKey(pos.relative(direction))) {
        return true;
      }
    }
    return false;
  }

  @Nullable
  public Node node(BlockPos pos) {
    return nodes.get(pos);
  }

  public int distance(BlockPos pos) {
    Node node = nodes.get(pos);
    return node != null ? node.distance() : Integer.MAX_VALUE;
  }

  public List<BlockPos> chassisList() {
    return chassis;
  }

  public List<BlockPos> craftPipeList() {
    return craftPipes;
  }

  public List<BlockPos> tableList() {
    return tables;
  }

  public List<BlockPos> assemblyList() {
    return assemblies;
  }

  public List<BlockPos> route(BlockPos from, BlockPos to) {
    if (!nodes.containsKey(from) || !nodes.containsKey(to)) {
      return List.of();
    }
    Map<BlockPos, BlockPos> parents = new HashMap<>();
    ArrayDeque<BlockPos> queue = new ArrayDeque<>();
    queue.add(from);
    parents.put(from, from);
    while (!queue.isEmpty()) {
      BlockPos current = queue.poll();
      if (current.equals(to)) {
        break;
      }

      NodeType currentType = nodes.get(current).type();
      if (!current.equals(from) && !traversable(currentType)) {
        continue;
      }
      for (Direction direction : Direction.values()) {
        BlockPos next = current.relative(direction);
        if (nodes.containsKey(next) && !parents.containsKey(next)
            && linked(currentType, nodes.get(next).type())) {
          parents.put(next, current);
          queue.add(next);
        }
      }
    }
    if (!parents.containsKey(to)) {
      return List.of();
    }
    ArrayDeque<BlockPos> path = new ArrayDeque<>();
    BlockPos cursor = to;
    while (!cursor.equals(from)) {
      path.push(cursor);
      cursor = parents.get(cursor);
    }
    path.push(from);
    return new ArrayList<>(path);
  }
}

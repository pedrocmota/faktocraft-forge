package com.faktocraft.common.energy.provider;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.core.BlockPos;

final class TrackedPosSet extends AbstractSet<BlockPos> {

  interface Listener {
    void added(BlockPos pos);

    void removed(BlockPos pos);
  }

  private final HashSet<BlockPos> inner = new HashSet<>();
  private final Listener listener;

  TrackedPosSet(Listener listener) {
    this.listener = listener;
  }

  @Override
  public int size() {
    return inner.size();
  }

  @Override
  public boolean contains(Object o) {
    return inner.contains(o);
  }

  @Override
  public boolean add(BlockPos pos) {
    if (inner.add(pos)) {
      listener.added(pos);
      return true;
    }
    return false;
  }

  @Override
  public boolean remove(Object o) {
    if (inner.remove(o)) {
      listener.removed((BlockPos) o);
      return true;
    }
    return false;
  }

  @Override
  public void clear() {
    if (inner.isEmpty()) {
      return;
    }
    for (BlockPos pos : inner) {
      listener.removed(pos);
    }
    inner.clear();
  }

  @Override
  public Iterator<BlockPos> iterator() {
    Iterator<BlockPos> it = inner.iterator();
    return new Iterator<>() {
      private BlockPos last;

      @Override
      public boolean hasNext() {
        return it.hasNext();
      }

      @Override
      public BlockPos next() {
        last = it.next();
        return last;
      }

      @Override
      public void remove() {
        it.remove();
        listener.removed(last);
      }
    };
  }

  Set<BlockPos> snapshot() {
    return new HashSet<>(inner);
  }
}

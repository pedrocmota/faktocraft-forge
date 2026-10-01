package com.faktocraft.common.entity.block;

import com.faktocraft.common.interfaces.entity.IProgress;
import net.minecraft.nbt.CompoundTag;

public class BlockEntityProgress implements IProgress {

  private float progress;
  private float progressMax;
  private boolean changed = false;

  public BlockEntityProgress() {
    this(-1, -1);
  }

  public BlockEntityProgress(float progress, float progressMax) {
    this.progress = progress;
    this.progressMax = progressMax;
  }

  public void setData(float progress, float progressMax) {
    if (this.progress != progress || this.progressMax != progressMax) {
      changed = true;
    }
    this.progress = progress;
    this.progressMax = progressMax;
  }

  public void setProgress(float progress) {
    setData(progress, this.progressMax);
  }

  public void incProgress(float amount) {
    setProgress(this.progress + amount);
  }

  public void decProgress(float amount) {
    setProgress(this.progress - amount);
  }

  public void setBoth(float value) {
    setData(value, value);
  }

  public void setProgressMax(float progressMax) {
    setData(this.progress, Math.max(progressMax, 1));
  }

  public void rescaleMax(float newMax) {
    newMax = Math.max(newMax, 1);
    if (this.progressMax == newMax) {
      return;
    }
    if (this.progress > 0 && this.progressMax > 0) {
      setData(this.progress * newMax / this.progressMax, newMax);
    } else {
      setProgressMax(newMax);
    }
  }

  @Override
  public float getProgress() {
    return progress;
  }

  @Override
  public float getProgressMax() {
    return progressMax;
  }

  public boolean changed() {
    return changed;
  }

  public void clearChanged() {
    changed = false;
  }

  public void save(CompoundTag tag) {
    tag.putFloat("progress", progress);
    tag.putFloat("progressMax", progressMax);
  }

  public void load(CompoundTag tag) {
    this.progress = tag.contains("progress") ? tag.getFloatOr("progress", 0.0F) : -1;
    this.progressMax = tag.contains("progressMax") ? tag.getFloatOr("progressMax", 0.0F) : -1;
  }
}

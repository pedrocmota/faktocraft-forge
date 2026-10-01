package com.faktocraft.gametest;

import com.faktocraft.Faktocraft;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.gametest.framework.TestReporter;

public final class ProgressTestReporter implements TestReporter {

  private static boolean installed;

  private final TestReporter delegate;
  private int finished;
  private int failed;

  private ProgressTestReporter(TestReporter delegate) {
    this.delegate = delegate;
  }

  public static void installIfGameTestServer() {
    if (installed || System.getProperty("neoforge.enabledGameTestNamespaces") == null) {
      return;
    }
    installed = true;

    GlobalTestReporter.replaceWith(
        new ProgressTestReporter(new net.minecraft.gametest.framework.LogTestReporter()));
  }

  @Override
  public void onTestSuccess(GameTestInfo info) {
    finished++;
    Faktocraft.LOGGER.info("[Gametest] {} ok      {} ({} ms)", finished, info.id(), info.getRunTime());
    delegate.onTestSuccess(info);
  }

  @Override
  public void onTestFailed(GameTestInfo info) {
    finished++;
    failed++;
    Faktocraft.LOGGER.info("[Gametest] {} FAILED  {}", finished, info.id());
    delegate.onTestFailed(info);
  }

  @Override
  public void finish() {
    Faktocraft.LOGGER.info("[Gametest] done: {} tests, {} failed", finished, failed);
    delegate.finish();
  }
}

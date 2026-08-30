package com.faktocraft.gametest;

import com.faktocraft.IndReb;
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
    if (installed || System.getProperty("forge.enabledGameTestNamespaces") == null) {
      return;
    }
    installed = true;

    GlobalTestReporter.replaceWith(
        new ProgressTestReporter(new net.minecraft.gametest.framework.LogTestReporter()));
  }

  @Override
  public void onTestSuccess(GameTestInfo info) {
    finished++;
    IndReb.LOGGER.info("[Gametest] {} ok      {} ({} ms)", finished, info.getTestName(),
        info.getRunTime());
    delegate.onTestSuccess(info);
  }

  @Override
  public void onTestFailed(GameTestInfo info) {
    finished++;
    failed++;
    IndReb.LOGGER.info("[Gametest] {} FAILED  {}", finished, info.getTestName());
    delegate.onTestFailed(info);
  }

  @Override
  public void finish() {
    IndReb.LOGGER.info("[Gametest] done: {} tests, {} failed", finished, failed);
    delegate.finish();
  }
}

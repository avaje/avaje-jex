package io.avaje.jex.jetty.threadpool;

import org.eclipse.jetty.util.thread.ThreadPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Loom Virtual threads based Jetty ThreadPool.
 */
public class VirtualThreadPool implements ThreadPool {

  private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

  @Override
  public void execute(Runnable command) {
    executorService.submit(command);
  }

  @Override
  public void join() {
    // do nothing
  }

  @Override
  public int getThreads() {
    return 1;
  }

  @Override
  public int getIdleThreads() {
    return 1;
  }

  @Override
  public boolean isLowOnThreads() {
    return false;
  }

}

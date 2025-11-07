package io.avaje.jex.http3.flupke.impl;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.avaje.applog.AppLog;
import tech.kwik.core.log.BaseLogger;

class FlupkeSystemLogger extends BaseLogger {
  public static final Logger LOG = AppLog.getLogger(FlupkeSystemLogger.class);

  private final Lock lock = new ReentrantLock();

  FlupkeSystemLogger() {}

  @Override
  protected void log(String text) {
    this.lock.lock();
    try {
      LOG.log(Level.INFO, text);
    } finally {
      this.lock.unlock();
    }
  }

  @Override
  protected void log(String text, Throwable throwable) {
    if (throwable == null) {
      log(text);
      return;
    }

    this.lock.lock();
    try {
      LOG.log(Level.INFO, text, throwable);
    } finally {
      this.lock.unlock();
    }
  }

  @Override
  protected void logWithHexDump(String text, byte[] data, int length) {
    this.lock.lock();
    try {
      LOG.log(Level.INFO, text + "\n" + super.byteToHexBlock(data, length));
    } finally {
      this.lock.unlock();
    }
  }

  @Override
  protected void logWithHexDump(String text, ByteBuffer data, int offset, int length) {
    this.lock.lock();
    try {
      LOG.log(Level.INFO, text + "\n" + super.byteToHexBlock(data, offset, length));
    } finally {
      this.lock.unlock();
    }
  }
}

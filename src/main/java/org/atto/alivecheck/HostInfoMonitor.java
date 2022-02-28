package org.atto.alivecheck;

public interface HostInfoMonitor {

    void start(int threadCount, int intervalMs);

    boolean isRunning();

    void stopNow() throws InterruptedException;

    void stop();

    int getThreadCount();
}
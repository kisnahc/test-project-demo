package org.atto.alivecheck.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atto.alivecheck.AliveStatus;
import org.atto.alivecheck.HostInfo;
import org.atto.alivecheck.HostInfoMonitor;
import org.atto.alivecheck.HostInfoStore;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HostInfoMonitorImpl implements HostInfoMonitor {

    private final HostInfoStore hostInfoStore = HostInfoStoreTestImpl.getInstance();
    private ExecutorService executorService;
    private ForkJoinPool forkJoinAlivePool;
    private ForkJoinPool forkJoinDeadPool;
    private ScheduledExecutorService scheduledExecutor;

    private static class InstanceHolder {
        private static final HostInfoMonitorImpl INSTANCE = new HostInfoMonitorImpl();
    }

    public static HostInfoMonitorImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public void start(int threadCount, int intervalMs) {
        if (scheduledExecutor != null) {
            stopNow();
        }
        createScheduler(threadCount, intervalMs);
    }

    @Override
    public void stopNow() {
        if (scheduledExecutor != null) {
            stopNowScheduler();
        } else {
            throw new IllegalStateException("There are no monitors running.");
        }
    }

    @Override
    public void stop() {
        if (scheduledExecutor != null) {
            stopScheduler();
        } else {
            throw new IllegalStateException("There are no monitors running.");
        }
    }

    @Override
    public int getThreadCount() {
        return forkJoinAlivePool.getPoolSize();
    }

    @Override
    public boolean isRunning() {
        if (scheduledExecutor.isTerminated()) {
            return true;
        } else if (scheduledExecutor == null) {
            return false;
        } else {
            return false;
        }
    }

    /**
     * 스케줄러 스레드, 상태 체크 스레드 생성 메서드.
     * 수용 가능한 Dead Host 설정.
     *
     * @param intervalMs
     */
    private void createScheduler(int threadCount, int intervalMs) {
        forkJoinAlivePool = new ForkJoinPool(threadCount);
        forkJoinDeadPool = new ForkJoinPool(threadCount);
        executorService = Executors.newFixedThreadPool(threadCount);
        scheduledExecutor = Executors.newScheduledThreadPool(1);
        scheduledExecutor.scheduleWithFixedDelay(new ScheduleRunnable(), 0, intervalMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 스케줄러의 실행 커맨드.
     */
    private void getScheduleCommand() {
        List<HostInfo> hostInfos = hostInfoStore.getAll();
        List<HostInfo> deadList = hostInfoStore.getDeadList();

        List<HostInfo> collect = hostInfos.stream()
                .filter(hostInfo -> hostInfo.getAliveStatus().equals(AliveStatus.NONE) ||
                        hostInfo.getAliveStatus().equals(AliveStatus.ALIVE))
                .collect(Collectors.toList());

        forkJoinAlivePool.submit(() -> {
            log.info("*** check ALIVE or NONE *** ");
            collect.parallelStream().forEach(this::aliveCheckTask);
        });

        if (!deadList.isEmpty()) {
            for (HostInfo hostInfo : deadList) {
                forkJoinDeadPool.submit(() -> aliveCheckTask(hostInfo));
//                executorService.submit(() -> aliveCheckTask(hostInfo));
            }
            log.info("*** check dead *** {}", deadList);
        }
    }

    /**
     * 상태 체크 쓰레드 할당 메서드.
     *
     * @param hostInfo
     */
    private void aliveCheckTask(HostInfo hostInfo) {
        try {
            boolean reachable = InetAddress.getByName(hostInfo.getHostName()).isReachable(2000);
            if (reachable) {
                updateAliveStatus(hostInfo, AliveStatus.ALIVE);
                log.info("ALIVE {}", Thread.currentThread().getState());
            } else {
                updateAliveStatus(hostInfo, AliveStatus.DEAD);
                log.info("DEAD {}", Thread.currentThread().getState());
            }
        } catch (Exception e) {
            hostInfoStore.remove(hostInfo);
            log.info("remove hostInfo = {}", hostInfo);
            log.warn(e.getMessage(), e);
        }
    }

    /**
     * 호스트 상태 업데이트 메세드.
     *
     * @param hostInfo
     * @param status
     */
    private void updateAliveStatus(HostInfo hostInfo, AliveStatus status) {
        hostInfo.setAliveStatus(status);
        hostInfoStore.update(hostInfo);
    }

    /**
     * 스케줄러 즉시 종료 메서드.
     */
    private void stopNowScheduler() {
//        executorService.shutdownNow();
        scheduledExecutor.shutdownNow();
        forkJoinAlivePool.shutdownNow();
        forkJoinDeadPool.shutdownNow();
        log.info("Monitoring has Stopped");
    }

    /**
     * 스케줄러 종료 메서드. (제출된 모든 작업이 완료 후)
     */
    private void stopScheduler() {
        executorService.shutdown();
        scheduledExecutor.shutdown();
        forkJoinAlivePool.shutdown();
        log.info("Monitoring is stopped. {}");
    }

    private class ScheduleRunnable implements Runnable {

        private ScheduleRunnable() {
        }

        @Override
        public void run() {
            try {
                getScheduleCommand();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

}

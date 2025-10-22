package com.jovia.sim;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.time.LocalTime;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 在应用启动后，持续向业务线程池提交任务的负载模拟器。
 * 目的：让线程池 activeCount/poolSize/queueSize 持续有波动，便于前端页面观察。
 */
@Slf4j
@Component
public class ThreadPoolLoadSimulator {

    private final ThreadPoolExecutor businessExecutor;

    private final ScheduledExecutorService feederScheduler;

    private final Random random = new Random();

    public ThreadPoolLoadSimulator(ThreadPoolExecutor threadPoolExecutor) {
        this.businessExecutor = threadPoolExecutor;
        this.feederScheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "thread-pool-load-feeder");
                t.setDaemon(true);
                return t;
            }
        });
    }

    /**
     * 应用就绪后开始以固定频率投喂任务。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void startFeeding() {
        log.info("[LoadSimulator] 启动线程池负载模拟，核心线程数={}, 最大线程数={}, 队列容量剩余={}",
                businessExecutor.getCorePoolSize(),
                businessExecutor.getMaximumPoolSize(),
                businessExecutor.getQueue().remainingCapacity());

        // 每 200ms 提交一批任务，批次大小随负载动态调整
        feederScheduler.scheduleAtFixedRate(this::submitBatchTasks, 0, 200, TimeUnit.MILLISECONDS);
    }

    private void submitBatchTasks() {
        try {
            int active = businessExecutor.getActiveCount();
            int max = businessExecutor.getMaximumPoolSize();

            // 目标活跃区间：50% ~ 85%
            int targetActive = (int) Math.max(1, Math.round(max * (0.5 + random.nextDouble() * 0.35)));

            int deficit = Math.max(0, targetActive - active);

            // 批量大小：缺口的 30% ~ 70%，且至少 5 个，最多 100 个
            int batch = Math.min(100, Math.max(5, (int) Math.round(deficit * (0.3 + random.nextDouble() * 0.4))));

            if (batch == 0) {
                // 轻负载时也偶尔投喂少量任务制造波动
                batch = random.nextInt(4);
            }

            for (int i = 0; i < batch; i++) {
                businessExecutor.submit(this::simulateWorkload);
            }

            if (random.nextDouble() < 0.02) {
                log.info("[LoadSimulator] @{} 提交任务批次：{}，active={}/{}，queueSize={}，remaining={}",
                        LocalTime.now(),
                        batch,
                        businessExecutor.getActiveCount(),
                        businessExecutor.getMaximumPoolSize(),
                        businessExecutor.getQueue().size(),
                        businessExecutor.getQueue().remainingCapacity());
            }
        } catch (Throwable t) {
            log.warn("[LoadSimulator] 提交任务失败: {}", t.getMessage());
        }
    }

    /**
     * 模拟一次耗时业务任务：
     * - 70% 概率 Sleep 300~1200ms（模拟IO/外部调用）
     * - 30% 概率 CPU 计算 50~150ms（模拟计算）
     */
    private void simulateWorkload() {
        try {
            if (random.nextDouble() < 0.7) {
                // IO型任务
                long sleepMs = 300 + random.nextInt(900);
                Thread.sleep(sleepMs);
            } else {
                // 计算型任务
                long end = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(50 + random.nextInt(100));
                double acc = 0;
                while (System.nanoTime() < end) {
                    acc += Math.sqrt(random.nextDouble());
                }
                // 防止JIT优化
                if (acc == -1) log.debug("never happen");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @PreDestroy
    public void shutdown() {
        feederScheduler.shutdownNow();
    }
}



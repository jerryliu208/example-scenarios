package com.vincent.example;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

@Slf4j
public class SellService {
    private final String logText;
    private final String serviceName;
    private final RedissonClient redisson;
    private final Boolean withWatchDog;
    private final Integer lockExpireTime;
    private final Integer mockGcSleepTime;

    public SellService(String serviceName, RedissonClient redisson, Boolean withWatchDog,
                       Integer lockExpireTime, Integer mockGcSleepTime) {
        this.serviceName = serviceName;
        this.redisson = redisson;
        this.withWatchDog = withWatchDog;
        this.lockExpireTime = lockExpireTime;
        this.mockGcSleepTime = mockGcSleepTime;
        logText = this.serviceName + " log: ";
    }

    public void execute() {
        try {
            // print thread name
            log.info(this.serviceName + " started");

            // acquire lock
            RLock lock = this.redisson.getLock("test");
            boolean locked;
            if (this.withWatchDog == Boolean.TRUE) {
                // with watch dog
                locked = lock.tryLock(this.lockExpireTime, TimeUnit.SECONDS);
            } else {
                // without watch dog
                locked = lock.tryLock(this.lockExpireTime, this.lockExpireTime, TimeUnit.SECONDS);
            }
            if (!locked) {
                log.info(logText + "Failed to acquire lock");
                return;
            }

            // execute sell logic
            try {
                // get stock
                String stockKey = "stock";
                RBucket<Integer> stockRBucket = this.redisson.getBucket(stockKey);
                Integer stock = stockRBucket.get();
                log.info(logText + stockKey + " = " + stock);

                // lock expire countdown
                if (this.withWatchDog == Boolean.FALSE
                        && this.mockGcSleepTime > this.lockExpireTime) {
                    CompletableFuture.runAsync(() -> {
                        try {
                            Thread.sleep(this.lockExpireTime * 1000);
                            log.warn(logText + "lock is expired");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                }

                // mock GC stop the world
                if (this.mockGcSleepTime > 0) {
                    log.warn(
                            logText + "mock GC stop the world for " + this.mockGcSleepTime
                                    + " seconds");
                    Thread.sleep(this.mockGcSleepTime * 1000);
                    log.info(logText + "continue");
                }

                // sell stock
                stock--;
                stockRBucket.set(stock);
                log.info(logText + stockKey + " set to " + stock);
            } catch (Exception e) {
              e.printStackTrace();
            } finally {
                try {
                    lock.unlock();
                    log.info(logText + "unlock success");
                } catch (Exception e) {
                    log.error(logText + "Failed to unlock");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

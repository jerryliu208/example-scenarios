package com.vincent.example;

import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OversoldDemo {

    @Autowired
    private RedissonClient redisson;

    public void execute(boolean withWatchDog) {
        if (withWatchDog) {
            log.info("========== NOW START DEMO WITH WATCHDOG ==========");
        } else {
            log.info("========== NOW START DEMO WITHOUT WATCHDOG ==========");
        }

        // initialize test data
        initTestData(100);

        // first user
        CompletableFuture<Void> firstUserFuture = CompletableFuture.runAsync(() ->
                new SellService("first user thread", redisson, withWatchDog,
                        2, 8).execute());

        wait(3);

        // second user
        CompletableFuture<Void> secondUserFuture = CompletableFuture.runAsync(() ->
                new SellService("second user thread", redisson, true,
                        2, 0).execute());

        CompletableFuture.allOf(firstUserFuture, secondUserFuture).join();
        log.info(" ");
    }

    private void initTestData(int initStock) {
        final String rBucketName = "stock";
        RBucket<Integer> rBucketToInitialize = redisson.getBucket(rBucketName);
        rBucketToInitialize.set(initStock);
        log.info("initialStock = " + initStock);
    }

    private static void wait(int sec) {
        try {
            Thread.sleep((long)sec * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

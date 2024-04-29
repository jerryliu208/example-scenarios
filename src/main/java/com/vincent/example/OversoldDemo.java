package com.vincent.example;

import java.util.concurrent.CompletableFuture;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OversoldDemo {

    @Autowired
    private RedissonClient redisson;

    public void execute(boolean withWatchDog) {
        if (withWatchDog) {
            System.out.println("========== NOW START DEMO WITH WATCHDOG ==========");
        } else {
            System.out.println("========== NOW START DEMO WITHOUT WATCHDOG ==========");
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
        System.out.println(" ");
    }

    private void initTestData(int initStock) {
        final String rBucketName = "stock";
        RBucket<Integer> rBucketToInitialize = redisson.getBucket(rBucketName);
        rBucketToInitialize.set(initStock);
        System.out.println("initialStock = " + initStock);
    }

    private static void wait(int sec) {
        try {
            Thread.sleep((long)sec * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

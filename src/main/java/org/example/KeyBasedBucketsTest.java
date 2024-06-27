package org.example;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.grid.jcache.Bucket4jJCache;
import io.github.bucket4j.grid.jcache.JCacheProxyManager;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class KeyBasedBucketsTest {

    private static volatile JCacheProxyManager<String> buckets;

    private static final AtomicInteger counter = new AtomicInteger();

    public static void main(String[] args) throws InterruptedException {

        ReallySimpleCacheTest cache = new ReallySimpleCacheTest();

        cache.inspectCache();

        buckets = Bucket4jJCache.entryProcessorBasedBuilder(cache)
                                .build();

//        putLoadOnStuff("user1", 500, 20);
//        System.out.println("Waiting");
//        Thread.sleep(15000);
//        System.out.println("Waited");
        putLoadOnStuff("user1", 50, 10000);
        putLoadOnStuff("user2", 1000, 10000);
        putLoadOnStuff("user3", 2000, 10000);
    }

    private static void putLoadOnStuff(String userId, int delay, int iterations) {
        AtomicInteger atomicInteger = new AtomicInteger(iterations);
            while (atomicInteger.decrementAndGet() >= 0) {
                try {
                    BucketProxy bucketProxy = buckets.getProxy(userId, () -> {
                        BucketConfiguration config = BucketConfiguration
                                .builder()
                                .addLimit(limit -> limit.capacity(1000).refillGreedy(1000, Duration.ofSeconds(1)))
                                .build();
                        return config;
                    });
                    boolean isConsumed = bucketProxy.tryConsume(1);

                    System.out.println(userId + ": consumed: " + isConsumed + ". Counter: " + counter.incrementAndGet());
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
    }

}

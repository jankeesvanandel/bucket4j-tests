package org.example;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleBucketTest {
    private static final Bucket bucket = Bucket.builder()
            .addLimit(limit -> limit.capacity(3).refillGreedy(3, Duration.ofSeconds(1)).id("per-second"))
            .addLimit(limit -> limit.capacity(60).refillGreedy(60, Duration.ofMinutes(1)).id("per-minute"))
                                               // 150 tps
                                               // 150tps * 2 * 60 per minute
                                               // 150tps * 2 * 2 per second burst
            .build();
private static final AtomicInteger counter = new AtomicInteger();
    public static void main(String[] args) {
//        Inspector.inspectBucket(bucket);

        putLoadOnStuff();
    }

    private static void putLoadOnStuff() {
        long start = System.nanoTime();
        while (true) {
            try {
                ConsumptionProbe consumptionProbe = bucket.tryConsumeAndReturnRemaining(1);
                int i = counter.incrementAndGet();
                long now = System.nanoTime();
                System.out.println(((now - start) / 1_000_000) + "ms - calls: " + i + " - Consumed: " + consumptionProbe.isConsumed() + " remaining: " + consumptionProbe.getRemainingTokens());
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

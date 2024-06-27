package org.example;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ExecutionStrategy;
import io.github.bucket4j.distributed.proxy.RecoveryStrategy;
import io.github.bucket4j.distributed.proxy.optimization.DelayParameters;
import io.github.bucket4j.distributed.proxy.optimization.Optimizations;
import org.example.dynamodb.Bucket4jDynamoDB;
import org.example.dynamodb.DynamoDBProxyManager;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class DynamoDBBucketsTest {

    private static final AtomicInteger counter = new AtomicInteger();

    private static DynamoDBProxyManager proxyManager;
    private static final Executor executor = Executors.newCachedThreadPool();

    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(10000);

    public static void main(String[] args) {
        proxyManager = Bucket4jDynamoDB.builder().executionStrategy(ExecutionStrategy.background(executor)).build();

        IntStream.range(0, 3).forEach(i -> putLoadOnStuff("user11", 500, 10000, 1200));
//        putLoadOnStuff("user2", 10, 10000);
//        putLoadOnStuff("user3", 20, 10000);
    }

    private static void putLoadOnStuff(String userId, int delay, int iterations, int capacity) {
        new Thread(() -> {
            while (ATOMIC_INTEGER.decrementAndGet() >= 0) {
//                try {
                    Bucket bucket = proxyManager.builder()
                                .withOptimization(Optimizations.delaying(
                                        new DelayParameters(1000, Duration.ofSeconds(10)))
                                )
                                .withRecoveryStrategy(RecoveryStrategy.THROW_BUCKET_NOT_FOUND_EXCEPTION)
                                .build(userId, () -> BucketConfiguration.builder()
                                                                        .addLimit(limit -> limit
                                                                                .capacity(capacity)
                                                                                .refillGreedy(capacity, Duration.ofMinutes(1)))
                                                                        .build());
                    ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

                    System.out.printf("%s: consumed: %s, remaining: %s, counter: %d%n", userId, probe.isConsumed(), probe.getRemainingTokens(), counter.incrementAndGet());
//                    Thread.sleep(delay);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
            }
        }).start();
    }

}

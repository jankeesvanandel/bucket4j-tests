package org.example;

import io.github.bucket4j.Bucket;

public class Inspector {
    public static void inspectBucket(Bucket bucket) {
        new Thread(() -> {
            while (true) {
                System.out.println("Available tokens: " + bucket.getAvailableTokens());
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}

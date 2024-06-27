package org.example;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import javax.cache.processor.MutableEntry;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ReallySimpleCacheTest implements Cache<String, byte[]> {

    private final ConcurrentHashMap<String, byte[]> map = new ConcurrentHashMap<>();

    private final AtomicInteger getCounter = new AtomicInteger();
    private final AtomicInteger putCounter = new AtomicInteger();
    private final AtomicInteger delCounter = new AtomicInteger();

    @Override
    public byte[] get(String key) {
        getCounter.incrementAndGet();
        return map.get(key);
    }

    @Override
    public Map<String, byte[]> getAll(Set<? extends String> keys) {
        getCounter.incrementAndGet();
        return map;
    }

    @Override
    public boolean containsKey(String key) {
        getCounter.incrementAndGet();
        return map.containsKey(key);
    }

    @Override
    public void loadAll(Set<? extends String> keys, boolean replaceExistingValues, CompletionListener completionListener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void put(String key, byte[] value) {
        putCounter.incrementAndGet();
        this.map.put(key, value);
    }

    @Override
    public byte[] getAndPut(String key, byte[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void putAll(Map<? extends String, ? extends byte[]> map2) {
        putCounter.incrementAndGet();
        map.putAll(map2);
    }

    @Override
    public boolean putIfAbsent(String key, byte[] value) {
        putCounter.incrementAndGet();
        return Arrays.equals(map.putIfAbsent(key, value), value);
    }

    @Override
    public boolean remove(String key) {
        delCounter.incrementAndGet();
        return map.remove(key) != null;
    }

    @Override
    public boolean remove(String key, byte[] oldValue) {
        delCounter.incrementAndGet();
        return map.remove(key, oldValue);
    }

    @Override
    public byte[] getAndRemove(String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean replace(String key, byte[] oldValue, byte[] newValue) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean replace(String key, byte[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] getAndReplace(String key, byte[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeAll(Set<? extends String> keys) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeAll() {
        delCounter.incrementAndGet();
        map.clear();
    }

    @Override
    public void clear() {
        delCounter.incrementAndGet();
        map.clear();
    }

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public <C extends Configuration<String, byte[]>> C getConfiguration(Class<C> clazz) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T invoke(String key, EntryProcessor<String, byte[], T> entryProcessor, Object... arguments) throws EntryProcessorException {
        return entryProcessor.process(new MutableEntry<String, byte[]>() {
            @Override
            public boolean exists() {
                getCounter.incrementAndGet();
                return map.containsKey(key);
            }

            @Override
            public void remove() {
                delCounter.incrementAndGet();
                map.remove(key);
            }

            @Override
            public void setValue(byte[] value) {
                putCounter.incrementAndGet();
                map.put(key, value);
            }

            @Override
            public String getKey() {
                getCounter.incrementAndGet();
                return key;
            }

            @Override
            public byte[] getValue() {
                getCounter.incrementAndGet();
                return map.get(key);
            }

            @Override
            public <T2> T2 unwrap(Class<T2> clazz) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }, arguments);
    }

    @Override
    public <T> Map<String, EntryProcessorResult<T>> invokeAll(Set<? extends String> keys, EntryProcessor<String, byte[], T> entryProcessor,
            Object... arguments) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CacheManager getCacheManager() {
        return null;
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isClosed() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void registerCacheEntryListener(CacheEntryListenerConfiguration<String, byte[]> cacheEntryListenerConfiguration) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deregisterCacheEntryListener(CacheEntryListenerConfiguration<String, byte[]> cacheEntryListenerConfiguration) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterator<Entry<String, byte[]>> iterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void inspectCache() {
        new Thread(() -> {
            while (true) {
                System.out.println("gets: " + getCounter.get() + ", puts: " + putCounter.get() + ", dels: " + delCounter.get());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}

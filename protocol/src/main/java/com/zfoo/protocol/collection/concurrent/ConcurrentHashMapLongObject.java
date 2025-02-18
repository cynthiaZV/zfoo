/*
 * Copyright (C) 2020 The zfoo Authors
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.protocol.collection.concurrent;

import io.netty.util.collection.LongObjectHashMap;
import io.netty.util.collection.LongObjectMap;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;


/**
 * EN: The performance is much lower than ConcurrentHashMap of the JDK, and the purpose is to reduce the memory footprint of the Long wrapper type
 * CN: 性能远低于JDK自带的ConcurrentHashMap，主要就是减少Long包装类型的内存占用
 *
 * @author godotg
 */
public class ConcurrentHashMapLongObject<V> implements Map<Long, V> {

    public static final int DEFAULT_BUCKET_SIZE = 16;

    // 分段锁
    private int buckets;
    private int mask;
    private ReadWriteLock[] locks;
    // bucket对应的分段map
    private List<LongObjectHashMap<V>> maps;

    public ConcurrentHashMapLongObject(int buckets) {
        var shift = Integer.numberOfLeadingZeros(buckets);
        this.mask = 0XFFFF_FFFF >>> shift;

        this.buckets = this.mask + 1;
        this.locks = new ReadWriteLock[this.buckets];
        this.maps = new ArrayList<>(this.buckets);

        for (var i = 0; i < this.buckets; i++) {
            locks[i] = new ReentrantReadWriteLock();
            maps.add(new LongObjectHashMap<>());
        }
    }

    public ConcurrentHashMapLongObject() {
        this(DEFAULT_BUCKET_SIZE);
    }

    private int bucketOf(long key) {
        return ((int) key) & mask;
    }

    @Override
    public int size() {
        var sum = 0;
        for (var map : maps) {
            sum += map.size();
        }
        return sum;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return containsKey(((Long) key).longValue());
    }

    public boolean containsKey(long key) {
        var bucket = bucketOf(key);
        var readLock = locks[bucket].readLock();
        readLock.lock();
        try {
            return maps.get(bucket).containsKey(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean containsValue(Object value) {
        for (var i = 0; i < buckets; i++) {
            var readLock = locks[i].readLock();
            var map = maps.get(i);
            readLock.lock();
            try {
                if (map.containsValue(value)) {
                    return true;
                }
            } finally {
                readLock.unlock();
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        return get(((Long) key).longValue());
    }

    public V get(long key) {
        var bucket = bucketOf(key);
        var readLock = locks[bucket].readLock();
        readLock.lock();
        try {
            return maps.get(bucket).get(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public V put(Long key, V value) {
        return put(key.longValue(), value);
    }

    public V put(long key, V value) {
        var bucket = bucketOf(key);
        var writeLock = locks[bucket].writeLock();
        writeLock.lock();
        try {
            return maps.get(bucket).put(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V remove(Object key) {
        return remove(((Long) key).longValue());
    }

    public V remove(long key) {
        var bucket = bucketOf(key);
        var writeLock = locks[bucket].writeLock();
        writeLock.lock();
        try {
            return maps.get(bucket).remove(key);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void putAll(Map<? extends Long, ? extends V> m) {
        for (var entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        for (var i = 0; i < buckets; i++) {
            var writeLock = locks[i].writeLock();
            var map = maps.get(i);
            writeLock.lock();
            try {
                map.clear();
            } finally {
                writeLock.unlock();
            }
        }
    }

    public void forEachPrimitive(Consumer<LongObjectMap.PrimitiveEntry<V>> consumer) {
        for (var i = 0; i < buckets; i++) {
            var readLock = locks[i].readLock();
            var map = maps.get(i);
            readLock.lock();
            try {
                for (var entry : map.entries()) {
                    consumer.accept(entry);
                }
            } finally {
                readLock.unlock();
            }
        }
    }

    private Map<Long, V> copyMap() {
        var copyMap = new HashMap<Long, V>();
        for (var i = 0; i < buckets; i++) {
            var readLock = locks[i].readLock();
            var map = maps.get(i);
            readLock.lock();
            try {
                copyMap.putAll(map);
            } finally {
                readLock.unlock();
            }
        }
        return copyMap;
    }

    @Override
    public Set<Long> keySet() {
        return copyMap().keySet();
    }

    @Override
    public Collection<V> values() {
        return copyMap().values();
    }

    @Override
    public Set<Entry<Long, V>> entrySet() {
        return copyMap().entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Map<?, ?> m)) {
            return false;
        }
        return m.equals(this);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}

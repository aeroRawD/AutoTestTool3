package com.spx.adb;

import java.util.LinkedHashMap;

/**
 * 一个可以设置过期时间的缓存结构
 * 
 * @author SHAOPENGXIANG
 * @param <V>
 * @param <K>
 */
public class ExpirationCache<Key, Val> {
    LinkedHashMap<Key, Val> data = new LinkedHashMap<Key, Val>();
    LimitedHashMap<Key, Long> timeMap = null;
    private int timeToExpire = 30;
    private int capacity = 100;

    class LimitedHashMap<K, V> extends LinkedHashMap<K, V> {
        private int capacity = 100;

        public LimitedHashMap(int maxCapacity) {
            capacity = maxCapacity;
        }

        @Override
        protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
            if (size() > capacity) {
                data.remove(eldest.getKey());
                return true;
            }

            return false;
        }

    }

    /**
     * @param seconds
     *            数据过期时长, 秒为单位
     * @param cap
     *            缓存尺寸
     */
    public ExpirationCache(int seconds, int cap) {
        super();
        timeToExpire = seconds;
        capacity = cap;
        timeMap = new LimitedHashMap<Key, Long>(capacity);
    }

    //
    public void clear() {
        timeMap.clear();
        data.clear();
    }

    public Val get(Key key) {
        if (timeMap.get(key) == null) {
            data.remove(key);
            return null;
        }

        if (data.get(key) == null) {
            timeMap.remove(key);
            return null;
        }
        //
        Long time = timeMap.get(key);
        //
        // 失效数据
        if (System.currentTimeMillis() - time > timeToExpire * 1000) {
            timeMap.remove(key);
            data.remove(key);
            return null;
        }
        return data.get(key);
    }

    public void put(Key key, Val value) {
        timeMap.put(key, System.currentTimeMillis());
        data.put(key, value);
    }

    public Val remove(Key key) {
        timeMap.remove(key);
        return data.remove(key);
    }

    public String toString() {
        String s = timeMap.toString();
        s += ", ==> " + data.toString();
        return s;
    }


    public static void main(String[] args) {
        ExpirationCache<String, String> cache = new ExpirationCache<String, String>(6, 10);
        cache.put("1", "3");
        //System.out.println("cache:" + cache.toString());
        cache.put("2", "22");
        //System.out.println("cache:" + cache.toString());
        cache.put("3", "33");
        //System.out.println("cache:" + cache.toString());
        cache.put("4", "44");
        //System.out.println("cache:" + cache.toString());
        cache.put("5", "55");
        cache.put("6", "66");
        cache.put("7", "77");
        //System.out.println("cache:" + cache.toString());
        cache.put("8", "88");
        cache.put("9", "99");
        System.out.println("cache:" + cache.toString());
        cache.put("10", "10");
        System.out.println("cache:" + cache.toString());

        cache.put("11", "11-11");
        System.out.println("cache:" + cache.toString());
        cache.put("12", "12");
        System.out.println("cache:" + cache.toString());
        cache.put("13", "13");
        System.out.println("cache:" + cache.toString());
        
        System.out.println("cache:  KEY_1:" + cache.get("1"));
        System.out.println("cache:  KEY_2:" + cache.get("2"));
        System.out.println("cache:  KEY_3:" + cache.get("3"));
        System.out.println("cache:  KEY_4:" + cache.get("4"));
        try {
            Thread.currentThread().sleep(6*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("cache:  KEY_4:" + cache.get("4"));
    }
}

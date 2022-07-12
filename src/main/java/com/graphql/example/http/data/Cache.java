package com.graphql.example.http.data;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class Cache {
    public static LoadingCache<String, Object> cache;
    public static Mongo db;
    static final Object NULL = new Object();

    public static int initializeCache() {
        db = new Mongo();
        cache = CacheBuilder.newBuilder().build(new CacheLoader<String, Object>() {
            @Override
            public Object load(String id) {
                if (Integer.parseInt(id) >= 50000) {
                    Human data = db.getHuman("humans", id);
                    if (data == null) {
                        return new Object();
                    }
                    return data;
                } else {
                    Droid data = db.getDroid("droids", id);
                    if (data == null) {
                        return new Object();
                    }
                    return data;
                }
            }
        });
        if (cache == null) {
            return -1;
        }
        return 0;
    }
}

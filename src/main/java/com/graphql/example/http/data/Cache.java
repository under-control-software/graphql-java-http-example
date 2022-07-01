package com.graphql.example.http.data;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class Cache {
    public static LoadingCache<String, Object> cache;
    static final Object NULL = new Object();

    public static void initializeCache() {
        cache = CacheBuilder.newBuilder().build(new CacheLoader<String, Object>() {
            @Override
            public Object load(String id) {
                System.out.println("\nCache miss\n");
                if (Integer.parseInt(id) >= 6000) {
                    Human data = Mongo.db.getHuman("humans", id);
                    if (data == null) {
                        return new Object();
                    }
                    return data;
                } else {
                    Droid data = Mongo.db.getDroid("droids", id);
                    if (data == null) {
                        return new Object();
                    }
                    return data;
                }
            }
        });
    }
}

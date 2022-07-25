package com.graphql.example.http.data;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class Cache {
    private static Cache cache = null; 
    public LoadingCache<String, Object> loader;
    final Object NULL = new Object();

    private Cache() {
        initializeCache();
    }

    public static Cache getInstance() {
        if(cache == null) {
            synchronized(Cache.class) {
                if(cache == null) {
                    cache = new Cache();
                }
            }
        }
        return cache;
    }

    public static void instantiate() {
        getInstance();
    }

    public void initializeCache() {
        loader = CacheBuilder.newBuilder().build(new CacheLoader<String, Object>() {
            @Override
            public Object load(String id) {
                Mongo db = Mongo.getInstance();
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
    }
}

package com.graphql.example.http.data;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.graphql.example.http.StarWarsWiring;

public class Cache {
    public LoadingCache<String, Object> cache;
    final Object NULL = new Object();

    public Cache() {
        initializeCache();
    }

    public void initializeCache() {
        cache = CacheBuilder.newBuilder().build(new CacheLoader<String, Object>() {
            @Override
            public Object load(String id) {
                if (Integer.parseInt(id) >= 50000) {
                    Human data = StarWarsWiring.db.getHuman("humans", id);
                    if (data == null) {
                        return new Object();
                    }
                    return data;
                } else {
                    Droid data = StarWarsWiring.db.getDroid("droids", id);
                    if (data == null) {
                        return new Object();
                    }
                    return data;
                }
            }
        });
    }
}

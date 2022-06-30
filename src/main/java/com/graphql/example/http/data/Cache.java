package com.graphql.example.http.data;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class Cache {
    public static LoadingCache<String, Object> cache;

    public static void initializeCache() {
        final Mongo db = new Mongo();
        cache = CacheBuilder.newBuilder().build(new CacheLoader<String, Object>() {
            @Override
            public Object load(String id) {
                System.out.println("\nCache miss\n");
                if (Integer.parseInt(id) >= 6000) {
                    db.connectToCollection("humans");
                    Human data = db.getHuman(id);
                    return data;
                } else {
                    db.connectToCollection("droids");
                    Droid data = db.getDroid(id);
                    return data;
                }
            }
        });
    }
}

package com.graphql.example.http.data;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class Cache {
    public static LoadingCache<String, Object> cache;
    static final Object NULL = new Object();

    public static void initializeCache() {
        // final Mongo db = new Mongo();
        cache = CacheBuilder.newBuilder().build(new CacheLoader<String, Object>() {
            @Override
            public Object load(String id) {
                System.out.println("\nCache miss\n");
                if (Integer.parseInt(id) >= 6000) {
                    Mongo.db.connectToCollection("humans");
                    Human data = Mongo.db.getHuman(id);
                    if (data == null) {
                        return new Object();
                    }
                    return data;
                } else {
                    Mongo.db.connectToCollection("droids");
                    Droid data = Mongo.db.getDroid(id);
                    if (data == null) {
                        return new Object();
                    }
                    return data;
                }
            }
        });
    }
}

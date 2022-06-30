package com.graphql.example.http.data;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static java.util.Arrays.asList;

/**
 * This contains our data used in this example. Imagine it is a database or an
 * upstream REST resource
 * pf data (and not just an in memory representation)
 */
@SuppressWarnings("unused")
public class StarWarsData {

    public static Object getCharacterData(String id) {
        try {
            System.out.println("\nGetting data:");
            Object hd = Cache.cache.get(id);
            // Cache.cache.invalidateAll();
            return hd;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

}

package com.graphql.example.http.data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javafx.util.Pair;

import static java.util.Arrays.asList;

/**
 * This contains our data used in this example. Imagine it is a database or an
 * upstream REST resource
 * pf data (and not just an in memory representation)
 */
@SuppressWarnings("unused")
public class StarWarsData {

    public static Object getCharacterData(Pair<String, String> key) {
        String id = key.getKey();
        String type = key.getValue();
        if (type == "Human" && Integer.parseInt(id) < 6000) {
            return null;
        } else if (type == "Droid" && Integer.parseInt(id) >= 6000) {
            return null;
        }
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

    public static void addHumanData(Human data) {
        Mongo.db.connectToCollection("humans");
        Mongo.db.addHuman(data);
    }

    public static void addDroidData(Droid data) {
        Mongo.db.connectToCollection("droids");
        Mongo.db.addDroid(data);
    }

    public static void updateHumanData(Human data) {
        Mongo.db.connectToCollection("humans");
        Mongo.db.updateHuman(data);
    }

    public static void updateDroidData(Droid data) {
        Mongo.db.connectToCollection("droids");
        Mongo.db.updateDroid(data);
    }

}

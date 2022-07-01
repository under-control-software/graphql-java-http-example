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
        if (type == "Human" && Integer.parseInt(id) < 50000) {
            return null;
        } else if (type == "Droid" && Integer.parseInt(id) >= 50000) {
            return null;
        }
        try {
            Object hd = Cache.cache.get(id);
            if (!(hd instanceof Human) && !(hd instanceof Droid)) {
                return null;
            }
            return hd;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void addHumanData(Human data) {
        Mongo.db.addHuman("humans", data);
    }

    public static void addDroidData(Droid data) {
        Mongo.db.addDroid("droids", data);
    }

    public static void updateHumanData(Human data) {
        Mongo.db.updateHuman("humans", data);
    }

    public static void updateDroidData(Droid data) {
        Mongo.db.updateDroid("droids", data);
    }

}

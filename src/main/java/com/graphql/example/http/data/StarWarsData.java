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
        // try {
        //     Object hd = Cache.getInstance().loader.get(id);
        //     if (!(hd instanceof Human) && !(hd instanceof Droid)) {
        //         return null;
        //     }
        //     return hd;
        // } catch (ExecutionException e) {
        //     e.printStackTrace();
        //     return null;
        // }
        if (Integer.parseInt(id) >= 50000) {
            Human data = Mongo.getInstance().getHuman("humans", id);
            return data;
        } else {
            Droid data = Mongo.getInstance().getDroid("droids", id);
            return data;
        }
    }

    public static void addHumanData(Human data) {
        Mongo.getInstance().addHuman("humans", data);
    }

    public static void addDroidData(Droid data) {
        Mongo.getInstance().addDroid("droids", data);
    }

    public static void updateHumanData(Human data) {
        Mongo.getInstance().updateHuman("humans", data);
    }

    public static void updateDroidData(Droid data) {
        Mongo.getInstance().updateDroid("droids", data);
    }

}

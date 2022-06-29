package com.graphql.example.http.data;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * This contains our data used in this example. Imagine it is a database or an
 * upstream REST resource
 * pf data (and not just an in memory representation)
 */
@SuppressWarnings("unused")
public class StarWarsData {

    static Mongo db = new Mongo();

    public static Object getCharacterData(String id) {
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

}

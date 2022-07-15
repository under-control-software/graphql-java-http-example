package com.graphql.example.http.data;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.graphql.example.http.utill.Utility;

import static java.util.Arrays.asList;

/**
 * This contains our data used in this example. Imagine it is a database or an
 * upstream REST resource
 * pf data (and not just an in memory representation)
 */
@SuppressWarnings("unused")
public class StarWarsData {

    private static Logger swdLogger = Logger.getLogger(StarWarsData.class);

    static Mongo db = new Mongo();

    public static Object getCharacterData(String id) {

        if (Integer.parseInt(id) >= 50000) {
            db.connectToCollection("humans");
            Human data = db.getHuman(id);
            return data;
        } else if (Integer.parseInt(id) < 50000 && Integer.parseInt(id) >= 0) {
            db.connectToCollection("droids");
            Droid data = db.getDroid(id);
            return data;
        } else {
            swdLogger.error("Invalid ID in request");
            return null;
        }
    }

}

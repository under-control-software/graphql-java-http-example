package com.graphql.example.http.data;

import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;

import com.codahale.metrics.Timer;
import com.graphql.example.http.utill.Utility;

import static com.mongodb.client.model.Filters.eq;

public class Mongo {
    private MongoClient mongoClient = null;
    private MongoDatabase database = null;
    private MongoCollection<Document> collection = null;
    private Timer timer;
    private Timer.Context timerContext;

    public Mongo() {
        mongoClient = MongoClients.create();
        database = mongoClient.getDatabase("starwardb");
        timer = new Timer();
    }

    public void disconnect() {
        mongoClient.close();
    }

    public void connectToCollection(String collectionName) {
        collection = database.getCollection(collectionName);
    }

    @SuppressWarnings(value = "unchecked")
    public Human getHuman(String id) {
        timerContext = timer.time();
        Document doc = collection.find(eq("_id", id)).first();
        long elapsed = timerContext.stop();

        if (doc == null)
            return null;

        final String queryTime = String.valueOf(Utility.formatTime(elapsed));

        Human data = new Human(
                (String) doc.get("_id"),
                (String) doc.get("name"),
                (List<String>) doc.get("friends"),
                (List<Integer>) doc.get("appearsIn"),
                (String) doc.get("homePlanet"),
                queryTime);

        return data;
    }

    @SuppressWarnings(value = "unchecked")
    public Droid getDroid(String id) {
        timerContext = timer.time();
        Document doc = collection.find(eq("_id", id)).first();
        long elapsed = timerContext.stop();

        if (doc == null)
            return null;

        final String queryTime = String.valueOf(Utility.formatTime(elapsed));

        Droid data = new Droid(
                (String) doc.get("_id"),
                (String) doc.get("name"),
                (List<String>) doc.get("friends"),
                (List<Integer>) doc.get("appearsIn"),
                (String) doc.get("primaryFunction"),
                queryTime);

        return data;
    }

}
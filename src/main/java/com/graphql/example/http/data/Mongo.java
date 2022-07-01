package com.graphql.example.http.data;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.MongoCollection;

public class Mongo {
    public static Mongo db = new Mongo();

    private MongoClient mongoClient = null;
    private MongoDatabase database = null;
    private long instant1, instant2;

    public Mongo() {
        System.out.println("Creating Mongo client");
        mongoClient = MongoClients.create();
        database = mongoClient.getDatabase("starwardb");
    }

    public void disconnect() {
        mongoClient.close();
    }

    public Human getHuman(String collectionName, String id) {
        instant1 = System.currentTimeMillis();
        MongoCollection<Document> collection = database.getCollection(collectionName);
        Document doc = collection.find(eq("_id", id)).first();
        instant2 = System.currentTimeMillis();

        if (doc == null)
            return null;

        final String queryTime = String.valueOf(instant2 - instant1);

        Human data = new Human(
                (String)doc.get("_id"),
                (String)doc.get("name"),
                (List<String>) doc.get("friends"),
                (List<Integer>) doc.get("appearsIn"),
                (String)doc.get("homePlanet"),
                queryTime);

        return data;
    }

    public Droid getDroid(String collectionName, String id) {
        instant1 = System.currentTimeMillis();
        MongoCollection<Document> collection = database.getCollection(collectionName);
        Document doc = collection.find(eq("_id", id)).first();
        instant2 = System.currentTimeMillis();

        if (doc == null)
            return null;

        final String queryTime = String.valueOf(instant2 - instant1);

        Droid data = new Droid(
                (String)doc.get("_id"),
                (String)doc.get("name"),
                (List<String>) doc.get("friends"),
                (List<Integer>) doc.get("appearsIn"),
                (String)doc.get("primaryFunction"),
                queryTime);

        return data;
    }

    public void addHuman(String collectionName, Human data) {
        try {
            MongoCollection<Document> collection = database.getCollection(collectionName);
            InsertOneResult result = collection.insertOne(new Document()
                    .append("_id", data.getId())
                    .append("name", data.getName())
                    .append("friends", data.getFriends())
                    .append("appearsIn", data.getAppearsIn())
                    .append("homePlanet", data.getHomePlanet()));
            System.out.println("Success! Inserted document id: " + result.getInsertedId());
        } catch (MongoException me) {
            System.err.println("Unable to insert due to an error: " + me);
        }
    }

    public void addDroid(String collectionName, Droid data) {
        try {
            MongoCollection<Document> collection = database.getCollection(collectionName);
            InsertOneResult result = collection.insertOne(new Document()
                    .append("_id", data.getId())
                    .append("name", data.getName())
                    .append("friends", data.getFriends())
                    .append("appearsIn", data.getAppearsIn())
                    .append("primaryFunction", data.getPrimaryFunction()));
            System.out.println("Success! Inserted document id: " + result.getInsertedId());
        } catch (MongoException me) {
            System.err.println("Unable to insert due to an error: " + me);
        }
    }

    public void updateHuman(String collectionName, Human data) {
        Document query = new Document().append("_id", data.getId());

        Bson updates = Updates.set("_id", data.getId());
        if (data.getName() != null) updates = Updates.combine(updates, Updates.set("name", data.getName()));
        if (data.getFriends() != null) updates = Updates.combine(updates, Updates.set("friends", data.getFriends()));
        if (data.getAppearsIn() != null) updates = Updates.combine(updates, Updates.set("appearsIn", data.getAppearsIn()));
        if (data.getHomePlanet() != null) updates = Updates.combine(updates, Updates.set("homePlanet", data.getHomePlanet()));
        
        try {
            MongoCollection<Document> collection = database.getCollection(collectionName);
            UpdateResult result = collection.updateOne(query, updates);
            System.out.println("Modified document count: " + result.getModifiedCount());
        } catch (MongoException me) {
            System.err.println("Unable to update due to an error: " + me);
        }
    }

    public void updateDroid(String collectionName, Droid data) {
        Document query = new Document().append("_id", data.getId());

        Bson updates = Updates.set("_id", data.getId());
        if (data.getName() != null) updates = Updates.combine(updates, Updates.set("name", data.getName()));
        if (data.getFriends() != null) updates = Updates.combine(updates, Updates.set("friends", data.getFriends()));
        if (data.getAppearsIn() != null) updates = Updates.combine(updates, Updates.set("appearsIn", data.getAppearsIn()));
        if (data.getPrimaryFunction() != null) updates = Updates.combine(updates, Updates.set("primaryFunction", data.getPrimaryFunction()));
        System.out.println(updates);
        try {
            MongoCollection<Document> collection = database.getCollection(collectionName);
            UpdateResult result = collection.updateOne(query, updates);
            System.out.println("Modified document count: " + result.getModifiedCount());
        } catch (MongoException me) {
            System.err.println("Unable to update due to an error: " + me);
        }
    }

}
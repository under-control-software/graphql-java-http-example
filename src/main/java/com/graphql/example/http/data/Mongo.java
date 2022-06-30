package com.graphql.example.http.data;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.MongoCollection;

public class Mongo {
    public static Mongo db = new Mongo();

    private MongoClient mongoClient = null;
    private MongoDatabase database = null;
    private MongoCollection<Document> collection = null;
    private long instant1, instant2;

    public Mongo() {
        System.out.println("Creating Mongo client");
        mongoClient = MongoClients.create("mongodb://host.docker.internal:27020");
        // mongoClient = MongoClients.create("mongodb://localhost:27020");
        database = mongoClient.getDatabase("starWars");
    }

    public void disconnect() {
        mongoClient.close();
    }

    public void connectToCollection(String collectionName) {
        collection = database.getCollection(collectionName);
    }

    public Human getHuman(String id) {
        instant1 = System.currentTimeMillis();
        Document doc = collection.find(eq("id", id)).first();
        instant2 = System.currentTimeMillis();

        if (doc == null)
            return null;

        final String queryTime = String.valueOf(instant2 - instant1);

        Human data = new Human(
                doc.get("id").toString(),
                doc.get("name").toString(),
                (List<String>) doc.get("friends"),
                (List<Integer>) doc.get("appearsIn"),
                doc.get("homePlanet").toString(),
                queryTime);

        return data;
    }

    public Droid getDroid(String id) {
        instant1 = System.currentTimeMillis();
        Document doc = collection.find(eq("id", id)).first();
        instant2 = System.currentTimeMillis();

        if (doc == null)
            return null;

        final String queryTime = String.valueOf(instant2 - instant1);

        Droid data = new Droid(
                doc.get("id").toString(),
                doc.get("name").toString(),
                (List<String>) doc.get("friends"),
                (List<Integer>) doc.get("appearsIn"),
                doc.get("primaryFunction").toString(),
                queryTime);

        return data;
    }

    public void addHuman(Human data) {
        try {
            InsertOneResult result = collection.insertOne(new Document()
                    .append("id", data.getId())
                    .append("name", data.getName())
                    .append("friends", data.getFriends())
                    .append("appearsIn", data.getAppearsIn())
                    .append("homePlanet", data.getHomePlanet()));
            System.out.println("Success! Inserted document id: " + result.getInsertedId());
        } catch (MongoException me) {
            System.err.println("Unable to insert due to an error: " + me);
        }
    }

    public void addDroid(Droid data) {
        try {
            InsertOneResult result = collection.insertOne(new Document()
                    .append("id", data.getId())
                    .append("name", data.getName())
                    .append("friends", data.getFriends())
                    .append("appearsIn", data.getAppearsIn())
                    .append("primaryFunction", data.getPrimaryFunction()));
            System.out.println("Success! Inserted document id: " + result.getInsertedId());
        } catch (MongoException me) {
            System.err.println("Unable to insert due to an error: " + me);
        }
    }

    public void updateHuman(Human data) {
        Document query = new Document().append("id", data.getId());

        Bson updates = Updates.combine(
                            Updates.set("name", data.getName()),
                            Updates.set("friends", data.getFriends()),
                            Updates.set("appearsIn", data.getAppearsIn()),
                            Updates.set("homePlanet", data.getHomePlanet())
                        );
        UpdateOptions options = new UpdateOptions().upsert(true);
        
        try {
            UpdateResult result = collection.updateOne(query, updates, options);
            System.out.println("Modified document count: " + result.getModifiedCount());
            System.out.println("Upserted id: " + result.getUpsertedId()); // only contains a value when an upsert is performed
        } catch (MongoException me) {
            System.err.println("Unable to update due to an error: " + me);
        }
    }

    public void updateDroid(Droid data) {
        Document query = new Document().append("id", data.getId());

        Bson updates = Updates.combine(
                            Updates.set("name", data.getName()),
                            Updates.set("friends", data.getFriends()),
                            Updates.set("appearsIn", data.getAppearsIn()),
                            Updates.set("primaryFunction", data.getPrimaryFunction())
                        );
        UpdateOptions options = new UpdateOptions().upsert(true);
        
        try {
            UpdateResult result = collection.updateOne(query, updates, options);
            System.out.println("Modified document count: " + result.getModifiedCount());
            System.out.println("Upserted id: " + result.getUpsertedId()); // only contains a value when an upsert is performed
        } catch (MongoException me) {
            System.err.println("Unable to update due to an error: " + me);
        }
    }
}
package com.graphql.example.http.data;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import org.bson.Document;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.MongoCollection;

public class Mongo {
    public static Mongo db = new Mongo();

    private MongoClient mongoClient = null;
    private MongoDatabase database = null;
    private MongoCollection<Document> collection = null;
    private long instant1, instant2;

    public Mongo() {
        System.out.println("Creating Mongo client");
        mongoClient = MongoClients.create();
        database = mongoClient.getDatabase("starwardb");
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
                (String) doc.get("id").toString(),
                (String) doc.get("name").toString(),
                (List<String>) doc.get("friends"),
                (List<Integer>) doc.get("appearsIn"),
                (String) doc.get("primaryFunction").toString(),
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
                    .append("homePlanet", data.getHomePlanet())
                );
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
                    .append("primaryFunction", data.getPrimaryFunction())
                );
            System.out.println("Success! Inserted document id: " + result.getInsertedId());
        } catch (MongoException me) {
            System.err.println("Unable to insert due to an error: " + me);
        }
    }
}
package com.graphql.example.http.data;

import java.util.Arrays;
import static java.util.Arrays.asList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;

// import com.mongodb.client.*;

public class Mongo {
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    public Mongo() {
        mongoClient = MongoClients.create();
        database = mongoClient.getDatabase("starwardb");
    }

    public void connectToCollection(String collectionName) {
        collection = database.getCollection(collectionName);
    }

    public Human getHuman(String id) {
        Document doc = collection.find(eq("id", id)).first();
        if (doc == null) return null;

        // System.out.println(doc.get("id").getClass());
        // System.out.println(doc.get("name").getClass());
        // System.out.println(doc.get("friends").getClass());
        // System.out.println(doc.get("appearsIn").getClass());
        // System.out.println(doc.get("homePlanet").getClass());

        // System.out.println(doc.get("id"));
        // System.out.println(doc.get("name"));
        // System.out.println(doc.get("friends"));
        // System.out.println(doc.get("appearsIn"));
        // System.out.println(doc.get("homePlanet"));

        Human data = new Human(
            (String)doc.get("id"), 
            (String)doc.get("name"), 
            (List<String>)doc.get("friends"), 
            (List<Integer>)doc.get("appearsIn"), 
            (String)doc.get("homePlanet")
        );

        return data;
    }

    public Droid getDroid(String id) {
        Document doc = collection.find(eq("id", id)).first();
        if (doc == null) return null;

        Droid data = new Droid(
            (String)doc.get("id"), 
            (String)doc.get("name"), 
            (List<String>)doc.get("friends"), 
            (List<Integer>)doc.get("appearsIn"), 
            (String)doc.get("primaryFunction")
        );

        return data;
    }
}
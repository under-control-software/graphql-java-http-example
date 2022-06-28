package com.graphql.example.http.data;

import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import org.bson.Document;

// import com.mongodb.client.MongoClient;
// import com.mongodb.client.MongoClients;
// import com.mongodb.client.MongoDatabase;
// import com.mongodb.client.MongoCollection;

import com.mongodb.client.*;

public class Mongo {
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    public Mongo() {
        mongoClient = MongoClients.create();
        database = mongoClient.getDatabase("starWars");
    }

    public void connectToCollection(String collectionName) {
        collection = database.getCollection(collectionName);
    }

    public void getHuman(String id) {
        // DBObject query = new BasicDBObject("id", id);
        // DBCursor cursor = collection.find(query);
        // DBObject jo = cursor.one();

        Document doc = collection.find(eq("id", id)).first();
        System.out.println(doc.toJson());

        // String name = (String)jo.get("name");
        // System.out.println("name of id 1000: " + name);
    }
}
package com.graphql.example.http.data;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.reactivestreams.Publisher;

import com.graphql.example.http.RequestHandler;
import com.mongodb.ConnectionString;

import com.mongodb.client.model.Updates;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.diagnostics.logging.Logger;
import com.mongodb.diagnostics.logging.Loggers;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;

import io.reactivex.Single;


public class Mongo {
    private static Mongo mongodb = null;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private long instant1, instant2;

    private static final Logger LOGGER = Loggers.getLogger("connection");

    private Mongo() {
        LOGGER.info("Creating Mongo client");
        ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017");
        mongoClient = MongoClients.create(connectionString);
        database = mongoClient.getDatabase("starwardb");
    }

    public static Mongo getInstance() {
        if(mongodb == null) {
            synchronized(Mongo.class) {
                if(mongodb == null) {
                    mongodb = new Mongo();
                }
            }
        }
        return mongodb;
    }

    public static void instantiate() {
        getInstance();
    }

    public void disconnect() {
        mongoClient.close();
    }

    public Human getHuman(String collectionName, String id) {
        instant1 = System.currentTimeMillis();
        MongoCollection<Document> collection = database.getCollection(collectionName);
        RequestHandler.getInstance().poolPop();
        Publisher<Document> pub = collection.find(eq("_id", id)).first();
        Document doc = null;

        try {
            doc = Single.fromPublisher(pub).blockingGet();
        } catch(Exception e) {
            LOGGER.warn("Possibly no document found by find query for corresponding id. Check msg: " + e);
        }

        RequestHandler.getInstance().poolPut(1);
        instant2 = System.currentTimeMillis();
        if (doc == null)
            return null;

        final String queryTime = String.valueOf(instant2 - instant1);
        Human data = new Human(
            (String) doc.get("_id"),
            (String) doc.get("name"),
            (List<String>) doc.get("friends"),
            (List<Integer>) doc.get("appearsIn"),
            (String) doc.get("homePlanet"),
            queryTime);

        return data;
    }

    public Droid getDroid(String collectionName, String id) {
        instant1 = System.currentTimeMillis();
        MongoCollection<Document> collection = database.getCollection(collectionName);
        RequestHandler.getInstance().poolPop();
        Publisher<Document> pub = collection.find(eq("_id", id)).first();
        Document doc = null;

        try {
            doc = Single.fromPublisher(pub).blockingGet();
        } catch(Exception e) {
            LOGGER.warn("Possibly no document found by find query for corresponding id. Check msg: " + e);
        }

        RequestHandler.getInstance().poolPut(1);
        instant2 = System.currentTimeMillis();
        if (doc == null)
            return null;
        
        final String queryTime = String.valueOf(instant2 - instant1);
        Droid data = new Droid(
            (String) doc.get("_id"),
            (String) doc.get("name"),
            (List<String>) doc.get("friends"),
            (List<Integer>) doc.get("appearsIn"),
            (String) doc.get("primaryFunction"),
            queryTime);

        return data;
    }

    public void addHuman(String collectionName, Human data) {
        if(Integer.parseInt(data.getId()) < 50000) {
            LOGGER.warn("Error in inserting human: Id less than 50000 not allowed");
        }
        MongoCollection<Document> collection = database.getCollection(collectionName);
        Publisher<InsertOneResult> pub = collection.insertOne(new Document()
            .append("_id", data.getId())
            .append("name", data.getName())
            .append("friends", data.getFriends())
            .append("appearsIn", data.getAppearsIn())
            .append("homePlanet", data.getHomePlanet())
        );

        try {
            InsertOneResult result = Single.fromPublisher(pub).blockingGet();
        } catch(Exception e) {
            LOGGER.warn("Error in inserting human: " + e);
        }
    }

    public void addDroid(String collectionName, Droid data) {
        if(Integer.parseInt(data.getId()) >= 50000) {
            LOGGER.warn("Error in inserting droid: Id greater than 49999 not allowed");
        }
        MongoCollection<Document> collection = database.getCollection(collectionName);
        Publisher<InsertOneResult> pub = collection.insertOne(new Document()
            .append("_id", data.getId())
            .append("name", data.getName())
            .append("friends", data.getFriends())
            .append("appearsIn", data.getAppearsIn())
            .append("primaryFunction", data.getPrimaryFunction())
        );

        try {
            InsertOneResult result = Single.fromPublisher(pub).blockingGet();
        } catch(Exception e) {
            LOGGER.warn("Error in inserting droid: " + e);
        }
    }

    public void updateHuman(String collectionName, Human data) {
        Document query = new Document().append("_id", data.getId());

        Bson updates = Updates.set("_id", data.getId());
        if (data.getName() != null) updates = Updates.combine(updates, Updates.set("name", data.getName()));
        if (data.getFriends() != null) updates = Updates.combine(updates, Updates.set("friends", data.getFriends()));
        if (data.getAppearsIn() != null)
            updates = Updates.combine(updates, Updates.set("appearsIn", data.getAppearsIn()));
        if (data.getHomePlanet() != null)
            updates = Updates.combine(updates, Updates.set("homePlanet", data.getHomePlanet()));

        MongoCollection<Document> collection = database.getCollection(collectionName);
        Publisher<UpdateResult> pub = collection.updateOne(query, updates);

        try {
            UpdateResult result = Single.fromPublisher(pub).blockingGet();
            if (result.getMatchedCount() == 0) {
                LOGGER.warn("Following update query for human failed : " + query);
            }
        } catch(Exception e) {
            LOGGER.warn("Error in updating human: " + e);
        }
    }

    public void updateDroid(String collectionName, Droid data) {
        Document query = new Document().append("_id", data.getId());

        Bson updates = Updates.set("_id", data.getId());
        if (data.getName() != null) updates = Updates.combine(updates, Updates.set("name", data.getName()));
        if (data.getFriends() != null) updates = Updates.combine(updates, Updates.set("friends", data.getFriends()));
        if (data.getAppearsIn() != null)
            updates = Updates.combine(updates, Updates.set("appearsIn", data.getAppearsIn()));
        if (data.getPrimaryFunction() != null)
            updates = Updates.combine(updates, Updates.set("primaryFunction", data.getPrimaryFunction()));
        
        MongoCollection<Document> collection = database.getCollection(collectionName);
        Publisher<UpdateResult> pub = collection.updateOne(query, updates);

        try {
            UpdateResult result = Single.fromPublisher(pub).blockingGet();
            if (result.getMatchedCount() == 0) {
                LOGGER.warn("Following update query for droid failed : " + query);
            }
        } catch(Exception e) {
            LOGGER.warn("Error in updating droid: " + e);
        }
    }

}

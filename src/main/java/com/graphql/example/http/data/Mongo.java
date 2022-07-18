package com.graphql.example.http.data;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoException;
// import com.mongodb.client.model.Updates;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.diagnostics.logging.Logger;
import com.mongodb.diagnostics.logging.Loggers;


public class Mongo {
    private MongoClient mongoClient = null;
    private MongoDatabase database = null;
    private long instant1, instant2;

    private static final Logger LOGGER = Loggers.getLogger("connection");

    public Mongo() {
        LOGGER.info("Creating Mongo client");
        mongoClient = MongoClients.create();
        database = mongoClient.getDatabase("starwardb");
    }

    public void disconnect() {
        mongoClient.close();
    }

    public Human getHuman(String collectionName, String id) {
        instant1 = System.currentTimeMillis();
        MongoCollection<Document> collection = database.getCollection(collectionName);
        CompletableFuture<Document> future = new CompletableFuture<>();
        collection.find(eq("_id", id)).first(new SingleResultCallback<Document>() {
            @Override
            public void onResult(final Document doc, final Throwable t) {
                future.complete(doc);
            }
        });

        return future.thenApply(doc -> {
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
        }).join();

    }

    public Droid getDroid(String collectionName, String id) {
        instant1 = System.currentTimeMillis();
        MongoCollection<Document> collection = database.getCollection(collectionName);
        CompletableFuture<Document> future = new CompletableFuture<>();
        collection.find(eq("_id", id)).first(new SingleResultCallback<Document>() {
            @Override
            public void onResult(final Document doc, final Throwable t) {
                future.complete(doc);
            }
        });

        return future.thenApply(doc -> {
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
        }).join();
    }

    public void addHuman(String collectionName, Human data) {
        try {
            MongoCollection<Document> collection = database.getCollection(collectionName);
            collection.insertOne(new Document()
                .append("_id", data.getId())
                .append("name", data.getName())
                .append("friends", data.getFriends())
                .append("appearsIn", data.getAppearsIn())
                .append("homePlanet", data.getHomePlanet()), 
                new SingleResultCallback<Void>() {
                    @Override
                    public void onResult(final Void doc, final Throwable t) {
                        if(t != null)
                            LOGGER.error("Unable to insert due to an error: " + t);
                    }
                });
        } catch (MongoException me) {
            LOGGER.error("Unable to insert due to an error: " + me);
        }
    }

    public void addDroid(String collectionName, Droid data) {
        try {
            MongoCollection<Document> collection = database.getCollection(collectionName);
            collection.insertOne(new Document()
                .append("_id", data.getId())
                .append("name", data.getName())
                .append("friends", data.getFriends())
                .append("appearsIn", data.getAppearsIn())
                .append("primaryFunction", data.getPrimaryFunction()), 
                new SingleResultCallback<Void>() {
                    @Override
                    public void onResult(final Void doc, final Throwable t) {
                        if(t != null)
                            LOGGER.error("Unable to insert due to an error: " + t);
                    }
                });
        } catch (MongoException me) {
            LOGGER.error("Unable to insert due to an error: " + me);
        }
    }

    public void updateHuman(String collectionName, Human data) {
        Document query = new Document().append("_id", data.getId());

        Document updates = new Document().append("_id", data.getId());
        if (data.getName() != null) 
            updates.append("name", data.getName());
        if (data.getFriends() != null) 
            updates.append("friends", data.getFriends());
        if (data.getAppearsIn() != null)
            updates.append("appearsIn", data.getAppearsIn());
        if (data.getHomePlanet() != null)
            updates.append("homePlanet", data.getHomePlanet());

        try {
            MongoCollection<Document> collection = database.getCollection(collectionName);
            collection.updateOne(query, new Document("$set", updates),
                new SingleResultCallback<UpdateResult>() {
                    @Override
                    public void onResult(final UpdateResult doc, final Throwable t) {
                        if(t != null)
                            LOGGER.error("Unable to update due to an error: " + t);
                    }
                });
        } catch (MongoException me) {
            LOGGER.error("Unable to update due to an error: " + me);
        }
    }

    public void updateDroid(String collectionName, Droid data) {
        Document query = new Document().append("_id", data.getId());
        
        Document updates = new Document().append("_id", data.getId());
        if (data.getName() != null) 
            updates.append("name", data.getName());
        if (data.getFriends() != null) 
            updates.append("friends", data.getFriends());
        if (data.getAppearsIn() != null)
            updates.append("appearsIn", data.getAppearsIn());
        if (data.getPrimaryFunction() != null)
            updates.append("primaryFunction", data.getPrimaryFunction());
        
        try {
            MongoCollection<Document> collection = database.getCollection(collectionName);
            collection.updateOne(query, new Document("$set", updates),
                new SingleResultCallback<UpdateResult>() {
                    @Override
                    public void onResult(final UpdateResult doc, final Throwable t) {
                        if(t != null)
                            LOGGER.error("Unable to update due to an error: " + t);
                    }
                });
        } catch (MongoException me) {
            LOGGER.error("Unable to update due to an error: " + me);
        }
    }

}

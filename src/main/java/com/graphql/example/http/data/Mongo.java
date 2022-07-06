package com.graphql.example.http.data;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.event.ConnectionAddedEvent;
import com.mongodb.event.ConnectionCheckedInEvent;
import com.mongodb.event.ConnectionCheckedOutEvent;
import com.mongodb.event.ConnectionPoolClosedEvent;
import com.mongodb.event.ConnectionPoolListener;

import com.mongodb.event.ConnectionPoolOpenedEvent;
import com.mongodb.event.ConnectionPoolWaitQueueEnteredEvent;
import com.mongodb.event.ConnectionPoolWaitQueueExitedEvent;
import com.mongodb.event.ConnectionRemovedEvent;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;


public class Mongo {
    private MongoDatabase database = null;
    private long instant1, instant2;

    public Mongo() {
        System.out.println("Creating Mongo client");
        // mongoClient = new MongoClient();
        // database = mongoClient.getDatabase("starwardb");
        ConnectionPoolLibrarian cpListener = new ConnectionPoolLibrarian();
        MongoClientSettings settings =
            MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString("mongodb://localhost:27017/?minPoolSize=1&maxPoolSize=100"))
                .applyToConnectionPoolSettings(builder ->
                    builder.addConnectionPoolListener(cpListener))
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        database = mongoClient.getDatabase("starwardb");

// Run a command to trigger connection pool events

    }

    public void disconnect() {
        System.out.println("Disconnecting Mongo client");
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
        Document doc = collection.find(eq("_id", id)).first();
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
        try {
            MongoCollection<Document> collection = database.getCollection(collectionName);
            collection.insertOne(new Document()
                .append("_id", data.getId())
                .append("name", data.getName())
                .append("friends", data.getFriends())
                .append("appearsIn", data.getAppearsIn())
                .append("homePlanet", data.getHomePlanet()));
        } catch (MongoException me) {
            System.err.println("Unable to insert due to an error: " + me);
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
                .append("primaryFunction", data.getPrimaryFunction()));
        } catch (MongoException me) {
            System.err.println("Unable to insert due to an error: " + me);
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

        try {
            MongoCollection<Document> collection = database.getCollection(collectionName);
            collection.updateOne(query, updates);
        } catch (MongoException me) {
            System.err.println("Unable to update due to an error: " + me);
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
        System.out.println(updates);
        try {
            MongoCollection<Document> collection = database.getCollection(collectionName);
            collection.updateOne(query, updates);
        } catch (MongoException me) {
            System.err.println("Unable to update due to an error: " + me);
        }
    }

}


class ConnectionPoolLibrarian implements ConnectionPoolListener {
    @Override
    public void connectionPoolOpened(ConnectionPoolOpenedEvent event) {
        System.out.println("Connection pool opened:" + event.toString());
    }

    @Override
    public void connectionPoolClosed(ConnectionPoolClosedEvent event) {
        System.out.println("Connection pool closed:" + event.toString());

    }

    @Override
    public void connectionCheckedOut(final ConnectionCheckedOutEvent event) {
        System.out.println(String.format("Let me get you the connection with id %s...",
            event.getConnectionId().getLocalValue()));
    }

    @Override
    public void connectionCheckedIn(ConnectionCheckedInEvent event) {
        System.out.println(String.format("Connection with id %s checked in",
            event.getConnectionId().getLocalValue()));

    }

    @Override
    public void waitQueueEntered(ConnectionPoolWaitQueueEnteredEvent event) {

    }

    @Override
    public void waitQueueExited(ConnectionPoolWaitQueueExitedEvent event) {

    }

    @Override
    public void connectionAdded(ConnectionAddedEvent event) {


    }

    @Override
    public void connectionRemoved(ConnectionRemovedEvent event) {


    }

}

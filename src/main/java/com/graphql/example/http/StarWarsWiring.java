package com.graphql.example.http;

import com.codahale.metrics.Timer;
import com.graphql.example.http.data.FilmCharacter;
import com.graphql.example.http.data.Human;
import com.graphql.example.http.data.StarWarsData;
import com.graphql.example.http.utill.Utility;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLObjectType;
import graphql.schema.TypeResolver;

import org.apache.logging.log4j.ThreadContext;
import org.apache.log4j.Logger;
import org.dataloader.BatchLoader;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * This is our wiring used to put behaviour to a graphql type.
 */
public class StarWarsWiring {

    private static Logger swwLogger = Logger.getLogger(StarWarsWiring.class);
    private static Timer timer = new Timer();

    /**
     * The context object is passed to each level of a graphql query and in this
     * case it contains
     * the data loader registry. This allows us to keep our data loaders per request
     * since
     * they cache data and cross request caches are often not what you want.
     */

    public static class Context {

        final DataLoaderRegistry dataLoaderRegistry;
        final String uid;

        public Context(String uid) {
            this.uid = uid;
            this.dataLoaderRegistry = new DataLoaderRegistry();
            dataLoaderRegistry.register("characters", newCharacterDataLoader());
        }

        public DataLoaderRegistry getDataLoaderRegistry() {
            return dataLoaderRegistry;
        }

        public String getUid() {
            return uid;
        }

        public DataLoader<String, Object> getCharacterDataLoader() {
            return dataLoaderRegistry.getDataLoader("characters");
        }
    }

    private static List<Object> getCharacterDataViaBatchHTTPApi(List<String> keys) {
        try {
            ThreadContext.push("uid: " + getUid(keys));
        } catch (Exception e) {
            // this need not stop the function from executing
            swwLogger.error(e.getStackTrace());
        }

        //
        // the function StarWarsData.getCharacterData() majorly contains
        // all the MongoDB processing
        //
        // the time for the same is calculated here because calculating it
        // either in Mongo.java or StarWarsData.java will lead to "race conditions"
        final List<Object> result = keys.stream().map((key) -> {
            final Timer.Context timerContext = timer.time();
            final Object singleResult = StarWarsData.getCharacterData(getKey(key));
            swwLogger.info("(I/O) MongoDB processing: " + Utility.formatTime(timerContext.stop()));
            return singleResult;
        }).collect(Collectors.toList());

        if (ThreadContext.pop().equals("")) {
            swwLogger.error("NDC stack empty");
        }

        return result;
    }

    // a batch loader function that will be called with N or more keys for batch
    // loading
    private static BatchLoader<String, Object> characterBatchLoader = (keys) -> {

        //
        // we are using multi threading here. Imagine if getCharacterDataViaBatchHTTPApi
        // was
        // actually a HTTP call - its not here - but it could be done asynchronously as
        // a batch API call say
        //
        //
        // direct return of values
        // CompletableFuture.completedFuture(getCharacterDataViaBatchHTTPApi(keys))
        //
        // or
        //
        // async supply of values
        return CompletableFuture.supplyAsync(() -> getCharacterDataViaBatchHTTPApi(keys));
    };

    // a data loader for characters that points to the character batch loader
    private static DataLoader<String, Object> newCharacterDataLoader() {
        return new DataLoader<>(characterBatchLoader);
    }

    // we define the normal StarWars data fetchers so we can point them at our data
    // loader
    static DataFetcher humanDataFetcher = environment -> {
        Context ctx = environment.getContext();
        String id = environment.getArgument("id");
        return ctx.getCharacterDataLoader().load(getKeyId(id, ctx.getUid()));
    };

    static DataFetcher droidDataFetcher = environment -> {
        String id = environment.getArgument("id");
        Context ctx = environment.getContext();
        return ctx.getCharacterDataLoader().load(getKeyId(id, ctx.getUid()));
    };

    static DataFetcher heroDataFetcher = environment -> {
        Context ctx = environment.getContext();
        return ctx.getCharacterDataLoader().load("2001 "); // R2D2
    };

    static DataFetcher friendsDataFetcher = environment -> {
        FilmCharacter character = environment.getSource();
        List<String> friendIds = character.getFriends();
        Context ctx = environment.getContext();
        return ctx.getCharacterDataLoader().loadMany(getKeyId(friendIds, ctx.getUid()));
    };

    static String getKeyId(String key, String id) {
        return key.concat(" ".concat(id));
    }

    @SuppressWarnings(value = "unchecked")
    static List<String> getKeyId(List<String> keys, String id) {
        return (List<String>) keys.stream().map((key) -> key.concat(" ".concat(id)));
    }

    static String getKey(String keyId) {
        String[] split = keyId.split("\\s+");
        return split[0];
    }

    static String getUid(String keyId) {
        String[] split = keyId.split("\\s+");
        return split[1];
    }

    static String getUid(List<String> keyIds) throws Exception {
        if (keyIds.size() == 0) {
            throw new Exception("Empty keys array");
        }
        String[] split = keyIds.get(0).split("\\s+");
        return split[1];
    }

    /**
     * Character in the graphql type system is an Interface and something needs
     * to decide that concrete graphql object type to return
     */
    static TypeResolver characterTypeResolver = environment -> {
        FilmCharacter character = (FilmCharacter) environment.getObject();
        if (character instanceof Human) {
            return (GraphQLObjectType) environment.getSchema().getType("Human");
        } else {
            return (GraphQLObjectType) environment.getSchema().getType("Droid");
        }
    };

}

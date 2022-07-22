package com.graphql.example.http;

import com.graphql.example.http.data.FilmCharacter;
import com.graphql.example.http.data.Human;
import com.graphql.example.http.data.Mongo;
import com.graphql.example.http.data.Cache;
import com.graphql.example.http.data.Droid;
import com.graphql.example.http.data.StarWarsData;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLObjectType;
import graphql.schema.TypeResolver;
import javafx.util.Pair;
import okhttp3.Request;

import org.dataloader.BatchLoader;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * This is our wiring used to put behaviour to a graphql type.
 */
public class StarWarsWiring {

    public static Cache cache;
    public static Mongo db;
    public static RequestHandler requestHandler;

    public static void initialize() {
        cache = new Cache();
        db = new Mongo();
        requestHandler = new RequestHandler();
    }

    /**
     * The context object is passed to each level of a graphql query and in this
     * case it contains
     * the data loader registry. This allows us to keep our data loaders per request
     * since
     * they cache data and cross request caches are often not what you want.
     */
    public static class Context {

        final DataLoaderRegistry dataLoaderRegistry;

        public Context() {
            this.dataLoaderRegistry = new DataLoaderRegistry();
            dataLoaderRegistry.register("characters", newCharacterDataLoader());
        }

        public DataLoaderRegistry getDataLoaderRegistry() {
            return dataLoaderRegistry;
        }

        public DataLoader<Pair<String, String>, Object> getCharacterDataLoader() {
            return dataLoaderRegistry.getDataLoader("characters");
        }
    }

    private static List<Object> getCharacterDataViaBatchHTTPApi(List<Pair<String, String>> keys) {
        return keys.stream().map(StarWarsData::getCharacterData).collect(Collectors.toList());
    }

    // a batch loader function that will be called with N or more keys for batch
    // loading
    private static BatchLoader<Pair<String, String>, Object> characterBatchLoader = keys -> {

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
    private static DataLoader<Pair<String, String>, Object> newCharacterDataLoader() {
        return new DataLoader<>(characterBatchLoader);
    }

    // we define the normal StarWars data fetchers so we can point them at our data
    // loader
    static DataFetcher humanDataFetcher = environment -> {
        String id = environment.getArgument("id");
        Context ctx = environment.getContext();
        return ctx.getCharacterDataLoader().load(new Pair<String, String>(id, "Human"));
    };

    static DataFetcher droidDataFetcher = environment -> {
        String id = environment.getArgument("id");
        Context ctx = environment.getContext();
        return ctx.getCharacterDataLoader().load(new Pair<String, String>(id, "Droid"));
    };

    static DataFetcher heroDataFetcher = environment -> {
        Context ctx = environment.getContext();
        return ctx.getCharacterDataLoader().load(new Pair<String, String>("2001", "Droid")); // R2D2
    };

    static DataFetcher friendsDataFetcher = environment -> {
        FilmCharacter character = environment.getSource();
        List<String> friendIds = character.getFriends();
        Context ctx = environment.getContext();
        List<Pair<String, String>> friends = friendIds.stream()
            .map(x -> new Pair<String, String>(x, ""))
            .collect(Collectors.toList());
        return ctx.getCharacterDataLoader().loadMany(friends);
    };

    static DataFetcher createHumanDataFetcher = environment -> {
        String id = environment.getArgument("id");
        String name = environment.getArgument("name");
        List<String> friends = environment.getArgument("friends");
        List<Integer> appearsIn = environment.getArgument("appearsIn");
        String homePlanet = environment.getArgument("homePlanet");

        Human data = new Human(id, name, friends, appearsIn, homePlanet, "0");
        StarWarsData.addHumanData(data);
        sendRequest();

        Context ctx = environment.getContext();
        return ctx.getCharacterDataLoader().load(new Pair<String, String>(id, "Human"));
    };

    static DataFetcher createDroidDataFetcher = environment -> {
        String id = environment.getArgument("id");
        String name = environment.getArgument("name");
        List<String> friends = environment.getArgument("friends");
        List<Integer> appearsIn = environment.getArgument("appearsIn");
        String primaryFunction = environment.getArgument("primaryFunction");

        Droid data = new Droid(id, name, friends, appearsIn, primaryFunction, "0");
        StarWarsData.addDroidData(data);
        sendRequest();

        Context ctx = environment.getContext();
        return ctx.getCharacterDataLoader().load(new Pair<String, String>(id, "Droid"));
    };

    static DataFetcher updateHumanDataFetcher = environment -> {
        String id = environment.getArgument("id");
        String name = environment.getArgument("name");
        List<String> friends = environment.getArgument("friends");
        List<Integer> appearsIn = environment.getArgument("appearsIn");
        String homePlanet = environment.getArgument("homePlanet");

        Human data = new Human(id, name, friends, appearsIn, homePlanet, "-10");
        StarWarsData.updateHumanData(data);
        sendRequest();

        Context ctx = environment.getContext();
        return ctx.getCharacterDataLoader().load(new Pair<String, String>(id, "Human"));
    };

    static DataFetcher updateDroidDataFetcher = environment -> {
        String id = environment.getArgument("id");
        String name = environment.getArgument("name");
        List<String> friends = environment.getArgument("friends");
        List<Integer> appearsIn = environment.getArgument("appearsIn");
        String primaryFunction = environment.getArgument("primaryFunction");

        Droid data = new Droid(id, name, friends, appearsIn, primaryFunction, "-10");
        StarWarsData.updateDroidData(data);
        sendRequest();

        Context ctx = environment.getContext();
        return ctx.getCharacterDataLoader().load(new Pair<String, String>(id, "Droid"));
    };

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

    private static void sendRequest() {
        try {
            URL url = new URL("http://localhost:3000/clearcache");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("accept", "text/html");
            connection.setDoOutput(true);
            connection.getInputStream();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

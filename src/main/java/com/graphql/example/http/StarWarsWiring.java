package com.graphql.example.http;

import com.graphql.example.http.data.FilmCharacter;
import com.graphql.example.http.data.Human;
import com.graphql.example.http.data.StarWarsData;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLObjectType;
import graphql.schema.TypeResolver;
import javafx.util.Pair;

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

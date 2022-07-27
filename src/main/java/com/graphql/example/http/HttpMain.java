package com.graphql.example.http;

import com.graphql.example.http.data.Cache;
import com.graphql.example.http.data.Mongo;
import com.graphql.example.http.utill.JsonKit;

import org.dataloader.DataLoaderRegistry;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import com.graphql.example.http.utill.QueryParameters;
import com.graphql.example.http.utill.RequestPoolHandler;

import graphql.ExecutionInput;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation;
import graphql.schema.GraphQLSchema;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.execution.instrumentation.tracing.TracingInstrumentation;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import static graphql.ExecutionInput.newExecutionInput;
import static graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentationOptions.newOptions;
import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;
import static java.util.Arrays.asList;

/**
 * An very simple example of serving a qraphql schema over http.
 * <p>
 * More info can be found here : http://graphql.org/learn/serving-over-http/
 */
@SuppressWarnings("unchecked")
public class HttpMain extends AbstractHandler {

    static final int PORT = 3000;
    private static GraphQLSchema starWarsSchema = null;

    public static void main(String[] args) throws Exception {
        Mongo.instantiate();
        Cache.instantiate();
        RequestPoolHandler.instantiate();
        //
        // This example uses Jetty as an embedded HTTP server
        Server server = new Server(PORT);
        //
        // In Jetty, handlers are how your get called backed on a request
        HttpMain main_handler = new HttpMain();

        // this allows us to server our index.html and GraphIQL JS code
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(false);
        resource_handler.setWelcomeFiles(new String[] { "index.html" });
        resource_handler.setResourceBase("./src/main/resources/httpmain");

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resource_handler, main_handler });
        server.setHandler(handlers);

        server.start();

        server.join();
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        if ("/graphql".equals(target)) {
            baseRequest.setHandled(true);
            RequestPoolHandler.getInstance().poolPut();
            handleStarWars(request, response);
            RequestPoolHandler.getInstance().poolPop();
        } else if ("/clearcache".equals(target)) {
            baseRequest.setHandled(true);
            handleCacheClear(request, response);
        }
    }

    private void handleCacheClear(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
        throws IOException {
        try {
            Cache.getInstance().loader.invalidateAll();
            returnAsString(httpResponse, "Success: Cache invalidated");
        } catch (Exception e) {
            e.printStackTrace();
            returnAsString(httpResponse, "Error: Cache could not be invalidated");
        }
    }

    private void returnAsString(HttpServletResponse response, String message) throws IOException {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        JsonKit.toJson(response, message);
    }

    private void handleStarWars(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
        throws IOException {
        //
        // this builds out the parameters we need like the graphql query from the http
        // request
        QueryParameters parameters = QueryParameters.from(httpRequest);
        if (parameters.getQuery() == null) {
            //
            // how to handle nonsensical requests is up to your application
            httpResponse.setStatus(400);
            return;
        }

        ExecutionInput.Builder executionInput = newExecutionInput()
            .query(parameters.getQuery())
            .operationName(parameters.getOperationName())
            .variables(parameters.getVariables());

        //
        // the context object is something that means something to down stream code. It
        // is instructions
        // from yourself to your other code such as DataFetchers. The engine passes this
        // on unchanged and
        // makes it available to inner code
        //
        // the graphql guidance says :
        //
        // - GraphQL should be placed after all authentication middleware, so that you
        // - have access to the same session and user information you would in your
        // - HTTP endpoint handlers.
        //
        StarWarsWiring.Context context = new StarWarsWiring.Context();
        executionInput.context(context);

        //
        // you need a schema in order to execute queries
        GraphQLSchema schema = buildStarWarsSchema();

        //
        // This example uses the DataLoader technique to ensure that the most efficient
        // loading of data (in this case StarWars characters) happens. We pass that to
        // data
        // fetchers via the graphql context object.
        //
        DataLoaderRegistry dataLoaderRegistry = context.getDataLoaderRegistry();

        DataLoaderDispatcherInstrumentation dlInstrumentation = new DataLoaderDispatcherInstrumentation(
            dataLoaderRegistry, newOptions().includeStatistics(true));

        Instrumentation instrumentation = new ChainedInstrumentation(
            asList(new TracingInstrumentation(), dlInstrumentation));

        // finally you build a runtime graphql object and execute the query
        GraphQL graphQL = GraphQL
            .newGraphQL(schema)
            // instrumentation is pluggable
            .instrumentation(instrumentation)
            .build();
        ExecutionResult executionResult = graphQL.execute(executionInput.build());

        returnAsJson(httpResponse, executionResult);
    }

    private void returnAsJson(HttpServletResponse response, ExecutionResult executionResult) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        JsonKit.toJson(response, executionResult.toSpecification());
    }

    private GraphQLSchema buildStarWarsSchema() {
        //
        // using lazy loading here ensure we can debug the schema generation
        // and potentially get "wired" components that cant be accessed
        // statically.
        //
        // A full application would use a dependency injection framework (like Spring)
        // to manage that lifecycle.
        //
        if (starWarsSchema == null) {

            //
            // reads a file that provides the schema types
            //
            Reader streamReader = loadSchemaFile("starWarsSchemaAnnotated.graphqls");
            TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(streamReader);

            RuntimeWiring wiring = RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("Query")
                    .dataFetcher("hero", StarWarsWiring.heroDataFetcher)
                    .dataFetcher("human", StarWarsWiring.humanDataFetcher)
                    .dataFetcher("droid", StarWarsWiring.droidDataFetcher))
                .type(newTypeWiring("Human")
                    .dataFetcher("friends", StarWarsWiring.friendsDataFetcher))
                .type(newTypeWiring("Droid")
                    .dataFetcher("friends", StarWarsWiring.friendsDataFetcher))
                .type(newTypeWiring("Character")
                    .typeResolver(StarWarsWiring.characterTypeResolver))
                .type(newTypeWiring("Mutation")
                    .dataFetcher("createHuman", StarWarsWiring.createHumanDataFetcher)
                    .dataFetcher("createDroid", StarWarsWiring.createDroidDataFetcher)
                    .dataFetcher("updateHuman", StarWarsWiring.updateHumanDataFetcher)
                    .dataFetcher("updateDroid", StarWarsWiring.updateDroidDataFetcher))
                .build();

            // finally combine the logical schema with the physical runtime
            starWarsSchema = new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring);
        }
        return starWarsSchema;
    }

    @SuppressWarnings("SameParameterValue")
    private Reader loadSchemaFile(String name) {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(name);
        return new InputStreamReader(stream);
    }

}

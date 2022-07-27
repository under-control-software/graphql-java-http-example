package com.graphql.example.http;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dataloader.DataLoaderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.graphql.example.http.utill.QueryParameters;

import graphql.ExecutionInput;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation;
import graphql.schema.GraphQLSchema;

import com.graphql.example.http.utill.JsonKit;
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

public class RequestHandler {

    private static RequestHandler requestHandler = null;

    private static GraphQLSchema starWarsSchema = null;
    private ConcurrentLinkedQueue<AsyncContext> clq;
    private ArrayBlockingQueue<Integer> pool;
    private final int MAX_POOL_SIZE = 6;
    private static Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);

    private RequestHandler() {
        LOGGER.info("Request Handler Object Created");
        clq = new ConcurrentLinkedQueue<AsyncContext>();
        pool = new ArrayBlockingQueue<Integer>(MAX_POOL_SIZE);

        new Thread() {
            @Override
            public void run() {
                processQueue();
            }
        }.start();
    }

    public static RequestHandler getInstance() {
        if(requestHandler == null) {
            synchronized(RequestHandler.class) {
                if(requestHandler == null) {
                    requestHandler = new RequestHandler();
                }
            }
        }
        return requestHandler;
    }

    public static void instantiate() {
        getInstance();
    }

    private void processQueue() {
        while(true) {
            AsyncContext asyncContext;
            synchronized(clq) {
                while(clq.isEmpty()) {
                    try {
                        clq.wait();
                    } catch (InterruptedException e) {
                        LOGGER.error("Error in wait on concurrent linked queue", e);
                    }
                }
                asyncContext = clq.poll();
            }
            poolPut(1);
            new Thread() {
                @Override
                public void run() {
                    try {
                        HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
                        HttpServletRequest request = (HttpServletRequest) asyncContext.getRequest();
                        handleStarWars(request, response);
                        asyncContext.complete();
                        poolPop();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }.start();
        }
    }

    public void add(AsyncContext asyncContext) {
        synchronized(clq) {
            clq.add(asyncContext);
            clq.notify();
        }
    }

    public void poolPop() {
        pool.poll();
    }

    public void poolPut(int x) {
        try {
            pool.put(x);
        } catch (InterruptedException e) {
            LOGGER.error("Error in pool put", e);
        }
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

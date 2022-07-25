package com.graphql.example.http;

import com.codahale.metrics.Timer;
import com.codahale.metrics.servlets.MetricsServlet;
import com.graphql.example.http.data.Mongo;
import com.graphql.example.http.utill.JsonKit;
import com.graphql.example.http.utill.MetricsServletContextListener;
import com.graphql.example.http.utill.QueryParameters;
import com.graphql.example.http.utill.Utility;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation;
import graphql.execution.instrumentation.tracing.TracingInstrumentation;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import org.apache.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.dataloader.DataLoaderRegistry;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ProcessBuilder.Redirect;

import static graphql.ExecutionInput.newExecutionInput;
import static graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentationOptions.newOptions;
import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;
import static java.util.Arrays.asList;

/**
 * An very simple example of serving a qraphql schema over http.
 * <p>
 * More info can be found here : http://graphql.org/learn/serving-over-http/
 */
public class HttpMain extends AbstractHandler {

    static final int PORT = 3000;
    static GraphQLSchema starWarsSchema = null;
    static Logger httpLogger = Logger.getLogger(HttpMain.class);
    static MetricsServletContextListener servletContextListener;
    final static Timer timer = new Timer();

    public static void main(String[] args) throws Exception {
        //
        // This example uses Jetty as an embedded HTTP server
        Server server = new Server(PORT);
        //
        // In Jetty, handlers are how your get called backed on a request
        HttpMain main_handler = new HttpMain();

        Timer.Context t = timer.time();
        Mongo.db = new Mongo();
        // Mongo.db.connectToCollection("humans");
        httpLogger.info(
                "Connect to collection: " +
                        Utility.formatTime(t.stop()) + "ms");

        // this allows us to server our index.html and GraphIQL JS code
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(false);
        resource_handler.setWelcomeFiles(new String[] { "index.html" });
        resource_handler.setResourceBase("./src/main/resources/httpmain");

        ServletContextHandler context_handler = new ServletContextHandler(server, "/");
        servletContextListener = new MetricsServletContextListener();
        context_handler.addServlet(
                new ServletHolder(new MetricsServlet(servletContextListener.getMetricRegistry())),
                "/metrics");

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resource_handler, main_handler, context_handler });
        server.setHandler(handlers);

        httpLogger.info(
                "(Compute) query params time: " +
                        "ms");

        server.start();

        server.join();
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        if ("/graphql".equals(target)) {
            Timer.Context totalTimerContext = timer.time();
            baseRequest.setHandled(true);
            handleStarWars(request, response);
            servletContextListener.update(totalTimerContext.stop());

            // Timer.Context timerContext = timer.time();

            // Reader streamReader = loadSchemaFile("starWarsSchemaAnnotated.graphqls");

            // //
            // // reads a file that provides the schema types
            // //
            // TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(streamReader);

            // httpLogger.info(
            // "(I/O) reading schema file: " +
            // Utility.formatTime(timerContext.stop()) + "ms");

            // System.out.println(typeRegistry);

            // System.exit(0);
        }
    }

    private void handleStarWars(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        Timer.Context timerContext = timer.time();
        //
        // this builds out the parameters we need like the graphql query from the http
        // request

        // Runtime rt = Runtime.getRuntime();
        // Process p = rt.exec("/usr/bin/time -lp 1");

        // ProcessBuilder pb = new ProcessBuilder("time", "1");
        // pb.redirectOutput(Redirect.INHERIT);
        // pb.redirectError(Redirect.INHERIT);
        // Process p = pb.start();

        QueryParameters parameters = QueryParameters.from(httpRequest);
        if (parameters.getQuery() == null) {
            //
            // how to handle nonsensical requests is up to your application
            httpResponse.setStatus(400);
            // timerContext.stop();
            return;
        }

        // Timer.Context timerContext = timer.time();

        // int x = QueryParameters.test();

        // String line;
        // BufferedReader input = new BufferedReader(new
        // InputStreamReader(p.getInputStream()));
        // while ((line = input.readLine()) != null) {
        // System.out.println(line);
        // }
        // input.close();
        // p.destroy();

        // httpLogger.info(
        // "(Compute) query params time: " +
        // Utility.formatTime(timerContext.stop()) + " " + "ms");
        // System.out.println(x);
        // QueryParameters parameters = QueryParameters.from(httpRequest);
        // if (parameters.getQuery() == null) {
        // //
        // // how to handle nonsensical requests is up to your application
        // httpResponse.setStatus(400);
        // timerContext.stop();
        // return;
        // }

        try {
            ThreadContext.push("uid: " + parameters.getUid());
        } catch (Exception e) {
            httpLogger.error(e.getStackTrace());
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
        StarWarsWiring.Context context = new StarWarsWiring.Context(parameters.getUid());
        executionInput.context(context);

        long elapsed = timerContext.stop();
        //
        // you need a schema in order to execute queries
        GraphQLSchema schema = buildStarWarsSchema();
        timerContext = timer.time();

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

        // httpLogger.info(
        // "(Compute) graphql build and execution: " +
        // Utility.formatTime(timerContext.stop()) + "ms");

        // finally you build a runtime graphql object and execute the query
        GraphQL graphQL = GraphQL
                .newGraphQL(schema)
                // instrumentation is pluggable
                .instrumentation(instrumentation)
                .build();

        // httpLogger.info(
        // "(Compute) graphql build and execution: " +
        // Utility.formatTime(elapsed) + " " + Utility.formatTime(timerContext.stop()) +
        // "ms");

        ExecutionResult executionResult = graphQL.execute(executionInput.build());

        returnAsJson(httpResponse, executionResult);

        // p.destroy();

        httpLogger.info(
                "(Compute) graphql build and execution (includes MongoDB I/O): " +
                        Utility.formatTime(elapsed) + " " + Utility.formatTime(timerContext.stop()) +
                        "ms");

        if (ThreadContext.pop().equals("")) {
            httpLogger.error("NDC stack empty");
        }

    }

    private void returnAsJson(HttpServletResponse response, ExecutionResult executionResult)
            throws IOException {
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

            Timer.Context timerContext = timer.time();
            //
            // reads a file that provides the schema types
            //
            Reader streamReader = loadSchemaFile("starWarsSchemaAnnotated.graphqls");
            TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(streamReader);

            // httpLogger.info(
            // "(I/O) reading schema file: " +
            // Utility.formatTime(timerContext.stop()) + "ms");

            timerContext = timer.time();

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
                    .build();

            // finally combine the logical schema with the physical runtime
            starWarsSchema = new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring);

            // httpLogger.info(
            // "(Compute) wiring fetchers: " +
            // Utility.formatTime(timerContext.stop()) + "ms");

        }
        return starWarsSchema;
    }

    @SuppressWarnings("SameParameterValue")
    private Reader loadSchemaFile(String name) {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(name);
        return new InputStreamReader(stream);
    }
}

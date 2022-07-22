package com.graphql.example.http;

import com.graphql.example.http.utill.JsonKit;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * An very simple example of serving a qraphql schema over http.
 * <p>
 * More info can be found here : http://graphql.org/learn/serving-over-http/
 */
@SuppressWarnings("unchecked")
public class HttpMain extends AbstractHandler {

    static final int PORT = 3000;

    public static void main(String[] args) throws Exception {
        StarWarsWiring.initialize();
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
            AsyncContext asyncContext = request.startAsync(request, response);
            StarWarsWiring.requestHandler.add(asyncContext);
        } else if ("/clearcache".equals(target)) {
            baseRequest.setHandled(true);
            handleCacheClear(request, response);
        }
    }

    private void handleCacheClear(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
        throws IOException {
        try {
            StarWarsWiring.cache.cache.invalidateAll();
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

}

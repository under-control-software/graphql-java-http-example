package com.graphql.example.http.utill;

import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestPoolHandler {
    private static RequestPoolHandler requestHandler = null;

    private final int MAX_POOL_SIZE = 12;
    private Semaphore pool;
    private static Logger LOGGER = LoggerFactory.getLogger(RequestPoolHandler.class);

    private RequestPoolHandler() {
        LOGGER.info("Request Handler Object Created");
        // fairness setting enabled to avoid starvation
        pool = new Semaphore(MAX_POOL_SIZE, true);
    }

    public static RequestPoolHandler getInstance() {
        if(requestHandler == null) {
            synchronized(RequestPoolHandler.class) {
                if(requestHandler == null) {
                    requestHandler = new RequestPoolHandler();
                }
            }
        }
        return requestHandler;
    }

    public static void instantiate() {
        getInstance();
    }

    public void poolPop() {
        pool.release();
    }

    public void poolPut() {
        try {
            pool.acquire();
        } catch (InterruptedException e) {
            LOGGER.error("Error in pool put", e);
        }
    }
}

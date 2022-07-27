package com.graphql.example.http.utill;

import java.util.concurrent.ArrayBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestPoolHandler {
    private static RequestPoolHandler requestHandler = null;

    private ArrayBlockingQueue<Integer> pool;
    private final int MAX_POOL_SIZE = 12;
    private static Logger LOGGER = LoggerFactory.getLogger(RequestPoolHandler.class);

    private RequestPoolHandler() {
        LOGGER.info("Request Handler Object Created");
        pool = new ArrayBlockingQueue<Integer>(MAX_POOL_SIZE);
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
        Integer x = pool.poll();
        // LOGGER.info("*****polled value: " + x);
    }

    public void poolPut(int x) {
        // LOGGER.info("pool size" + pool.size());
        try {
            pool.put(x);
        } catch (InterruptedException e) {
            // LOGGER.error("Error in pool put", e);
        }
    }
}

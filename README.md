# graphql-java-http-example

## About
Runs the application in two different conatiners.

## Quick Start
Make sure you have [docker](https://docs.docker.com/engine/install/) installed. Run the following in the root directory:
```
$ docker-compose up
```

It will start up both the instances exposed at `port 5011` and `port 5012` respectively. Send 'read' requests to port 5011 and 'write' requests to port 5012. Each write request will invalidate cache in the application running at port 5011.

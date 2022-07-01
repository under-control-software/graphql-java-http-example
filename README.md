# graphql-java-http-example

## About
An example of using graphql-java in a HTTP application.

## Features
* It demonstrates the use of **graphql IDL** to define a schema in a textual way.
* It also shows how to use **data loader** to ensure that most efficient way to load
inside a graphql query.
* Connects and retrieves data from a local MongoDB database, with connection pooling implemented.
* Caches the results using Google Guava package, thus reducing response time.
* Supports GraphQL query and mutation types for creation and updation operations. Check out the [schema](https://github.com/under-control-software/graphql-java-http-example/blob/master/src/main/resources/starWarsSchemaAnnotated.graphqls) file.

## Quick Start

* Make sure you have [mongodb](https://www.mongodb.com/docs/manual/installation/) and [Java JDK 8](https://www.oracle.com/java/technologies/downloads/#:~:text=Java%20SE%20subscribers%20have%20more%20choices) installed.
* Start up the *mongod* service and create a database called **'starwardb'**. Insert data into collections **'humans'** and **'droids'** following the [schema](https://github.com/under-control-software/graphql-java-http-example/blob/master/src/main/resources/starWarsSchemaAnnotated.graphqls) provided.
* Run the following in the root dorectory of this project:
```
$ ./gradlew build
$ ./gradlew run
```
* Point your browser at http://localhost:3000/.
    
Example query:
```
{
  hero {
    name
    friends {
      name
      friends {
        id
        name
      }
    }
  }
}
```

## Server Endpoints
1. **/graphql**: For retrieving, creating and updating data.
2. **/clearcache**: For invalidating the cache.

## Virtual Machine Setup
Head over to [docker-2](https://github.com/under-control-software/graphql-java-http-example/tree/docker-2) branch for the read-write VM setup.


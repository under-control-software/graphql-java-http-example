# graphql-java-http-example

This contains the VM used for performing load tests as an effort to reduce the number of Full GC events.

## Quick Start
Run the following in the root directory:
```
$ docker build -t graphql-http .
$ docker run -it --memory="4g" --cpus="1.0" --rm -i -t -p 3000:3000 http-graphql
```

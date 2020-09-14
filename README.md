# graphqlcrud-java

This is framework built in Java to build GraphQLCRUD based API to a provided database. You simply bring existing your database and its data, and use the framework to build simple application using the given template or embed into your Java application to expose the GraphQL API over your relational database.

The GraphQLCrud specification is defined at https://graphqlcrud.org specification. 

## Build the Application
To build the application using maven run

```
mvn clean package
```

## Running the application 

The default application requires a Postgresql database. If you do not have one, you start a docker container for same by executing 

```
docker run -p 5432:5432 --name sampledb -e POSTGRES_DB=sampledb \
  -e POSTGRES_USER=user -e POSTGRES_PASSWORD=password \
  -d postgres -c log_statement=all
```

You can run your application in dev mode that enables live coding, switch into the `app` directory and run

```
../mvnw quarkus:dev
```

> ** NOTE: **
The sample application is designed to load a sample schema to work with, if you bring your own database make sure to disable update of the schema

once the application starts, using the browser goto URL [http://localhost:8080](http://localhost:8080)


to run in non development mode:

The application is now runnable using `java -jar app/target/app-1.0.0-SNAPSHOT-runner.jar`.



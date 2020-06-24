# graphqlcrud-java

GraphQLCRUD Java automates schema generation and provides CRUD capabilities. 

## Running the application 

You can run your application in dev mode that enables live coding using:
```
./mvnw quarkus:dev
```

## Packaging and running the application

The application can be packaged using `./mvnw package`.

It produces the `graphqlcrud-java-1.0.0-SNAPSHOT-runner.jar` file in the `/target` directory.

Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/graphqlcrud-java-1.0.0-SNAPSHOT-runner.jar`.


package io.graphqlcrud;

import java.io.StringReader;

import org.junit.jupiter.api.Test;

import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;

@QuarkusTestResource(H2DatabaseTestResource.class)
public class DatabaseSchemaTest {

    @Test
    void test() {
        String schema = "    schema {\n" + 
                "        query: QueryType\n" + 
                "    }\n" + 
                "\n" + 
                "    type QueryType {\n" + 
                "        hero(episode: Episode): Character\n" + 
                "        human(id : String) : Human\n" + 
                "        droid(id: ID!): Droid\n" + 
                "    }\n" + 
                "\n" + 
                "\n" + 
                "    enum Episode {\n" + 
                "        NEWHOPE\n" + 
                "        EMPIRE\n" + 
                "        JEDI\n" + 
                "    }\n" + 
                "\n" + 
                "    interface Character {\n" + 
                "        id: ID!\n" + 
                "        name: String!\n" + 
                "        friends: [Character]\n" + 
                "        appearsIn: [Episode]!\n" + 
                "    }\n" + 
                "\n" + 
                "    type Human implements Character {\n" + 
                "        id: ID!\n" + 
                "        name: String!\n" + 
                "        friends: [Character]\n" + 
                "        appearsIn: [Episode]!\n" + 
                "        homePlanet: String\n" + 
                "    }\n" + 
                "\n" + 
                "    type Droid implements Character {\n" + 
                "        id: ID!\n" + 
                "        name: String!\n" + 
                "        friends: [Character]\n" + 
                "        appearsIn: [Episode]!\n" + 
                "        primaryFunction: String\n" + 
                "    }";
        
        
        SchemaParser schemaParser = new SchemaParser();
        SchemaGenerator schemaGenerator = new SchemaGenerator();

        TypeDefinitionRegistry typeRegistry = schemaParser.parse(new StringReader(schema));
        System.out.println(typeRegistry);
    }

    @Test
    public void testSchemaPrint() {
        System.out.println("xdsfds");
    }
}

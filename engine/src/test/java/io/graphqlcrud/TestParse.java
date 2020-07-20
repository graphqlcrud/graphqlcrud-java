package io.graphqlcrud;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

class TestParse {

    @Test
    void test() {
        String sdl = " schema {query: Query}\n"
                + "type Query {\n" + 
                "  books: [Book] \n" + 
                "}\n" + 
                "\n" + 
                "type Book {\n" + 
                "  id: ID\n" + 
                "  name: String\n" + 
                "  pageCount: Int\n" + 
                "  author: Author\n" + 
                "}\n" + 
                "\n" + 
                "type Author {\n" + 
                "  id: ID\n" + 
                "  firstName: String\n" + 
                "  lastName: String\n" + 
                "}" ;
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
        
        RuntimeWiring runtimeWiring = buildWiring();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema schema = schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
        String query = "{ books {id, name} }";
        //GraphQL build = GraphQL.newGraphQL(schema).queryExecutionStrategy(new SQLExcecutionStrategy()).build();
        GraphQL build = GraphQL.newGraphQL(schema).build();
        ExecutionResult executionResult = build.execute(query);

        System.out.println(executionResult.getData().toString());        

    }

    static class MyFetcher implements DataFetcher<List<Map<String, Object>>> {
        @Override
        public List<Map<String, Object>> get(DataFetchingEnvironment environment) throws Exception {
            List<Map<String, Object>> list = new ArrayList<Map<String,Object>>(); 
            
            HashMap<String, Object> results = new HashMap<String, Object>();
            results.put("id", "1");
            results.put("name", "foo");
            results.put("pageCount", "200");
            list.add(results);
            
            results = new HashMap<String, Object>();
            results.put("id", "2");
            results.put("name", "bar");
            results.put("pageCount", "100");            
            list.add(results);
            
            return list;
        }
    };
    
    private RuntimeWiring buildWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("Query").dataFetcher("books", new MyFetcher()))
                .type(newTypeWiring("Book").dataFetcher("author", new MyFetcher()))
                .build();
    }
}

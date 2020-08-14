package io.graphqlcrud;

import graphql.schema.GraphQLSchema;
import io.agroal.api.AgroalDataSource;
import io.graphqlcrud.model.Schema;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.sql.Connection;

@QuarkusTestResource(H2DatabaseTestResource.class)
@QuarkusTest
public class FilterInputTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(FilterInputTest.class);

    @Inject
    private AgroalDataSource datasource;
    private GraphQLSchema graphQLSchema;

    @BeforeEach
    public void setup() throws Exception {
        try (Connection connection = datasource.getConnection()) {
            Assertions.assertNotNull(connection);
            Schema schema = DatabaseSchemaBuilder.getSchema(connection, "PUBLIC");
            Assertions.assertNotNull(schema);
            this.graphQLSchema = GraphQLSchemaBuilder.getSchema(schema);
            Assertions.assertNotNull(this.graphQLSchema);
        }
    }

    @Test
    public void testFloatInput() {
        graphQLSchema.getTypeMap().entrySet().forEach( parent -> {
            if(parent.getKey().equals("FloatInput")) {
                Assertions.assertEquals("FloatInput", parent.getValue().getName());
                parent.getValue().getChildren().forEach(child -> {
                    if(child.toString().contains("name=ne"))
                        Assertions.assertTrue(child.toString().contains("name=Float"));
                    else if(child.toString().contains("name=eq"))
                        Assertions.assertTrue(child.toString().contains("name=Float"));
                    else if(child.toString().contains("name=le"))
                        Assertions.assertTrue(child.toString().contains("name=Float"));
                    else if(child.toString().contains("name=lt"))
                        Assertions.assertTrue(child.toString().contains("name=Float"));
                    else if(child.toString().contains("name=ge"))
                        Assertions.assertTrue(child.toString().contains("name=Float"));
                    else if(child.toString().contains("name=gt"))
                        Assertions.assertTrue(child.toString().contains("name=Float"));
                    else if(child.toString().contains("name=in"))
                        Assertions.assertTrue(child.toString().contains("originalType=[Float!]"));
                });
            }
        });
    }

    @Test
    public void testBooleanInput() {
        graphQLSchema.getTypeMap().entrySet().forEach( parent -> {
            if(parent.getKey().equals("BooleanInput")) {
                Assertions.assertEquals("BooleanInput", parent.getValue().getName());
                parent.getValue().getChildren().forEach(child -> {
                    if (child.toString().contains("name=eq"))
                        Assertions.assertTrue(child.toString().contains("name=Boolean"));
                    else if (child.toString().contains("name=ne"))
                        Assertions.assertTrue(child.toString().contains("name=Boolean"));
                });
            }
        });
    }

    @Test
    public void testIDInput() {
        graphQLSchema.getTypeMap().entrySet().forEach( parent -> {
            if(parent.getKey().equals("IDInput")) {
                Assertions.assertEquals("IDInput", parent.getValue().getName());
                parent.getValue().getChildren().forEach(child -> {
                    if (child.toString().contains("name=eq"))
                        Assertions.assertTrue(child.toString().contains("name=ID"));
                    else if (child.toString().contains("name=ne"))
                        Assertions.assertTrue(child.toString().contains("name=ID"));
                    else if (child.toString().contains("name=in"))
                        Assertions.assertTrue(child.toString().contains("name=originalType=[ID!]"));
                });
            }
        });
    }

    @Test
    public void testOrderByInput() {
        graphQLSchema.getTypeMap().entrySet().forEach( parent -> {
            if(parent.getKey().equals("OrderByInput")) {
                    Assertions.assertEquals("OrderByInput",parent.getValue().getName());
                    parent.getValue().getChildren().forEach(child -> {
                        if(child.toString().contains("name=field"))
                            Assertions.assertTrue(child.toString().contains("originalType=String!"));
                        else if(child.toString().contains("name=order"))
                            Assertions.assertTrue(child.toString().contains("defaultValue=ASC"));
                    });
                }
        });
    }

    @Test
    public void testSortDirectionEnumInput() {
        graphQLSchema.getTypeMap().entrySet().forEach( parent -> {
            if(parent.getKey().equals("SortDirectionEnum")) {
                    Assertions.assertEquals("SortDirectionEnum",parent.getValue().getName());
                }
        });
    }

    @Test
    public void testIntInput() {
        graphQLSchema.getTypeMap().entrySet().forEach( parent -> {
            if(parent.getKey().equals("IntInput")) {
                Assertions.assertEquals("IntInput", parent.getValue().getName());
                parent.getValue().getChildren().forEach(child -> {
                    if(child.toString().contains("name=ne"))
                        Assertions.assertTrue(child.toString().contains("name=Int"));
                    else if(child.toString().contains("name=eq"))
                        Assertions.assertTrue(child.toString().contains("name=Int"));
                    else if(child.toString().contains("name=le"))
                        Assertions.assertTrue(child.toString().contains("name=Int"));
                    else if(child.toString().contains("name=lt"))
                        Assertions.assertTrue(child.toString().contains("name=Int"));
                    else if(child.toString().contains("name=ge"))
                        Assertions.assertTrue(child.toString().contains("name=Int"));
                    else if(child.toString().contains("name=gt"))
                        Assertions.assertTrue(child.toString().contains("name=Int"));
                    else if(child.toString().contains("name=in"))
                        Assertions.assertTrue(child.toString().contains("originalType=[Int!]"));
                });
            }
        });
    }

    @Test
    public void testStringInput() {
        graphQLSchema.getTypeMap().entrySet().forEach( parent -> {
            if(parent.getKey().equals("StringInput")) {
                Assertions.assertEquals("StringInput", parent.getValue().getName());
                parent.getValue().getChildren().forEach(child -> {
                    if(child.toString().contains("name=ne"))
                        Assertions.assertTrue(child.toString().contains("name=String"));
                    else if(child.toString().contains("name=eq"))
                        Assertions.assertTrue(child.toString().contains("name=String"));
                    else if(child.toString().contains("name=le"))
                        Assertions.assertTrue(child.toString().contains("name=String"));
                    else if(child.toString().contains("name=lt"))
                        Assertions.assertTrue(child.toString().contains("name=String"));
                    else if(child.toString().contains("name=ge"))
                        Assertions.assertTrue(child.toString().contains("name=String"));
                    else if(child.toString().contains("name=gt"))
                        Assertions.assertTrue(child.toString().contains("name=String"));
                    else if(child.toString().contains("name=in"))
                        Assertions.assertTrue(child.toString().contains("originalType=[String!]"));
                    else if(child.toString().contains("name=contains"))
                        Assertions.assertTrue(child.toString().contains("name=String"));
                    else if(child.toString().contains("name=endsWith"))
                        Assertions.assertTrue(child.toString().contains("name=String"));
                    else if(child.toString().contains("name=startsWith"))
                        Assertions.assertTrue(child.toString().contains("name=String"));
                });
            }
        });
    }

    @Test
    public void testPageRequestInput() {
        graphQLSchema.getTypeMap().entrySet().forEach( parent -> {
            if(parent.getKey().equals("PageRequest")) {
                    parent.getValue().getChildren().forEach(child -> {
                        if(child.toString().contains("name=limit"))
                            Assertions.assertTrue(child.toString().contains("name=Int"));
                        else if(child.toString().contains("name=offset"))
                            Assertions.assertTrue(child.toString().contains("name=Int"));
                    });
                }
        });
    }

    @Test
    public void testQueryFilterInput() {
        graphQLSchema.getTypeMap().entrySet().forEach( parent -> {
            if(parent.getKey().equals("QueryFilter")) {
                    Assertions.assertEquals("QueryFilter",parent.getValue().getName());
                    parent.getValue().getChildren().forEach(child -> {
                        if(child.toString().contains("name=and"))
                            Assertions.assertTrue(child.toString().contains("originalType=[QueryFilter]"));
                        else if(child.toString().contains("name=or"))
                            Assertions.assertTrue(child.toString().contains("originalType=[QueryFilter]"));
                    });
                }
        });
    }
}

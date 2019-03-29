package de.samply.neo4jloader.manual;

import de.samply.neo4jloader.statement.Neo4jStatementExecutor;
import de.samply.neo4jloader.statement.AbstractCreateStatement;

public class ExampleFhirResourceDeleter {

    public static void main(String[] args) {
        executeDeleteStatement();
    }

    private static void executeDeleteStatement() {
        try (Neo4jStatementExecutor executor = new Neo4jStatementExecutor()) {
            AbstractCreateStatement deleteStatement = new AbstractCreateStatement() {
                @Override
                public String getCreateStatementTemplate() {
                    return "match (s), (m)-[r]-(n) delete s, m, n, r";
                }
            };

            executor.execute(deleteStatement);
        }
    }
}

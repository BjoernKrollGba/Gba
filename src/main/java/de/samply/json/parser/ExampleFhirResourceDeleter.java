package de.samply.json.parser;

public class ExampleFhirResourceDeleter {

    public static void main(String[] args) {
        executeDeleteStatement();
    }

    private static void executeDeleteStatement() {
        try (Neo4jStatementExecutor executor = new Neo4jStatementExecutor()) {
            executor.execute("match (s), (m)-[r]-(n) delete s, m, n, r");
        }
    }
}

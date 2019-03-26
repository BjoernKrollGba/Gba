package de.samply.json.parser;

import org.neo4j.driver.v1.*;

public class Neo4jStatementExecutor implements AutoCloseable {

    private final Driver driver;

    Neo4jStatementExecutor() {
        this("bolt://localhost:7687", "neo4j", "neo4jneo4j");
    }

    private Neo4jStatementExecutor(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    @Override
    public void close() {
        driver.close();
    }

    void execute(final String statement) {
        try (Session session = driver.session()) {
            String observation = session.writeTransaction(tx -> {
                StatementResult result = tx.run(statement);
                return "DONE";
            });
        }
    }

}

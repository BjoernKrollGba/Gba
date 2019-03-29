package de.samply.json.parser;

import de.samply.json.parser.model.AbstractCreateStatement;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;

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

    void execute(final AbstractCreateStatement... statements) {
        try (Session session = driver.session()) {
            session.writeTransaction(transaction -> {
                for (AbstractCreateStatement statement : statements) {
                    statement.executeStatementInTransactionFunction().apply(transaction);
                }
                return "DONE";
            });
        }
    }
}

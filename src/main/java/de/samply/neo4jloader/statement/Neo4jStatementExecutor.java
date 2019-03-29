package de.samply.neo4jloader.statement;

import de.samply.neo4jloader.statement.AbstractCreateStatement;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;

public class Neo4jStatementExecutor implements AutoCloseable {

    private final Driver driver;

    public Neo4jStatementExecutor() {
        this("bolt://localhost:7687", "neo4j", "neo4jneo4j");
    }

    private Neo4jStatementExecutor(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    @Override
    public void close() {
        driver.close();
    }

    public void execute(final AbstractCreateStatement... statements) {
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

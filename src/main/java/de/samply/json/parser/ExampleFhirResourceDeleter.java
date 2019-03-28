package de.samply.json.parser;

import de.samply.json.parser.model.MergeStatementProvidable;
import org.neo4j.driver.v1.Transaction;

import java.util.function.Function;

public class ExampleFhirResourceDeleter {

    public static void main(String[] args) {
        executeDeleteStatement();
    }

    private static void executeDeleteStatement() {
        try (Neo4jStatementExecutor executor = new Neo4jStatementExecutor()) {
            MergeStatementProvidable mergeStatementProvidable = new MergeStatementProvidable() {
                @Override
                public Function<Transaction, String> getCallback() {
                    return tx -> {
                        tx.run("match (s), (m)-[r]-(n) delete s, m, n, r");
                        return "DONE";
                    };
                }
            };

            executor.execute(mergeStatementProvidable);
        }
    }
}

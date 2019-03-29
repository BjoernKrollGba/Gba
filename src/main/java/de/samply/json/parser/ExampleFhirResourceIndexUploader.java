package de.samply.json.parser;

import de.samply.json.parser.model.MergeStatementProvidable;
import org.neo4j.driver.v1.Transaction;

import java.util.function.Function;

public class ExampleFhirResourceIndexUploader {

    public static void main(String[] args) {
        executeIndexStatement();
    }

    private static final String[] NEO4J_LABELS = new String[]{
            "Address",
            "Category",
            "Class",
            "Code",
            "Coding",
            "Communication",
            "Encounter",
            "Extension",
            "Identifier",
            "Language",
            "MartialStatus",
            "Meta",
            "Name",
            "Observation",
            "Organization",
            "Participant",
            "Patient",
            "Period",
            "Practioner",
            "Telecom",
            "Text",
            "Type",
            "ValueAddress",
            "ValueCoding",
            "ValueQuantity"
    };

    private static void executeIndexStatement() {
        try (Neo4jStatementExecutor executor = new Neo4jStatementExecutor()) {
            MergeStatementProvidable mergeStatementProvidable = new MergeStatementProvidable() {
                @Override
                public Function<Transaction, String> getCallback() {
                    return tx -> {
                        for (String label : NEO4J_LABELS) {
                            tx.run("CREATE CONSTRAINT ON (node:" + label + ") ASSERT node.neo4jId IS UNIQUE");
                        }
                        return "DONE";
                    };
                }
            };

            executor.execute(mergeStatementProvidable);
        }
    }
}

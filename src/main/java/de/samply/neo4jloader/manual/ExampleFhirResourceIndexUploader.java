package de.samply.neo4jloader.manual;

import de.samply.neo4jloader.statement.Neo4jStatementExecutor;
import de.samply.neo4jloader.statement.AbstractCreateStatement;

import java.util.ArrayList;
import java.util.List;

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
            List<AbstractCreateStatement> indexCreateStatements = new ArrayList<>();
            for (String label : NEO4J_LABELS) {
                indexCreateStatements.add(new AbstractCreateStatement() {

                    @Override
                    protected String getCreateStatementTemplate() {
                        return "CREATE CONSTRAINT ON (node:" + label + ") ASSERT node.neo4jId IS UNIQUE";
                    }
                });
            }
            executor.execute(indexCreateStatements.toArray(new AbstractCreateStatement[]{}));
        }
    }
}

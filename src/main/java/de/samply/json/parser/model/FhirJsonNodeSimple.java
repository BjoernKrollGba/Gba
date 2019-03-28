package de.samply.json.parser.model;

import java.util.UUID;

public class FhirJsonNodeSimple extends AbstractFhirJsonNode {

    private final String neo4jLabel;
    private final String neo4jId = "'" + UUID.randomUUID().toString() + "'";

    public FhirJsonNodeSimple(String neo4jLabel) {
        this.neo4jLabel = neo4jLabel;
    }

    @Override
    public String getNeo4jLabel() {
        return neo4jLabel;
    }

    @Override
    public String getNeo4jId() {
        return neo4jId;
    }

    @Override
    public boolean isEntityNode() {
        return false;
    }
}

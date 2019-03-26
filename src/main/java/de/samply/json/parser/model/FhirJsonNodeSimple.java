package de.samply.json.parser.model;

public class FhirJsonNodeSimple extends AbstractFhirJsonNode {
    private String neo4jLabel;

    public FhirJsonNodeSimple(String neo4jLabel) {
        this.neo4jLabel = neo4jLabel;
    }
    @Override
    public boolean isEntityNode() {
        return false;
    }

    @Override
    public String getNeo4jNodeLabel() {
        return neo4jLabel;
    }
}

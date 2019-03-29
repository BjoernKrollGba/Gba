package de.samply.neo4jloader.model;

public class FhirJsonNodeEntity extends AbstractFhirJsonNode {
    private String resurceType;
    private String id;

     public String getResurceType() {
        return resurceType;
    }

    public void setResurceType(String resurceType) {
        this.resurceType = resurceType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean isEntityNode() {
        return true;
    }

    @Override
    public String getNeo4jLabel() {
        return resurceType;
    }

    @Override
    public String getNeo4jId() {
        return resurceType + "/" + id;
    }
}

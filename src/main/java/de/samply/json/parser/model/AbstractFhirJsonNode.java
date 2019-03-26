package de.samply.json.parser.model;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractFhirJsonNode {
    private List<FhirJsonProperty> properties = new ArrayList<>();
    private List<FhirJsonRelationTo> relations = new ArrayList<>();
    private List<NamedCollection> primitiveArrays = new ArrayList<>();

    public List<FhirJsonProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<FhirJsonProperty> properties) {
        this.properties = properties;
    }

    public List<FhirJsonRelationTo> getRelations() {
        return relations;
    }

    public void setRelations(List<FhirJsonRelationTo> relations) {
        this.relations = relations;
    }

    public List<NamedCollection> getPrimitiveArrays() {
        return primitiveArrays;
    }

    public void setPrimitiveArrays(List<NamedCollection> primitiveArrays) {
        this.primitiveArrays = primitiveArrays;
    }

    public abstract boolean isEntityNode();

    public abstract String getNeo4jNodeLabel();
}

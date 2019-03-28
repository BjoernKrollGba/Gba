package de.samply.json.parser.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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

    public String getNeo4jNodeJsonKeyPattern(String prefix) {
        return "{ neo4jId: $" + prefix + "neo4jId, neo4jLabel: $" + prefix + "neo4jLabel }";
    }

    public abstract String getNeo4jLabel();

    public abstract String getNeo4jId();

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FhirJsonNodeEntity)) {
            return false;
        }

        FhirJsonNodeEntity other = (FhirJsonNodeEntity) obj;

        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.getNeo4jId(), other.getNeo4jId());
        builder.append(this.getNeo4jLabel(), other.getNeo4jLabel());

        return builder.build();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.append(getNeo4jId());
        builder.append(getNeo4jLabel());

        return builder.build();
    }
}

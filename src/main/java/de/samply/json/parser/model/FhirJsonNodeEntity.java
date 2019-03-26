package de.samply.json.parser.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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
    public String getNeo4jNodeLabel() {
        return getResurceType();
    }

    public String getNeo4jId() {
        return resurceType + "/" + id;
    }

    @Override
    public boolean equals(Object obj) {
         if (!(obj instanceof FhirJsonNodeEntity)) {
             return false;
         }

         FhirJsonNodeEntity other = (FhirJsonNodeEntity) obj;

        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.id, other.id);
        builder.append(this.resurceType, other.resurceType);

        return builder.build();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.append(id);
        builder.append(resurceType);

        return builder.build();
    }
}

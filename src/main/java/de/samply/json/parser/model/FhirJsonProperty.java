package de.samply.json.parser.model;

public class FhirJsonProperty {
    private String name;
    private Object value;
    private Neo4jType type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Neo4jType getType() {
        return type;
    }

    public void setType(Neo4jType type) {
        this.type = type;
    }
}

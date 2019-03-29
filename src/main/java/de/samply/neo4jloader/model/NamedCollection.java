package de.samply.neo4jloader.model;

import java.util.ArrayList;
import java.util.List;

public class NamedCollection {
    private String name;
    private Neo4jType type;

    private List<Object> values = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Neo4jType getType() {
        return type;
    }

    public void setType(Neo4jType type) {
        this.type = type;
    }

    public List<Object> getValues() {
        return values;
    }

    public void setValues(List<Object> values) {
        this.values = values;
    }
}

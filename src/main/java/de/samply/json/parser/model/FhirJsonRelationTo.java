package de.samply.json.parser.model;

public class FhirJsonRelationTo {
    private String name;
    private String tag;
    private AbstractFhirJsonNode target;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public AbstractFhirJsonNode getTarget() {
        return target;
    }

    public void setTarget(AbstractFhirJsonNode target) {
        this.target = target;
    }
}

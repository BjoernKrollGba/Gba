package de.samply.json.fhirentity;

public abstract class FhirResource {
    public String resourceType;
    public String id;

    protected FhirResource() {
        resourceType = getResourceType();
    }

    protected abstract String getResourceType();
}

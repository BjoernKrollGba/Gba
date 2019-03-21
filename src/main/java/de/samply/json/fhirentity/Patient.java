package de.samply.json.fhirentity;

public class Patient extends FhirResource {
    public static final String RESOURCE_TYPE = "Patient";

    @Override
    protected String getResourceType() {
        return RESOURCE_TYPE;
    }
}

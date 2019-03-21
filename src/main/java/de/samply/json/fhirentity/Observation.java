package de.samply.json.fhirentity;

import de.samply.json.nested.Code;
import de.samply.json.nested.Reference;
import de.samply.json.nested.ValueQuantity;

import java.util.ArrayList;
import java.util.List;

public class Observation extends FhirResource {
    public static final String RESOURCE_TYPE = "Observation";

    @Override
    protected String getResourceType() {
        return RESOURCE_TYPE;
    }

    public String status = "final";
    public Reference subject;

    public List<Code> code = new ArrayList<>();

    public ValueQuantity valueQuantity;
}

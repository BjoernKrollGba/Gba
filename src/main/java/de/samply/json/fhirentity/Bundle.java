package de.samply.json.fhirentity;

import de.samply.json.nested.Code;

import java.util.ArrayList;
import java.util.List;

public class Bundle {
    public List<FhirResource> items = new ArrayList<>();

    public void add(FhirResource item) {
       items.add(item);
    }

    public void addAll(List<? extends FhirResource> itemsTemp) {
        this.items.addAll(itemsTemp);
    }
}

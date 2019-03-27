package de.samply.json.parser;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class FakeArrayBundleProvider {

    private final int mainId;

    FakeArrayBundleProvider(int mainId) {
        this.mainId = mainId;
    }

    Writer createWriter() {
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile("BundleArray_template.mustache");
        return mustache.execute(new StringWriter(), this);
    }

    List<BundleItem> items() {
        return Collections.singletonList(new BundleItem(mainId));
    }

    private static class BundleItem {
        String patientId;
        String encounterId;

        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        List<ObservationItem> observations = new ArrayList<>();

        BundleItem(int mainId) {
            patientId = "P_" + mainId;
            encounterId = "E_" + mainId;

            for (int i = 1; i <= 10; i++) {
                observations.add(new ObservationItem("O_" + i));
            }

        }
    }

    private static class ObservationItem {

        String observationId;
        double value = 100 * Math.random();

        ObservationItem(String observationId) {
            this.observationId = observationId;
        }
    }
}

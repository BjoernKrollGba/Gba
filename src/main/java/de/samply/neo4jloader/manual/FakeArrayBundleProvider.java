package de.samply.neo4jloader.manual;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class FakeArrayBundleProvider {

    private final int mainId;
    private final int numberOfObservations;

    FakeArrayBundleProvider(int mainId, int numberOfObservations) {
        this.mainId = mainId;
        this.numberOfObservations = numberOfObservations;
    }

    Writer createWriter() {
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile("BundleArray_template.mustache");
        return mustache.execute(new StringWriter(), this);
    }

    List<BundleItem> items() {
        return Collections.singletonList(new BundleItem(mainId, numberOfObservations));
    }

    private static class BundleItem {
        String patientId;
        String encounterId;
        private final int numberOfObservations;

        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        List<ObservationItem> observations = new ArrayList<>();

        BundleItem(int mainId, int numberOfObservations) {
            this.numberOfObservations = numberOfObservations;

            patientId = "P_" + mainId;
            encounterId = "E_" + mainId;

            for (int i = 1; i <= this.numberOfObservations; i++) {
                observations.add(new ObservationItem("O_" + mainId + "_" + i));
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

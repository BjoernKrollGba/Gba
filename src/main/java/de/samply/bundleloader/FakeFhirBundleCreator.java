package de.samply.bundleloader;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.samply.json.fhirentity.Bundle;
import de.samply.json.fhirentity.Observation;
import de.samply.json.fhirentity.Patient;
import de.samply.json.nested.Code;
import de.samply.json.nested.Reference;
import de.samply.json.nested.ValueQuantity;
import de.samply.loader.Neo4jLoader;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FakeFhirBundleCreator {

    private final static String FILE_PRAEFIX = "Bundle_";
    private static final String SUFFIX_JSON = ".json";

    private final static int NUMBER_BUNDLES = 100000;
    private final static int NUMBER_PATIENTS_PER_BUNDLE = 1;
    private final static int NUMBER_OBSERVATIONS_PER_PATIENT = 100;

    private final static int PADDING_SIZE = Double.valueOf(Math.ceil(Math.log10(NUMBER_BUNDLES))).intValue();

    public static void main(String[] args) throws IOException {
        new FakeFhirBundleCreator().create();
    }

    public void create() throws IOException {
        for (int countBundle = 1; countBundle <= NUMBER_BUNDLES; countBundle++) {
            System.out.println(LocalDateTime.now() + "Bundle: " + countBundle);
            createBundle(countBundle);
        }
    }

    private void createBundle(int countBundle) throws IOException {
        String patientId = Integer.toString(countBundle);
        Path path = createPath(FILE_PRAEFIX, patientId);

        Bundle bundle = new Bundle();
        for (int countPatient = NUMBER_PATIENTS_PER_BUNDLE * countBundle + 1; countPatient <= NUMBER_PATIENTS_PER_BUNDLE * countBundle + NUMBER_PATIENTS_PER_BUNDLE; countPatient++) {
            System.out.println(LocalDateTime.now() + "Patient: " + countPatient);
            bundle.add(createPatient(countPatient));
            bundle.addAll(createObservations(countPatient));
        }

        writeJson(path, bundle);

    }

    private Patient createPatient(int countPatient) throws IOException {
        String patientId = Integer.toString(countPatient);

        Patient patient = new Patient();
        patient.id = patientId;

        return patient;
    }

    private List<Observation> createObservations(int countPatient) throws IOException {
        List<Observation> observationList = new ArrayList<>();
        for (int countObservation = 1; countObservation <= NUMBER_OBSERVATIONS_PER_PATIENT; countObservation++) {
            observationList.add(createObservation(countObservation, countPatient));
        }

        return observationList;
    }

    private Observation createObservation(int countObservation, int countPatient) throws IOException {
        String observationId = Integer.toString(countPatient) + "_" + Integer.toString(countObservation);

        Observation observation = new Observation();
        observation.id = observationId;

        Reference subject = new Reference();
        subject.reference = Patient.RESOURCE_TYPE + "/" + countPatient;
        observation.subject = subject;

        observation.code.add(createCode("lens", countObservation));
        if (countPatient % 10 == 0 && countObservation > NUMBER_OBSERVATIONS_PER_PATIENT - 3) {
            observation.code.add(createCode("Alternative", 8128));
        }

        ValueQuantity valueQuantity = new ValueQuantity();
        valueQuantity.value = 100 * Math.random();
        observation.valueQuantity = valueQuantity;

        return observation;
    }

    private Code createCode(String system, int countObservation) {
        Code code = new Code();
        code.system = system;
        code.code = Integer.toString(countObservation);
        return code;
    }

    private Path createPath(String praefix, String id) {
        return Paths.get(Neo4jBundleLoader.DIR_IMPORT, praefix + StringUtils.leftPad(id, PADDING_SIZE, "0") + SUFFIX_JSON);
    }

    private void writeJson(Path path, Object jsonObject) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(path.toFile(), jsonObject);
    }
}

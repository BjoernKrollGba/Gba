package de.samply.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.samply.json.fhirentity.Observation;
import de.samply.json.fhirentity.Patient;
import de.samply.json.nested.Code;
import de.samply.json.nested.Reference;
import de.samply.json.nested.ValueQuantity;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FakeFhirResourceCreator {

    private final static String FILE_PRAEFIX_PATIENT = Patient.RESOURCE_TYPE + "_";
    private final static String FILE_PRAEFIX_OBSERVATION = Observation.RESOURCE_TYPE + "_";
    private static final String SUFFIX_JSON = ".json";

    private final static int NUMBER_PATIENTS = 1000;
    private final static int NUMBER_OBSERVATIONS_PER_PATIENT = 100;

    private final static int PADDING_SIZE = Math.max(
            Double.valueOf(Math.ceil(Math.log10(NUMBER_PATIENTS))).intValue(),
            Double.valueOf(Math.ceil(Math.log10(NUMBER_OBSERVATIONS_PER_PATIENT))).intValue());

    public static void main(String[] args) throws IOException {
        new FakeFhirResourceCreator().create();
    }

    public void create() throws IOException {
        for (int countPatient = 1; countPatient <= NUMBER_PATIENTS; countPatient++) {
            createPatient(countPatient);
        }
    }

    private void createPatient(int countPatient) throws IOException {
        String patientId = Integer.toString(countPatient);
        Path path = createPath(FILE_PRAEFIX_PATIENT, patientId);

        Patient patient = new Patient();
        patient.id = patientId;

        writeJson(path, patient);

        createObservations(countPatient);
    }

    private void createObservations(int countPatient) throws IOException {
        for (int countObservation = 1; countObservation <= NUMBER_OBSERVATIONS_PER_PATIENT; countObservation++) {
            createObservation(countObservation, countPatient);
        }
    }

    private void createObservation(int countObservation, int countPatient) throws IOException {
        String observationId = Integer.toString(countPatient) + "_" + Integer.toString(countObservation);
        Path path = createPath(FILE_PRAEFIX_OBSERVATION, observationId);

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

        writeJson(path, observation);
    }

    private Code createCode(String system, int countObservation) {
        Code code = new Code();
        code.system = system;
        code.code = Integer.toString(countObservation);
        return code;
    }

    private Path createPath(String praefix, String id) {
        return Paths.get(Neo4jLoader.DIR_IMPORT, praefix + StringUtils.leftPad(id, PADDING_SIZE) + SUFFIX_JSON);
    }

    private void writeJson(Path path, Object jsonObject) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(path.toFile(), jsonObject);
    }
}

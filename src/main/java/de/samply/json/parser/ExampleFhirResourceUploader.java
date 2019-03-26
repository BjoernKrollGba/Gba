package de.samply.json.parser;

import de.samply.json.parser.model.FhirJsonNodeEntity;

import java.io.File;
import java.io.IOException;

public class ExampleFhirResourceUploader {

    private static final String PATHNAME_PATIENT_1 = "C:\\Users\\BjoernKroll\\IdeaProjects\\neo4jloader\\src\\main\\resources\\fhir\\example\\resource\\Patient_1325609.json";
    private static final String PATHNAME_OBSERVATION_1 = "C:\\Users\\BjoernKroll\\IdeaProjects\\neo4jloader\\src\\main\\resources\\fhir\\example\\resource\\Observation_1325973.json";
    private static final String PATHNAME_OBSERVATION_2 = "C:\\Users\\BjoernKroll\\IdeaProjects\\neo4jloader\\src\\main\\resources\\fhir\\example\\resource\\Observation_1325860.json";
    private static final String PATHNAME_ENCOUNTER_1 = "C:\\Users\\BjoernKroll\\IdeaProjects\\neo4jloader\\src\\main\\resources\\fhir\\example\\resource\\Encounter_1325858.json";

    public static void main(String[] args) throws Exception {
        executeStatement(PATHNAME_OBSERVATION_1);
        executeStatement(PATHNAME_PATIENT_1);
        executeStatement(PATHNAME_OBSERVATION_2);
        executeStatement(PATHNAME_ENCOUNTER_1);
    }

    private static void executeStatement(String pathnameObservation1) throws Exception {
        try (Neo4jStatementExecutor executor = new Neo4jStatementExecutor()) {
            executor.execute(createStatement(pathnameObservation1));
        }
    }

    private static String createStatement(String filename) throws IOException {
        FhirJsonNodeEntity entity = (FhirJsonNodeEntity) new FhirJsonParser(new File(filename)).parseFhirResource();
        String statement = new MergeStatementProvider().create(entity);

        System.out.println(statement);
        System.out.println("-------------------------------------------------------------------------------");

        return statement;
    }


}

package de.samply.json.parser;

import de.samply.json.parser.model.AbstractFhirJsonNode;
import de.samply.json.parser.model.FhirJsonNodeEntity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExampleFhirResourceUploader {

    private static final String PATHNAME_PATIENT_1 = "C:\\Users\\BjoernKroll\\IdeaProjects\\neo4jloader\\src\\main\\resources\\fhir\\example\\resource\\Patient_1325609.json";
    private static final String PATHNAME_OBSERVATION_1 = "C:\\Users\\BjoernKroll\\IdeaProjects\\neo4jloader\\src\\main\\resources\\fhir\\example\\resource\\Observation_1325973.json";
    private static final String PATHNAME_OBSERVATION_2 = "C:\\Users\\BjoernKroll\\IdeaProjects\\neo4jloader\\src\\main\\resources\\fhir\\example\\resource\\Observation_1325860.json";
    private static final String PATHNAME_ENCOUNTER_1 = "C:\\Users\\BjoernKroll\\IdeaProjects\\neo4jloader\\src\\main\\resources\\fhir\\example\\resource\\Encounter_1325858.json";
    private static final String PATHNAME_BUNDLE_ARRAY = "C:\\Users\\BjoernKroll\\IdeaProjects\\neo4jloader\\src\\main\\resources\\fhir\\example\\resource\\BundleArray_1325609.json";

    public static void main(String[] args) throws Exception {
        executeStatement(new String[]{
                PATHNAME_BUNDLE_ARRAY,
/*
                PATHNAME_OBSERVATION_1,
                PATHNAME_PATIENT_1,
                PATHNAME_OBSERVATION_2,
                PATHNAME_ENCOUNTER_1,
*/
        });
    }

    private static void executeStatement(String[] pathnames) throws Exception {
        List<String> statements = new ArrayList<>();
        for (String pathname: pathnames) {
            statements.add(createStatement(pathname));
        }

        try (Neo4jStatementExecutor executor = new Neo4jStatementExecutor()) {
            executor.execute(statements.toArray(new String[]{}));
        }
    }

    private static String createStatement(String filename) throws IOException {
        List<AbstractFhirJsonNode> nodes = new FhirJsonParser(new File(filename)).parseFhirResource();
        List<FhirJsonNodeEntity> entities = nodes.stream().
                filter(AbstractFhirJsonNode::isEntityNode).
                map(node -> (FhirJsonNodeEntity) node).collect(Collectors.toList());
        String statement = new MergeStatementProvider().create(entities);

        System.out.println(statement);
        System.out.println("-------------------------------------------------------------------------------");

        return statement;
    }


}

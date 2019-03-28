package de.samply.json.parser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import de.samply.json.parser.model.AbstractFhirJsonNode;
import de.samply.json.parser.model.FhirJsonNodeEntity;
import de.samply.json.parser.model.MergeStatementProvidable;

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
        List<MergeStatementProvidable> statements = new ArrayList<>();
        for (String pathname: pathnames) {
            statements.addAll(createStatement(pathname));
        }

        try (Neo4jStatementExecutor executor = new Neo4jStatementExecutor()) {
            executor.execute(statements.toArray(new MergeStatementProvidable[]{}));
        }
    }

    private static List<MergeStatementProvidable> createStatement(String filename) throws IOException {
        JsonParser jsonParser = createJsonParser(filename);
        List<AbstractFhirJsonNode> nodes = new FhirJsonParser().parseFhirResource(jsonParser);
        List<FhirJsonNodeEntity> entities = nodes.stream().
                filter(AbstractFhirJsonNode::isEntityNode).
                map(node -> (FhirJsonNodeEntity) node).collect(Collectors.toList());
        List<MergeStatementProvidable> statements = new ArrayList<>();
        for (FhirJsonNodeEntity entity : entities) {
            statements.addAll(new MergeStatementProvider().create(entity));
        }
        for (MergeStatementProvidable statement : statements) {
            System.out.println(statement);
            System.out.println("-------------------------------------------------------------------------------");
        }

        return statements;
    }

    private static JsonParser createJsonParser(String filename) throws IOException {
        FakeArrayBundleProvider provider = new FakeArrayBundleProvider(1);
        return new JsonFactory().createParser(provider.createWriter().toString());
//        return new JsonFactory().createParser(new File(filename));
    }


}

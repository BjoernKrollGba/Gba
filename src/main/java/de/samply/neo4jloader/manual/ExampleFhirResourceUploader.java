package de.samply.neo4jloader.manual;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import de.samply.neo4jloader.statement.MergeStatementFactory;
import de.samply.neo4jloader.statement.Neo4jStatementExecutor;
import de.samply.neo4jloader.model.AbstractFhirJsonNode;
import de.samply.neo4jloader.model.FhirJsonNodeEntity;
import de.samply.neo4jloader.parser.FhirJsonParser;
import de.samply.neo4jloader.statement.AbstractCreateStatement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExampleFhirResourceUploader {

    private static final int NUMBER_PATIENT_BUNDLES = 2;
    private static final int NUMBER_OBSERVATIONS_PER_PATIENT = 100;

    public static void main(String[] args) throws Exception {

        for (int patientId = 1; patientId <= NUMBER_PATIENT_BUNDLES; patientId++) {
            FakeArrayBundleProvider fakeArrayBundleProvider = new FakeArrayBundleProvider(patientId, NUMBER_OBSERVATIONS_PER_PATIENT);

            List<AbstractCreateStatement> statements = new ArrayList<>(createStatement(fakeArrayBundleProvider));
            logToSystemOut(patientId, statements);

            try (Neo4jStatementExecutor executor = new Neo4jStatementExecutor()) {
                executor.execute(statements.toArray(new AbstractCreateStatement[]{}));
            }
        }
    }

    private static List<AbstractCreateStatement> createStatement(FakeArrayBundleProvider fakeArrayBundleProvider) throws IOException {
        JsonParser jsonParser = new JsonFactory().createParser(fakeArrayBundleProvider.createWriter().toString());

        List<AbstractFhirJsonNode> nodes = new FhirJsonParser().parseFhirResource(jsonParser);
        List<FhirJsonNodeEntity> entities = castNodesToEntityNodes(nodes);

        return new MergeStatementFactory().create(entities);
    }

    private static List<FhirJsonNodeEntity> castNodesToEntityNodes(List<AbstractFhirJsonNode> nodes) {
        return nodes.stream().
                    filter(AbstractFhirJsonNode::isEntityNode).
                    map(node -> (FhirJsonNodeEntity) node).collect(Collectors.toList());
    }

    private static void logToSystemOut(int patientId, List<AbstractCreateStatement> statements) {
        System.out.println("============================================================================");
        System.out.println("===  Patient-ID: " + patientId);
        System.out.println("============================================================================");
        for (AbstractCreateStatement statement : statements) {
            System.out.println(statement);
            System.out.println("-------------------------------------------------------------------------------");
        }
    }
}

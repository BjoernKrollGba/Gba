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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExampleFhirResourceUploader {

    private static final int FIRST_PATIENT_ID = 1000001;
    private static final int NUMBER_BUNDLES = 4000;
    private static final int NUMBER_PATIENTS_PER_BUNDLE = 25;
    private static final int NUMBER_OBSERVATIONS_PER_PATIENT = 100;

    private static LocalDateTime timeStampCurrent;
    private static LocalDateTime timeStampBegin;
    private static LocalDateTime timeStampEnd;

    public static void main(String[] args) throws Exception {
        timeStampBegin = LocalDateTime.now();
        int patientId = FIRST_PATIENT_ID;
        while (patientId < FIRST_PATIENT_ID + NUMBER_BUNDLES * NUMBER_PATIENTS_PER_BUNDLE) {
            List<AbstractCreateStatement> statements = new ArrayList<>();
            timeStampCurrent = LocalDateTime.now();

            for (int patientIdTemp = patientId; patientIdTemp < patientId + NUMBER_PATIENTS_PER_BUNDLE; patientIdTemp++) {
                FakeArrayBundleProvider fakeArrayBundleProvider = new FakeArrayBundleProvider(patientIdTemp, NUMBER_OBSERVATIONS_PER_PATIENT);
                statements.addAll(createStatement(fakeArrayBundleProvider));

            }
            try (Neo4jStatementExecutor executor = new Neo4jStatementExecutor()) {
                executor.execute(statements.toArray(new AbstractCreateStatement[]{}));
            }

            logToSystemOut(patientId, statements, timeStampCurrent);
            patientId += NUMBER_PATIENTS_PER_BUNDLE;
        }
        timeStampEnd = LocalDateTime.now();

        System.out.println();
        System.out.println();
        System.out.println("Started at: " + timeStampBegin);
        System.out.println("Finished at: " + timeStampEnd);
        long between = ChronoUnit.MILLIS.between(timeStampBegin, timeStampEnd);
        System.out.println("Gesamtdauer (in Millisekunden): " + between);
        System.out.println("Durchschnittliche Dauer pro Bundle (in Millisekunden): " + (between / NUMBER_BUNDLES));
        System.out.println("Durchschnittliche Dauer pro Patient (in Millisekunden): " + (between / (NUMBER_BUNDLES * NUMBER_PATIENTS_PER_BUNDLE)));
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

    private static void logToSystemOut(int patientId, List<AbstractCreateStatement> statements, LocalDateTime timeStampCurrent) {
        //      System.out.println("============================================================================");
        System.out.println("===  Patient-ID: " + patientId);
        System.out.println("Dauer (für " + NUMBER_PATIENTS_PER_BUNDLE + " Patienten): " + ChronoUnit.MILLIS.between(timeStampCurrent, LocalDateTime.now()));
        //    System.out.println("============================================================================");
 /*
        for (AbstractCreateStatement statement : statements) {
            System.out.println(statement);
            System.out.println("-------------------------------------------------------------------------------");
        }
*/
    }
}

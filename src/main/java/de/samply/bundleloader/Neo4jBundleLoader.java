package de.samply.bundleloader;

import de.samply.json.fhirentity.Observation;
import de.samply.json.fhirentity.Patient;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.v1.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class Neo4jBundleLoader implements AutoCloseable {

    final static String DIR_IMPORT = "C:\\Users\\BjoernKroll\\neo4j\\fhirsource";

    private final Driver driver;

    private void start() {
        setNodeKeys();
        loadFiles();
    }

    private void setNodeKeys() {
        executeStatement("CREATE CONSTRAINT ON (n:" + Patient.RESOURCE_TYPE + ") ASSERT (n.id) IS UNIQUE");
        executeStatement("CREATE CONSTRAINT ON (n:" + Observation.RESOURCE_TYPE + ") ASSERT (n.id) IS UNIQUE");
    }

    private Neo4jBundleLoader(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    @Override
    public void close() throws Exception {
        driver.close();
    }

    private void loadFiles() {
        Path directory = Paths.get(DIR_IMPORT);
        File[] files = ObjectUtils.defaultIfNull(directory.toFile().listFiles(), new File[]{});
        for (File file : files) {
            System.out.println(LocalDateTime.now() + "File: " + file.getName());
            String fileType = file.getName().split("_")[0];
            if (!StringUtils.equals(fileType, "Bundle")) {
                continue;
            }
            insertBundle(file);
        }
    }

/*
CALL apoc.load.json("file:/import/Bundle_2.json")
YIELD value AS b
UNWIND b.items AS item
FOREACH(ignoreMe IN CASE WHEN item.resourceType = "Patient" THEN [1] ELSE [] END |
	MERGE (patient:Patient {id: "Patient/" + item.id})
	SET patient.resourceType = item.resourceType)
FOREACH(ignoreMe IN CASE WHEN item.resourceType = "Observation" THEN [1] ELSE [] END |
    MERGE (patient:Patient {id: item.subject.reference})
    MERGE (observation:Observation {id: "Observation/" + item.id})
    ON CREATE SET
        observation.status = item.status,
        observation.resourceType = item.resourceType,
        observation.status = item.status,
        observation.value = item.valueQuantity.value
    FOREACH (c IN item.code |
        MERGE (code:Code {system: c.system, code:  c.code})
        MERGE (observation)-[:HAS_CODE]->(code))
    MERGE (patient)<-[:SUBJECT]-(observation))
 */

    private void insertBundle(final File file) {
        final String statement = "CALL apoc.load.json(\"file:///import/" + file.getName() + "\") " +
                "YIELD value AS b " +
                "UNWIND b.items AS item " +
                // FOREACH is used as IF ... THEN and iterates over either an empty or one-element list depending on the condition
                // see https://stackoverflow.com/questions/27576427/cypher-neo4j-case-expression-with-merge
                "FOREACH(ignoreMe IN CASE WHEN item.resourceType = \"Patient\" THEN [1] ELSE [] END | " +
                "        MERGE (patient:Patient {id: \"Patient/\" +item.id}) " +
                "        SET patient.resourceType = item.resourceType) " +
                // FOREACH is used as IF ... THEN and iterates over either an empty or one-element list depending on the condition
                "FOREACH(ignoreMe IN CASE WHEN item.resourceType = \"Observation\" THEN [1] ELSE [] END | " +
                "        MERGE (patient:Patient {id: item.subject.reference}) " +
                "        MERGE (observation:Observation {id: \"Observation/\" +item.id}) " +
                "        ON CREATE SET " +
                "        observation.status = item.status, " +
                "                observation.resourceType = item.resourceType, " +
                "                observation.status = item.status, " +
                "                observation.value = item.valueQuantity.value " +
                "        FOREACH (c IN item.code | " +
                "                MERGE (code:Code {system: c.system, code:  c.code}) " +
                "                MERGE (observation)-[:HAS_CODE]->(code)) " +
                "        MERGE (patient)<-[:SUBJECT]-(observation))";
        executeStatement(statement);
    }

    private void executeStatement(final String statement ) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run(statement);
                return "DONE";
            });
        }
    }

    /*
    !!!!!!!!!!!!! DELETE ALL nodes and relations !!!!!!!!!!!!!!

    match (s), (m)-[r]-(n) delete s, m, n, r
     */
    public static void main(String[] args) throws Exception {
        try (Neo4jBundleLoader neo4jLoader = new Neo4jBundleLoader("bolt://localhost:7687", "neo4j", "neo4jneo4j")) {
            neo4jLoader.start();
        }
    }
}
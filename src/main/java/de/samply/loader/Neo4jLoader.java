package de.samply.loader;

import de.samply.json.fhirentity.Observation;
import de.samply.json.fhirentity.Patient;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Neo4jLoader implements AutoCloseable {

    final static String DIR_IMPORT = "C:\\Users\\BjoernKroll\\neo4j\\fhirsource";

    private final Driver driver;

    public Neo4jLoader(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    @Override
    public void close() throws Exception {
        driver.close();
    }

    public void loadFiles() {
        Path directory = Paths.get(DIR_IMPORT);
        for (File file : directory.toFile().listFiles()) {
            String fileType = file.getName().split("_")[0];
            switch (fileType) {
                case Patient.RESOURCE_TYPE:
                    insertPatient(file);
                    break;
                case Observation.RESOURCE_TYPE:
                    insertObservation(file);
                    break;
            }

        }
    }

/*
CALL apoc.load.json("file:/import/Observation_1_2.json")
YIELD value AS o
MERGE (patient:Patient {id: o.subject.reference})
MERGE (observation:Observation {id:o.id})
ON CREATE SET
	observation.status = o.status,
    observation.resourceType = o.resourceType,
    observation.status = o.status,
    observation.value = o.valueQuantity.value
FOREACH (c IN o.code |
	MERGE (code:Code {system: c.system, code:  c.code})
    MERGE (observation)-[:HAS_CODE]->(code))
MERGE (patient)<-[:SUBJECT]-(observation)
 */

    private void insertObservation(final File file) {
        try (Session session = driver.session()) {
            String observation = session.writeTransaction(new TransactionWork<String>() {
                @Override
                public String execute(Transaction tx) {
                    String statement = "CALL apoc.load.json(\"file:///import/" + file.getName() + "\") " +
                            "YIELD value AS o " +
                            "MERGE (patient:Patient {id: o.subject.reference}) " +
                            "MERGE (observation:Observation {id: \"Observation/\" + o.id}) " +
                            "ON CREATE SET " +
                            "   observation.status = o.status, " +
                            "   observation.resourceType = o.resourceType, " +
                            "   observation.status = o.status, " +
                            "   observation.value = o.valueQuantity.value " +
                            "FOREACH (c IN o.code |  " +
                            "   MERGE (code:Code {system: c.system, code:  c.code}) " +
                            "   MERGE (observation)-[:HAS_CODE]->(code)) " +
                            "MERGE (patient)<-[:SUBJECT]-(observation) ";
                    StatementResult result = tx.run(statement);

                    return "DONE";
                }
            });
        }

    }
/*
CALL apoc.load.json("file:/import/Observation_1_2.json")
YIELD value AS p
MERGE (patient:Patient {id: "Patient/" +p.id})
SET p.resourceType = patient.resourceType
*/
    private void insertPatient(final File file) {
        try (Session session = driver.session()) {
            String patient = session.writeTransaction(new TransactionWork<String>() {
                @Override
                public String execute(Transaction tx) {
                    String statement = "CALL apoc.load.json(\"file:///import/" + file.getName() + "\") " +
                            "YIELD value AS patient " +
                            "MERGE (p:Patient {id: \"Patient/\" +patient.id}) " +
                            "SET p.resourceType = patient.resourceType";
                    StatementResult result = tx.run(statement);

                    return "DONE";
                }
            });
        }
    }

/*
!!!!!!!!!!!!! DELETE ALL nodes and relations !!!!!!!!!!!!!!

match (s), (m)-[r]-(n) delete s, m, n, r
 */
    public static void main(String[] args) throws Exception {
        try (Neo4jLoader neo4jLoader = new Neo4jLoader("bolt://localhost:7687", "neo4j", "neo4jneo4j")) {
            neo4jLoader.loadFiles();
        }
    }
}
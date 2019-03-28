package de.samply.json.parser.model;

import org.neo4j.driver.v1.Transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class RelationMergeStatement implements  MergeStatementProvidable {
    private final String mergeStatement;
    private Map<String, Object> parameters = new HashMap<>();

    public RelationMergeStatement(AbstractFhirJsonNode startNode, AbstractFhirJsonNode targetNode, String tag) {
        String mergeStatementTemp = "MERGE (s: " + startNode.getNeo4jLabel() + " " + startNode.getNeo4jNodeJsonKeyPattern("start_") + ")";
        mergeStatementTemp += "-[:" + tag + "]->";
        mergeStatementTemp += "(t: " + targetNode.getNeo4jLabel() + " " + targetNode.getNeo4jNodeJsonKeyPattern("target_") + ")";

        addParameter("start_neo4jId", startNode.getNeo4jId());
        addParameter("start_neo4jLabel", startNode.getNeo4jLabel());
        addParameter("target_neo4jId", targetNode.getNeo4jId());
        addParameter("target_neo4jLabel", targetNode.getNeo4jLabel());

        this.mergeStatement = mergeStatementTemp;
    }

    @Override
    public Function<Transaction, String> getCallback() {
        return (tx -> {
            Object[] keysAndValues = createKeysAndValuesArray();
            tx.run(mergeStatement,
                    org.neo4j.driver.v1.Values.parameters( keysAndValues ) );
            return "DONE";
        });
    }

    public void addParameter(String name, Object value) {
        parameters.put(name, value);
    }

    @Override
    public String toString() {
        return mergeStatement;
    }

    private Object[] createKeysAndValuesArray() {
        Object[] keysAndValues = new Object[2 * parameters.size()];
        int index = 0;
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            keysAndValues[index] = entry.getKey();
            index++;
            keysAndValues[index] = entry.getValue();
            index++;
        }
        return keysAndValues;
    }
}

package de.samply.json.parser.model;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.v1.Transaction;

public class NodeMergeStatement implements MergeStatementProvidable {

    private static final String NEWLINE = System.lineSeparator();
    private static final String IDENT = "   ";

    private final boolean isEntityNode;
    private final String mergeStatement;
    private Map<String, Object> parameters = new HashMap<>();

    public NodeMergeStatement(AbstractFhirJsonNode node) {
        isEntityNode = node.isEntityNode();

        mergeStatement = (isEntityNode ? "MERGE" : "CREATE")
                + " (n: " + node.getNeo4jLabel() + node.getNeo4jNodeJsonKeyPattern("") + ")";
        addParameter("neo4jId", node.getNeo4jId());
        addParameter("neo4jLabel", node.getNeo4jLabel());
    }

    public void addParameter(String name, Object value) {
        parameters.put(name, value);
    }

    public String getMergeStatement() {
        return mergeStatement;
    }

    private String getSetStatement() {
        String setStatement;
        if (isEntityNode) {
            setStatement = getSetStatement("ON CREATE SET ");
            setStatement += getSetStatement(NEWLINE + "ON MATCH SET");
        } else {
            setStatement = getSetStatement("SET ");
        }
        return setStatement;
    }

    private String getSetStatement(String onTypeSetStatement) {
        String setStatement = onTypeSetStatement ;//+ NEWLINE;
        int counter = parameters.size();
        for (String name : parameters.keySet()) {
            setStatement += IDENT + "n." + name + " = $" + name;
            counter--;
            if (counter > 0) {
                setStatement += ",";
            }
        }
        return setStatement;
    }

    @Override
    public Function<Transaction, String> getCallback() {
        return (tx -> {
            Object[] keysAndValues = createKeysAndValuesArray();
            tx.run( getMergeStatement() + NEWLINE + getSetStatement(),
                    org.neo4j.driver.v1.Values.parameters( keysAndValues ) );
            return "DONE";
        });
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

    @Override
    public String toString() {
        return mergeStatement + " \n Parameters: " + StringUtils.join(parameters.keySet().stream().map(key -> key + "=" + parameters.get(key)).collect(Collectors.toList()), ",");
    }
}

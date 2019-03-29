package de.samply.json.parser.model;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.v1.Transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractCreateStatement {

    private Map<String, Object> parameters = new HashMap<>();

    static final String NEWLINE = System.lineSeparator();
    static final String IDENT = "   ";

    public Function<Transaction, String> executeStatementInTransactionFunction() {
        return (tx -> {
            tx.run( getCreateStatementTemplate(),
                    org.neo4j.driver.v1.Values.parameters( getKeysAndValuesArray() ) );
            return "DONE";
        });
    }

    protected abstract String getCreateStatementTemplate();

    private Object[] getKeysAndValuesArray() {
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

    public void addParameter(String name, Object value) {
        parameters.put(name, value);
    }

    Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return getCreateStatementTemplate() + " Parameters: " + StringUtils.join(getParameters().keySet().stream().map(key -> key + "=" + getParameters().get(key)).collect(Collectors.toList()), ",");
    }
}

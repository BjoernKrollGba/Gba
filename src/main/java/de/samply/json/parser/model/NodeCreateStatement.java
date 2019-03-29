package de.samply.json.parser.model;

public class NodeCreateStatement extends AbstractCreateStatement {

    private final boolean isEntityNode;
    private final String mergeStatement;

    public NodeCreateStatement(AbstractFhirJsonNode node) {
        isEntityNode = node.isEntityNode();

        mergeStatement = (isEntityNode ? "MERGE" : "CREATE")
                + " (n: " + node.getNeo4jLabel() + node.getNeo4jNodeJsonKeyPattern("") + ")";
        addParameter("neo4jId", node.getNeo4jId());
        addParameter("neo4jLabel", node.getNeo4jLabel());
    }

    @Override
    protected String getCreateStatementTemplate() {
        return getMergeStatement() + NEWLINE + getSetStatement();
    }

    private String getMergeStatement() {
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
        StringBuilder setStatement = new StringBuilder(onTypeSetStatement).append(NEWLINE);
        int counter = getParameters().size();
        for (String name : getParameters().keySet()) {
            setStatement.append(IDENT + "n.").append(name).append(" = $").append(name);
            counter--;
            if (counter > 0) {
                setStatement.append(",");
            }
        }
        return setStatement.toString();
    }
}

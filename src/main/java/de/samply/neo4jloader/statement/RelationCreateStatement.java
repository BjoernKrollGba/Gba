package de.samply.neo4jloader.statement;

import de.samply.neo4jloader.model.AbstractFhirJsonNode;

public class RelationCreateStatement extends AbstractCreateStatement {

    private final String mergeStatement;

    RelationCreateStatement(AbstractFhirJsonNode startNode, AbstractFhirJsonNode targetNode, String tag) {
        String mergeStatementTemp = "MATCH (s: " + startNode.getNeo4jLabel() + " " + startNode.getNeo4jNodeJsonKeyPattern("start_") + ")" + NEWLINE;
        mergeStatementTemp += "MATCH (t: " + targetNode.getNeo4jLabel() + " " + targetNode.getNeo4jNodeJsonKeyPattern("target_") + ")" + NEWLINE;
        mergeStatementTemp += "MERGE (s)-[:" + tag + "]->(t)";

        addParameter("start_neo4jId", startNode.getNeo4jId());
        addParameter("start_neo4jLabel", startNode.getNeo4jLabel());
        addParameter("target_neo4jId", targetNode.getNeo4jId());
        addParameter("target_neo4jLabel", targetNode.getNeo4jLabel());

        this.mergeStatement = mergeStatementTemp;
    }

    @Override
    protected String getCreateStatementTemplate() {
        return mergeStatement;
    }
}

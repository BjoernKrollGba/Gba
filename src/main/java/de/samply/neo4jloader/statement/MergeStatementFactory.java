package de.samply.neo4jloader.statement;

import de.samply.neo4jloader.model.AbstractFhirJsonNode;
import de.samply.neo4jloader.model.FhirJsonNodeEntity;
import de.samply.neo4jloader.model.FhirJsonProperty;
import de.samply.neo4jloader.model.FhirJsonRelationTo;
import de.samply.neo4jloader.statement.AbstractCreateStatement;
import de.samply.neo4jloader.statement.NodeCreateStatement;
import de.samply.neo4jloader.statement.RelationCreateStatement;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class MergeStatementFactory {

    private static final String QUOTATION_MARKS = "'";

    public List<AbstractCreateStatement> create(FhirJsonNodeEntity entity) {
        return create(Collections.singletonList(entity));
    }

    public List<AbstractCreateStatement> create(List<? extends FhirJsonNodeEntity> entities) {
        Set<AbstractFhirJsonNode> mergedNodes = new HashSet<>();
        List<AbstractCreateStatement> statements = new ArrayList<>();

        for (FhirJsonNodeEntity entity : entities) {
            append(entity, mergedNodes, statements);
        }

        return statements;
    }

    private void append(AbstractFhirJsonNode node, Set<AbstractFhirJsonNode> mergedNodes, List<AbstractCreateStatement> statements) {
        if (mergedNodes.contains(node)) {
            return;
        }

        mergedNodes.add(node);
        NodeCreateStatement nodeMergeStatement = new NodeCreateStatement(node);
        statements.add(nodeMergeStatement);

        appendPropertiesCreate(node, nodeMergeStatement);
        for (FhirJsonRelationTo relation : node.getRelations()) {
            appendRelation(node, relation, mergedNodes, statements);
        }
    }

    private void appendPropertiesCreate(AbstractFhirJsonNode node, NodeCreateStatement nodeMergeStatement) {
        node.getProperties().forEach(property ->
                nodeMergeStatement.addParameter(property.getName(),
                QUOTATION_MARKS + getPropertyValue(node, property) + QUOTATION_MARKS));
        node.getPrimitiveArrays().forEach(namedCollection -> nodeMergeStatement.addParameter(
                namedCollection.getName(),
                "[" + StringUtils.join(namedCollection.getValues().stream().map(Object::toString).map(this::escape).map(value -> QUOTATION_MARKS + value + QUOTATION_MARKS).collect(Collectors.toList()), ",") + "]"));
    }

    private String getPropertyValue(AbstractFhirJsonNode node, FhirJsonProperty property) {
        if (StringUtils.equals(property.getName(), "id") && node instanceof FhirJsonNodeEntity) {
            return ((FhirJsonNodeEntity) node).getResurceType() + "/" + escape(property.getValue().toString());
        }

        return escape(property.getValue().toString());
    }

    private void appendRelation(AbstractFhirJsonNode node, FhirJsonRelationTo relation, Set<AbstractFhirJsonNode> mergedNodes, List<AbstractCreateStatement> statements) {
        AbstractFhirJsonNode target = relation.getTarget();
        append(target, mergedNodes, statements);

        statements.add(new RelationCreateStatement(node, target, StringUtils.upperCase(relation.getTag())));
    }

    private String escape(String input) {
        String result = input.replace("\\", "\\\\");
        result = result.replace("'", "\\'");

        return result;
    }
}

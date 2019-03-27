package de.samply.json.parser;

import de.samply.json.parser.model.AbstractFhirJsonNode;
import de.samply.json.parser.model.FhirJsonNodeEntity;
import de.samply.json.parser.model.FhirJsonProperty;
import de.samply.json.parser.model.FhirJsonRelationTo;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

class MergeStatementProvider {

    private static final String NEWLINE = System.lineSeparator();
    private static final String QUOTATION_MARKS = "'";
    private static final String IDENT = "   ";

    String create(FhirJsonNodeEntity entity) {
        return create(Collections.singletonList(entity));
    }

    String create(List<? extends FhirJsonNodeEntity> entities) {
        StringBuilder builder = new StringBuilder();
        Map<AbstractFhirJsonNode, String> nodeKeyMap = new HashMap<>();
        Set<AbstractFhirJsonNode> mergedNodes = new HashSet<>();

        for (FhirJsonNodeEntity entity : entities) {
            append(entity, builder, nodeKeyMap, mergedNodes);
        }

        return builder.toString();
    }

    private void append(AbstractFhirJsonNode entity, StringBuilder builder, Map<AbstractFhirJsonNode, String> nodeKeyMap, Set<AbstractFhirJsonNode> mergedNodes) {
        appendNodeMerge(entity, builder, nodeKeyMap, mergedNodes);
        appendPropertiesCreate(entity, builder, nodeKeyMap);
        for (FhirJsonRelationTo relation : entity.getRelations()) {
            appendRelation(entity, relation, builder, nodeKeyMap, mergedNodes);
        }
    }

    private void appendNodeMerge(AbstractFhirJsonNode node, StringBuilder builder, Map<AbstractFhirJsonNode, String> nodeKeyMap, Set<AbstractFhirJsonNode> mergedNodes) {
        if (mergedNodes.contains(node)) {
            return;
        } else {
            mergedNodes.add(node);
        }

        if (node.isEntityNode()) {
            FhirJsonNodeEntity targetEntity = (FhirJsonNodeEntity) node;
            builder.append("MERGE (").append(getAliasName(node, nodeKeyMap)).append(":").append(targetEntity.getNeo4jNodeLabel());
            builder.append(" { id: ").append(QUOTATION_MARKS).append(getExtendedId(targetEntity)).append(QUOTATION_MARKS).
                    append(", resourceType: ").append(QUOTATION_MARKS).append(targetEntity.getResurceType()).append(QUOTATION_MARKS).append(" }");
            builder.append(")").append(NEWLINE);
        } else {
            builder.append("CREATE (").append(getAliasName(node, nodeKeyMap)).append(":").append(node.getNeo4jNodeLabel());
            builder.append(" { ");
            boolean first = true;
            for (FhirJsonProperty property : node.getProperties()) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }

                builder.append(property.getName()).append(": ").append(QUOTATION_MARKS).append(getPropertyValue(node, property)).append(QUOTATION_MARKS);
            }
            builder.append(" }");
            builder.append(")").append(NEWLINE);
        }
    }

    private void appendPropertiesCreate(AbstractFhirJsonNode node, StringBuilder builder, Map<AbstractFhirJsonNode, String> nodeKeyMap) {
        List<String> propertyLines = new ArrayList<>();

        String alias = getAliasName(node, nodeKeyMap);
        node.getProperties().forEach(property ->
                propertyLines.add(alias + "." + property.getName() + " = "
                        + QUOTATION_MARKS + getPropertyValue(node, property) + QUOTATION_MARKS));
        node.getPrimitiveArrays().forEach(namedCollection -> propertyLines.add(alias + "." + namedCollection.getName() + " = " +
                "[" + StringUtils.join(
                namedCollection.getValues().stream().map(Object::toString).map(this::escape).map(value -> QUOTATION_MARKS + value + QUOTATION_MARKS).collect(Collectors.toList()), ",") + "]"));
        if (!propertyLines.isEmpty()) {
            if (node.isEntityNode()) {
                appenSetStatement("ON MATCH SET ", propertyLines, builder);
                appenSetStatement("ON CREATE SET ", propertyLines, builder);
            } else {
                appenSetStatement("SET ", propertyLines, builder);
            }
        }
    }

    private void appenSetStatement(String on_set_mode, List<String> propertyLines, StringBuilder builder) {
        builder.append(on_set_mode);
        builder.append(NEWLINE);
        builder.append(IDENT);

        builder.append(StringUtils.join(propertyLines, ", " + NEWLINE + IDENT));
        builder.append(NEWLINE);
    }

    private String getPropertyValue(AbstractFhirJsonNode node, FhirJsonProperty property) {
        if (StringUtils.equals(property.getName(), "id") && node instanceof FhirJsonNodeEntity) {
            return ((FhirJsonNodeEntity) node).getResurceType() + "/" + escape(property.getValue().toString());
        }

        return escape(property.getValue().toString());
    }

    private void appendRelation(AbstractFhirJsonNode node, FhirJsonRelationTo relation, StringBuilder builder, Map<AbstractFhirJsonNode, String> nodeKeyMap, Set<AbstractFhirJsonNode> mergedNodes) {
        //     builder.append("MERGE (").append(relation.getName()).append(":").append(StringUtils.capitalize(relation.getName())).append(")");
        AbstractFhirJsonNode target = relation.getTarget();
        append(target, builder, nodeKeyMap, mergedNodes);

        builder.append("MERGE (").append(getAliasName(node, nodeKeyMap))
                .append(")-[:").append(StringUtils.upperCase(relation.getTag())).append("]->(")
                .append(getAliasName(target, nodeKeyMap)).append(") ").append(NEWLINE);
    }

    private String getExtendedId(FhirJsonNodeEntity entity) {
        return entity.getNeo4jId();
    }

    private String getAliasName(AbstractFhirJsonNode entity, Map<AbstractFhirJsonNode, String> nodeKeyMap) {
        if (nodeKeyMap.get(entity) == null) {
            String key = entity.isEntityNode() ? "entity_" : "simple_";
            key += (nodeKeyMap.size() + 1);

            nodeKeyMap.put(entity, key);
        }

        return nodeKeyMap.get(entity);
    }

    private String escape(String input) {
        String result = input.replace("\\", "\\\\");
        result = result.replace("'", "\\'");

        return result;
    }
}

package de.samply.json.parser;

import de.samply.json.parser.model.*;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

class MergeStatementProvider {

    private static final String QUOTATION_MARKS = "'";

    List<MergeStatementProvidable> create(FhirJsonNodeEntity entity) {
        return create(Collections.singletonList(entity));
    }

    List<MergeStatementProvidable> create(List<? extends FhirJsonNodeEntity> entities) {
        Set<AbstractFhirJsonNode> mergedNodes = new HashSet<>();
        List<MergeStatementProvidable> mergeStatementProvidables = new ArrayList<>();

        for (FhirJsonNodeEntity entity : entities) {
            append(entity, mergedNodes, mergeStatementProvidables);
        }

        return mergeStatementProvidables;
    }

    private void append(AbstractFhirJsonNode node, Set<AbstractFhirJsonNode> mergedNodes, List<MergeStatementProvidable> mergeStatementProvidables) {
        if (mergedNodes.contains(node)) {
            return;
        }

        mergedNodes.add(node);
        NodeMergeStatement nodeMergeStatement = new NodeMergeStatement(node);
        mergeStatementProvidables.add(nodeMergeStatement);

        appendPropertiesCreate(node, nodeMergeStatement);
        for (FhirJsonRelationTo relation : node.getRelations()) {
            appendRelation(node, relation, mergedNodes, mergeStatementProvidables);
        }
    }

    private void appendPropertiesCreate(AbstractFhirJsonNode node, NodeMergeStatement nodeMergeStatement) {
        node.getProperties().forEach(property -> {
            nodeMergeStatement.addParameter(property.getName(),
                    QUOTATION_MARKS + getPropertyValue(node, property) + QUOTATION_MARKS);});
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

    private void appendRelation(AbstractFhirJsonNode node, FhirJsonRelationTo relation, Set<AbstractFhirJsonNode> mergedNodes, List<MergeStatementProvidable> mergeStatementProvidables) {
        AbstractFhirJsonNode target = relation.getTarget();
        append(target, mergedNodes, mergeStatementProvidables);

        mergeStatementProvidables.add(new RelationMergeStatement(node, target, StringUtils.upperCase(relation.getTag())));
    }

    private String escape(String input) {
        String result = input.replace("\\", "\\\\");
        result = result.replace("'", "\\'");

        return result;
    }
}

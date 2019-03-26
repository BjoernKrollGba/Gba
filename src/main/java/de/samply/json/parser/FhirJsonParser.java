package de.samply.json.parser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import de.samply.json.parser.model.*;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class FhirJsonParser {

    private static final String PATHNAME_PATIENT = "C:\\Users\\BjoernKroll\\IdeaProjects\\neo4jloader\\src\\main\\resources\\fhir\\example\\resource\\Patient_430200.json";
    private static final String PATHNAME_OBSERVATION = "C:\\Users\\BjoernKroll\\IdeaProjects\\neo4jloader\\src\\main\\resources\\fhir\\example\\resource\\Observation_1342501.json";

    private static final String NAME_RESOURCE_TYPE = "resourceType";
    private static final String NAME_ID = "id";
    private static final String NAME_REFERENCE = "reference";

    private final File sourceFile;

    public static void main(String[] args) throws IOException {
        AbstractFhirJsonNode result = new FhirJsonParser(new File(PATHNAME_OBSERVATION)).parseFhirResource();

        System.out.println("Resource Type: " + ((FhirJsonNodeEntity) result).getResurceType());
        System.out.println("ID: " + ((FhirJsonNodeEntity) result).getId());
    }

    FhirJsonParser(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    AbstractFhirJsonNode parseFhirResource() throws IOException {
        JsonParser jsonParser = new JsonFactory().createParser(sourceFile);

        JsonToken token = jsonParser.nextToken();
        if (token == JsonToken.START_OBJECT) {
            return parseFhirResourceNode(true, jsonParser, "");
        }

        if (token == JsonToken.START_ARRAY) {
            FhirJsonNodeEntity nodeEntity = new FhirJsonNodeEntity();
            addFhirResourceArrayToNode(nodeEntity, "array", jsonParser);
            return nodeEntity;
        }

        return null;
    }

    private AbstractFhirJsonNode parseFhirResourceNode(final boolean isEntity, final JsonParser jsonParser, String neo4jLabel) throws IOException {
        final AbstractFhirJsonNode node = createNode(isEntity, neo4jLabel);

        String fhirJsonPropertyName = "";
        JsonToken token = jsonParser.nextToken();
        while (token != JsonToken.END_OBJECT) {
            switch (token) {
                case START_OBJECT:
                    AbstractFhirJsonNode relatedNode =
                            parseFhirResourceNode(false, jsonParser, StringUtils.capitalize(fhirJsonPropertyName));
                    node.getRelations().add(createRelation(fhirJsonPropertyName, relatedNode));
                    break;
                case START_ARRAY:
                    addFhirResourceArrayToNode(node, fhirJsonPropertyName, jsonParser);
                    break;

                case FIELD_NAME:
                    fhirJsonPropertyName = jsonParser.getCurrentName();
                    break;
                case VALUE_TRUE:
                case VALUE_FALSE:
                case VALUE_STRING:
                case VALUE_NUMBER_INT:
                case VALUE_NUMBER_FLOAT:
                    addProperty(jsonParser, node, fhirJsonPropertyName, token);
                    break;
            }

            token = jsonParser.nextToken();
        }

        removeExtraReferenceRelations(node);
        return node;
    }

    private void removeExtraReferenceRelations(AbstractFhirJsonNode node) {
        for (FhirJsonRelationTo relation : node.getRelations()) {
            AbstractFhirJsonNode intermediateNode = relation.getTarget();
            if (hasExtraReferenceRelation(intermediateNode)) {
                AbstractFhirJsonNode targetNode = intermediateNode.getRelations().get(0).getTarget();
                relation.setTarget(targetNode);
            }
        }
    }

    private boolean hasExtraReferenceRelation(AbstractFhirJsonNode intermediateNode) {

        if (intermediateNode.getRelations().size() != 1 || !intermediateNode.getPrimitiveArrays().isEmpty() || !intermediateNode.getProperties().isEmpty()) {
            return false;
        }

        FhirJsonRelationTo relationToCheck = intermediateNode.getRelations().get(0);
        AbstractFhirJsonNode targetNode = relationToCheck.getTarget();

        if (!(targetNode instanceof FhirJsonNodeEntity)) {
            return false;
        }

        return StringUtils.equals(relationToCheck.getName(), NAME_REFERENCE);
    }

    private void addProperty(JsonParser jsonParser, AbstractFhirJsonNode node, String fhirJsonPropertyName, JsonToken token) throws IOException {
        FhirJsonProperty property = createFhirResourceProperty(fhirJsonPropertyName, token, jsonParser);
        if (property == null) {
            return;
        }

        if (node instanceof FhirJsonNodeEntity) {
            setPropertiesForEntityNode((FhirJsonNodeEntity) node, property);
        }

        if (!StringUtils.equals(property.getName(), NAME_REFERENCE)) {
            node.getProperties().add(property);
        } else {
            addReferenceProperty(node, fhirJsonPropertyName, property);
        }
    }

    private void addReferenceProperty(AbstractFhirJsonNode node, String fhirJsonPropertyName, FhirJsonProperty property) {
        String[] splittedValue = StringUtils.split((String) property.getValue(), "/");
        if (splittedValue.length == 2) {
            FhirJsonNodeEntity targetNode = new FhirJsonNodeEntity();
            targetNode.setResurceType(splittedValue[0]);
            targetNode.setId(splittedValue[1]);

            node.getRelations().add(createRelation(fhirJsonPropertyName, targetNode));
        }
    }

    private void setPropertiesForEntityNode(FhirJsonNodeEntity entity, FhirJsonProperty property) {
        if (StringUtils.equals(property.getName(), NAME_RESOURCE_TYPE)) {
            entity.setResurceType((String) property.getValue());
        }

        if (StringUtils.equals(property.getName(), NAME_ID)) {
            entity.setId((String) property.getValue());
        }
    }

    private void addFhirResourceArrayToNode(AbstractFhirJsonNode node, String fhirJsonPropertyName, JsonParser jsonParser) throws IOException {
        NamedCollection primitveArray = new NamedCollection();
        primitveArray.setName(fhirJsonPropertyName);

        JsonToken token = jsonParser.nextToken();
        while (token != JsonToken.END_ARRAY) {
            switch (token) {
                case START_OBJECT:
                    AbstractFhirJsonNode targetNode = parseFhirResourceNode(false, jsonParser, StringUtils.capitalize(fhirJsonPropertyName));
                    node.getRelations().add(createRelation(fhirJsonPropertyName, targetNode));
                    break;
                case VALUE_TRUE:
                case VALUE_FALSE:
                case VALUE_STRING:
                case VALUE_NUMBER_INT:
                case VALUE_NUMBER_FLOAT:
                    Optional<Object> optionalValue = getCurrentValue(token, jsonParser);
                    optionalValue.ifPresent(value -> primitveArray.getValues().add(value));

                    Optional<Neo4jType> optionalType = getCurrentType(token, jsonParser);
                    optionalType.ifPresent(primitveArray::setType);
                    break;
            }

            token = jsonParser.nextToken();
        }

        if (primitveArray.getType() != null) {
            node.getPrimitiveArrays().add(primitveArray);
        }
    }

    private FhirJsonProperty createFhirResourceProperty(String fhirJsonPropertyName, JsonToken token, JsonParser jsonParser) throws IOException {
        FhirJsonProperty property = new FhirJsonProperty();
        property.setName(fhirJsonPropertyName);

        switch (token) {
            case VALUE_TRUE:
            case VALUE_FALSE:
            case VALUE_STRING:
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT:
                Optional<Object> optionalValue = getCurrentValue(token, jsonParser);
                optionalValue.ifPresent(property::setValue);

                Optional<Neo4jType> optionalType = getCurrentType(token, jsonParser);
                optionalType.ifPresent(property::setType);

                return property;

            case VALUE_NULL:
            default:
                return null;
        }
    }

    private Optional<Object> getCurrentValue(JsonToken token, JsonParser jsonParser) throws IOException {
        switch (token) {
            case VALUE_TRUE:
                return Optional.of(jsonParser.getValueAsBoolean());
            case VALUE_FALSE:
                return Optional.of(jsonParser.getValueAsBoolean());
            case VALUE_STRING:
                return Optional.of(jsonParser.getValueAsString());
            case VALUE_NUMBER_INT:
                return Optional.of(jsonParser.getValueAsLong());
            case VALUE_NUMBER_FLOAT:
                return Optional.of(jsonParser.getValueAsDouble());

            case VALUE_NULL:
            default:
                return Optional.empty();
        }
    }

    private Optional<Neo4jType> getCurrentType(JsonToken token, JsonParser jsonParser) {
        switch (token) {
            case VALUE_TRUE:
                return Optional.of(Neo4jType.BOOLEAN);
            case VALUE_FALSE:
                return Optional.of(Neo4jType.BOOLEAN);
            case VALUE_STRING:
                return Optional.of(Neo4jType.STRING);
            case VALUE_NUMBER_INT:
                return Optional.of(Neo4jType.INT);
            case VALUE_NUMBER_FLOAT:
                return Optional.of(Neo4jType.DOUBLE);

            case VALUE_NULL:
            default:
                return Optional.empty();
        }
    }

    private FhirJsonRelationTo createRelation(String fhirJsonPropertyName, AbstractFhirJsonNode relatedNode) {
        FhirJsonRelationTo relation = new FhirJsonRelationTo();
        relation.setName(fhirJsonPropertyName);
        relation.setTag("HAS_" + fhirJsonPropertyName);
        relation.setTarget(relatedNode);

        return relation;
    }

    private AbstractFhirJsonNode createNode(boolean isEntity, String neo4jLabel) {
        final AbstractFhirJsonNode node;
        if (isEntity) {
            node = new FhirJsonNodeEntity();
        } else {
            node = new FhirJsonNodeSimple(neo4jLabel);
        }
        return node;
    }
}

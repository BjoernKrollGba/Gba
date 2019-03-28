package de.samply.json.parser.model;

import org.neo4j.driver.v1.Transaction;

import java.util.function.Function;

public interface MergeStatementProvidable {

    Function<Transaction, String> getCallback();
}

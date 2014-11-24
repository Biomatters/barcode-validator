package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.PluginDocument;
import com.biomatters.plugins.barcoding.validator.validation.results.ResultFact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the result of a validation task.
 *
 * @author Matthew Cheung
 *         Created on 21/08/14 4:46 PM
 */
public class ValidationResult {

    private boolean passed;
    private String message;
    private List<PluginDocument> intermediateDocuments = new ArrayList<PluginDocument>();

    private List<ResultFact> facts = new ArrayList<ResultFact>();

    public List<ResultFact> getFacts() {
        return facts;
    }

    public void setFacts(List<ResultFact> facts) {
        this.facts = facts;
    }

    public void addFact(ResultFact fact) {
        facts.add(fact);
    }

    /**
     *
     * @param passed true if the validation was successful.  False if the input failed the validation process.
     * @param message Extra information to display to the user about why the validation failed.
     */
    public ValidationResult(boolean passed, String message) {
        this.passed = passed;
        this.message = message;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }
    /**
     *
     * @return true if the validation passed.  False if not.
     */
    public boolean isPassed() {
        return passed;
    }


    public void setMessage(String message) {
        this.message = message;
    }
    /**
     *
     * @return The reason why the validation failed.  Or null if the validation succeeded.
     */
    public String getMessage() {
        return message;
    }

    /**
     *
     * @return any {@link com.biomatters.geneious.publicapi.documents.PluginDocument} generated during the validation
     * process that should be returned to the user.
     */
    public List<PluginDocument> getIntermediateDocuments() {
        return Collections.unmodifiableList(intermediateDocuments);
    }

    /**
     *
     * @param document to be returned from {@link #getIntermediateDocuments()}
     */
    public void addIntermediateDocument(PluginDocument document) {
        intermediateDocuments.add(document);
    }
}
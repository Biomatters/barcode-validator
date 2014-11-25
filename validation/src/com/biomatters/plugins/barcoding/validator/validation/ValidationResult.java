package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.plugins.barcoding.validator.validation.results.ResultFact;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the result of a validation task.
 *
 * @author Matthew Cheung
 *         Created on 21/08/14 4:46 PM
 */
public class ValidationResult {

    private boolean passed;
    private String message;

    private Map<SequenceDocument, ResultFact> facts = new HashMap<SequenceDocument, ResultFact>();

    public Map<SequenceDocument, ResultFact> getFacts() {
        return facts;
    }

    public void addFact(SequenceDocument sequenceDocument, ResultFact fact) {
        facts.put(sequenceDocument, fact);
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
}
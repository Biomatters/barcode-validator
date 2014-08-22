package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;

import java.util.List;
import java.util.Map;

/**
 * Represents a task that validates a collection of user supplied barcode sequences
 *
 * @author Matthew Cheung
 *         Created on 21/08/14 4:52 PM
 */
public interface BulkBarcodeValidation {

    /**
     *
     * @param barcodeSequences A collection of user supplied barcode sequences
     * @return A map containing a {@link com.biomatters.plugins.barcoding.validator.validation.ValidationResult} for each input sequence
     */
    public Map<SequenceDocument, ValidationResult> validate(List<SequenceDocument> barcodeSequences);
}

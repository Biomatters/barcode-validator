package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;

/**
 * @author Gen Li
 *         Created on 2/10/14 12:06 PM
 */
public abstract class SingleBarcodeValidaton extends BarcodeValidation {
    /**
     * @param barcodeSequence The barcode sequence generated from the user's trace files.
     * @param options Options to run the validation with.  Obtained from calling {@link #getOptions()}.
     * @return a {@link com.biomatters.plugins.barcoding.validator.validation.ValidationResult}.
     */
    public abstract ValidationResult validate(NucleotideGraphSequenceDocument barcodeSequence, ValidationOptions options);
}
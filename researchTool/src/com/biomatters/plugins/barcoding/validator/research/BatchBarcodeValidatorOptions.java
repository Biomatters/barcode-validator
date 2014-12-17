package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.plugins.barcoding.validator.research.options.BatchOptions;
import com.biomatters.plugins.barcoding.validator.validation.input.InputOptions;

/**
 * A {@link com.biomatters.plugins.barcoding.validator.research.options.BatchOptions} for the Barcode Validator Operation
 *
 * @author Matthew Cheung
 *         Created on 31/10/14 2:57 PM
 */
public class BatchBarcodeValidatorOptions extends BatchOptions<BarcodeValidatorOptions> {

    private static final String INPUT_OPTIONS_NAME = "input";

    public BatchBarcodeValidatorOptions() throws DocumentOperationException {
        super(new BarcodeValidatorOptions());
    }

    @Override
    protected void addFirstOptions() {
        addCollapsibleChildOptions(INPUT_OPTIONS_NAME, "Input", "", new InputOptions(BarcodeValidatorOptions.class), false, false);
    }

    public InputOptions getInputOptions() {
        return (InputOptions)getChildOptions().get(INPUT_OPTIONS_NAME);
    }
}

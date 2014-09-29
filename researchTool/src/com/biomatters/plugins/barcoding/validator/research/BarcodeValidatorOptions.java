package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.plugins.barcoding.validator.validation.BarcodeValidation;
import com.biomatters.plugins.barcoding.validator.validation.TraceValidation;
import com.biomatters.plugins.barcoding.validator.validation.Validation;
import com.biomatters.plugins.barcoding.validator.validation.ValidationOptions;
import com.biomatters.plugins.barcoding.validator.validation.assembly.Cap3AssemblerOptions;
import com.biomatters.plugins.barcoding.validator.validation.input.InputOptions;
import com.biomatters.plugins.barcoding.validator.validation.trimming.ErrorProbabilityOptions;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Gen Li
 *         Created on 3/09/14 2:42 PM
 */
public class BarcodeValidatorOptions extends Options {
    private static final String INPUT_OPTIONS_NAME              = "input";
    private static final String TRIMMING_OPTIONS_NAME           = "trimming";
    private static final String ASSEMBLY_OPTIONS_NAME           = "assembly";
    private static final String TRACE_VALIDATION_OPTIONS_NAME   = "traceValidation";
    private static final String BARCODE_VALIDATION_OPTIONS_NAME = "barcodeValidation";

    public BarcodeValidatorOptions(Class cls) {
        super(cls);

        addInputOptions();

        addTrimmingOptions();

        addTraceValidationOptions();

        addAssemblyOptions();

        addBarcodeValidationOptions();
    }

    public InputOptions getInputOptions() {
        return (InputOptions)getChildOptions().get(INPUT_OPTIONS_NAME);
    }

    public ErrorProbabilityOptions getTrimmingOptions() {
        return (ErrorProbabilityOptions)getChildOptions().get(TRIMMING_OPTIONS_NAME);
    }

    public Map<String, Options> getTraceValidationOptions() {
        return getChildOptions().get(TRACE_VALIDATION_OPTIONS_NAME).getChildOptions();
    }

    public Cap3AssemblerOptions getAssemblyOptions() {
        return (Cap3AssemblerOptions)getChildOptions().get(ASSEMBLY_OPTIONS_NAME);
    }

    private void addInputOptions() {
        addCollapsibleChildOptions(INPUT_OPTIONS_NAME, "Input", "", new InputOptions(), false, false);
    }

    private void addTrimmingOptions() {
        addCollapsibleChildOptions(TRIMMING_OPTIONS_NAME, "Trimming", "", new ErrorProbabilityOptions(), false, true);
    }

    private void addValidationOptions(List<? extends Validation> validations, String name, String label) {
        if (validations.isEmpty()) {
            return;
        }

        Options validationOptions = new Options(BarcodeValidatorOptions.class);

        for (Validation validation : validations) {
            ValidationOptions options = validation.getOptions();

            validationOptions.addChildOptions(options.getName(),
                                              options.getLabel(),
                                              options.getDescription(),
                                              options,
                                              true);
        }

        validationOptions.addChildOptionsPageChooser("chooser",
                                                     "Validation steps: ",
                                                     Collections.<String>emptyList(),
                                                     PageChooserType.BUTTONS,
                                                     true);

        addCollapsibleChildOptions(name, label, "", validationOptions, false, true);
    }

    private void addTraceValidationOptions() {
        addValidationOptions(TraceValidation.getTraceValidations(), TRACE_VALIDATION_OPTIONS_NAME, "Trace validation");
    }

    private void addBarcodeValidationOptions() {
        addValidationOptions(BarcodeValidation.getBarcodeValidations(),
                             BARCODE_VALIDATION_OPTIONS_NAME,
                             "Barcode validation");
    }

    private void addAssemblyOptions() {
        addCollapsibleChildOptions(ASSEMBLY_OPTIONS_NAME, "Assembly", "", new Cap3AssemblerOptions(), false, true);
    }
}
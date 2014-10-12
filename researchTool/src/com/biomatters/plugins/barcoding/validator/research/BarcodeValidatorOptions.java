package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.plugins.barcoding.validator.validation.BarcodeCompareValidation;
import com.biomatters.plugins.barcoding.validator.validation.TraceValidation;
import com.biomatters.plugins.barcoding.validator.validation.Validation;
import com.biomatters.plugins.barcoding.validator.validation.ValidationOptions;
import com.biomatters.plugins.barcoding.validator.validation.assembly.CAP3Options;
import com.biomatters.plugins.barcoding.validator.validation.input.InputOptions;
import com.biomatters.plugins.barcoding.validator.validation.trimming.ErrorProbabilityOptions;

import java.util.Collections;
import java.util.HashMap;
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

    /**
     * @return Map of {@link com.biomatters.plugins.barcoding.validator.validation.ValidationOptions#getIdentifier()}
     *         to {@link com.biomatters.plugins.barcoding.validator.validation.ValidationOptions}
     *         for all loaded {@link com.biomatters.plugins.barcoding.validator.validation.TraceValidation}s.
     */
    public Map<String, ValidationOptions> getTraceValidationOptions() {
        return getValidationOptions(TRACE_VALIDATION_OPTIONS_NAME);
    }

    public CAP3Options getAssemblyOptions() {
        return (CAP3Options)getChildOptions().get(ASSEMBLY_OPTIONS_NAME);
    }

    /**
     * @return Map of {@link com.biomatters.plugins.barcoding.validator.validation.ValidationOptions#getIdentifier()}
     *         to {@link com.biomatters.plugins.barcoding.validator.validation.ValidationOptions}
     *         for all loaded {@link com.biomatters.plugins.barcoding.validator.validation.BarcodeValidation}s.
     */
    public Map<String, ValidationOptions> getBarcodeValidationOptions() {
        return getValidationOptions(BARCODE_VALIDATION_OPTIONS_NAME);
    }

    private void addInputOptions() {
        addCollapsibleChildOptions(INPUT_OPTIONS_NAME, "Input", "", new InputOptions(BarcodeValidatorOptions.class), false, false);
    }

    private void addTrimmingOptions() {
        addCollapsibleChildOptions(TRIMMING_OPTIONS_NAME, "Trimming", "", new ErrorProbabilityOptions(), false, true);
    }

    private void addValidationOptions(List<? extends Validation> validations, String name, String label) {
        Options validationOptions = new Options(BarcodeValidatorOptions.class);

        for (Validation validation : validations) {
            ValidationOptions options = validation.getOptions();
            validationOptions.addChildOptions(options.getIdentifier(), options.getLabel(), options.getDescription(), options, true);
        }

        if (!validations.isEmpty()) {
            validationOptions.addChildOptionsPageChooser("chooser", "Validation steps: ", Collections.<String>emptyList(), PageChooserType.BUTTONS, true);
        }

        addCollapsibleChildOptions(name, label, "", validationOptions, false, true);
    }

    private void addTraceValidationOptions() {
        addValidationOptions(TraceValidation.getTraceValidations(), TRACE_VALIDATION_OPTIONS_NAME, "Trace Validation");
    }

    private void addBarcodeValidationOptions() {
        addValidationOptions(BarcodeCompareValidation.getBarcodeValidations(), BARCODE_VALIDATION_OPTIONS_NAME, "Barcode Validation");
    }

    private void addAssemblyOptions() {
        addCollapsibleChildOptions(ASSEMBLY_OPTIONS_NAME, "Assembly", "", new CAP3Options(BarcodeValidatorOptions.class), false, true);
    }

    /**
     * @param optionsName Validation type.
     * @return Map of {@link com.biomatters.plugins.barcoding.validator.validation.ValidationOptions#getIdentifier()}
     *         to {@link com.biomatters.plugins.barcoding.validator.validation.ValidationOptions}
     *         for all loaded {@link com.biomatters.plugins.barcoding.validator.validation.Validation}s of type
     *         optionsName.
     */
    private Map<String, ValidationOptions> getValidationOptions(String optionsName) {
        Options validationOptions = getChildOptions().get(optionsName);
        Map<String, ValidationOptions> result = new HashMap<String, ValidationOptions>();

        for (Map.Entry<String, Options> entry : validationOptions.getChildOptions().entrySet()) {
            result.put(entry.getKey(), (ValidationOptions)entry.getValue());
        }

        return Collections.unmodifiableMap(result);
    }
}
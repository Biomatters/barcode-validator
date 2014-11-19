package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.plugins.barcoding.validator.validation.Validation;
import com.biomatters.plugins.barcoding.validator.validation.ValidationOptions;
import com.biomatters.plugins.barcoding.validator.validation.assembly.CAP3Options;
import com.biomatters.plugins.barcoding.validator.validation.trimming.TrimmingOptions;

import java.util.*;

/**
 * @author Gen Li
 *         Created on 3/09/14 2:42 PM
 */
public class BarcodeValidatorOptions extends Options {
    private static final String TRIMMING_OPTIONS_NAME   = "trimming";
    private static final String ASSEMBLY_OPTIONS_NAME   = "assembly";
    private static final String VALIDATION_OPTIONS_NAME = "validation";

    public BarcodeValidatorOptions() {
        super(BarcodeValidatorOperation.class);
        addTrimmingOptions();
        addAssemblyOptions();
        addValidationOptions();
    }

    public TrimmingOptions getTrimmingOptions() {
        return (TrimmingOptions)getChildOptions().get(TRIMMING_OPTIONS_NAME);
    }

    public CAP3Options getAssemblyOptions() {
        return (CAP3Options)getChildOptions().get(ASSEMBLY_OPTIONS_NAME);
    }

    /**
     * @return Map of {@link com.biomatters.plugins.barcoding.validator.validation.ValidationOptions#getIdentifier()}
     * to {@link com.biomatters.plugins.barcoding.validator.validation.ValidationOptions} for all loaded
     * {@link com.biomatters.plugins.barcoding.validator.validation.Validation} objects.
     */
    public Map<String, ValidationOptions> getValidationOptions() {
        Options validationOptions = getChildOptions().get(VALIDATION_OPTIONS_NAME);
        Map<String, ValidationOptions> result = new HashMap<String, ValidationOptions>();

        for (Map.Entry<String, Options> entry : validationOptions.getChildOptions().entrySet()) {
            result.put(entry.getKey(), (ValidationOptions)entry.getValue());
        }

        return Collections.unmodifiableMap(result);
    }

    private void addTrimmingOptions() {
        addCollapsibleChildOptions(TRIMMING_OPTIONS_NAME, "Trimming", "", new TrimmingOptions(BarcodeValidatorOptions.class), false, false);
    }

    private void addValidationOptions() {
        Options validationOptions = new Options(BarcodeValidatorOptions.class);
        List<Validation> validations = Validation.getValidations();

        for (Validation validation : Validation.getValidations()) {
            ValidationOptions options = validation.getOptions();
            validationOptions.addChildOptions(options.getIdentifier(), options.getLabel(), options.getDescription(), options, true);
        }

        if (!validations.isEmpty()) {
            validationOptions.addChildOptionsPageChooser("validationChooser", "Validation steps: ", Collections.<String>emptyList(), PageChooserType.BUTTONS, true);
        }

        addCollapsibleChildOptions(VALIDATION_OPTIONS_NAME, "Validation", "", validationOptions, false, true);
    }

    private void addAssemblyOptions() {
        addCollapsibleChildOptions(ASSEMBLY_OPTIONS_NAME, "Assembly", "", new CAP3Options(BarcodeValidatorOptions.class), false, true);
    }
}
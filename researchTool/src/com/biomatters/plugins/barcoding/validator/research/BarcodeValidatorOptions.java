package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.plugins.barcoding.validator.validation.SlidingWindowValidationOptions;
import com.biomatters.plugins.barcoding.validator.validation.assembly.Cap3AssemblerOptions;
import com.biomatters.plugins.barcoding.validator.validation.input.InputOptions;
import com.biomatters.plugins.barcoding.validator.validation.trimming.ErrorProbabilityOptions;

/**
 * @author Gen Li
 *         Created on 3/09/14 2:42 PM
 */
public class BarcodeValidatorOptions extends Options {
    private static final String INPUT_OPTIONS_NAME            = "input";
    private static final String TRIMMING_OPTIONS_NAME         = "trimming";
    private static final String ASSEMBLY_OPTIONS_NAME         = "assembly";
    private static final String TRACE_VALIDATION_OPTIONS_NAME = "traceValidation";

    public BarcodeValidatorOptions(Class cls) {
        super(cls);

        addInputOptions();

        addTrimmingOptions();

        addTraceValidationOptions();

        addAssemblyOptions();
    }

    public InputOptions getInputOptions() {
        return (InputOptions)getChildOptions().get(INPUT_OPTIONS_NAME);
    }

    public ErrorProbabilityOptions getTrimmingOptions() {
        return (ErrorProbabilityOptions)getChildOptions().get(TRIMMING_OPTIONS_NAME);
    }

    public SlidingWindowValidationOptions getTraceValidationOptions() {
        return (SlidingWindowValidationOptions)getChildOptions().get(TRACE_VALIDATION_OPTIONS_NAME);
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

    private void addTraceValidationOptions() {
        addCollapsibleChildOptions(TRACE_VALIDATION_OPTIONS_NAME,
                                   "Trace validation",
                                   "",
                                   new SlidingWindowValidationOptions(),
                                   false,
                                   true);
    }

    private void addAssemblyOptions() {
        addCollapsibleChildOptions(ASSEMBLY_OPTIONS_NAME, "Assembly", "", new Cap3AssemblerOptions(), false, true);
    }
}
package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.plugins.barcoding.validator.validation.assembly.Cap3AssemblerOptions;
import com.biomatters.plugins.barcoding.validator.validation.input.InputSplitterOptions;
import com.biomatters.plugins.barcoding.validator.validation.trimming.ErrorProbabilityOptions;

/**
 * @author Gen Li
 *         Created on 3/09/14 2:42 PM
 */
public class BarcodeValidatorOptions extends Options {
    private static final String INPUT_OPTIONS_NAME    = "input";
    private static final String TRIMMING_OPTIONS_NAME = "trimming";
    private static final String ASSEMBLY_OPTIONS_NAME = "assembly";

    public BarcodeValidatorOptions() {
        super(BarcodeValidatorOptions.class);

        addInputOptions();

        addTrimmingOptions();

        addAssemblyOptions();
    }

    public InputSplitterOptions getInputOptions() {
        return (InputSplitterOptions)getChildOptions().get(INPUT_OPTIONS_NAME);
    }

    public ErrorProbabilityOptions getTrimmingOptions() {
        return (ErrorProbabilityOptions)getChildOptions().get(TRIMMING_OPTIONS_NAME);
    }

    public Cap3AssemblerOptions getAssemblyOptions() {
        return (Cap3AssemblerOptions)getChildOptions().get(ASSEMBLY_OPTIONS_NAME);
    }

    private void addInputOptions() {
        addCollapsibleChildOptions(INPUT_OPTIONS_NAME, "Input", "", new InputSplitterOptions(), false, false);
    }

    private void addTrimmingOptions() {
        addCollapsibleChildOptions(TRIMMING_OPTIONS_NAME, "Trimming", "", new ErrorProbabilityOptions(), false, true);
    }

    private void addAssemblyOptions() {
        addCollapsibleChildOptions(ASSEMBLY_OPTIONS_NAME, "Assembly", "", new Cap3AssemblerOptions(), false, true);
    }
}
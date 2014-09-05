package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.plugins.barcoding.validator.research.assembly.Cap3AssemblerOptions;
import com.biomatters.plugins.barcoding.validator.research.input.InputSplitterOptions;
import com.biomatters.plugins.barcoding.validator.research.trimming.ErrorProbabilityOptions;

/**
 * @author Gen Li
 *         Created on 3/09/14 2:42 PM
 */
public class BarcodeValidatorOptions extends Options {
    public BarcodeValidatorOptions() {
        super(BarcodeValidatorOptions.class);

        addInputOptions();

        addTrimmingOptions();

        addAssemblyOptions();
    }

    private void addInputOptions() {
        addCollapsibleChildOptions("input", "Input", "", new InputSplitterOptions(), false, false);
    }

    private void addTrimmingOptions() {
        addCollapsibleChildOptions("trim", "Trimming", "", new ErrorProbabilityOptions(), false, true);
    }

    private void addAssemblyOptions() {
        addCollapsibleChildOptions("assembly", "Assembly", "", new Cap3AssemblerOptions(), false, true);
    }
}
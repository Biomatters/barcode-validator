package com.biomatters.plugins.barcoding.validator.research.options;

import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.plugins.barcoding.validator.research.BarcodeValidatorMockupPlugin;

import java.util.Arrays;

/**
 * @author Matthew Cheung
 *         Created on 22/07/14 1:42 PM
 */
public class ConsensusOptions extends Options {

    public ConsensusOptions() {
        super(BarcodeValidatorMockupPlugin.class);

        OptionValue high = new OptionValue("high", "Highest Quality");
        OptionValue hundred = new OptionValue("100", "100% - Identical");
        OptionValue ninety = new OptionValue("90", "90%");
        OptionValue fifty = new OptionValue("50", "50%");

        addComboBoxOption("threshold", "Threshold:", Arrays.asList(high, hundred, ninety, fifty), high);
        addBooleanOption("ignoreGaps", "Ignore Gaps", false);
    }
}

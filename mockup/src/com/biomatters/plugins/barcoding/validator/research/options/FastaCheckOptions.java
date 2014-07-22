package com.biomatters.plugins.barcoding.validator.research.options;

import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.plugins.barcoding.validator.research.BarcodeValidatorMockupPlugin;

import java.util.Arrays;

/**
 * @author Matthew Cheung
 *         Created on 22/07/14 1:48 PM
 */
public class FastaCheckOptions extends Options {

    public FastaCheckOptions() {
        super(BarcodeValidatorMockupPlugin.class);

        OptionValue exactValue = new OptionValue("exact", "Exactly");
        OptionValue ninetyPercentValue = new OptionValue("90%", "90% similar");

        addBooleanOption("enable", "Check FASTA matches generated barcode", true ) ;
        addComboBoxOption("matches", "FASTA matches generated barcode:", Arrays.asList(exactValue, ninetyPercentValue), exactValue);

    }
}

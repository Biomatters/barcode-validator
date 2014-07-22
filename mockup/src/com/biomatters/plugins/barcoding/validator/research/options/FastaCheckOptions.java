package com.biomatters.plugins.barcoding.validator.research.options;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.plugin.Icons;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.plugins.barcoding.validator.research.BarcodeValidatorMockupPlugin;
import com.biomatters.plugins.barcoding.validator.research.Utilities;

import java.util.Arrays;

/**
 * @author Matthew Cheung
 *         Created on 22/07/14 1:48 PM
 */
public class FastaCheckOptions extends Options {

    public FastaCheckOptions() {
        super(BarcodeValidatorMockupPlugin.class);

        Utilities.addQuestionToOptions(this, "How should the sequence be checked against the generated FASTA?");

        OptionValue exactValue = new OptionValue("exact", "Exactly");
        OptionValue ninetyPercentValue = new OptionValue("90%", "90% similar");

        addBooleanOption("enable", "Check FASTA matches generated barcode", true ) ;
        addComboBoxOption("matches", "FASTA matches generated barcode:", Arrays.asList(exactValue, ninetyPercentValue), exactValue);

    }
}

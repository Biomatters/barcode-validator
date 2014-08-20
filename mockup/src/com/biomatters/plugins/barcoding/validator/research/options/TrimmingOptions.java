package com.biomatters.plugins.barcoding.validator.research.options;

import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.plugins.barcoding.validator.research.BarcodeValidatorMockupPlugin;
import com.biomatters.plugins.barcoding.validator.research.Utilities;

/**
 * @author Matthew Cheung
 *         Created on 15/07/14 3:48 PM
 */
public class TrimmingOptions extends Options {
    public TrimmingOptions() {
        super(BarcodeValidatorMockupPlugin.class);

        Utilities.addNoteToOptions(this, "Also include primer trimming");


        beginAlignHorizontally(null, false);
        BooleanOption useLow = addBooleanOption("useLow", "", true);
        IntegerOption maxLowQualityBases = addIntegerOption("low", "Maximum low quality bases:", 0);
        useLow.addDependent(maxLowQualityBases, true);
        endAlignHorizontally();

        beginAlignHorizontally(null, false);
        BooleanOption useAmbig = addBooleanOption("useAmbig", "", true);
        IntegerOption maxAmbig = addIntegerOption("ambig", "Maximum ambiguities:", 2);
        useAmbig.addDependent(maxAmbig, true);
        endAlignHorizontally();
    }
}

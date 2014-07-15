package com.biomatters.plugins.barcoding.validator.research.options;

import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.plugins.barcoding.validator.research.BarcodeValidatorMockupPlugin;

/**
 * @author Matthew Cheung
 *         Created on 15/07/14 3:48 PM
 */
public class TrimmingOptions extends Options {
    public TrimmingOptions() {
        super(BarcodeValidatorMockupPlugin.class);

        beginAlignHorizontally(null, false);
        BooleanOption useLow = addBooleanOption("useLow", "", false);
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

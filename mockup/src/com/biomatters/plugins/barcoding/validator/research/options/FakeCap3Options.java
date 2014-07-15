package com.biomatters.plugins.barcoding.validator.research.options;

import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.plugins.barcoding.validator.research.BarcodeValidatorMockupPlugin;

/**
 * @author Matthew Cheung
 *         Created on 15/07/14 3:56 PM
 */
public class FakeCap3Options extends Options {

    public FakeCap3Options() {
        super(BarcodeValidatorMockupPlugin.class);
        IntegerOption o = addIntegerOption("o", "Min overlap length:", 40, 16, 1000);
        o.setUnits("bp");
        o.setDescription("Minimum length of overlap between reads for them to be assembled");

        IntegerOption p = addIntegerOption("p", "Min overlap identity:", 90, 66, 1000);
        p.setUnits("%");
        p.setDescription("Minimum identity of the overlap between reads for them to be assembled");
    }
}

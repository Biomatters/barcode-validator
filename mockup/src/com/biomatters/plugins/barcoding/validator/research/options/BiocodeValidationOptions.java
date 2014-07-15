package com.biomatters.plugins.barcoding.validator.research.options;

import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.plugins.barcoding.validator.research.BarcodeValidatorMockupPlugin;

/**
 * @author Matthew Cheung
 *         Created on 16/07/14 12:41 AM
 */
public class BiocodeValidationOptions extends Options {

    public BiocodeValidationOptions() {
        super(BarcodeValidatorMockupPlugin.class);

        addIntegerOption("minDepth", "Minimum Depth:", 100).setUnits("cm");
    }
}

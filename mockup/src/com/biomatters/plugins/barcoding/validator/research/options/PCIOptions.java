package com.biomatters.plugins.barcoding.validator.research.options;

import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.plugins.barcoding.validator.research.BarcodeValidatorMockupPlugin;

/**
 * @author Matthew Cheung
 *         Created on 22/07/14 1:37 PM
 */
public class PCIOptions extends Options {

    public PCIOptions() {
        super(BarcodeValidatorMockupPlugin.class);

        addIntegerOption("something", "A Setting:", 100);
    }
}

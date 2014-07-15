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

        addIntegerOption("test", "Test:", 1);
    }
}

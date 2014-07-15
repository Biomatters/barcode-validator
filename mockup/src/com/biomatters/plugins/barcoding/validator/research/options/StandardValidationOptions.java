package com.biomatters.plugins.barcoding.validator.research.options;

import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.plugins.barcoding.validator.research.BarcodeValidatorMockupPlugin;

/**
 * @author Matthew Cheung
 *         Created on 16/07/14 12:43 AM
 */
public class StandardValidationOptions extends Options {

    public StandardValidationOptions() {
        super(BarcodeValidatorMockupPlugin.class);

        addStringOption("family", "Required Family:", "");
    }
}

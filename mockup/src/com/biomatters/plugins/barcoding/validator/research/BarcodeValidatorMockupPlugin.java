package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.plugin.DocumentOperation;
import com.biomatters.geneious.publicapi.plugin.GeneiousPlugin;

/**
 * @author Matthew Cheung
 *         Created on 15/07/14 2:57 PM
 */
public class BarcodeValidatorMockupPlugin extends GeneiousPlugin {
    @Override
    public String getName() {
        return "Mockup";
    }

    @Override
    public String getDescription() {
        return "Mockup for Barcode Validator Research Tool";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public String getAuthors() {
        return "Biomatters Ltd";
    }

    @Override
    public String getVersion() {
        return "0.0";
    }

    @Override
    public String getMinimumApiVersion() {
        return "4.71";
    }

    @Override
    public int getMaximumApiVersion() {
        return 4;
    }

    @Override
    public DocumentOperation[] getDocumentOperations() {
        return new DocumentOperation[]{new BarcodeValidatorMockOperation()};
    }
}
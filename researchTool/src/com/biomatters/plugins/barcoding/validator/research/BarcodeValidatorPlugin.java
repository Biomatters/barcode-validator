package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.plugin.DocumentOperation;
import com.biomatters.geneious.publicapi.plugin.GeneiousPlugin;

/**
 * @author Gen Li
 *         Created on 20/08/14 3:06 PM
 */
public class BarcodeValidatorPlugin extends GeneiousPlugin {

    private static final String PLUGIN_VERSION = "0.0.0";

    @Override
    public String getName() {
        return "Barcode Validator";
    }

    @Override
    public String getDescription() {
        return "Description";
    }

    @Override
    public String getHelp() {
        return "Help";
    }

    @Override
    public String getAuthors() {
        return "Biomatters Ltd";
    }

    @Override
    public String getVersion() {
        return PLUGIN_VERSION;
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
        return new DocumentOperation[]{new BarcodeValidatorOperation()};
    }
}
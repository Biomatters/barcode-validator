package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.plugin.DocumentOperation;
import com.biomatters.geneious.publicapi.plugin.GeneiousPlugin;

/**
 * @author Matthew Cheung
 *         Created on 18/08/14 10:46 AM
 */
public class BarcodeValidatorResearchToolPlugin extends GeneiousPlugin {

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
        return new DocumentOperation[] {
                new BiocodeValidatorResearchToolOperation()
        };
    }
}

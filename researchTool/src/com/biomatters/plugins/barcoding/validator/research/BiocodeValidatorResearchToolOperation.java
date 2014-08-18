package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.plugin.*;
import jebl.util.ProgressListener;

import java.util.List;

/**
 * @author Gen Li
 *         Created on 18/08/14 12:48 PM
 */
public class BiocodeValidatorResearchToolOperation extends DocumentOperation {

    @Override
    public GeneiousActionOptions getActionOptions() {
        return new GeneiousActionOptions("Barcode Validator").setInMainToolbar(true);
    }

    @Override
    public String getHelp() {
        return "Help";
    }

    @Override
    public DocumentSelectionSignature[] getSelectionSignatures() {
        return new DocumentSelectionSignature[0];
    }

    @Override
    public Options getOptions(DocumentOperationInput operationInput) throws DocumentOperationException {
        return new BarcodeValidatorResearchToolOptions();
    }
}
package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.plugin.*;
import jebl.util.ProgressListener;

import javax.swing.*;
import java.net.URL;
import java.util.List;

/**
 * @author Gen Li
 *         Created on 20/08/14 3:11 PM
 */
public class BarcodeValidatorOperation extends DocumentOperation {
    @Override
    public GeneiousActionOptions getActionOptions() {
        URL icon = BarcodeValidatorOperation.class.getResource("barcodeTick24.png");
        return new GeneiousActionOptions("Barcode Validator", "", new Icons(new ImageIcon(icon)))
                .setInMainToolbar(true)
                .setMainMenuLocation(GeneiousActionOptions.MainMenu.Tools);
    }

    @Override
    public String getHelp() { return null; }

    @Override
    public DocumentSelectionSignature[] getSelectionSignatures() {
        return new DocumentSelectionSignature[0];
    }

    @Override
    public List<AnnotatedPluginDocument> performOperation(AnnotatedPluginDocument[] annotatedDocuments,
                                                          ProgressListener progressListener,
                                                          Options options) throws DocumentOperationException {
        return null;
    }
}
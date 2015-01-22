package com.biomatters.plugins.barcoding.validator.research.report;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentSelectionSignature;
import com.biomatters.geneious.publicapi.plugin.DocumentViewer;
import com.biomatters.geneious.publicapi.plugin.DocumentViewerFactory;
import com.biomatters.plugins.barcoding.validator.output.PCICalculatorReportDocument;

/**
 * @author Frank Lee
 *         Created on 20/01/15 5:14 PM
 */

public class PCICalculatorReportViewerFactory extends DocumentViewerFactory {
    @Override
    public String getName() {
        return "PCI Calculator Summary";
    }

    @Override
    public String getDescription() {
        return "Displays a summary of PCI Calculator reports.";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public DocumentSelectionSignature[] getSelectionSignatures() {
        return new DocumentSelectionSignature[] {
                new DocumentSelectionSignature(PCICalculatorReportDocument.class, 1, 1)
        };
    }

    @Override
    public DocumentViewer createViewer(AnnotatedPluginDocument[] annotatedPluginDocuments) {
        assert(annotatedPluginDocuments.length == 1);
        if(!(PCICalculatorReportDocument.class.isAssignableFrom(annotatedPluginDocuments[0].getDocumentClass()))) {
            throw new IllegalArgumentException("Document passed into createViewer() did not match " +
                    "DocumentSelectionSignature for this DocumentViewerFactory");
        }
        return new PCICalculatorReportViewer((PCICalculatorReportDocument)annotatedPluginDocuments[0].getDocumentOrNull());
    }
}

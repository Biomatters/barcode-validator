package com.biomatters.plugins.barcoding.validator.research.report;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentSelectionSignature;
import com.biomatters.geneious.publicapi.plugin.DocumentViewer;
import com.biomatters.geneious.publicapi.plugin.DocumentViewerFactory;
import com.biomatters.plugins.barcoding.validator.output.ValidationReportDocument;

/**
 * Provides {@link com.biomatters.plugins.barcoding.validator.research.report.ValidationReportViewer}.
 *
 * @author Matthew Cheung
 *         Created on 2/10/14 10:02 AM
 */
public class ValidationReportViewerFactory extends DocumentViewerFactory {
    @Override
    public String getName() {
        return "Validation Report";
    }

    @Override
    public String getDescription() {
        return "Displays reports generated from running the Barcode Validator Research Tool";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public DocumentSelectionSignature[] getSelectionSignatures() {
        return new DocumentSelectionSignature[] {
                new DocumentSelectionSignature(ValidationReportDocument.class, 1, 1)
        };
    }

    @Override
    public DocumentViewer createViewer(AnnotatedPluginDocument[] annotatedPluginDocuments) {
        assert(annotatedPluginDocuments.length == 1);
        if(!(ValidationReportDocument.class.isAssignableFrom(annotatedPluginDocuments[0].getDocumentClass()))) {
            throw new IllegalArgumentException("Document passed into createViewer() did not match " +
                    "DocumentSelectionSignature for this DocumentViewerFactory");
        }
        return new ValidationReportViewer((ValidationReportDocument)annotatedPluginDocuments[0].getDocumentOrNull());
    }
}

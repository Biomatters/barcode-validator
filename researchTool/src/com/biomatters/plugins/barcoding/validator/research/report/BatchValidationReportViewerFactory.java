package com.biomatters.plugins.barcoding.validator.research.report;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentSelectionSignature;
import com.biomatters.geneious.publicapi.plugin.DocumentViewer;
import com.biomatters.geneious.publicapi.plugin.DocumentViewerFactory;
import com.biomatters.plugins.barcoding.validator.output.ValidationReportDocument;

/**
 * A viewer that displays a summary of multiple {@link com.biomatters.plugins.barcoding.validator.output.ValidationReportDocument}.
 * Will warn the user if the selection contains reports on different input.
 *
 * @author Matthew Cheung
 *         Created on 4/11/14 11:32 AM
 */
public class BatchValidationReportViewerFactory extends DocumentViewerFactory {
    @Override
    public String getName() {
        return "Overall Validation Summary";
    }

    @Override
    public String getDescription() {
        return "Displays a summary of multiple validation reports.";
    }

    @Override
    public String getHelp() {
        return "Select two or more Validation Reports to view a summary.";
    }

    @Override
    public DocumentSelectionSignature[] getSelectionSignatures() {
        return new DocumentSelectionSignature[] {
                new DocumentSelectionSignature(new DocumentSelectionSignature.DocumentSelectionSignatureAtom(
                        ValidationReportDocument.class, 2, Integer.MAX_VALUE
                ))
        };
    }

    @Override
    public DocumentViewer createViewer(AnnotatedPluginDocument[] annotatedPluginDocuments) {
        return new BatchValidationReportViewer(annotatedPluginDocuments);
    }
}

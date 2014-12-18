package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.plugin.DocumentOperation;
import com.biomatters.geneious.publicapi.plugin.DocumentViewerFactory;
import com.biomatters.geneious.publicapi.plugin.GeneiousPlugin;
import com.biomatters.plugins.barcoding.validator.research.report.BatchValidationReportViewerFactory;
import com.biomatters.plugins.barcoding.validator.research.report.ValidationReportViewerFactory;

/**
 * @author Gen Li
 *         Created on 20/08/14 3:06 PM
 */
public class BarcodeValidatorPlugin extends GeneiousPlugin {
    private static final String PLUGIN_VERSION = "1.0.1";

    @Override
    public String getName() {
        return "Barcode Validator";
    }

    @Override
    public String getDescription() {
        return "Runs batch validation on sets of traces and barcode sequences against user specified parameters.";
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
        return PLUGIN_VERSION;
    }

    @Override
    public String getMinimumApiVersion() {
        return "4.710";
    }

    @Override
    public int getMaximumApiVersion() {
        return 4;
    }

    @Override
    public DocumentOperation[] getDocumentOperations() {
        return new DocumentOperation[] { new BarcodeValidatorOperation() };
    }

    @Override
    public DocumentViewerFactory[] getDocumentViewerFactories() {
        return new DocumentViewerFactory[] {
                new ValidationReportViewerFactory(),
                new BatchValidationReportViewerFactory()
        };
    }
}
package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.documents.*;


/**
 * @author Matthew Cheung
 *         Created on 19/08/14 4:26 PM
 */
public class MockupReport extends AbstractPluginDocument {

    public MockupReport() {
    }

    public MockupReport(String name) {
        setFieldValue(DocumentField.NAME_FIELD, name);
    }

    @Override
    public String getName() {
        return String.valueOf(getFieldValue(DocumentField.NAME_FIELD.getCode()));
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String toHTML() {
        return "<h1>Report Mockup</h1>" +
                "This is a report!";
    }
}

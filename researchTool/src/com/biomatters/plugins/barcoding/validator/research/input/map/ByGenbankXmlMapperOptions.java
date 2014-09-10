package com.biomatters.plugins.barcoding.validator.research.input.map;

import com.biomatters.plugins.barcoding.validator.research.BarcodeValidatorPlugin;

/**
 * @author Gen Li
 *         Created on 5/09/14 9:57 AM
 */
public class ByGenbankXmlMapperOptions extends BarcodesToTracesMapperOptions {
    private static final String XML_FILE_SELECTION_OPTION_NAME = "xmlFile";
    public ByGenbankXmlMapperOptions() {
        super(BarcodeValidatorPlugin.class);

        addFileSelectionOption(XML_FILE_SELECTION_OPTION_NAME, "XML File: ", "");
    }

    public String getBoldListFilePath() {
        return (String)getOption(XML_FILE_SELECTION_OPTION_NAME).getValue();
    }
}
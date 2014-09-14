package com.biomatters.plugins.barcoding.validator.validation.input.map;

import com.biomatters.plugins.barcoding.validator.validation.input.InputSplitterOptions;

/**
 * @author Gen Li
 *         Created on 5/09/14 9:57 AM
 */
public class GenbankXmlMapperOptions extends BarcodesToTracesMapperOptions {
    private static final String XML_FILE_SELECTION_OPTION_NAME = "xmlFile";
    public GenbankXmlMapperOptions() {
        super(InputSplitterOptions.class);

        addFileSelectionOption(XML_FILE_SELECTION_OPTION_NAME, "XML File: ", "");
    }

    public String getBoldListFilePath() {
        return (String)getOption(XML_FILE_SELECTION_OPTION_NAME).getValue();
    }
}
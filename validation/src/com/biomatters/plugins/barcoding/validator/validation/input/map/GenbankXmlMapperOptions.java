package com.biomatters.plugins.barcoding.validator.validation.input.map;

/**
 * @author Gen Li
 *         Created on 5/09/14 9:57 AM
 */
public class GenbankXmlMapperOptions extends BarcodesToTracesMapperOptions {
    private FileSelectionOption genbankXMLFilePathOption;

    public GenbankXmlMapperOptions(Class cls) {
        super(cls);

        genbankXMLFilePathOption = addFileSelectionOption("xmlFile", "XML File: ", "");
    }

    public String getGenbankXMLFilePath() {
        return genbankXMLFilePathOption.getValue();
    }
}
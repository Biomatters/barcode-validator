package com.biomatters.plugins.barcoding.validator.validation.input.map;

/**
 * @author Gen Li
 *         Created on 5/09/14 9:57 AM
 */
public class GenbankXmlMapperOptions extends BarcodesToTracesMapperOptions {
    private FileSelectionOption genbankXMLFilePathOption;
    private NamePartOption barcodeNamePartOption;
    private NameSeparatorOption barcodeNameSeparatorOption;

    public GenbankXmlMapperOptions(Class cls) {
        super(cls);

        genbankXMLFilePathOption = addFileSelectionOption("xmlFile", "XML File: ", "");
        addNameOptions();
    }

    public String getGenbankXMLFilePath() {
        return genbankXMLFilePathOption.getValue();
    }

    private void addNameOptions() {
        beginAlignHorizontally(null, false);

        barcodeNamePartOption = addCustomOption(new NamePartOption("accessionPart", "Accession is ", 3));
        barcodeNameSeparatorOption = addCustomOption(new NameSeparatorOption("accessionSeparator", "part of sequence names separated by ", 2));

        endAlignHorizontally();
    }

    public int getBarcodeNamePart() {
        return barcodeNamePartOption.getPart();
    }

    public String getBarcodeNameSeparator() {
        return barcodeNameSeparatorOption.getSeparatorString();
    }
}
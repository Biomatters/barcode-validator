package com.biomatters.plugins.barcoding.validator.research.input;

import com.biomatters.geneious.publicapi.documents.XMLSerializable;
import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.plugins.barcoding.validator.research.BarcodeValidatorPlugin;
import org.jdom.Element;

import javax.swing.*;

/**
 * @author Matthew Cheung
 *         Created on 15/07/14 4:09 PM
 */
public class InputFileOptions extends Options {
    private final String FILE_SELECTION_OPTION_NAME = "fileInput";

    private String label;

    public InputFileOptions(String label) {
        super(BarcodeValidatorPlugin.class);
        this.label = label;
        beginAlignHorizontally(null, false);
        addFileSelectionOption(FILE_SELECTION_OPTION_NAME, this.label, "").setSelectionType(JFileChooser.FILES_ONLY);
        endAlignHorizontally();
    }

    public InputFileOptions(Element element) throws XMLSerializationException {
        this(element.getText());
    }

    public String getFilePath() {
        return getOption(FILE_SELECTION_OPTION_NAME).getValueAsString();
    }

    @Override
    public Element toXML() {
        return new Element(XMLSerializable.ROOT_ELEMENT_NAME).setText(label);
    }
}
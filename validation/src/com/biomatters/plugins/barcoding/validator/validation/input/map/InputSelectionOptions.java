package com.biomatters.plugins.barcoding.validator.validation.input.map;

import com.biomatters.geneious.publicapi.documents.XMLSerializable;
import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import com.biomatters.geneious.publicapi.plugin.Options;
import org.jdom.Element;

import javax.swing.*;

/**
 * @author Matthew Cheung
 *         Created on 15/07/14 4:09 PM
 */
public class InputSelectionOptions extends Options {
    private final String INPUT_SELECTION_OPTION_NAME = "input";

    private String label;

    public InputSelectionOptions(String label) {
        super(com.biomatters.plugins.barcoding.validator.validation.input.InputOptions.class);
        this.label = label;
        beginAlignHorizontally(null, false);
        addFileSelectionOption(INPUT_SELECTION_OPTION_NAME, this.label, "").setSelectionType(JFileChooser.FILES_AND_DIRECTORIES);
        endAlignHorizontally();
    }

    public InputSelectionOptions(Element element) throws XMLSerializationException {
        this(element.getText());
    }

    public String getFilePath() {
        return getOption(INPUT_SELECTION_OPTION_NAME).getValueAsString();
    }

    @Override
    public Element toXML() {
        return new Element(XMLSerializable.ROOT_ELEMENT_NAME).setText(label);
    }
}
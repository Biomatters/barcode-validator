package com.biomatters.plugins.barcoding.validator.research.options;

import com.biomatters.geneious.publicapi.documents.XMLSerializable;
import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.plugins.barcoding.validator.research.BarcodeValidatorMockupPlugin;
import org.jdom.Element;

import javax.swing.*;

/**
 * @author Matthew Cheung
 *         Created on 15/07/14 4:09 PM
 */
public class InputFileOptions extends Options {

    private String label;

    public InputFileOptions(String label) {
        super(BarcodeValidatorMockupPlugin.class);
        this.label = label;
        beginAlignHorizontally(null, false);
        addFileSelectionOption("input", this.label, "").setSelectionType(JFileChooser.FILES_ONLY);
        endAlignHorizontally();
    }

    public InputFileOptions(Element element) throws XMLSerializationException {
        this(element.getText());
    }

    @Override
    public Element toXML() {
        return new Element(XMLSerializable.ROOT_ELEMENT_NAME).setText(label);
    }
}

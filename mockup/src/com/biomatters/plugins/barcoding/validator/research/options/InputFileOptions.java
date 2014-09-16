package com.biomatters.plugins.barcoding.validator.research.options;

import com.biomatters.geneious.publicapi.documents.XMLSerializable;
import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.plugins.barcoding.validator.research.BarcodeValidatorMockupPlugin;
import org.jdom.Element;

import javax.swing.*;
import java.util.Arrays;

/**
 * @author Matthew Cheung
 *         Created on 15/07/14 4:09 PM
 */
public class InputFileOptions extends Options {

    private String label;
    private boolean includeDirection;

    public InputFileOptions(String label) {
        this(label, false);
    }

    public InputFileOptions(String label, boolean includeDirection) {
        super(BarcodeValidatorMockupPlugin.class);
        this.includeDirection = includeDirection;
        this.label = label;
        beginAlignHorizontally(null, false);
        if(includeDirection) {
            OptionValue forward = new OptionValue("forward", "Forward");
            OptionValue reverse = new OptionValue("reverse", "Reverse");
            addComboBoxOption("direction", "", Arrays.asList(forward, reverse), forward);
        }
        addFileSelectionOption("input", this.label, "").setSelectionType(JFileChooser.FILES_ONLY);
        endAlignHorizontally();
    }

    public InputFileOptions(Element element) throws XMLSerializationException {
        this(element.getText(), Boolean.valueOf(element.getAttributeValue(DIR)));
    }

    private static final String DIR = "includeDirection";

    @Override
    public Element toXML() {
        return new Element(XMLSerializable.ROOT_ELEMENT_NAME).setText(label).setAttribute(DIR, String.valueOf(includeDirection));
    }
}

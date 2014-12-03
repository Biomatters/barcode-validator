package com.biomatters.plugins.barcoding.validator.validation.input.map;

import com.biomatters.geneious.publicapi.documents.XMLSerializable;
import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.geneious.publicapi.utilities.StringUtilities;
import org.jdom.Element;

import javax.swing.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Matthew Cheung
 *         Created on 15/07/14 4:09 PM
 */
public class InputSelectionOptions extends Options {
    private final static String SEPERATOR = ",";
    private final static String EXT_ATTR = "extensions";

    private FileSelectionOption inputSelectionOption;
    private String label;
    private Set<String> extensions;

    public InputSelectionOptions(String label) {
        this(label, null);
    }

    public InputSelectionOptions(String label, Set<String> extensions) {
        super(com.biomatters.plugins.barcoding.validator.validation.input.InputOptions.class);
        initialize(label, extensions);
    }

    private void initialize(String label, Set<String> exts) {
        this.label = label;
        this.extensions = exts;
        final Set<String> extsSet = exts;

        beginAlignHorizontally(null, false);

        if (exts == null || exts.size() == 0) {
            inputSelectionOption = addFileSelectionOption("input", this.label, "");
        } else {
            inputSelectionOption = addFileSelectionOption("input", this.label, "", new String[0], "Browse", new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if (new File(dir, name).isDirectory()) {
                        return true;
                    }

                    for (String ext : extsSet) {
                        if (name.toLowerCase().endsWith("." + ext.toLowerCase()))
                            return true;
                    }

                    return false;
                }
            });
        }
        inputSelectionOption.setSelectionType(JFileChooser.FILES_AND_DIRECTORIES);

        endAlignHorizontally();
    }

    public InputSelectionOptions(Element element) throws XMLSerializationException {
        super(com.biomatters.plugins.barcoding.validator.validation.input.InputOptions.class);
        String lable = element.getText();
        String extStr = element.getAttributeValue(EXT_ATTR);

        if (extStr != null && extStr.trim().length() > 0) {
            extensions = new HashSet<String>();
            for (String tmp : extStr.split(SEPERATOR)) {
                extensions.add(tmp);
            }
        }

        initialize(lable, extensions);
    }

    public String getFilePath() {
        return inputSelectionOption.getValueAsString();
    }

    @Override
    public Element toXML() {
        Element element = new Element(XMLSerializable.ROOT_ELEMENT_NAME);
        element.setText(label);
        if (extensions != null && extensions.size() > 0)
            element.setAttribute(EXT_ATTR, StringUtilities.join(SEPERATOR, extensions));

        return element;
    }
}
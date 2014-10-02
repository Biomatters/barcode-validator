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
    private final String INPUT_SELECTION_OPTION_NAME = "input";
    private final static String SEPERATOR = ",";
    private final static String EXT_ATTR = "exts";

    private String label;
    private Set<String> exts;

    public InputSelectionOptions(String label) {
        this(label, null);
    }

    public InputSelectionOptions(String label, Set<String> exts) {
        super(com.biomatters.plugins.barcoding.validator.validation.input.InputOptions.class);
        initialize(label, exts);
    }

    private void initialize(String label, Set<String> exts) {
        this.label = label;
        this.exts = exts;
        final Set<String> extsSet = exts;

        beginAlignHorizontally(null, false);

        if (exts == null || exts.size() == 0) {
            addFileSelectionOption(INPUT_SELECTION_OPTION_NAME, this.label, "").setSelectionType(JFileChooser.FILES_AND_DIRECTORIES);
        } else {
            addFileSelectionOption(INPUT_SELECTION_OPTION_NAME, this.label, "", new String[0], "Browse", new FilenameFilter() {
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
            }).setSelectionType(JFileChooser.FILES_AND_DIRECTORIES);
        }
        endAlignHorizontally();
    }

    public InputSelectionOptions(Element element) throws XMLSerializationException {
        super(com.biomatters.plugins.barcoding.validator.validation.input.InputOptions.class);
        String lable = element.getText();
        String extStr = element.getAttributeValue(EXT_ATTR);

        if (extStr != null && extStr.trim().length() > 0) {
            exts = new HashSet<String>();
            for (String tmp : extStr.split(SEPERATOR)) {
                exts.add(tmp);
            }
        }

        initialize(lable, exts);
    }

    public String getFilePath() {
        return getOption(INPUT_SELECTION_OPTION_NAME).getValueAsString();
    }

    @Override
    public Element toXML() {
        Element element = new Element(XMLSerializable.ROOT_ELEMENT_NAME);
        element.setText(label);
        if (exts != null && exts.size() > 0)
            element.setAttribute(EXT_ATTR, StringUtilities.join(SEPERATOR, exts));

        return element;
    }
}
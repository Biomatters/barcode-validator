package com.biomatters.plugins.barcoding.validator.output;

import com.biomatters.geneious.publicapi.documents.*;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents the result of running the validation pipeline on a set of barcode sequences and their associated traces.
 *
 * @author Matthew Cheung
 *         Created on 30/09/14 3:03 PM
 */
public class ValidationReportDocument implements PluginDocument, PluginDocument.ReferencedDocumentsNotLoaded {

    private static final String NAME_KEY = "name";
    private static final String OUTPUT_KEY = "output";

    private String name;
    private List<ValidationOutputRecord> outputs;

    public ValidationReportDocument(String name, List<ValidationOutputRecord> outputs) {
        this.name = name;
        this.outputs = outputs;
    }

    @SuppressWarnings("UnusedDeclaration")
    public ValidationReportDocument() {
        // for de-serialization
    }

    @Override
    public void fromXML(Element element) throws XMLSerializationException {
        name = element.getChildText(NAME_KEY);
        if(name == null) {
            name = "";
        }
        outputs = new ArrayList<ValidationOutputRecord>();
        List<Element> children = element.getChildren(OUTPUT_KEY);
        for (Element child : children) {
            outputs.add(XMLSerializer.classFromXML(child, ValidationOutputRecord.class));
        }
    }

    @Override
    public Element toXML() {
        Element element = new Element(XMLSerializable.ROOT_ELEMENT_NAME);
        element.addContent(new Element(NAME_KEY).setText(name));
        for (ValidationOutputRecord output : outputs) {
            element.addContent(XMLSerializer.classToXML(OUTPUT_KEY, output));
        }
        return element;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return "Report from validation of " + outputs.size() + " barcode sequences and associated traces";
    }

    @Override
    public List<DocumentField> getDisplayableFields() {
        return null;
    }

    @Override
    public Object getFieldValue(String s) {
        return null;
    }

    @Override
    public URN getURN() {
        return null;
    }

    @Override
    public Date getCreationDate() {
        return null;
    }

    @Override
    public String toHTML() {
        return null;
    }
}

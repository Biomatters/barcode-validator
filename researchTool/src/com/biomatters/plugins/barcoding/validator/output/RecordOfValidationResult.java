package com.biomatters.plugins.barcoding.validator.output;

import com.biomatters.geneious.publicapi.documents.*;
import com.biomatters.plugins.barcoding.validator.validation.ValidationOptions;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a record of the results of a {@link com.biomatters.plugins.barcoding.validator.validation.Validation} task.
 * Implements the {@link com.biomatters.geneious.publicapi.documents.XMLSerializable} interface so it can easily be
 * persisted to XML using {@link com.biomatters.geneious.publicapi.documents.XMLSerializer}.
 *
 * @author Matthew Cheung
 *         Created on 30/09/14 5:04 PM
 */
public class RecordOfValidationResult implements XMLSerializable {

    private ValidationOptions options;

    private boolean passed;
    private String message;
    private List<URN> docs = new ArrayList<URN>();

    public RecordOfValidationResult(ValidationOptions options, boolean passed, String message, List<URN> docs) {
        this.options = options;
        this.passed = passed;
        this.message = message;
        this.docs = docs;
    }

    private static final String OPTIONS = "optionValues";

    private static final String PASSED = "passed";
    private static final String MESSAGE = "message";
    private static final String DOCS = "doc";

    @SuppressWarnings("UnusedDeclaration")
    public RecordOfValidationResult(Element element) throws XMLSerializationException {
        Element optionsElement = element.getChild(OPTIONS);
        if(optionsElement == null) {
            throw new XMLSerializationException("Required " + OPTIONS + " child element missing from element.");
        }
        options = XMLSerializer.classFromXML(optionsElement, ValidationOptions.class);
        String passedString = element.getChildText(PASSED);
        passed = Boolean.valueOf(passedString);
        message = element.getChildText(MESSAGE);
        for (Element docElement : element.getChildren(DOCS)) {
            try {
                docs.add(URN.fromXML(docElement));
            } catch (MalformedURNException e) {
                throw new XMLSerializationException("Failed to de-serialize validation record: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public Element toXML() {
        Element root = new Element(XMLSerializable.ROOT_ELEMENT_NAME);
        root.addContent(XMLSerializer.classToXML(OPTIONS, options));
        root.addContent(new Element(PASSED).setText(Boolean.toString(passed)));
        root.addContent(new Element(MESSAGE).setText(message));
        for (URN doc : docs) {
            root.addContent(doc.toXML(DOCS));
        }
        return root;
    }

    @Override
    public void fromXML(Element element) throws XMLSerializationException {
        throw new UnsupportedOperationException("Cannot use fromXML() use RecordOfValidationResult(Element e)");
    }

    public boolean isPassed() {
        return passed;
    }

    public String getMessage() {
        return message;
    }

    public List<URN> getGeneratedDocuments() {
        return docs;
    }

    public ValidationOptions getOptions() {
        return options;
    }
}

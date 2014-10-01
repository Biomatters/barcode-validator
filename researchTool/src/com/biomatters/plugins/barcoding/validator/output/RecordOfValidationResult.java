package com.biomatters.plugins.barcoding.validator.output;

import com.biomatters.geneious.publicapi.documents.*;
import org.jdom.Element;

import java.util.List;

/**
 * Represents a record of the results of a {@link com.biomatters.plugins.barcoding.validator.validation.Validation} task.
 *
 * @author Matthew Cheung
 *         Created on 30/09/14 5:04 PM
 */
public class RecordOfValidationResult implements XMLSerializable {

    private boolean passed;
    private String message;
    private List<URN> docs;

    public RecordOfValidationResult(boolean passed, String message, List<URN> docs) {
        this.passed = passed;
        this.message = message;
        this.docs = docs;
    }

    private static final String PASSED = "passed";
    private static final String MESSAGE = "message";
    private static final String DOCS = "doc";

    public RecordOfValidationResult(Element element) throws XMLSerializationException {
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
}

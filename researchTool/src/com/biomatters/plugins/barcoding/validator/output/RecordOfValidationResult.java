package com.biomatters.plugins.barcoding.validator.output;

import com.biomatters.geneious.publicapi.documents.XMLSerializable;
import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import com.biomatters.geneious.publicapi.documents.XMLSerializer;
import com.biomatters.plugins.barcoding.validator.validation.ValidationOptions;
import com.biomatters.plugins.barcoding.validator.validation.results.ResultFact;
import org.jdom.Element;

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
    private ResultFact fact;

    public RecordOfValidationResult(ValidationOptions options, boolean passed, ResultFact fact) {
        this.options = options;
        this.passed = passed;
        this.fact = fact;
    }

    private static final String OPTIONS = "optionValues";

    private static final String PASSED = "passed";
    private static final String FACT = "fact";

    @SuppressWarnings("UnusedDeclaration")
    public RecordOfValidationResult(Element element) throws XMLSerializationException {
        Element optionsElement = element.getChild(OPTIONS);
        if(optionsElement == null) {
            throw new XMLSerializationException("Required " + OPTIONS + " child element missing from element.");
        }
        options = XMLSerializer.classFromXML(optionsElement, ValidationOptions.class);
        String passedString = element.getChildText(PASSED);
        passed = Boolean.valueOf(passedString);

        Element entryElement = element.getChild(FACT);
        if(entryElement == null) {
            throw new XMLSerializationException("Required " + FACT + " child element missing from element.");
        }
        fact = XMLSerializer.classFromXML(entryElement, ResultFact.class);
    }

    @Override
    public Element toXML() {
        Element root = new Element(XMLSerializable.ROOT_ELEMENT_NAME);
        root.addContent(XMLSerializer.classToXML(OPTIONS, options));
        root.addContent(new Element(PASSED).setText(Boolean.toString(passed)));
        root.addContent(XMLSerializer.classToXML(FACT, fact));
        return root;
    }

    @Override
    public void fromXML(Element element) throws XMLSerializationException {
        throw new UnsupportedOperationException("Cannot use fromXML() use RecordOfValidationResult(Element e)");
    }

    public boolean isPassed() {
        return passed;
    }

    public ValidationOptions getOptions() {
        return options;
    }

    public ResultFact getFact() {
        return fact;
    }
}

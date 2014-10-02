package com.biomatters.plugins.barcoding.validator.output;

import com.biomatters.geneious.publicapi.documents.*;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the output from running the Barcode Validator validation pipeline on a barcode sequence and it's
 * associated traces.
 *
 * @author Matthew Cheung
 *         Created on 30/09/14 3:04 PM
 */
public class ValidationOutputRecord implements XMLSerializable {

    private static final String BARCODE = "barcode";
    private static final String TRACE = "trace";
    private static final String TRIMMED = "trimmedTrace";
    private static final String ASSEMBLY = "contigAssembly";
    private static final String CONSENSUS = "assemblyConsensus";
    private static final String VALIDATION_RECORD = "validationRecord";

    URN barcodeSequenceUrn;
    List<URN> traceDocumentUrns = new ArrayList<URN>();

    List<URN> trimmedDocumentUrns = new ArrayList<URN>();
    URN assemblyUrn;
    URN consensusUrn;

    List<RecordOfValidationResult> validationRecords = new ArrayList<RecordOfValidationResult>();

    ValidationOutputRecord() {
    }

    public ValidationOutputRecord(Element element) throws XMLSerializationException {
        try {
            barcodeSequenceUrn = URN.fromXML(element.getChild(BARCODE));
            for (Element child : element.getChildren(TRACE)) {
                traceDocumentUrns.add(URN.fromXML(child));
            }

            for (Element child : element.getChildren(TRIMMED)) {
                trimmedDocumentUrns.add(URN.fromXML(child));
            }
            assemblyUrn = URN.fromXML(element.getChild(ASSEMBLY));
            consensusUrn = URN.fromXML(element.getChild(CONSENSUS));

            for (Element recordElement : element.getChildren(VALIDATION_RECORD)) {
                validationRecords.add(XMLSerializer.classFromXML(recordElement, RecordOfValidationResult.class));
            }
        } catch (MalformedURNException e) {
            throw new XMLSerializationException("Could not de-serialize validation record: " + e.getMessage(), e);
        }
    }

    @Override
    public Element toXML() {
        Element root = new Element(XMLSerializable.ROOT_ELEMENT_NAME);
        root.addContent(barcodeSequenceUrn.toXML(BARCODE));
        addElementsForUrnList(root, TRACE, traceDocumentUrns);

        addElementsForUrnList(root, TRIMMED, trimmedDocumentUrns);
        root.addContent(assemblyUrn.toXML(ASSEMBLY));
        root.addContent(consensusUrn.toXML(CONSENSUS));

        for (RecordOfValidationResult result : validationRecords) {
            root.addContent(XMLSerializer.classToXML(VALIDATION_RECORD, result));
        }

        return root;
    }

    public void addElementsForUrnList(Element root, String elementName, List<URN> urnList) {
        for (URN traceDocumentUrn : urnList) {
            root.addContent(traceDocumentUrn.toXML(elementName));
        }
    }

    @Override
    public void fromXML(Element element) throws XMLSerializationException {
        throw new UnsupportedOperationException("Cannot use fromXML().  Use ValidationOutput(Element e)");
    }

    /**
     *
     * @return true iff all validation tasks run passed
     */
    public boolean isAllPassed() {
        for (RecordOfValidationResult result : validationRecords) {
            if(!result.isPassed()) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return The {@link URN} of the input barcode sequence.  Can be used to locate the input document.
     */
    public URN getBarcodeSequenceUrn() {
        return barcodeSequenceUrn;
    }

    public List<RecordOfValidationResult> getValidationResults() {
        return validationRecords;
    }
}

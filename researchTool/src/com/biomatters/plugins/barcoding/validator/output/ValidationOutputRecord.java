package com.biomatters.plugins.barcoding.validator.output;

import com.biomatters.geneious.publicapi.documents.*;
import org.jdom.Element;

import java.util.*;

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
    private static final String TRACE_KEY = "traceKey";
    private static final String TRACE_URN = "traceURN";
    private static final String TRIMMED = "trimmedTrace";
    private static final String TRIMMED_KEY = "trimmedTraceKey";
    private static final String TRIMMED_URN = "trimmedTraceURN";
    private static final String ASSEMBLY = "contigAssembly";
    private static final String CONSENSUS = "assemblyConsensus";
    private static final String VALIDATION_RECORD = "validationRecord";

    URN barcodeSequenceUrn;
    Map<String, URN> traceDocumentUrnsMap = new HashMap<String, URN>();

    Map<String, URN> trimmedDocumentUrnsMap = new HashMap<String, URN>();
    URN assemblyUrn;
    URN consensusUrn;

    List<RecordOfValidationResult> validationRecords = new ArrayList<RecordOfValidationResult>();

    ValidationOutputRecord() {
    }

    public ValidationOutputRecord(Element element) throws XMLSerializationException {
        try {
            barcodeSequenceUrn = URN.fromXML(element.getChild(BARCODE));
            for (Element child : element.getChildren(TRACE)) {
                String key = child.getChildText(TRACE_KEY);
                URN urn = URN.fromXML(child.getChild(TRACE_URN));
                traceDocumentUrnsMap.put(key, urn);
            }

            for (Element child : element.getChildren(TRIMMED)) {
                String key = child.getChildText(TRIMMED_KEY);
                URN urn = URN.fromXML(child.getChild(TRIMMED_URN));
                trimmedDocumentUrnsMap.put(key, urn);
            }
            assemblyUrn = getUrnFromElement(element, ASSEMBLY);
            consensusUrn = getUrnFromElement(element, CONSENSUS);

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

        for (Map.Entry<String, URN> entry: traceDocumentUrnsMap.entrySet()) {
            Element element = new Element(TRACE);
            element.addContent(new Element(TRACE_KEY).setText(entry.getKey()));
            element.addContent(entry.getValue().toXML(TRACE_URN));
            root.addContent(element);
        }

        for (Map.Entry<String, URN> entry: traceDocumentUrnsMap.entrySet()) {
            Element element = new Element(TRIMMED);
            element.addContent(new Element(TRIMMED_KEY).setText(entry.getKey()));
            element.addContent(entry.getValue().toXML(TRIMMED_URN));
            root.addContent(element);
        }

        addUrnToXml(root, assemblyUrn, ASSEMBLY);
        addUrnToXml(root, consensusUrn, CONSENSUS);

        for (RecordOfValidationResult result : validationRecords) {
            root.addContent(XMLSerializer.classToXML(VALIDATION_RECORD, result));
        }

        return root;
    }

    private static URN getUrnFromElement(Element root, String childElementName) throws MalformedURNException {
        Element childElement = root.getChild(childElementName);
        if(childElement != null) {
            return URN.fromXML(childElement);
        } else {
            return null;
        }
    }

    private static void addUrnToXml(Element root, URN urn, String elementName) {
        if(urn != null) {
            root.addContent(urn.toXML(elementName));
        }
    }

    public void addElementsForUrnList(Element root, String elementName, List<URN> urnList) {
        for (URN traceDocumentUrn : urnList) {
            root.addContent(traceDocumentUrn.toXML(elementName));
        }
    }

    public void addElementsForUrnMap(Element root, String elementName, Map<String, URN> urnMap) {
        for (Map.Entry<String, URN> entry: urnMap.entrySet()) {
            Element element = new Element(elementName);
            element.addContent(new Element(TRACE_KEY).setText(entry.getKey()));
            element.addContent(entry.getValue().toXML(TRACE_URN));
            root.addContent(element);
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

    /**
     *
     * @return The {@link URN}s of the input traces.  Can be used to locate the input trace documents.
     */
    public List<URN> getTraceDocumentUrns() {
        List<URN> ret = new ArrayList<URN>();
        for (URN urn : traceDocumentUrnsMap.values()) {
            ret.add(urn);
        }

        return Collections.unmodifiableList(ret);
    }

    public List<URN> getTrimmedDocumentUrns() {
        List<URN> ret = new ArrayList<URN>();
        for (URN urn : trimmedDocumentUrnsMap.values()) {
            ret.add(urn);
        }

        return Collections.unmodifiableList(ret);
    }

    public URN getTraceDocumentUrnByName(String name) {
        return traceDocumentUrnsMap.get(name);
    }

    public URN getgetTrimmedDocumentUrnByName(String name) {
        return trimmedDocumentUrnsMap.get(name);
    }

    public void addTraceDocumentUrns(String key, URN urn) {
        traceDocumentUrnsMap.put(key, urn);
    }

    public void getTrimmedDocumentUrns(String key, URN urn) {
        trimmedDocumentUrnsMap.put(key, urn);
    }

    public List<RecordOfValidationResult> getValidationResults() {
        return validationRecords;
    }

    public URN getAssemblyUrn() {
        return assemblyUrn;
    }
}

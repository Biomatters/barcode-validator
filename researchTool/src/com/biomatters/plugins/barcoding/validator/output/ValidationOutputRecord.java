package com.biomatters.plugins.barcoding.validator.output;

import com.biomatters.geneious.publicapi.documents.*;
import com.biomatters.plugins.barcoding.validator.validation.ValidationOptions;
import com.biomatters.plugins.barcoding.validator.validation.results.*;
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

    private static final String SETNAME = "setname";
    private static final String BARCODE = "barcode";
    private static final String TRACE = "trace";
    private static final String TRACE_KEY = "traceKey";
    private static final String TRACE_URN = "traceURN";
    private static final String TRIMMED = "trimmedTrace";
    private static final String TRIMMED_KEY = "trimmedTraceKey";
    private static final String TRIMMED_URN = "trimmedTraceURN";
    private static final String ASSEMBLY = "contigAssembly";
    private static final String CONSENSUS = "assemblyConsensus";
    private static final String VALIDATION_RESULT = "validationResult";

    private String setName;

    URN barcodeSequenceUrn;
    Map<String, URN> traceDocumentUrnsMap = new HashMap<String, URN>();

    Map<String, URN> trimmedDocumentUrnsMap = new HashMap<String, URN>();
    URN assemblyUrn;
    URN consensusUrn;

    private Map<Class, Map<URN, RecordOfValidationResult>> validationResults = new LinkedHashMap<Class, Map<URN, RecordOfValidationResult>>();
    private Map<Integer, ValidationOptions> colunmOptionsMap = null;

    ValidationOutputRecord() {
    }

    @SuppressWarnings("unused")
    public ValidationOutputRecord(Element element) throws XMLSerializationException {
        try {
            setName = element.getChildText(SETNAME);
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

            for (Element recordElement : element.getChildren(VALIDATION_RESULT)) {
                addValidationResult(XMLSerializer.classFromXML(recordElement, RecordOfValidationResult.class));
            }
        } catch (MalformedURNException e) {
            throw new XMLSerializationException("Could not de-serialize validation record: " + e.getMessage(), e);
        }
    }

    @Override
    public Element toXML() {
        Element root = new Element(XMLSerializable.ROOT_ELEMENT_NAME);
        root.addContent(new Element(SETNAME).setText(getSetName()));
        root.addContent(barcodeSequenceUrn.toXML(BARCODE));

        for (Map.Entry<String, URN> entry: traceDocumentUrnsMap.entrySet()) {
            Element element = new Element(TRACE);
            element.addContent(new Element(TRACE_KEY).setText(entry.getKey()));
            element.addContent(entry.getValue().toXML(TRACE_URN));
            root.addContent(element);
        }

        for (Map.Entry<String, URN> entry: trimmedDocumentUrnsMap.entrySet()) {
            Element element = new Element(TRIMMED);
            element.addContent(new Element(TRIMMED_KEY).setText(entry.getKey()));
            element.addContent(entry.getValue().toXML(TRIMMED_URN));
            root.addContent(element);
        }

        addUrnToXml(root, assemblyUrn, ASSEMBLY);
        addUrnToXml(root, consensusUrn, CONSENSUS);

        for (Map<URN, RecordOfValidationResult> map : validationResults.values()) {
            for (RecordOfValidationResult result : map.values()) {
                root.addContent(XMLSerializer.classToXML(VALIDATION_RESULT, result));
            }
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

    @Override
    public void fromXML(Element element) throws XMLSerializationException {
        throw new UnsupportedOperationException("Cannot use fromXML().  Use ValidationOutput(Element e)");
    }

    /**
     *
     * @return true iff all validation tasks run passed
     */
    public boolean isAllPassed() {
        for (Map<URN, RecordOfValidationResult> map : validationResults.values()) {
            for (RecordOfValidationResult result : map.values()) {
                if(!result.isPassed()) {
                    return false;
                }
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


    public void addTraceDocumentUrns(String key, URN urn) {
        traceDocumentUrnsMap.put(key, urn);
    }

    public void addTrimmedDocumentUrns(String key, URN urn) {
        trimmedDocumentUrnsMap.put(key, urn);
    }

    public List<RecordOfValidationResult> getValidationResults() {
        List<RecordOfValidationResult> ret = new ArrayList<RecordOfValidationResult>();
        for (Map<URN, RecordOfValidationResult> map : validationResults.values()) {
            for (RecordOfValidationResult result : map.values()) {
                ret.add(result);
            }
        }
        return ret;
    }

    public Map<Class, Map<URN, RecordOfValidationResult>> getValidationResultsMap() {
        return validationResults;
    }

    public URN getAssemblyUrn() {
        return assemblyUrn;
    }

    public void addValidationResult(RecordOfValidationResult result) {
        Class<? extends ResultFact> factClass = result.getFact().getClass();
        Map<URN, RecordOfValidationResult> factMap = validationResults.get(factClass);
        if (factMap == null) {
            factMap = new HashMap<URN, RecordOfValidationResult>();
            validationResults.put(factClass, factMap);
        }

        factMap.put(result.getFact().getTargetURN(), result);
    }

    public Set<URN> getDoclist() {
        Set<URN> ret = new HashSet<URN>();
        for (Map<URN, RecordOfValidationResult> entry : validationResults.values()) {
            for (URN urn : entry.keySet()) {
                ret.add(urn);
            }
        }
        return ret;
    }

    public List<ResultColumn> getFixedColumns(URN urn) {
        List<ResultColumn> ret = new ArrayList<ResultColumn>();
        AnnotatedPluginDocument barcodeUrn = DocumentUtilities.getDocumentByURN(getBarcodeSequenceUrn());
        String label = "";
        if (barcodeUrn != null) {
            label = barcodeUrn.getName();
        }

        LinkResultColumn setCol = new LinkResultColumn("Set");
        if (urn.equals(consensusUrn) || (consensusUrn == null && trimmedDocumentUrnsMap.size() == 1)) {
            List<URN> links = new ArrayList<URN>();
            links.add(getBarcodeSequenceUrn());
            for (URN urn1 : getTraceDocumentUrns()) {
                links.add(urn1);
            }
            setCol.setData(new LinkResultColumn.LinkBox(label, links));
        } else {
            setCol.setData(new LinkResultColumn.LinkBox("", null));
        }
        ret.add(setCol);

        LinkResultColumn sequenceCold = new LinkResultColumn("Sequence");
        sequenceCold.setData(new LinkResultColumn.LinkBox(DocumentUtilities.getDocumentByURN(urn).getName(), Collections.singletonList(urn)));
        ret.add(sequenceCold);

        LinkResultColumn tracesCol = new LinkResultColumn("Number of traces used");
        if (urn.equals(consensusUrn)) {
            tracesCol.setData(new LinkResultColumn.LinkBox("" + getTrimmedDocumentUrns().size(), getTrimmedDocumentUrns()));
        } else {
            tracesCol.setData(new LinkResultColumn.LinkBox("", null));
        }
        ret.add(tracesCol);

        LinkResultColumn assemblefCol = new LinkResultColumn("Number of traces not used");
        if (urn.equals(consensusUrn)) {
            List<URN> links = new ArrayList<URN>();
            Set<URN> assembles = DocumentUtilities.getDocumentByURN(getAssemblyUrn()).getReferencedDocuments();

            if (assembles != null) {
                for (URN urn2 : getTrimmedDocumentUrns()) {
                    if (!assembles.contains(urn2)) {
                        links.add(urn2);
                    }
                }
            } else {
                links.addAll(getTrimmedDocumentUrns());
            }

            assemblefCol.setData(new LinkResultColumn.LinkBox("" + links.size(), links));
        } else {
            assemblefCol.setData(new LinkResultColumn.LinkBox("", null));
        }
        ret.add(assemblefCol);

        StringResultColumn isAssembledCol = new StringResultColumn("Assembled");
        if (urn.equals(consensusUrn)) {
            isAssembledCol.setData("");
        } else {
            URN assemblyUrn1 = getAssemblyUrn();
            if (assemblyUrn1 == null
                    || DocumentUtilities.getDocumentByURN(assemblyUrn1) == null
                    || !DocumentUtilities.getDocumentByURN(assemblyUrn1).getReferencedDocuments().contains(urn)) {
                isAssembledCol.setData("No");
            } else {
                isAssembledCol.setData("Yes");
            }
        }
        ret.add(isAssembledCol);
        return ret;
    }

    public List<List<ResultColumn>> exportTable() {
        List<List<ResultColumn>> ret = new ArrayList<List<ResultColumn>>();

        Set<URN> doclist = getDoclist();
        List<ResultColumn> row;
        for (URN urn : doclist) {
            row = new ArrayList<ResultColumn>();
            row.addAll(getFixedColumns(urn));
            for (Map<URN, RecordOfValidationResult> entry : validationResults.values()) {
                RecordOfValidationResult result = entry.get(urn);
                row.addAll(result.getFact().getColumns());
            }

            if (urn.equals(consensusUrn)) {
                ret.add(0, row);    //consensus row should be the first
            } else {
                ret.add(row);
            }
        }

        return ret;
    }

    public String getSetName() {
        return setName;
    }

    public void setSetName(String setName) {
        this.setName = setName;
    }

    public Map<Integer, ValidationOptions> getColunmOptionsMap(boolean refresh) {
        if (colunmOptionsMap == null || refresh) {
            colunmOptionsMap = new HashMap<Integer, ValidationOptions>();
            List<ResultColumn> fixedColumns = getFixedColumns(getOneURN());
            int i = fixedColumns.size();

            for (Map.Entry<Class, Map<URN, RecordOfValidationResult>> entry : validationResults.entrySet()) {
                RecordOfValidationResult next = entry.getValue().values().iterator().next();
                int entryColumnSize = next.getFact().getColumns().size();
                for (int j = 0; j < entryColumnSize; j++) {
                    colunmOptionsMap.put((j + i), next.getOptions());
                }

                i += entryColumnSize;
            }
        }

        return colunmOptionsMap;
    }

    public URN getOneURN() {
        Set<URN> doclist = getDoclist();
        assert doclist != null && doclist.size() > 0;
        return doclist.iterator().next();
    }
}

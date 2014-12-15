package com.biomatters.plugins.barcoding.validator.output;

import com.biomatters.geneious.publicapi.documents.*;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.plugins.barcoding.validator.validation.ValidationOptions;
import com.biomatters.plugins.barcoding.validator.validation.results.*;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
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
    private static final String SET_NAME = "setname";
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
    private static final String TARGET_URN = "targetUrn";
    private static final String VALIDATION_RECORD = "record";

    private String setName;

    URN barcodeSequenceUrn;
    Multimap<String, URN> traceDocumentUrnsMap = ArrayListMultimap.create();

    Multimap<String, URN> trimmedDocumentUrnsMap = ArrayListMultimap.create();
    URN assemblyUrn;
    URN consensusUrn;

    private Map<Class, Map<URN, RecordOfValidationResult>> validationResults = new LinkedHashMap<Class, Map<URN, RecordOfValidationResult>>();
    private Map<Integer, ValidationOptions> colunmOptionsMap = null;
    private Map<Integer, Class> colunmClassMap = null;

    ValidationOutputRecord() {
    }

    @SuppressWarnings("unused")
    public ValidationOutputRecord(Element element) throws XMLSerializationException {
        try {
            setName = element.getChildText(SET_NAME);
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

            for (Element resultElement : element.getChildren(VALIDATION_RESULT)) {
                Element urnElement = resultElement.getChild(TARGET_URN);
                Element recordElement = resultElement.getChild(VALIDATION_RECORD);
                if(urnElement == null || recordElement == null) {
                    throw new XMLSerializationException("Missing information necessary to deserialize validation result");
                }
                URN urn = URN.fromXML(urnElement);
                addValidationResult(urn, XMLSerializer.classFromXML(recordElement, RecordOfValidationResult.class));
            }
        } catch (MalformedURNException e) {
            throw new XMLSerializationException("Could not de-serialize validation record: " + e.getMessage(), e);
        }
    }

    @Override
    public Element toXML() {
        Element root = new Element(XMLSerializable.ROOT_ELEMENT_NAME);
        root.addContent(new Element(SET_NAME).setText(getParameterSetName()));
        root.addContent(barcodeSequenceUrn.toXML(BARCODE));

        for (String traceDocumentName : traceDocumentUrnsMap.keySet()) {
            Collection<URN> urnOfDocumentsWithName = traceDocumentUrnsMap.get(traceDocumentName);
            for (URN urnOfDocumentWithName : urnOfDocumentsWithName) {
                Element element = new Element(TRACE);
                element.addContent(new Element(TRACE_KEY).setText(traceDocumentName));
                element.addContent(urnOfDocumentWithName.toXML(TRACE_URN));
                root.addContent(element);
            }
        }

        for (String trimmedTraceDocumentName : trimmedDocumentUrnsMap.keySet()) {
            Collection<URN> urnOfTrimmedTraceDocumentsWithName = trimmedDocumentUrnsMap.get(trimmedTraceDocumentName);
            for (URN urnOfTrimmedTraceDocumentWithName : urnOfTrimmedTraceDocumentsWithName) {
                Element element = new Element(TRIMMED);
                element.addContent(new Element(TRIMMED_KEY).setText(trimmedTraceDocumentName));
                element.addContent(urnOfTrimmedTraceDocumentWithName.toXML(TRIMMED_URN));
                root.addContent(element);
            }
        }

        addUrnToXml(root, assemblyUrn, ASSEMBLY);
        addUrnToXml(root, consensusUrn, CONSENSUS);

        for (Map<URN, RecordOfValidationResult> map : validationResults.values()) {
            for (Map.Entry<URN, RecordOfValidationResult> entry : map.entrySet()) {
                Element resultElement = new Element(VALIDATION_RESULT);
                resultElement.addContent(entry.getKey().toXML(TARGET_URN));
                Element elementForValidationResult = XMLSerializer.classToXML(VALIDATION_RECORD, entry.getValue());
                resultElement.addContent(elementForValidationResult);
                root.addContent(resultElement);
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
     * @return true iff all validation tasks run passed for consensus sequences
     */
    public boolean isAllPassedForConsensus() {
        for (Map<URN, RecordOfValidationResult> map : validationResults.values()) {
            RecordOfValidationResult resultForConsensus = map.get(consensusUrn);
            // Count no consensus as a fail
            if(resultForConsensus == null || !resultForConsensus.isPassed()) {
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

    public URN getConsensusUrn() {
        return consensusUrn;
    }

    public void addValidationResult(URN targetSequenceUrn, RecordOfValidationResult result) {
        Class<? extends ResultFact> factClass = result.getFact().getClass();
        Map<URN, RecordOfValidationResult> factMap = validationResults.get(factClass);
        if (factMap == null) {
            factMap = new HashMap<URN, RecordOfValidationResult>();
            validationResults.put(factClass, factMap);
        }

        factMap.put(targetSequenceUrn, result);
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

    public List<ResultColumn> getFixedColumns(URN urn, Double pciValue) {
        List<ResultColumn> fixedColumns = new ArrayList<ResultColumn>();

        PluginDocument barcode = DocumentUtilities.getDocumentByURN(getBarcodeSequenceUrn()).getDocumentOrNull();
        PluginDocument sequence = DocumentUtilities.getDocumentByURN(urn).getDocumentOrNull();

        LinkResultColumn setColumn = new LinkResultColumn("Barcode");
        if (urn.equals(consensusUrn) || (consensusUrn == null && !trimmedDocumentUrnsMap.isEmpty() && trimmedDocumentUrnsMap.values().iterator().next().equals(urn))) {
            List<URN> links = new ArrayList<URN>();
            links.add(getBarcodeSequenceUrn());
            for (URN urn1 : getTraceDocumentUrns()) {
                links.add(urn1);
            }
            setColumn.setData(new LinkResultColumn.LinkBox(barcode.getName(), links));
        } else {
            setColumn.setData(new LinkResultColumn.LinkBox("", null));
        }
        fixedColumns.add(setColumn);

        StringResultColumn barcodeLengthColumn = new StringResultColumn("Barcode length");
        barcodeLengthColumn.setData(String.valueOf(((SequenceDocument)barcode).getSequenceLength()));
        fixedColumns.add(barcodeLengthColumn);

        LinkResultColumn sequenceColumns = new LinkResultColumn("Sequence");
        sequenceColumns.setData(new LinkResultColumn.LinkBox(sequence.getName(), Collections.singletonList(urn)));
        fixedColumns.add(sequenceColumns);

        IntegerResultColumn sequenceLengthColumn = new IntegerResultColumn("Sequence length");
        sequenceLengthColumn.setData(((SequenceDocument) sequence).getSequenceLength());
        fixedColumns.add(sequenceLengthColumn);

        DoubleResultColumn pciColumn = new DoubleResultColumn("PCI");
        pciColumn.setData(pciValue);
        fixedColumns.add(pciColumn);

        LinkResultColumn numberOfTracesUsedColumn = new LinkResultColumn("Number of traces used");
        if (urn.equals(consensusUrn)) {
            numberOfTracesUsedColumn.setData(new LinkResultColumn.LinkBox(String.valueOf(getTrimmedDocumentUrns().size()), getTrimmedDocumentUrns()));
        } else {
            numberOfTracesUsedColumn.setData(new LinkResultColumn.LinkBox("", null));
        }
        fixedColumns.add(numberOfTracesUsedColumn);

        LinkResultColumn numberOfTracesNotUsedColumn = new LinkResultColumn("Number of traces not used");
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

            numberOfTracesNotUsedColumn.setData(new LinkResultColumn.LinkBox(String.valueOf(links.size()), links));
        } else {
            numberOfTracesNotUsedColumn.setData(new LinkResultColumn.LinkBox("", null));
        }
        fixedColumns.add(numberOfTracesNotUsedColumn);

        StringResultColumn isAssembledColumn = new StringResultColumn("Assembled");
        if (urn.equals(consensusUrn)) {
            isAssembledColumn.setData("");
        } else {
            URN assemblyUrn1 = getAssemblyUrn();
            if (assemblyUrn1 == null
                    || DocumentUtilities.getDocumentByURN(assemblyUrn1) == null
                    || !DocumentUtilities.getDocumentByURN(assemblyUrn1).getReferencedDocuments().contains(urn)) {
                isAssembledColumn.setData("No");
            } else {
                isAssembledColumn.setData("Yes");
            }
        }
        fixedColumns.add(isAssembledColumn);
        return fixedColumns;
    }

    public List<List<ResultColumn>> exportTable(Map<URN, Double> pciValues) {
        List<List<ResultColumn>> ret = new ArrayList<List<ResultColumn>>();

        for (URN urn : getDoclist()) {
            List<ResultColumn> row = new ArrayList<ResultColumn>();

            Double pciValue = pciValues == null ? null : pciValues.get(urn);
            row.addAll(getFixedColumns(urn, pciValue));

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

    public String getParameterSetName() {
        return setName;
    }

    public void setParameterSetName(String setName) {
        this.setName = setName;
    }

    public Map<Integer, ValidationOptions> getColunmOptionsMap(boolean refresh) {
        if (colunmOptionsMap == null || refresh) {
            initMapping();
        }

        return colunmOptionsMap;
    }

    public Map<Integer, Class> getColunmClassMap(boolean refresh) {
        if (colunmClassMap == null || refresh) {
            initMapping();
        }
        return colunmClassMap;
    }

    private void initMapping() {
        colunmOptionsMap = new HashMap<Integer, ValidationOptions>();
        colunmClassMap = new HashMap<Integer, Class>();

        List<ResultColumn> fixedColumns = getFixedColumns(getOneURN(), null);
        int i = fixedColumns.size();

        for (Map.Entry<Class, Map<URN, RecordOfValidationResult>> entry : validationResults.entrySet()) {
            RecordOfValidationResult next = entry.getValue().values().iterator().next();
            int entryColumnSize = next.getFact().getColumns().size();
            for (int j = 0; j < entryColumnSize; j++) {
                colunmOptionsMap.put((j + i), next.getOptions());
                colunmClassMap.put((j + i), entry.getKey());
            }

            i += entryColumnSize;
        }
    }

    public URN getOneURN() {
        Set<URN> doclist = getDoclist();
        assert doclist != null && doclist.size() > 0;
        return doclist.iterator().next();
    }
}
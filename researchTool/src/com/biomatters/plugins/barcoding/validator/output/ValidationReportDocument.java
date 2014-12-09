package com.biomatters.plugins.barcoding.validator.output;

import com.biomatters.geneious.publicapi.documents.*;
import com.biomatters.plugins.barcoding.validator.research.BarcodeValidatorOptions;
import org.jdom.Element;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Represents the result of running the validation pipeline on a set of barcode sequences and their associated traces.
 *
 * @author Matthew Cheung
 *         Created on 30/09/14 3:03 PM
 */
public class ValidationReportDocument implements PluginDocument {

    private static final String NAME_KEY = "name";
    private static final String OUTPUT_KEY = "output";
    private static final String OPTIONS_DESC_KEY = "optionsUsed";
    private static final String OPTION_VALUES_KEY = "optionsValuesUsed";
    private static final String PCI_VALUES_KEY = "pciValues";
    private static final String PCI_ENTRY_KEY = "entry";
    private static final String URN_KEY = "urn";
    private static final String VALUE_KEY = "pci";

    private String name;
    private List<ValidationOutputRecord> outputs;
    // One of the following is non-null.  Early version stored only the description.
    @Nullable private BarcodeValidatorOptions optionsUsed;
    @Nullable private String optionsDescription;  // todo Can remove this now since we broke backwards compatibility in 0.5
    @Nullable private Map<URN, Double> pciValues;

    public ValidationReportDocument(String name, List<ValidationOutputRecord> outputs, BarcodeValidatorOptions options, Map<URN, Double> pciValues) {
        this.name = name;
        this.outputs = outputs;
        optionsUsed = options;
        this.pciValues = pciValues;
    }

    private static String generateDescriptionFromOptions(BarcodeValidatorOptions options) {
        StringBuilder descriptionBuilder = new StringBuilder();
        descriptionBuilder.append("The following trimming and assembly parameters were used.<br>")
                .append("<br>")
                .append("<u>Trimming by quality</u><br>")
                .append("Error Probability Limit = ").append(options.getTrimmingOptions().getQualityTrimmingOptions().getErrorProbabilityLimit())
                .append("<br>")
                .append("<br>");

         if (options.getTrimmingOptions().getPrimerTrimmingOptions().getHasPrimerTrimmed()) {
             descriptionBuilder.append("<u>Trimming by primers</u><br>")
                 .append("Max Mismatches = ").append(options.getTrimmingOptions().getPrimerTrimmingOptions().getMaximumMismatches()).append("<br>")
                 .append("Min Match Length = ").append(options.getTrimmingOptions().getPrimerTrimmingOptions().getMinimumMatchLength()).append("<br>")
                 .append("Score Matrix ").append(options.getTrimmingOptions().getPrimerTrimmingOptions().getScores().getName()).append("<br>")
                 .append("Gap Option Penalty = ").append(options.getTrimmingOptions().getPrimerTrimmingOptions().getGapOptionPenalty()).append("<br>")
                 .append("Gap Extension Penalty = ").append(options.getTrimmingOptions().getPrimerTrimmingOptions().getGapExtensionPenalty()).append("<br>")
                 .append("<br>");
        }

        descriptionBuilder.append("<u>Assembly</u><br>")
                .append("Min Overlap Length = ").append(options.getAssemblyOptions().getMinOverlapLength()).append("<br>")
                .append("Min Overlap Identity = ").append(options.getAssemblyOptions().getMinOverlapIdentity());

        return descriptionBuilder.toString();
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

        Element optionsElement = element.getChild(OPTION_VALUES_KEY);
        if(optionsElement != null) {
            optionsUsed = new BarcodeValidatorOptions();
            optionsUsed.valuesFromXML(optionsElement);
        } else {
            optionsDescription = element.getChildText(OPTIONS_DESC_KEY);
        }
        outputs = new ArrayList<ValidationOutputRecord>();
        List<Element> children = element.getChildren(OUTPUT_KEY);
        for (Element child : children) {
            outputs.add(XMLSerializer.classFromXML(child, ValidationOutputRecord.class));
        }
        Element pciElement = element.getChild(PCI_VALUES_KEY);
        if(pciElement != null) {
            pciValues = pciValuesMapFromXml(pciElement);
        }
    }

    private static Map<URN, Double> pciValuesMapFromXml(Element pciElement) throws XMLSerializationException {
        Map<URN, Double> result = new HashMap<URN, Double>();
        List<Element> entryElements = pciElement.getChildren(PCI_ENTRY_KEY);
        for (Element entryElement : entryElements) {
            String valueText = entryElement.getChildText(VALUE_KEY);
            try {
                URN urn = URN.fromXML(entryElement.getChild(URN_KEY));
                Double pci = Double.valueOf(valueText);
                result.put(urn, pci);
            } catch (MalformedURNException e) {
                throw new XMLSerializationException("Bad URN stored in report document: " + e.getMessage(), e);
            } catch (NumberFormatException e) {
                throw new XMLSerializationException("Bad PCI value (" + valueText + ") stored in report document: " + e.getMessage(), e);
            }
        }
        return result;
    }

    @Override
    public Element toXML() {
        Element element = new Element(XMLSerializable.ROOT_ELEMENT_NAME);
        element.addContent(new Element(NAME_KEY).setText(name));
        if(optionsUsed != null) {
            // We only serialize the values in case the format changes.  This also means we don't have to worry about
            // making BarcodeValidationOptions completely serializable.
            element.addContent(optionsUsed.valuesToXML(OPTION_VALUES_KEY));
        } else if(optionsDescription != null) {
            element.addContent(new Element(OPTIONS_DESC_KEY).setText(optionsDescription));
        }
        for (ValidationOutputRecord output : outputs) {
            element.addContent(XMLSerializer.classToXML(OUTPUT_KEY, output));
        }
        if(pciValues != null) {
            element.addContent(pciValuesToXml(pciValues));
        }
        return element;
    }

    private static Element pciValuesToXml(Map<URN, Double> pciValues) {
        Element pciElement = new Element(PCI_VALUES_KEY);
        for (Map.Entry<URN, Double> entry : pciValues.entrySet()) {
            Element valueElement = new Element(PCI_ENTRY_KEY);
            valueElement.addContent(entry.getKey().toXML(URN_KEY));
            valueElement.addContent(new Element(VALUE_KEY).setText(String.valueOf(entry.getValue())));
            pciElement.addContent(valueElement);
        }
        return pciElement;
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

    /**
     *
     * @return a list of {@link com.biomatters.plugins.barcoding.validator.output.ValidationOutputRecord}.  One record
     * per set of barcode->traces that were run through the validation process.
     */
    public List<ValidationOutputRecord> getRecords() {
        return Collections.unmodifiableList(outputs);
    }

    /**
     *
     * @return a human readable summary of the options used in the operation that generated this report
     */
    public String getDescriptionOfOptions() {
        if(optionsDescription == null) {
            return generateDescriptionFromOptions(optionsUsed);
        } else {
            return optionsDescription;
        }
    }

    @Nullable
    public BarcodeValidatorOptions getOptionsUsed() {
        return optionsUsed;
    }
}

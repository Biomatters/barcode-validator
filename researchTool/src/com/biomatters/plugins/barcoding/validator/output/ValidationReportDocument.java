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

    private String name;
    private List<ValidationOutputRecord> outputs;
    // One of the following is non-null.  Early version stored only the description.
    @Nullable private BarcodeValidatorOptions optionsUsed;
    @Nullable private String optionsDescription;

    public ValidationReportDocument(String name, List<ValidationOutputRecord> outputs, BarcodeValidatorOptions options) {
        this.name = name;
        this.outputs = outputs;
        optionsUsed = options;
    }

    private static String generateDescriptionFromOptions(final BarcodeValidatorOptions options) {
        StringBuilder descriptionBuilder = new StringBuilder();

        String trimmingByQualityParameters = buildFormattedAttributeValuePairsListInHTML(new HashMap<Object, Object>(){{
            put("Error probability limit", options.getTrimmingOptions().getQualityTrimmingOptions().getErrorProbabilityLimit());
        }});

        String trimmingByPrimersParameters = buildFormattedAttributeValuePairsListInHTML(new HashMap<Object, Object>() {{
            put("Max mismatches", options.getTrimmingOptions().getPrimerTrimmingOptions().getMaximumMismatches());
            put("Max match length", options.getTrimmingOptions().getPrimerTrimmingOptions().getMinimumMatchLength());
            put("Similary", options.getTrimmingOptions().getPrimerTrimmingOptions().getScores().getName());
            put("Gap option penalty", options.getTrimmingOptions().getPrimerTrimmingOptions().getGapOptionPenalty());
            put("Gap extension penalty", options.getTrimmingOptions().getPrimerTrimmingOptions().getGapExtensionPenalty());
        }});

        String assemblyParameters = buildFormattedAttributeValuePairsListInHTML(new HashMap<Object, Object>() {{
            put("Min overlap length", options.getAssemblyOptions().getMinOverlapLength());
            put("Min overlap identity", options.getAssemblyOptions().getMinOverlapIdentity());
        }});

        descriptionBuilder.append("The following trimming and assembly parameters were used.<br><br>");

        descriptionBuilder.append("<u>Trimming by quality</u><br>").append(trimmingByQualityParameters).append("<br><br>");

        if (options.getTrimmingOptions().getPrimerTrimmingOptions().getHasPrimerTrimmered()) {

               descriptionBuilder.append("<u>Trimming by primers</u><br>").append(trimmingByPrimersParameters).append("<br>");
        }

        descriptionBuilder.append("<u>Assembly</u><br>").append(assemblyParameters);

        return descriptionBuilder.toString();
    }

    private static <T, T2> String buildFormattedAttributeValuePairsListInHTML(Map<T, T2> attributeValuePairs) {
        StringBuilder formattedAttributeValuePairListBuilder = new StringBuilder();
        int longestAttributeLength = 0;

        for (T currentAttribute : attributeValuePairs.keySet()) {
            int currentAttributeLength = String.valueOf(currentAttribute).length();
            if (currentAttributeLength > longestAttributeLength) {
                longestAttributeLength = currentAttributeLength;
            }
        }

        for (Map.Entry<T, T2> currentAttributeValuePair : attributeValuePairs.entrySet()) {
            String currentAttribute = String.valueOf(currentAttributeValuePair.getKey());
            String whiteSpacesBetweenAttributeAndEqualsSign = generateNonCollapsingHTMLWhiteSpaces(longestAttributeLength - currentAttribute.length());
            formattedAttributeValuePairListBuilder.append(currentAttribute).append(whiteSpacesBetweenAttributeAndEqualsSign).append("= ").append(currentAttributeValuePair.getValue()).append("<br>");
        }

        if (formattedAttributeValuePairListBuilder.length() != 0) {
            formattedAttributeValuePairListBuilder.deleteCharAt(formattedAttributeValuePairListBuilder.length() - 1);
        }

        return formattedAttributeValuePairListBuilder.toString();
    }

    private static String generateNonCollapsingHTMLWhiteSpaces(int n) {
        String nonCollapsingHTMLWhiteSpace = "&nbsp;";
        int nonCollapsingHTMLWhiteSpaceLength = nonCollapsingHTMLWhiteSpace.length();
        return new String(new char[n*nonCollapsingHTMLWhiteSpaceLength]).replace(getNullCharacters(nonCollapsingHTMLWhiteSpaceLength), nonCollapsingHTMLWhiteSpace);
    }

    private static String getNullCharacters(int n) {
        return new String(new char[n]);
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

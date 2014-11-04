package com.biomatters.plugins.barcoding.validator.output;

import com.biomatters.geneious.publicapi.documents.*;
import com.biomatters.plugins.barcoding.validator.research.BarcodeValidatorOptions;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Collections;
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
    private static final String OPTIONS_KEY = "optionsUsed";

    private String name;
    private List<ValidationOutputRecord> outputs;
    private String optionsUsed;

    public ValidationReportDocument(String name, List<ValidationOutputRecord> outputs, BarcodeValidatorOptions options) {
        this.name = name;
        this.outputs = outputs;
        optionsUsed = generateDescriptionFromOptions(options);
    }

    private static String generateDescriptionFromOptions(BarcodeValidatorOptions options) {
        double errorProbabilityLimit = options.getTrimmingOptions().getQualityTrimmingOptions().getErrorProbabilityLimit();

        return "The following trimming and assembly parameters were used.<br>" +
               "<br>" +
               "Trimming by quality:<br>" +
               "Error Probability Limit = " + options.getTrimmingOptions().getQualityTrimmingOptions().getErrorProbabilityLimit() + "<br>" +
               "<br>" +
               "Trimming by primer:<br>" +
               "Score = " + options.getTrimmingOptions().getPrimerTrimmingOptions().getScores().getName() + "<br>" +
               "Gap Option Penalty = " + options.getTrimmingOptions().getPrimerTrimmingOptions().getGapOptionPenalty() + "<br>" +
               "Gap Extension Penalty = " + options.getTrimmingOptions().getPrimerTrimmingOptions().getGapExtensionPenalty() + "<br>" +
               "<br>"+
               "Assembly:<br>" +
               "Min Overlap Length = " + options.getAssemblyOptions().getMinOverlapLength() + "<br>" +
               "Min Overlap Identity = " + options.getAssemblyOptions().getMinOverlapIdentity();
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

        optionsUsed = element.getChildText(OPTIONS_KEY);
        if(optionsUsed == null) {
            optionsUsed = "";
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
        element.addContent(new Element(OPTIONS_KEY).setText(optionsUsed));
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
        return optionsUsed;
    }
}

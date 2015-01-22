package com.biomatters.plugins.barcoding.validator.output;

import com.biomatters.geneious.publicapi.documents.*;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.Geneious;
import com.biomatters.plugins.barcoding.validator.research.BarcodeValidatorOptions;
import com.biomatters.plugins.barcoding.validator.research.ValidationUtils;
import jebl.util.ProgressListener;
import org.jdom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Represents the result of running the validation pipeline on a set of barcode sequences and their associated traces.
 *
 * @author Matthew Cheung
 *         Created on 30/09/14 3:03 PM
 */
public class ValidationReportDocument implements PluginDocument, XMLSerializable.OldVersionCompatible {

    private static final String NAME_KEY = "name";
    private static final String OUTPUT_KEY = "output";
    private static final String OPTION_VALUES_KEY = "optionsValuesUsed";

    private String name;
    private List<ValidationOutputRecord> outputs;
    @Nonnull private BarcodeValidatorOptions optionsUsed;
    @Nullable private Map<URN, Double> pciValues;

    /**
     *
     * @param name The name to give the report document
     * @param outputs All output records for the validator operation.
     * @param options The options used to run the validation pipeline.
     * @param pciValues PCI scores for the input sequences obtained from running {@link com.biomatters.plugins.barcoding.validator.validation.pci.PCICalculator}.
     *                  May be null if the calculation was not run for any reason.
     */
    public ValidationReportDocument(String name, List<ValidationOutputRecord> outputs, BarcodeValidatorOptions options, @Nullable Map<URN, Double> pciValues) {
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
            try {
                optionsUsed = new BarcodeValidatorOptions();
                optionsUsed.valuesFromXML(optionsElement);
            } catch (DocumentOperationException e) {
                throw new XMLSerializationException("Could not re-create stored options: " + e.getMessage(), e);
            }
        } else {
            throw new XMLSerializationException("Child element " + OPTION_VALUES_KEY + " is missing");
        }
        outputs = new ArrayList<ValidationOutputRecord>();
        List<Element> children = element.getChildren(OUTPUT_KEY);
        for (Element child : children) {
            outputs.add(XMLSerializer.classFromXML(child, ValidationOutputRecord.class));
        }
        Element pciElement = element.getChild(ValidationUtils.PCI_VALUES_KEY);
        if(pciElement != null) {
            pciValues = ValidationUtils.pciValuesMapFromXml(pciElement, false);
        }
    }

    @Override
    public Element toXML() {
        return toXML(Geneious.getMajorVersion(), ProgressListener.EMPTY);
    }

    @Override
    public Element toXML(Geneious.MajorVersion majorVersion, ProgressListener progressListener) {
        Element element = new Element(XMLSerializable.ROOT_ELEMENT_NAME);
        element.addContent(new Element(NAME_KEY).setText(name));
        // We only serialize the values in case the format changes.  This also means we don't have to worry about
        // making BarcodeValidationOptions completely serializable.
        element.addContent(optionsUsed.valuesToXML(majorVersion, OPTION_VALUES_KEY));
        for (ValidationOutputRecord output : outputs) {
            element.addContent(XMLSerializer.classToXML(OUTPUT_KEY, output));
        }
        if(pciValues != null) {
            element.addContent(ValidationUtils.pciValuesToXml(pciValues));
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
        return generateDescriptionFromOptions(optionsUsed);
    }

    @Nullable
    public BarcodeValidatorOptions getOptionsUsed() {
        return optionsUsed;
    }

    @Nullable
    public Map<URN, Double> getPciValues() {
        return pciValues != null ? Collections.unmodifiableMap(pciValues) : null;
    }

    @Override
    public Geneious.MajorVersion getVersionSupport(VersionSupportType versionType) {
        // Max of: The API version we develop to (7.1) and the oldest version supported by valuesToXML().
        return Geneious.MajorVersion.max(Geneious.MajorVersion.Version7_1, optionsUsed.getVersionSupportForValuesToXML(versionType));
    }
}

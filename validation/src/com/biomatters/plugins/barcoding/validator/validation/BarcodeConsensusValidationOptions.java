package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import org.jdom.Element;

import java.util.Arrays;

/**
 * @author Frank Lee
 * Created by frank on 2/10/14.
 */
public class BarcodeConsensusValidationOptions extends ValidationOptions {

    private static final String IDENTIFIER  = "BarcodeConsensusValidationOptions";
    private static final String LABEL       = "FASTA check";
    private static final String DESCRIPTION = "Validates the provided barcode sequence (FASTA) against the consensus " +
                                                "generated by the barcode validator.";
    private static final String MATCHES       = "matches";

    @SuppressWarnings("UnusedDeclaration")
    public BarcodeConsensusValidationOptions(Element element) throws XMLSerializationException {
        super(element);
    }

    public BarcodeConsensusValidationOptions(Class cls) {
        super(cls);
        OptionValue exactValue = new OptionValue("100", "an exact match");
        OptionValue ninetyPercentValue = new OptionValue("90", "90% similar");
        OptionValue sevenPercentValue = new OptionValue("75", "75% similar");
        OptionValue fivePercentValue = new OptionValue("50", "50% similar");
        addComboBoxOption(MATCHES, "FASTA and barcode are:",
                Arrays.asList(exactValue, ninetyPercentValue, sevenPercentValue, fivePercentValue), ninetyPercentValue);
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public String getLabel() {
        return LABEL;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String getGroup() {
        return BARCODE_VALIDATION_GROUP;
    }

    public float getMatches() {
        String selected = getValueAsString(MATCHES);
        return Float.parseFloat(selected);
    }
}

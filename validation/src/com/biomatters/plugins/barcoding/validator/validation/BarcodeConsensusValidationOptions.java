package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import org.jdom.Element;

import java.util.Arrays;

/**
 * Created by frank on 2/10/14.
 */
public class BarcodeConsensusValidationOptions extends ValidationOptions {

    private static final String IDENTIFIER  = "BarcodeConsensusValidationOptions";
    private static final String LABEL       = "FSTA check";
    private static final String DESCRIPTION = "Validate provided barcode against generated consensus.";
    private static final String MATCHES       = "matches";

    @SuppressWarnings("UnusedDeclaration")
    public BarcodeConsensusValidationOptions(Element element) throws XMLSerializationException {
        super(element);
    }

    public BarcodeConsensusValidationOptions(Class cls) {
        super(cls);
        OptionValue exactValue = new OptionValue("100", "Exactly");
        OptionValue ninetyPercentValue = new OptionValue("90", "90% similar");
        OptionValue sevenPercentValue = new OptionValue("75", "75% similar");
        OptionValue fivePercentValue = new OptionValue("50", "50% similar");
        ComboBoxOption<OptionValue> matchesOption = addComboBoxOption(MATCHES, "FASTA matches generated barcode:", Arrays.asList(exactValue, ninetyPercentValue, sevenPercentValue, fivePercentValue), ninetyPercentValue);
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

    public float getMatches() {
        String selected = ((ComboBoxOption<OptionValue>) getOption(MATCHES)).getValueAsString();
        return Float.parseFloat(selected);
    }
}

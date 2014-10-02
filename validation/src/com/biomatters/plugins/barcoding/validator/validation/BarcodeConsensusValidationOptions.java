package com.biomatters.plugins.barcoding.validator.validation;

import java.util.Arrays;

/**
 * Created by frank on 2/10/14.
 */
public class BarcodeConsensusValidationOptions extends ValidationOptions {

    private static final String IDENTIFIER  = "BarcodeConsensusValidationOptions";
    private static final String LABEL       = "FSTA check";
    private static final String DESCRIPTION = "Validate provided barcode against generated consensus.";
    private static final String MATCHES       = "matches";

    public BarcodeConsensusValidationOptions(Class cls) {
        super(cls);
        OptionValue exactValue = new OptionValue("1", "Exactly");
        OptionValue ninetyPercentValue = new OptionValue("0.9", "90% similar");
        OptionValue sevenPercentValue = new OptionValue("0.75", "75% similar");
        OptionValue fivePercentValue = new OptionValue("0.5", "50% similar");
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

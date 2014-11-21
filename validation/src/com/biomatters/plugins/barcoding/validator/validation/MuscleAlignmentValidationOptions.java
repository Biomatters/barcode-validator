package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import org.jdom.Element;

import java.util.Arrays;

/**
 * @author Frank Lee
 * Created by frank on 2/10/14.
 */
public class MuscleAlignmentValidationOptions extends ValidationOptions {
    private static final String IDENTIFIER  = "muscleAlignmentValidation";
    private static final String LABEL       = "Muscle alignment";
    private static final String DESCRIPTION = "Validates the similarity between two sequences using the MUSCLE alignment.";

    private static final String MINIMUM_SIMILARITY_OPTIONS_NAME = "similarity";

    @SuppressWarnings("UnusedDeclaration")
    public MuscleAlignmentValidationOptions(Element element) throws XMLSerializationException {
        super(element);
    }

    public MuscleAlignmentValidationOptions(Class cls) {
        super(cls);

        OptionValue exactValue = new OptionValue("100", "An exact match");
        OptionValue ninetyPercentValue = new OptionValue("90", "90%");
        OptionValue sevenPercentValue = new OptionValue("75", "75%");
        OptionValue fivePercentValue = new OptionValue("50", "50%");

        addComboBoxOption(MINIMUM_SIMILARITY_OPTIONS_NAME, "Similarity:", Arrays.asList(exactValue, ninetyPercentValue, sevenPercentValue, fivePercentValue), ninetyPercentValue);
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

    public float getMinimumSimilarity() {
        return Float.parseFloat(getValueAsString(MINIMUM_SIMILARITY_OPTIONS_NAME));
    }
}

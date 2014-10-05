package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import org.jdom.Element;

/**
 * @author Gen Li
 *         Created on 26/09/14 9:42 AM
 */
public class SlidingWindowValidationOptions extends ValidationOptions {
    private static final String IDENTIFIER  = "slidingWindowValiator";
    private static final String LABEL       = "Quality (Sliding Window)";
    private static final String DESCRIPTION = "Validate sequence quality using sliding window approach.";

    private static final String WINDOW_SIZE_OPTION_NAME             = "windowSize";
    private static final String STEP_SIZE_OPTION_NAME               = "stepSize";
    private static final String MINIMUM_QUALITY_OPTION_NAME         = "minimumQuality";
    private static final String MINIMUM_RATIO_SATISFIED_OPTION_NAME = "minimumRatioSatisfied";

    public SlidingWindowValidationOptions(Class cls) {
        super(cls);

        addWindowSizeOption();
        addStepSizeOption();
        addMinimumQualityOption();
        addMinimumRatioSatisfiedOption();
    }

    @SuppressWarnings("UnusedDeclaration")
    public SlidingWindowValidationOptions(Element element) throws XMLSerializationException {
        super(element);
    }

    public int getWindowSize() {
        return ((IntegerOption)getOption(WINDOW_SIZE_OPTION_NAME)).getValue();
    }

    public int getStepSize() {
        return ((IntegerOption)getOption(STEP_SIZE_OPTION_NAME)).getValue();
    }

    public int getMinimumQuality() {
        return ((IntegerOption)getOption(MINIMUM_QUALITY_OPTION_NAME)).getValue();
    }

    public double getMinimumRatioSatisfied() {
        return ((DoubleOption)getOption(MINIMUM_RATIO_SATISFIED_OPTION_NAME)).getValue();
    }

    private void addWindowSizeOption() {
        addIntegerOption(WINDOW_SIZE_OPTION_NAME, "Window size: ", 300, 1, Integer.MAX_VALUE).setUnits("bp");
    }

    private void addStepSizeOption() {
        addIntegerOption(STEP_SIZE_OPTION_NAME, "Step size: ", 1, 1, Integer.MAX_VALUE).setUnits("bp");
    }

    private void addMinimumQualityOption() {
        addIntegerOption(MINIMUM_QUALITY_OPTION_NAME, "Minimum quality:", 40, 1, Integer.MAX_VALUE);
    }

    private void addMinimumRatioSatisfiedOption() {
        addDoubleOption(MINIMUM_RATIO_SATISFIED_OPTION_NAME, "Minimum ratio satisfied:", 80.0, 0.0, 100.0).setUnits("%");
    }

    /**
     * @return The identifier of the option.
     */
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    /**
     * @return A label associated with the option.
     */
    @Override
    public String getLabel() {
        return LABEL;
    }

    /**
     * @return A description of the option.
     */
    @Override
    public String getDescription() {
        return DESCRIPTION;
    }
}
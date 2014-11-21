package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import org.jdom.Element;

/**
 * @author Gen Li
 *         Created on 26/09/14 9:42 AM
 */
public class SlidingWindowQualityValidationOptions extends ValidationOptions {
    private static final String IDENTIFIER  = "slidingWindowValidation";
    private static final String LABEL       = "Quality (Sliding Window)";
    private static final String DESCRIPTION = "Validate sequence quality using sliding window approach.";

    private static final String WINDOW_SIZE_OPTION_NAME                = "windowSize";
    private static final String STEP_SIZE_OPTION_NAME                  = "stepSize";
    private static final String MINIMUM_QUALITY_OPTION_NAME            = "minimumQuality";
    private static final String MINIMUM_SATISFACTION_RATIO_OPTION_NAME = "minSatisfactionRatio";

    public SlidingWindowQualityValidationOptions(Class cls) {
        super(cls);

        addWindowSizeOption();
        addStepSizeOption();
        addMinimumQualityOption();
        addMinimumSatisfactionRatioOption();
    }

    @SuppressWarnings("UnusedDeclaration")
    public SlidingWindowQualityValidationOptions(Element element) throws XMLSerializationException {
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

    public double getMinimumSatisfactionRatio() {
        return ((DoubleOption)getOption(MINIMUM_SATISFACTION_RATIO_OPTION_NAME)).getValue();
    }

    private void addWindowSizeOption() {
        addIntegerOption(WINDOW_SIZE_OPTION_NAME, "Window Size: ", 300, 1, Integer.MAX_VALUE).setUnits("bp");
    }

    private void addStepSizeOption() {
        addIntegerOption(STEP_SIZE_OPTION_NAME, "Step Size: ", 1, 1, Integer.MAX_VALUE).setUnits("bp");
    }

    private void addMinimumQualityOption() {
        addIntegerOption(MINIMUM_QUALITY_OPTION_NAME, "Minimum Quality:", 40, 1, Integer.MAX_VALUE);
    }

    private void addMinimumSatisfactionRatioOption() {
        addDoubleOption(MINIMUM_SATISFACTION_RATIO_OPTION_NAME, "Minimum Satisfaction Ratio:", 80.0, 0.0, 100.0).setUnits("%");
    }

    /**
     * @return Identifier of the SlidingWindowValidationOptions.
     */
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    /**
     * @return Label for the SlidingWindowValidationOptions.
     */
    @Override
    public String getLabel() {
        return LABEL;
    }

    /**
     * @return Description of the SlidingWindowValidationOptions.
     */
    @Override
    public String getDescription() {
        return DESCRIPTION;
    }
}
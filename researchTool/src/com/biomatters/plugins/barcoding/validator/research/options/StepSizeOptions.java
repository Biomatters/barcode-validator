package com.biomatters.plugins.barcoding.validator.research.options;

import com.biomatters.geneious.publicapi.plugin.Options;

import javax.swing.*;
import java.util.List;

/**
 * @author Matthew Cheung
 *         Created on 31/10/14 11:59 AM
 */
class StepSizeOptions<T extends Number> extends Options {

    private MultiValueOption<T> baseOption;

    private Option<T, ? extends JComponent> minOption;
    private Option<T, ? extends JComponent> maxOption;
    private Option<T, ? extends JComponent> stepOption;

    StepSizeOptions(MultiValueOption<T> baseOption) {
        this.baseOption = baseOption;
        beginAlignHorizontally(baseOption.getLabel(), false);
        minOption = baseOption.addOption(this, "min", "Min Value:");
        maxOption = baseOption.addOption(this, "max", "Max Value:");
        stepOption = baseOption.addOption(this, "step", "Step Size:");
        endAlignHorizontally();
    }

    List<T> getValues() {
        // I needed to create the getForSteps in MultiValueOption because I couldn't add objects of type T and get type T back :(
        return baseOption.getForSteps(minOption.getValue(), maxOption.getValue(), stepOption.getValue());
    }
}

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
        minOption = baseOption.addOption(this, baseOption.getName()+"_min", "Min Value:", true);
        maxOption = baseOption.addOption(this, baseOption.getName()+"_max", "Max Value:", true);
        stepOption = baseOption.addOption(this, baseOption.getName()+"_step", "Step Size:", false);
        endAlignHorizontally();
    }

    List<T> getValues() {
        // I needed to create the getForSteps in MultiValueOption because I couldn't add objects of type T and get type T back :(
        return baseOption.getForSteps(minOption.getValue(), maxOption.getValue(), stepOption.getValue());
    }

    @Override
    public String verifyOptionsAreValid() {
        return baseOption.verifyInputs(minOption.getValue(), maxOption.getValue(), stepOption.getValue());
    }
}

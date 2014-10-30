package com.biomatters.plugins.barcoding.validator.research.options;

import com.biomatters.geneious.publicapi.plugin.Options;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Matthew Cheung
 *         Created on 31/10/14 10:02 AM
 */
public class IntegerMultiValueOption extends MultiValueOption<Integer> {

    private Options.IntegerOption baseOption;
    public IntegerMultiValueOption(Options.IntegerOption option) {
        super(option.getName()+"range", option.getLabel(), option.getDefaultValue());
        this.baseOption = option;
    }

    @Override
    Integer getSingleValueFromString(String valueString) {
        try {
            return Integer.parseInt(valueString);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    Options.Option<Integer, ? extends JComponent> addOption(Options options, String name, String label) {
        return options.addIntegerOption(name, label, baseOption.getDefaultValue(), baseOption.getMinimum(), baseOption.getMaximum());
    }

    @Override
    List<Integer> getForSteps(Integer min, Integer max, Integer step) {
        List<Integer> results = new ArrayList<Integer>();
        for (int i = min; i < max; i+=step) {
            results.add(i);
        }
        results.add(max);
        return results;
    }
}

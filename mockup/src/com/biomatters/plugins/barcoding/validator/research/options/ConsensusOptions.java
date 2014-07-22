package com.biomatters.plugins.barcoding.validator.research.options;

import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.plugins.barcoding.validator.research.BarcodeValidatorMockupPlugin;
import org.virion.jam.util.SimpleListener;

import java.util.Arrays;

/**
 * @author Matthew Cheung
 *         Created on 22/07/14 1:42 PM
 */
public class ConsensusOptions extends Options {

    public ConsensusOptions() {
        super(BarcodeValidatorMockupPlugin.class);

        OptionValue zero = new OptionValue("0", "0% - Majority");
        OptionValue twentyFive = new OptionValue("25", "25%");
        OptionValue fifty = new OptionValue("50", "50% - Strict");
        OptionValue seventyFive = new OptionValue("75", "75%");
        OptionValue eightyFive = new OptionValue("85", "85%");
        OptionValue ninety = new OptionValue("90", "90%");
        OptionValue ninetyFive = new OptionValue("95", "95%");
        OptionValue ninetyNine = new OptionValue("99", "99%");
        OptionValue hundred = new OptionValue("100", "100% - Identical");
        final OptionValue high = new OptionValue("high", "Highest Quality");

        final ComboBoxOption<OptionValue> thresholdOption = addComboBoxOption("threshold", "Threshold:", Arrays.asList(zero, twentyFive, fifty, seventyFive, eightyFive,
                ninety, ninetyFive, ninetyNine, hundred, high), high);

        final BooleanOption ignoreGapsOption = addBooleanOption("ignoreGaps", "Ignore Gaps", false);

        thresholdOption.addChangeListener(new SimpleListener() {
            @Override
            public void objectChanged() {
                ignoreGapsOption.setEnabled(thresholdOption.getValue() != high);
            }
        });


        beginAlignHorizontally(null, false);
        addBooleanOption("qualityEnable", "Assign Quality", true);
        OptionValue total = new OptionValue("total", "Total");
        OptionValue highest = new OptionValue("highest", "Highest");
        addComboBoxOption("quality", "", Arrays.asList(total, highest), total);
        endAlignHorizontally();

        OptionValue qMark = new OptionValue("?", "?");
        OptionValue gap = new OptionValue("-", "-");
        OptionValue n = new OptionValue("n", "N/X");
        addComboBoxOption("noCoverage", "If no coverage call", Arrays.asList(qMark, gap, n), qMark);



        addBooleanOption("split", "Split into separate seqeunces around '?' calls", false);
    }
}

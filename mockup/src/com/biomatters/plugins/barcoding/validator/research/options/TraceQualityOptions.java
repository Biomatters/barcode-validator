package com.biomatters.plugins.barcoding.validator.research.options;

import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.plugins.barcoding.validator.research.BarcodeValidatorMockupPlugin;

import java.util.Arrays;

/**
 * @author Matthew Cheung
 *         Created on 22/07/14 12:47 PM
 */
public class TraceQualityOptions extends Options {

    public TraceQualityOptions() {
        super(BarcodeValidatorMockupPlugin.class);

        addIntegerOption("size", "Moving Window Size:", 100).setUnits("bp");
        addIntegerOption("low", "Minimum Quality Value:", 40);
        addIntegerOption("percent", "Minimum Above Minimum Quality:", 60).setUnits("%");

        addBooleanOption("acceptNs", "Accept Ns:", true);
        addIntegerOption("qualityIndex", "Quality Index:?", 60);  // todo I don't know what this is. Ask Richard

        OptionValue asIs = new OptionValue("asIs", "is");
        OptionValue asN = new OptionValue("N", "N");
        OptionValue asQuestionMark = new OptionValue("?", "?");

        addComboBoxOption("manualEdits", "Treat manual edits as:", Arrays.asList(asIs, asN, asQuestionMark), asIs);
    }
}

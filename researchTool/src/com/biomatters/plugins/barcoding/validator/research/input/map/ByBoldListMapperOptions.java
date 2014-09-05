package com.biomatters.plugins.barcoding.validator.research.input.map;

import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.plugins.barcoding.validator.research.BarcodeValidatorPlugin;

/**
 * @author Gen Li
 *         Created on 5/09/14 9:56 AM
 */
public class ByBoldListMapperOptions extends Options {
    private static final String BOLD_LIST_FILE_SELECTION_OPTION_NAME = "traceList";
    public ByBoldListMapperOptions() {
        super(BarcodeValidatorPlugin.class);

        addFileSelectionOption(BOLD_LIST_FILE_SELECTION_OPTION_NAME, "Trace List: ", "");
    }

    public String getBoldListFilePath() {
        return (String)getOption(BOLD_LIST_FILE_SELECTION_OPTION_NAME).getValue();
    }
}

package com.biomatters.plugins.barcoding.validator.validation.input.map;

/**
 * @author Gen Li
 *         Created on 5/09/14 9:56 AM
 */
public class BoldListMapperOptions extends BarcodesToTracesMapperOptions {
    private static final String BOLD_LIST_FILE_SELECTION_OPTION_NAME = "traceList";
    public BoldListMapperOptions(Class cls) {
        super(cls);

        addFileSelectionOption(BOLD_LIST_FILE_SELECTION_OPTION_NAME, "Trace List: ", "");
    }

    public String getBoldListFilePath() {
        return (String)getOption(BOLD_LIST_FILE_SELECTION_OPTION_NAME).getValue();
    }
}

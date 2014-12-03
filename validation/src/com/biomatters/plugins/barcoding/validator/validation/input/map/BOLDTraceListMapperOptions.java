package com.biomatters.plugins.barcoding.validator.validation.input.map;

import javax.swing.*;

/**
 * @author Gen Li
 *         Created on 5/09/14 9:56 AM
 */
public class BOLDTraceListMapperOptions extends BarcodesToTracesMapperOptions {
    private static final String BOLD_LIST_FILE_SELECTION_OPTION_NAME = "traceList";

    private BooleanOption hasHeaderRow;
    private NamePartOption processIdIndexOption;
    private NamePartOption traceIndexOption;

    public BOLDTraceListMapperOptions(Class cls) {
        super(cls);

        addFileSelectionOption(BOLD_LIST_FILE_SELECTION_OPTION_NAME, "Trace List: ", "");

        hasHeaderRow = addBooleanOption("hasHeaderRow", "My file has a header row, autodetect columns.", true);

        beginAlignHorizontally(null, false);
        processIdIndexOption = addCustomOption(new NamePartOption("processIdIndex", "Process ID is "));
        Option<String, ? extends JComponent> processLabel = addLabel("column.");
        endAlignHorizontally();

        beginAlignHorizontally(null, false);
        traceIndexOption = addCustomOption(new NamePartOption("traceIndex", "Trace column is "));
        Option<String, ? extends JComponent> traceLabel = addLabel("column.");
        endAlignHorizontally();

        hasHeaderRow.addDependent(processIdIndexOption, false);
        hasHeaderRow.addDependent(processLabel, false);
        hasHeaderRow.addDependent(traceIndexOption, false);
        hasHeaderRow.addDependent(traceLabel, false);
    }

    public String getBoldListFilePath() {
        return (String)getOption(BOLD_LIST_FILE_SELECTION_OPTION_NAME).getValue();
    }

    public boolean hasHeader() {
        return hasHeaderRow.getValue();
    }

    public int getProcessIdIndex() {
        return processIdIndexOption.getPart();
    }

    public int getTraceIndex() {
        return traceIndexOption.getPart();
    }
}

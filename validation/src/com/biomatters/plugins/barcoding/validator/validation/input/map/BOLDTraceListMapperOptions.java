package com.biomatters.plugins.barcoding.validator.validation.input.map;

import javax.swing.*;

/**
 * @author Gen Li
 *         Created on 5/09/14 9:56 AM
 */
public class BOLDTraceListMapperOptions extends BarcodesToTracesMapperOptions {
    private FileSelectionOption boldTraceListFilePathOption;
    private BooleanOption hasHeaderRowOption;
    private NamePartOption processIdIndexOption;
    private NamePartOption traceIndexOption;

    public BOLDTraceListMapperOptions(Class cls) {
        super(cls);

        addBoldTraceListFilePathOption();
        addHasHeaderRowOption();
    }

    public String getBoldTraceListFilePath() {
        return boldTraceListFilePathOption.getValue();
    }

    public boolean hasHeader() {
        return hasHeaderRowOption.getValue();
    }

    public int getProcessIdIndex() {
        return processIdIndexOption.getPart();
    }

    public int getTraceIndex() {
        return traceIndexOption.getPart();
    }

    private void addBoldTraceListFilePathOption() {
        boldTraceListFilePathOption = addFileSelectionOption("traceList", "Trace List: ", "");
    }

    private void addHasHeaderRowOption() {
        hasHeaderRowOption = addBooleanOption("hasHeaderRow", "My file has a header row, autodetect columns.", true);

        beginAlignHorizontally(null, false);
        processIdIndexOption = addCustomOption(new NamePartOption("processIdIndex", "Process ID is "));
        Option<String, ? extends JComponent> processLabel = addLabel("column.");
        endAlignHorizontally();

        beginAlignHorizontally(null, false);
        traceIndexOption = addCustomOption(new NamePartOption("traceIndex", "Trace column is "));
        Option<String, ? extends JComponent> traceLabel = addLabel("column.");
        endAlignHorizontally();

        hasHeaderRowOption.addDependent(processIdIndexOption, false);
        hasHeaderRowOption.addDependent(processLabel, false);
        hasHeaderRowOption.addDependent(traceIndexOption, false);
        hasHeaderRowOption.addDependent(traceLabel, false);
    }
}
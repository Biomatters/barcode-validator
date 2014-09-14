package com.biomatters.plugins.barcoding.validator.validation.input;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.geneious.publicapi.utilities.IconUtilities;
import com.biomatters.plugins.barcoding.validator.validation.input.map.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Gen Li
 *         Created on 3/09/14 3:58 PM
 */
public class InputSplitterOptions extends Options {
    private static final String TRACE_INPUT_OPTION_NAME   = "traceInput";
    private static final String BARCODE_INPUT_OPTION_NAME = "barcodeInput";
    private static final String METHOD_OPTION_NAME        = "method";

    public static final String MATCH_USING_BOLD_OPTION_NAME      = "matchUsingBold";
    public static final String MATCH_USING_GENBANK_OPTION_NAME   = "matchUsingGenbank";
    public static final String MATCH_USING_FILE_NAME_OPTION_NAME = "matchUsingFilename";

    public InputSplitterOptions() {
        addHelpButtonOptions();

        addTraceInputOptions();

        addBarcodeInputOptions();

        addMethodSelectionOptions();
    }

    public List<String> getTraceFilePaths() {
        return getFilePathsFromMultipleInputFileOptions(TRACE_INPUT_OPTION_NAME);
    }

    public List<String> getBarcodeFilePaths() {
        return getFilePathsFromMultipleInputFileOptions(BARCODE_INPUT_OPTION_NAME);
    }

    public BarcodesToTracesMapperOptions getMethodOption() {
        return (BarcodesToTracesMapperOptions)
                getChildOptions().get(((OptionValue) getChildOptionsPageChooser().getValue()).getName());
    }

    private void addHelpButtonOptions() {
        beginAlignHorizontally(null, false);

        addButtonOption("helpButton",
                        "Specify any number of files or folders that contain traces or barcode sequences.",
                        "",
                        IconUtilities.getIcons("help16.png").getIcon16(),
                        ButtonOption.RIGHT).addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                Dialogs.showMessageDialog("Help text describing input formats and options");
                            }
                        });

        endAlignHorizontally();
    }

    private void addTraceInputOptions() {
        addMultipleOptions(TRACE_INPUT_OPTION_NAME, new InputFileOptions("Trace(s):"), false);
    }

    private void addBarcodeInputOptions() {
        addMultipleOptions(BARCODE_INPUT_OPTION_NAME, new InputFileOptions("Barcode Sequence(s):"), false);
    }

    private void addMethodSelectionOptions() {
        addChildOptions(MATCH_USING_BOLD_OPTION_NAME, "tracelist.txt (BOLD)", "", new BoldListMapperOptions());
        addChildOptions(MATCH_USING_GENBANK_OPTION_NAME, "XML File (Genbank)", "", new GenbankXmlMapperOptions());
        addChildOptions(MATCH_USING_FILE_NAME_OPTION_NAME, "part of names", "", new FileNameMapperOptions());

        addChildOptionsPageChooser(METHOD_OPTION_NAME,
                                   "Match traces to sequences by: ",
                                   Collections.<String>emptyList(),
                                   PageChooserType.COMBO_BOX,
                                   false);
    }

    private List<String> getFilePathsFromMultipleInputFileOptions(String optionName) {
        List<String> filePaths = new ArrayList<String>();

        for (Options traceInput : getMultipleOptions(optionName).getValues())
            filePaths.add(((InputFileOptions)traceInput).getFilePath());

        return filePaths;
    }
}
package com.biomatters.plugins.barcoding.validator.validation.input;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.geneious.publicapi.utilities.IconUtilities;
import com.biomatters.plugins.barcoding.validator.validation.input.map.*;
import com.biomatters.plugins.barcoding.validator.validation.utilities.ImportUtilities;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Gen Li
 *         Created on 3/09/14 3:58 PM
 */
public class InputOptions extends Options {
    private MultipleOptions barcodeInputOption;
    private MultipleOptions traceInputOption;

    public InputOptions(Class cls) {
        super(cls);

        addHelpButtonOptions();
        addTraceInputOptions();
        addBarcodeInputOptions();
        addMappingApproachOptions();
    }

    public List<String> getTraceFilePaths() {
        return getFilePathsFromMultipleInputFileOptions(traceInputOption);
    }

    public List<String> getBarcodeFilePaths() {
        return getFilePathsFromMultipleInputFileOptions(barcodeInputOption);
    }

    public BarcodesToTracesMapperOptions getMethodOption() {
        return (BarcodesToTracesMapperOptions)getChildOptions().get(((OptionValue) getChildOptionsPageChooser().getValue()).getName());
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
        traceInputOption = addMultipleOptions("traceInput", new InputSelectionOptions("Trace(s):", ImportUtilities.TRACE_ALLOWED_FILE_EXTENSIONS), false);
    }

    private void addBarcodeInputOptions() {
        barcodeInputOption = addMultipleOptions("barcodeInput", new InputSelectionOptions("Barcode Sequence(s):"), false);
    }

    private void addMappingApproachOptions() {
        addChildOptions("mapUsingBoldTraceInfoFile", "TRACE_FILE_INFO.txt (BOLD)", "", new BOLDTraceListMapperOptions(InputOptions.class));
        addChildOptions("mapUsingGenbankTraceInfoFile", "TRACEINFO.xml (Genbank)", "", new GenbankXmlMapperOptions(InputOptions.class));
        addChildOptions("mapUsingPartOfNames", "part of names", "", new FileNameMapperOptions(InputOptions.class));

        addChildOptionsPageChooser("mappingApproach", "Match traces to sequences by: ", Collections.<String>emptyList(), PageChooserType.COMBO_BOX, false);
    }

    private static List<String> getFilePathsFromMultipleInputFileOptions(MultipleOptions options) {
        List<String> filePaths = new ArrayList<String>();

        for (Options traceInput : options.getValues()) {
            filePaths.add(((InputSelectionOptions) traceInput).getFilePath());
        }

        return filePaths;
    }
}
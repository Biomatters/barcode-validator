package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.geneious.publicapi.utilities.IconUtilities;
import com.biomatters.plugins.barcoding.validator.research.options.FakeCap3Options;
import com.biomatters.plugins.barcoding.validator.research.options.InputFileOptions;
import com.biomatters.plugins.barcoding.validator.research.options.SpecimenValidationOptions;
import com.biomatters.plugins.barcoding.validator.research.options.TrimmingOptions;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;

/**
 * @author Matthew Cheung
 *         Created on 15/07/14 3:08 PM
 */
public class BarcodeValidatorMockOptions extends Options {

    public BarcodeValidatorMockOptions() {
        super(BarcodeValidatorMockupPlugin.class);

        Options specimenOptions = new Options(BarcodeValidatorMockupPlugin.class);
        addMultiInputOptions(specimenOptions, "Specify any number of specimen data files to validate",
                "Some help", "specimenInput", "Specimen Data File:");
        addCollapsibleChildOptions("specimen", "Specimen Data", "", specimenOptions, false, false);


        Options sequenceDataOptions = new Options(BarcodeValidatorMockupPlugin.class);
        addMultiInputOptions(sequenceDataOptions, "Specify any number of trace files or folders to validate",
                "Some help", "traceInput", "Trace File/Folder:");
        addMultiInputOptions(sequenceDataOptions, "Specify any number of barcode files to validate",
                "Some help", "barcodeInput", "Barcode Sequence File:");
        addCollapsibleChildOptions("sequenceData", "Sequence Data", "", sequenceDataOptions, false, false);

        Options outputOptions = new Options(BarcodeValidatorMockupPlugin.class);
        outputOptions.addFileSelectionOption("output", "Output Folder:", "").setSelectionType(JFileChooser.DIRECTORIES_ONLY);
        addChildOptions("output", "Output", "", outputOptions);

        Options validationOptions = new Options(BarcodeValidatorMockupPlugin.class);
        validationOptions.addChildOptions("specimen", "Specimen", null, new SpecimenValidationOptions());
        validationOptions.addChildOptions("trim", "Trimming", null, new TrimmingOptions());
        validationOptions.addChildOptions("cap3", "Assembly", "", new FakeCap3Options());
        validationOptions.addChildOptionsPageChooser("chooser", "Validation Steps:", Collections.<String>emptyList(), PageChooserType.BUTTONS, true);
        addChildOptions("validation", "Validation", "", validationOptions);
    }

    private int count = 1;
    void addMultiInputOptions(Options inputOutputOptions, String descriptionLabel, final String helpText, String multiOptionsName, String inputOptionsLabel) {
        inputOutputOptions.beginAlignHorizontally(null, false);
        inputOutputOptions.addLabel(descriptionLabel);
        inputOutputOptions.addButtonOption("help" + count++, "", "", IconUtilities.getIcons("help16.png").getIcon16(), ButtonOption.RIGHT)
            .addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    Dialogs.showMessageDialog(helpText);
                }
            });

        inputOutputOptions.endAlignHorizontally();
        inputOutputOptions.addMultipleOptions(multiOptionsName, new InputFileOptions(inputOptionsLabel), false);
    }
}

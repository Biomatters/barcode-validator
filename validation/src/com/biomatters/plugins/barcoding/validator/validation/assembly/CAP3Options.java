package com.biomatters.plugins.barcoding.validator.validation.assembly;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.geneious.publicapi.utilities.IconUtilities;
import com.biomatters.geneious.publicapi.utilities.SystemUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Gen Li
 *         Created on 3/09/14 3:42 PM
 */
public class CAP3Options extends Options {
    private final static String MIN_OVERLAP_LENGTH_OPTION_NAME   = "minOverlapLength";
    private final static String MIN_OVERLAP_IDENTITY_OPTION_NAME = "minOverlapIdentity";
    private final static String EXECUTABLE_OPTION_NAME           = "executable";

    private final static String DEFAULT_CAP3_EXECUTABLE_NAME_WINDOWS = "cap3.exe";
    private final static String DEFAULT_CAP3_EXECUTABLE_NAME_UNIX    = "cap3";

    public CAP3Options() {
        super(CAP3Options.class);

        addMinOverlapLengthOption();

        addMinOverlapIdentityOption();

        addExecutableOption();
    }

    public static String getDefaultCap3ExecutableName() {
        return SystemUtilities.isWindows() ? DEFAULT_CAP3_EXECUTABLE_NAME_WINDOWS : DEFAULT_CAP3_EXECUTABLE_NAME_UNIX;
    }

    public int getMinOverlapLength() {
        return ((IntegerOption)getOption(MIN_OVERLAP_LENGTH_OPTION_NAME)).getValue();
    }

    public int getMinOverlapIdentity() {
        return ((IntegerOption)getOption(MIN_OVERLAP_IDENTITY_OPTION_NAME)).getValue();
    }

    public String getExecutablePath() {
        return ((FileSelectionOption)getOption(EXECUTABLE_OPTION_NAME)).getValue();
    }

    private void addMinOverlapLengthOption() {
        addIntegerOption(MIN_OVERLAP_LENGTH_OPTION_NAME, "Min overlap length:", 40, 16, 1000);
    }

    private void addMinOverlapIdentityOption() {
        addIntegerOption(MIN_OVERLAP_IDENTITY_OPTION_NAME, "Min overlap identity:", 90, 66, 1000);
    }

    private void addExecutableOption() {
        beginAlignHorizontally(null, false);

        addFileSelectionOption(EXECUTABLE_OPTION_NAME, "CAP3 executable:", getDefaultCap3ExecutableName())
                .setSelectionType(JFileChooser.FILES_ONLY);

        ButtonOption button = addButtonOption("help",
                                              "",
                                              "",
                                              IconUtilities.getIcons("help16.png").getIcon16(),
                                              ButtonOption.RIGHT);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Dialogs.showMessageDialog(
                        "The Barcode Validator uses CAP3 " +
                        "(<a href=\"http://genome.cshlp.org/content/9/9/868.full\">Huang and Madan 1999</a>), " +
                        "available at " +
                        "<a href=\"http://seq.cs.iastate.edu/\">http://seq.cs.iastate.edu</a>."
                );
            }
        });

        endAlignHorizontally();
    }
}
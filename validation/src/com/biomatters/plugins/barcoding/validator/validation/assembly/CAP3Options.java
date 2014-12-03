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
    private IntegerOption minOverlapLengthOption;
    private IntegerOption minOverlapIdentityOption;
    private FileSelectionOption executableSelectionOption;

    private final static String DEFAULT_CAP3_EXECUTABLE_NAME_WINDOWS = "cap3.exe";
    private final static String DEFAULT_CAP3_EXECUTABLE_NAME_UNIX    = "cap3";

    public CAP3Options(Class cls) {
        super(cls);

        addMinOverlapLengthOption();
        addMinOverlapIdentityOption();
        addExecutableSelectionOption();
    }

    public int getMinOverlapLength() {
        return minOverlapLengthOption.getValue();
    }

    public int getMinOverlapIdentity() {
        return minOverlapIdentityOption.getValue();
    }

    public String getExecutablePath() {
        return executableSelectionOption.getValue();
    }

    private void addMinOverlapLengthOption() {
        minOverlapLengthOption = addIntegerOption("minOverlapOverlap", "Min Overlap Length:", 40, 16, 1000);
    }

    private void addMinOverlapIdentityOption() {
        minOverlapIdentityOption = addIntegerOption("minOverlapIdentity", "Min Overlap Identity:", 90, 66, 1000);
    }

    private void addExecutableSelectionOption() {
        beginAlignHorizontally(null, false);

        executableSelectionOption = addFileSelectionOption("executableSelection", "CAP3 Executable:", getDefaultCap3ExecutableName());
        executableSelectionOption.setSelectionType(JFileChooser.FILES_ONLY);

        ButtonOption button = addButtonOption("help", "", "", IconUtilities.getIcons("help16.png").getIcon16(), ButtonOption.RIGHT);
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

    public static String getDefaultCap3ExecutableName() {
        return SystemUtilities.isWindows() ? DEFAULT_CAP3_EXECUTABLE_NAME_WINDOWS : DEFAULT_CAP3_EXECUTABLE_NAME_UNIX;
    }
}
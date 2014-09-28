package com.biomatters.plugins.barcoding.validator.validation.assembly;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.geneious.publicapi.utilities.IconUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Gen Li
 *         Created on 3/09/14 3:42 PM
 */
public class Cap3AssemblerOptions extends Options {
    private IntegerOption minOverlapLengthOption;
    private IntegerOption minOverlapIdentityOption;
    private FileSelectionOption executableLocationOption;

    public Cap3AssemblerOptions() {
        super(Cap3AssemblerOptions.class);
        minOverlapLengthOption = addIntegerOption("minOverlapLength", "Min overlap length:", 40, 16, 1000);
        minOverlapIdentityOption = addIntegerOption("minOverlapIdentity", "Min overlap identity:", 90, 66, 1000);
        beginAlignHorizontally(null, false);
        addLabel("Please make sure that cap3 is in your PATH or that you specify it below");
        ButtonOption button = addButtonOption("help", "", "", IconUtilities.getIcons("help16.png").getIcon16(), ButtonOption.RIGHT);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Dialogs.showMessageDialog("The barcode validator uses CAP3 (<a href=\"http://genome.cshlp.org/content/9/9/868.full\">Huang and Madan 1999</a>) to assemble your traces.  Please make sure " +
                        "that it is installed on your path or that you specify the location that it has been installed to.\n\n" +
                        "CAP3 is freely available for non-commercial use from <a href=\"http://seq.cs.iastate.edu/\">http://seq.cs.iastate.edu</a>");
            }
        });
        endAlignHorizontally();
        executableLocationOption = addFileSelectionOption("executableLocationOption", "Cap3 Executable:", "cap3");
        executableLocationOption.setSelectionType(JFileChooser.FILES_ONLY);
    }

    public int getMinOverlapLength() {
        return minOverlapLengthOption.getValue();
    }

    public int getMinOverlapIdentity() {
        return minOverlapIdentityOption.getValue();
    }

    public String getExecutable() {
        return executableLocationOption.getValue();
    }
}
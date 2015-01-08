package com.biomatters.plugins.barcoding.validator.validation.pci;

import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.geneious.publicapi.utilities.Execution;
import com.biomatters.plugins.barcoding.validator.validation.utilities.AlignmentUtilities;
import jebl.util.ProgressListener;
import org.jdom.Element;

import java.io.IOException;

/**
 * @author Matthew Cheung
 *         Created on 3/12/14 1:02 PM
 */
public class PCICalculatorAlignmentOptions extends Options {
    private Options alignmentOptions;

    private final boolean canPerformPCICalculation = testRunPerlFromWithinWorkingDirectoryOnCommandLine();

    public PCICalculatorAlignmentOptions(Class cls) throws DocumentOperationException {
        super(cls);
        alignmentOptions = AlignmentUtilities.getOptions();
        addChildOptions("alignment", "Alignment", "", alignmentOptions, false);

        if (!canPerformPCICalculation) {
            setEnabled(false);
            addLabel("PCI calculation is disabled as an installation of perl could not be detected.");
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public PCICalculatorAlignmentOptions(Element element) throws XMLSerializationException {
        super(element);
    }

    public Options getAlignmentOptions() {
        return alignmentOptions;
    }

    public boolean canPerformPCICalculation() {
        return canPerformPCICalculation;
    }

    @SuppressWarnings("EmptyCatchBlock")
    private static boolean testRunPerlFromWithinWorkingDirectoryOnCommandLine() {
        boolean canRunPerlFromWithinWorkingDirectoryOnCommandLine = false;
        Execution printPerlVersion = new Execution(
                new String[] { "perl", "-v" },
                ProgressListener.EMPTY,
                new Execution.OutputListener() {
                    @Override
                    public void stdoutWritten(String s) {
                    }

                    @Override
                    public void stderrWritten(String s) {
                    }
                },
                (String)null,
                false
        );

        try {
            if (printPerlVersion.execute() == 0) {
                canRunPerlFromWithinWorkingDirectoryOnCommandLine = true;
            }
        } catch (InterruptedException e) {
        } catch (IOException e) {
        }

        return canRunPerlFromWithinWorkingDirectoryOnCommandLine;
    }
}
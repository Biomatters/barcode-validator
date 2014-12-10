package com.biomatters.plugins.barcoding.validator.validation.pci;

import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import com.biomatters.geneious.publicapi.utilities.Execution;
import com.biomatters.geneious.publicapi.plugin.Options;
import jebl.util.ProgressListener;
import org.jdom.Element;
import java.io.IOException;

/**
 * @author Matthew Cheung
 *         Created on 3/12/14 1:02 PM
 */
public class PCICalculatorOptions extends Options {
    private StringOption genusSelectionOption;
    private StringOption speciesSelectionOption;
    private FileSelectionOption barcodesSelectionOption;

    private final boolean canPerformPCICalculation = testRunPerlFromWithinWorkingDirectoryOnCommandLine();

    public PCICalculatorOptions(Class cls) {
        super(cls);

        addGenusSelectionOption();
        addSpeciesSelectionOption();
        addBarcodesFileSelectionOption();

        if (!canPerformPCICalculation) {
            setEnabled(false);
            addLabel("PCI calculation is disabled as an installation of perl could not be detected.");
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public PCICalculatorOptions(Element element) throws XMLSerializationException {
        super(element);
    }

    public String getGenus() {
        return genusSelectionOption.getValue();
    }

    public String getSpecies() {
        return speciesSelectionOption.getValue();
    }

    public String getPathToBarcodesFile() {
        return barcodesSelectionOption.getValue();
    }

    public boolean canPerformPCICalculation() {
        return canPerformPCICalculation;
    }

    private void addGenusSelectionOption() {
        genusSelectionOption = addStringOption("genusSelection", "Genus to test:", "");
    }

    private void addSpeciesSelectionOption() {
        speciesSelectionOption = addStringOption("speciesSelection", "Species to test:", "");
    }

    private void addBarcodesFileSelectionOption() {
        beginAlignHorizontally(null, false);

        barcodesSelectionOption = addFileSelectionOption("barcodesSelection", "Barcodes:", "");
        addHelpButton(
                "Help",
                "Please select a fasta file that contains reference barcode sequences for PCI calculation. " +
                "Barcode sequences must be named in the format: genus_species_id. Ids must not contain spaces."
        );

        endAlignHorizontally();
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
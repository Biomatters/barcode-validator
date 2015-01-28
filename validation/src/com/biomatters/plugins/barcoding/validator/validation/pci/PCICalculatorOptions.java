package com.biomatters.plugins.barcoding.validator.validation.pci;

import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.geneious.publicapi.utilities.Execution;
import jebl.util.ProgressListener;
import org.jdom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

/**
 * @author Matthew Cheung
 *         Created on 3/12/14 1:02 PM
 */
public class PCICalculatorOptions extends Options {
    private TaxonMappingOptions taxonMappingOptions;
    private FileSelectionOption barcodesSelectionOption;
    private Option barcodesSelectionHelpOption;

    private final boolean canPerformPCICalculation = testRunPerlFromWithinWorkingDirectoryOnCommandLine();

    public PCICalculatorOptions(Class cls) throws DocumentOperationException {
        super(cls);
        addBarcodesFileSelectionOption();
        taxonMappingOptions = new TaxonMappingOptions();
        addChildOptions("input", "Genus and Species for Input Barcodes", "", taxonMappingOptions);

        if (!canPerformPCICalculation) {
            setEnabled(false);
            addLabel("PCI calculation is disabled as an installation of perl could not be detected.");
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public PCICalculatorOptions(Element element) throws XMLSerializationException {
        super(element);
    }

    public String getPathToBarcodesFile() {
        return barcodesSelectionOption.getValue();
    }

    public void setBarcodesSelectionVisable(boolean visible) {
        barcodesSelectionOption.setVisible(visible);
        barcodesSelectionHelpOption.setVisible(visible);
    }

    public boolean canPerformPCICalculation() {
        return canPerformPCICalculation;
    }

    private void addBarcodesFileSelectionOption() {
        beginAlignHorizontally(null, false);

        barcodesSelectionOption = addFileSelectionOption("barcodesSelection", "Reference Barcodes:", "");
        barcodesSelectionHelpOption = addHelpButtonOption(
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

    @Nullable
    public PCICalculator.GenusAndSpecies getGenusAndSpeciesFromLine(String line) {
        String genus = taxonMappingOptions.getGenus(line);
        String species = taxonMappingOptions.getSpecies(line);
        if(genus == null || species == null) {
            return null;
        } else {
            return new PCICalculator.GenusAndSpecies(genus.replaceAll("[_\\s]+", "-"), species.replaceAll("[_\\s]+", "-"));
        }
    }

    @Nonnull
    public String getNameFromLine(@Nonnull String line) {
        return line.split(taxonMappingOptions.separatorOption.getSeparatorString())[0];
    }

    public boolean isUseInputFile() {
        return taxonMappingOptions.useInputFiles.getValue();
    }

    /**
     *
     * @return The map file if specified by the user.  null if no file has been specified.
     */
    @Nullable
    public File getTaxonMappingFile() {
        if(!taxonMappingOptions.mapFileOption.isEnabled()) {
            return null;
        }
        String path = taxonMappingOptions.mapFileOption.getValue();
        if(path == null) {
            return null;
        }
        File file = new File(path);
        if(!file.exists()) {
            return null;
        }
        return file;

    }
}
package com.biomatters.plugins.barcoding.validator.validation.pci;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.utilities.FileUtilities;
import com.biomatters.geneious.publicapi.utilities.GeneralUtilities;
import com.biomatters.plugins.barcoding.validator.validation.SingleSequenceValidation;
import com.biomatters.plugins.barcoding.validator.validation.ValidationOptions;
import com.biomatters.plugins.barcoding.validator.validation.results.BooleanResultColumn;
import com.biomatters.plugins.barcoding.validator.validation.results.ResultFact;
import com.biomatters.plugins.barcoding.validator.validation.utilities.AlignmentUtilities;
import com.biomatters.plugins.barcoding.validator.validation.utilities.ImportUtilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @author Matthew Cheung
 *         Created on 3/12/14 12:59 PM
 */
public class PciValidation extends SingleSequenceValidation {
    private static final String UID_PREFIX = "UnknownGenus_UnknownSpecies_";

    @Override
    public ValidationOptions getOptions() {
        return new PciValidationOptions();
    }

    @Override
    public ResultFact validate(NucleotideGraphSequenceDocument sequence, ValidationOptions _options) throws DocumentOperationException {
        if(!(_options instanceof PciValidationOptions)) {
            throw new IllegalStateException("validate() must be called with Options obtained from getOptions()");
        }

        PciValidationOptions options = (PciValidationOptions) _options;
        File barcodesFile = new File(options.getPathToBarcodesFile());
        if(!barcodesFile.exists()) {
            throw new DocumentOperationException("Barcodes file for PCI validation did not exist: " + barcodesFile.getAbsolutePath());
        }

        List<NucleotideSequenceDocument> toAlign = new ArrayList<NucleotideSequenceDocument>();
        toAlign.addAll(ImportUtilities.importNucleotidesFastaFile(barcodesFile));
        toAlign.add(sequence);

        SequenceAlignmentDocument alignment = AlignmentUtilities.performAlignment(toAlign);
        try {
            File inputAlignmentFile = createPciInputFile(alignment);
            File newUidFile = createNewUidFile(sequence);

            File outputFile = runPci(inputAlignmentFile, newUidFile);
            return getFactFromOutputFile(outputFile);
        } catch (IOException e) {
            throw new DocumentOperationException("Failed to write out alignment for input to PCI: " + e.getMessage(), e);
        }

    }

    private ResultFact getFactFromOutputFile(File outputFile) {
        // todo
        PciResultFact fact = new PciResultFact();
        BooleanResultColumn col = new BooleanResultColumn("Pass");
        col.setData(true);
        fact.addColumn(col);
        return fact;
    }

    private File runPci(File inputAlignmentFile, File newUidFile) {
        // todo
        return null;
    }

    /**
     * @param sequence The sequence that should have its UID in the file
     * @return A plain text file with the UID of the sequence in it
     * @throws IOException if there is a problem writing the file
     */
    private File createNewUidFile(NucleotideGraphSequenceDocument sequence) throws IOException {
        File newUids = FileUtilities.createTempFile("new", ".txt", false);
        FileUtilities.writeTextToFile(newUids, UID_PREFIX + sequence.getName());
        return newUids;
    }

    /**
     * Generates the input file to the PCI program.  The input file will name sequences as UnknownGenus_UnknownSpecies_sequenceName.
     *
     * @param alignment The alignment to write out to the PCI input file
     * @return The PCI input file
     * @throws IOException if there is a problem writing the file
     */
    private File createPciInputFile(SequenceAlignmentDocument alignment) throws IOException {
        File outputFile = FileUtilities.createTempFile("alignment", ".fasta", false);
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        try {
            for (SequenceDocument alignedSequence : alignment.getSequences()) {
                writer.write(">" + UID_PREFIX);
                writer.write(alignedSequence.getName());
                writer.newLine();

                writer.write(alignedSequence.getSequenceString());
                writer.newLine();
            }
        } finally {
            GeneralUtilities.attemptClose(writer);
        }


        return outputFile;
    }
}

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
     * Produces a file that can be used to specify new samples to the PCI program (using -s).
     *
     * @param sequence The sequence that should have its UID in the file
     * @return A plain text file with the UID of the sequence in it. Obtained by calling {@link #getUid(String)}
     * @throws IOException if there is a problem writing the file
     */
    private static File createNewUidFile(NucleotideGraphSequenceDocument sequence) throws IOException {
        File newUids = FileUtilities.createTempFile("new", ".txt", false);
        FileUtilities.writeTextToFile(newUids, UID_PREFIX + sequence.getName());
        return newUids;
    }

    /**
     * Generates the input file to the PCI program (-i).  The input file will name sequences using {@link #getUid(String)}
     *
     * @param alignment The alignment to write out to the PCI input file
     * @return The PCI input file
     * @throws IOException if there is a problem writing the file
     */
    private static File createPciInputFile(SequenceAlignmentDocument alignment) throws IOException {
        File outputFile = FileUtilities.createTempFile("alignment", ".fasta", false);
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        try {
            for (SequenceDocument alignedSequence : alignment.getSequences()) {
                writer.write(">" + getUid(alignedSequence.getName()));
                writer.newLine();

                writer.write(alignedSequence.getSequenceString());
                writer.newLine();
            }
        } finally {
            GeneralUtilities.attemptClose(writer);
        }


        return outputFile;
    }

    /**
     * The UID consists of Genus_Species_ID.  In our case we use UnknownGenus_UnknownSpecies_SequenceName.  This is
     * because all the sequences in the alignment need to be compared.  We are not splitting things by species.
     *
     * <strong>Note</strong>: Any white space in the sequence name will be replaced by an dash "-".  This is because the
     * compressed barcode format used as input for the PCI program is based on strict FASTA, which does not have spaces
     * in the names of sequences.  <a href="http://www.ncbi.nlm.nih.gov/CBBresearch/Spouge/html_ncbi/html/bib/119.html#The%20Format%20of%20an%20Compressed%20Barcode%20File">Details here.</a>.
     * Underscore is replaced because it is a special character in the UID used as a separator.
     *
     * @param name The name of the sequence
     * @return The UID of the sequence as it should appear in the compressed barcode format.  See
     *
     */
    private static String getUid(String name) {
        return UID_PREFIX + name.replaceAll("[_\\s]+", "-");
    }
}

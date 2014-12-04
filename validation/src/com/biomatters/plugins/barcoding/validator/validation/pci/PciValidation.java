package com.biomatters.plugins.barcoding.validator.validation.pci;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.utilities.Execution;
import com.biomatters.geneious.publicapi.utilities.FileUtilities;
import com.biomatters.geneious.publicapi.utilities.GeneralUtilities;
import com.biomatters.plugins.barcoding.validator.validation.SingleSequenceValidation;
import com.biomatters.plugins.barcoding.validator.validation.ValidationOptions;
import com.biomatters.plugins.barcoding.validator.validation.results.*;
import com.biomatters.plugins.barcoding.validator.validation.utilities.AlignmentUtilities;
import com.biomatters.plugins.barcoding.validator.validation.utilities.ImportUtilities;
import jebl.util.ProgressListener;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Matthew Cheung
 *         Created on 3/12/14 12:59 PM
 */
@SuppressWarnings("UnusedDeclaration")
public class PciValidation extends SingleSequenceValidation {
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

        Map<String, NucleotideSequenceDocument> toAlign = new HashMap<String, NucleotideSequenceDocument>();
        String uidForNewSample = getUid(options.getGenus(), options.getSpecies(), sequence.getName());
        toAlign.put(uidForNewSample, sequence);

        List<NucleotideSequenceDocument> imported = ImportUtilities.importNucleotidesFastaFile(barcodesFile);
        for (NucleotideSequenceDocument importedSeq : imported) {
            String uid = importedSeq.getName();
            if(!Pattern.matches(BARCODE_DEF_LINE_REGEX, uid)) {
                throw new DocumentOperationException(uid + " in Barcodes file for PCI is not in the correct format.  Needs to match Genus_Species_ID");
            }
            if(toAlign.containsKey(uid)) {
                throw new DocumentOperationException("Barcodes file for PCI contains duplicate UID: " + uid);
            }
            toAlign.put(uid, importedSeq);
        }

        SequenceAlignmentDocument alignment = AlignmentUtilities.performAlignment(new ArrayList<NucleotideSequenceDocument>(toAlign.values()));
        try {
            File inputAlignmentFile = createPciInputFile(alignment, Collections.singletonMap(sequence.getName(), uidForNewSample));
            File newUidFile = createNewUidFile(uidForNewSample);

            File outputFile = runPci(inputAlignmentFile, newUidFile);
            return getFactFromOutputFile(outputFile, uidForNewSample);
        } catch (IOException e) {
            throw new DocumentOperationException("Failed to write out alignment for input to PCI: " + e.getMessage(), e);
        } catch (PciValidationException e) {
            return new PciResultFact(false, 0.0, e.getMessage());
        }
    }

    private static ResultFact getFactFromOutputFile(File outputFile, String uid) throws IOException, PciValidationException {
        double pDistance = getPDistanceFromFile(outputFile, uid);
        return new PciResultFact(true, pDistance, "");
    }

    private static double getPDistanceFromFile(File outputFile, String uid) throws IOException, PciValidationException {
        BufferedReader reader = new BufferedReader(new FileReader(outputFile));
        try {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                String[] lineParts = currentLine.trim().split("\\s+");
                if(lineParts.length != 2) {
                    continue;
                }
                String lineUid = lineParts[0];
                if(lineUid.equals(uid)) {
                    try {
                        return Double.parseDouble(lineParts[1]);
                    } catch (NumberFormatException e) {
                        throw new PciValidationException("PCI program produced invalid output.  " + lineParts[1] + " was not a numerical value.", e);
                    }
                }
            }
        } finally {
            GeneralUtilities.attemptClose(reader);
        }
        return 0;
    }

    /**
     *  An error that occurred while running the PCI program and parsing the output.
     */
    private static class PciValidationException extends Exception {
        public PciValidationException(String message) {
            super(message);
        }

        public PciValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private File runPci(File inputAlignmentFile, File newUidFile) throws IOException, DocumentOperationException, PciValidationException {
        File output = FileUtilities.createTempFile("output", ".txt", false);

        File programFolder = FileUtilities.getResourceForClass(PciValidation.class, "program");
        if(!programFolder.exists()) {
            throw new DocumentOperationException("PCI program missing from Barcode Validator plugin.  Please re-install.  " +
                    "Contact Biomatters if this still occurs after re-installing the plugin.");
        }
        File validatorScriptFile = new File(programFolder, "validator_pci.pl");

        final StringBuilder stdErrBuilder = new StringBuilder();

        Execution execution = new Execution(new String[]{
                "perl",
                validatorScriptFile.getAbsolutePath(),
                "-i",
                inputAlignmentFile.getAbsolutePath(),
                "-o",
                output.getAbsolutePath(),
                "-s",
                newUidFile.getAbsolutePath()
        }, ProgressListener.EMPTY, new Execution.OutputListener() {
            @Override
            public void stdoutWritten(String s) {
            }

            @Override
            public void stderrWritten(String s) {
                stdErrBuilder.append(s);
            }
        }, (String)null, false);
        try {
            execution.setWorkingDirectory(programFolder.getAbsolutePath());
            int exitCode = execution.execute();
            if(exitCode != 0) {
                throw new PciValidationException("PCI program exited abnormally (exit code = " + exitCode + "): " + stdErrBuilder.toString());
            }
            return output;
        } catch (InterruptedException e) {
            throw new DocumentOperationException("Execution of validation was interrupted");
        }
    }

    /**
     * Produces a file that can be used to specify new samples to the PCI program (using -s).
     *
     * @param sequenceUid The sequence that should have its UID in the file
     * @return A plain text file with the UID of the sequence in it. Obtained by calling {@link #getUid(String, String, String)}
     * @throws IOException if there is a problem writing the file
     */
    private static File createNewUidFile(String sequenceUid) throws IOException {
        File newUids = FileUtilities.createTempFile("new", ".txt", false);
        FileUtilities.writeTextToFile(newUids, sequenceUid);
        return newUids;
    }

    private static final String BARCODE_DEF_LINE_REGEX = ".+_.+_.+";

    /**
     * Generates the input file to the PCI program (-i).  The input file will name sequences using {@link #getUid(String, String, String)}
     *
     * @param alignment The alignment to write out to the PCI input file
     * @return The PCI input file
     * @throws IOException if there is a problem writing the file
     */
    private static File createPciInputFile(SequenceAlignmentDocument alignment, Map<String, String> renameMap) throws IOException {
        File outputFile = FileUtilities.createTempFile("alignment", ".fasta", false);
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        try {
            for (SequenceDocument alignedSequence : alignment.getSequences()) {
                String uid = alignedSequence.getName();
                if(renameMap.containsKey(uid)) {
                    uid = renameMap.get(uid);
                }

                writer.write(">" + uid);
                writer.newLine();
                System.out.println(uid);

                writer.write(alignedSequence.getSequenceString());
                writer.newLine();
            }
        } finally {
            GeneralUtilities.attemptClose(writer);
        }


        return outputFile;
    }

    /**
     * The UID consists of Genus_Species_ID.
     *
     * <strong>Note</strong>: Any white space in the sequence name will be replaced by an dash "-".  This is because the
     * compressed barcode format used as input for the PCI program is based on strict FASTA, which does not have spaces
     * in the names of sequences.  <a href="http://www.ncbi.nlm.nih.gov/CBBresearch/Spouge/html_ncbi/html/bib/119.html#The%20Format%20of%20an%20Compressed%20Barcode%20File">Details here.</a>.
     * Underscore is replaced because it is a special character in the UID used as a separator.
     *
     * @param genus The genus name
     * @param species The species name
     * @param name The name of the sequence
     * @return The UID of the sequence as it should appear in the compressed barcode format.
     *
     */
    private static String getUid(@Nonnull String genus, @Nonnull String species, @Nonnull String name) {
        return genus + "_" + species + "_" + name.replaceAll("[_\\s]+", "-");
    }
}

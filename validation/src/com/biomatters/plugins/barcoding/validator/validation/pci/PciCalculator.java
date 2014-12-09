package com.biomatters.plugins.barcoding.validator.validation.pci;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.utilities.Execution;
import com.biomatters.geneious.publicapi.utilities.FileUtilities;
import com.biomatters.geneious.publicapi.utilities.GeneralUtilities;
import com.biomatters.geneious.publicapi.utilities.StringUtilities;
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
public class PciCalculator {

    public Map<NucleotideGraphSequenceDocument, Double> calculate(List<NucleotideGraphSequenceDocument> sequences, PciCalculationOptions options) throws DocumentOperationException {
        File barcodesFile = new File(options.getPathToBarcodesFile());
        if(!barcodesFile.exists()) {
            throw new DocumentOperationException("Barcodes file for PCI validation did not exist: " + barcodesFile.getAbsolutePath());
        }

        Map<String, NucleotideGraphSequenceDocument> newSamples = new HashMap<String, NucleotideGraphSequenceDocument>();
        for (NucleotideGraphSequenceDocument sequence : sequences) {
            String uidForNewSample = getUid(options.getGenus(), options.getSpecies(), sequence.getName());
            newSamples.put(uidForNewSample, sequence);
        }

        Map<String, NucleotideSequenceDocument> toAlign = new HashMap<String, NucleotideSequenceDocument>();
        toAlign.putAll(newSamples);


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
            File inputAlignmentFile = createPciInputFile(alignment, getRenameMap(newSamples));
            File newUidFile = createNewUidFile(newSamples.keySet());

            File outputFile = runPci(inputAlignmentFile, newUidFile);
            return getPciScoresForDocuments(newSamples, outputFile);
        } catch (IOException e) {
            throw new DocumentOperationException("Failed to write out alignment for input to PCI: " + e.getMessage(), e);
        } catch (PciValidationException e) {
            return null; // todo
        }
    }

    private Map<NucleotideGraphSequenceDocument, Double> getPciScoresForDocuments(Map<String, NucleotideGraphSequenceDocument> newSamples, File outputFile) throws IOException, PciValidationException {
        Map<String, Double> scores = getPDistancesFromFile(outputFile, newSamples.keySet());
        Map<NucleotideGraphSequenceDocument, Double> result = new HashMap<NucleotideGraphSequenceDocument, Double>();
        for (String uid : newSamples.keySet()) {
            result.put(newSamples.get(uid), scores.get(uid));
        }
        return result;
    }

    private Map<String, String> getRenameMap(Map<String, NucleotideGraphSequenceDocument> newSamples) {
        Map<String, String> renameMap = new HashMap<String, String>();

        for (Map.Entry<String, NucleotideGraphSequenceDocument> entry : newSamples.entrySet()) {
            renameMap.put(entry.getValue().getName(), entry.getKey());
        }

        return renameMap;
    }

    private static Map<String, Double> getPDistancesFromFile(File outputFile, Collection<String> uids) throws IOException, PciValidationException {
        Map<String, Double> result = new HashMap<String, Double>();

        BufferedReader reader = new BufferedReader(new FileReader(outputFile));
        try {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                String[] lineParts = currentLine.trim().split("\\s+");
                if(lineParts.length != 2) {
                    continue;
                }
                String lineUid = lineParts[0];
                if(uids.contains(lineUid)) {
                    try {
                        result.put(lineUid, Double.parseDouble(lineParts[1]));
                    } catch (NumberFormatException e) {
                        throw new PciValidationException("PCI program produced invalid output.  " + lineParts[1] + " was not a numerical value.", e);
                    }
                }
            }
        } finally {
            GeneralUtilities.attemptClose(reader);
        }
        return result;
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

        File programFolder = FileUtilities.getResourceForClass(PciCalculator.class, "program");
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
     * @param sequenceUids The UIDs of sequences that are new samples
     * @return A plain text file with the UID of the sequence in it. Obtained by calling {@link #getUid(String, String, String)}
     * @throws IOException if there is a problem writing the file
     */
    private static File createNewUidFile(Collection<String> sequenceUids) throws IOException {
        File newUids = FileUtilities.createTempFile("new", ".txt", false);
        FileUtilities.writeTextToFile(newUids, StringUtilities.join("\n", sequenceUids));
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

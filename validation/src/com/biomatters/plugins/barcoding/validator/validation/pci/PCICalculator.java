package com.biomatters.plugins.barcoding.validator.validation.pci;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.documents.PluginDocument;
import com.biomatters.geneious.publicapi.documents.URN;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.geneious.publicapi.utilities.Execution;
import com.biomatters.geneious.publicapi.utilities.FileUtilities;
import com.biomatters.geneious.publicapi.utilities.GeneralUtilities;
import com.biomatters.geneious.publicapi.utilities.StringUtilities;
import com.biomatters.plugins.barcoding.validator.validation.utilities.AlignmentUtilities;
import com.biomatters.plugins.barcoding.validator.validation.utilities.ImportUtilities;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import jebl.util.Cancelable;
import jebl.util.CompositeProgressListener;
import jebl.util.ProgressListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Matthew Cheung
 *         Created on 3/12/14 12:59 PM
 */
public class PCICalculator {

    /**
     * Calculates the PCI values for new samples compared with a reference barcode database.
     *
     * @param sequenceUrns The document {@link com.biomatters.geneious.publicapi.documents.URN}s of the new samples
     * @param options See {@link PCICalculatorOptions}
     * @param progressListener to report progress to and to check cancellation status
     * @return a map from {@link URN} to PCI value.  Or null  if the calculation was not run.
     * @throws DocumentOperationException if something goes wrong running the PCI calculation
     */
    public static Map<URN, Double> calculate(Map<URN, GenusAndSpecies> sequenceUrns, PCICalculatorOptions options, Options alignmentOptions, ProgressListener progressListener) throws DocumentOperationException {

        CompositeProgressListener overallProgress = new CompositeProgressListener(progressListener, 0.1, 0.6, 0.3);

        overallProgress.beginSubtask("Importing reference barcodes");
        String pathToBarcodesFile = options.getPathToBarcodesFile();
        if(pathToBarcodesFile.trim().isEmpty()) {
            return null;
        }
        File barcodesFile = new File(pathToBarcodesFile);
        if(!barcodesFile.exists()) {
            throw new DocumentOperationException("Barcodes file for PCI validation did not exist: " + barcodesFile.getAbsolutePath());
        }

        BiMap<String, AnnotatedPluginDocument> newSamples = HashBiMap.create();
        for (Map.Entry<URN, GenusAndSpecies> entry : sequenceUrns.entrySet()) {
            URN urn = entry.getKey();
            AnnotatedPluginDocument apd = DocumentUtilities.getDocumentByURN(urn);
            if(apd == null) {
                throw new IllegalStateException("Failed to locate document for URN=" + urn.toString());
            }

            GenusAndSpecies genusAndSpecies = entry.getValue();
            String uidForNewSample = getUid(genusAndSpecies, apd.getName());
            if(newSamples.containsKey(uidForNewSample)) {
                // If there are duplicate names, we'll just give it a random UUID
                uidForNewSample = getUid(genusAndSpecies, UUID.randomUUID().toString());
            }
            newSamples.put(uidForNewSample, apd);
        }

        Map<String, NucleotideSequenceDocument> toAlign = new HashMap<String, NucleotideSequenceDocument>();
        for (Map.Entry<String, AnnotatedPluginDocument> entry : newSamples.entrySet()) {
            AnnotatedPluginDocument apd = entry.getValue();
            PluginDocument seqDoc = apd.getDocumentOrNull();
            if(seqDoc == null) {
                throw new DocumentOperationException("Failed to load document: " + apd.getName());
            } else if(!(seqDoc instanceof NucleotideSequenceDocument)) {
                throw new IllegalStateException("Input document " + apd.getName() + " was not a NucleotideSequenceDocument");
            }
            toAlign.put(entry.getKey(), (NucleotideSequenceDocument)seqDoc);
        }


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
        if(overallProgress.isCanceled()) {
            throw new DocumentOperationException.Canceled();
        }

        overallProgress.beginSubtask();
        SequenceAlignmentDocument alignment = AlignmentUtilities.performAlignment(
                new ArrayList<NucleotideSequenceDocument>(toAlign.values()),
                alignmentOptions, overallProgress);
        if(overallProgress.isCanceled()) {
            throw new DocumentOperationException.Canceled();
        }

        overallProgress.beginSubtask("Computing PCI values");
        try {
            File inputAlignmentFile = createPciInputFile(alignment, newSamples.inverse());
            File newUidFile = createNewUidFile(newSamples.keySet());
            File outputFile = runPci(inputAlignmentFile, newUidFile, overallProgress);
            return getPciScoresForDocuments(newSamples, outputFile);
        } catch (IOException e) {
            throw new DocumentOperationException("Failed to write out alignment for input to PCI: " + e.getMessage(), e);
        } catch (PciValidationException e) {
            Dialogs.showMessageDialog("Encountered the following error while attempting to calculate PCI values: " + e.getMessage(), "Failed to Calculate PCI Values");
            return null;
        }
    }

    private static Map<URN, Double> getPciScoresForDocuments(Map<String, AnnotatedPluginDocument> newSamples, File outputFile) throws IOException, PciValidationException {
        Map<String, Double> scores = getPDistancesFromFile(outputFile, newSamples.keySet());
        Map<URN, Double> result = new HashMap<URN, Double>();
        for (String uid : newSamples.keySet()) {
            result.put(newSamples.get(uid).getURN(), scores.get(uid));
        }
        return result;
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

    private static File runPci(File inputAlignmentFile, File newUidFile, Cancelable cancelable) throws IOException, DocumentOperationException, PciValidationException {
        File output = FileUtilities.createTempFile("output", ".txt", false);

        File programFolder = FileUtilities.getResourceForClass(PCICalculator.class, "program");
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
        }, cancelable, new Execution.OutputListener() {
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
     * @return A plain text file with the UID of the sequence in it. Obtained by calling {@link #getUid(com.biomatters.plugins.barcoding.validator.validation.pci.PCICalculator.GenusAndSpecies, String)}
     * @throws IOException if there is a problem writing the file
     */
    private static File createNewUidFile(Collection<String> sequenceUids) throws IOException {
        File newUids = FileUtilities.createTempFile("new", ".txt", false);
        FileUtilities.writeTextToFile(newUids, StringUtilities.join("\n", sequenceUids));
        return newUids;
    }

    private static final String BARCODE_DEF_LINE_REGEX = ".+_.+_.+";

    /**
     * Generates the input file to the PCI program (-i).  The input file will name sequences using {@link #getUid(com.biomatters.plugins.barcoding.validator.validation.pci.PCICalculator.GenusAndSpecies, String)}
     *
     * @param alignment The alignment to write out to the PCI input file
     * @param renameMap A mapping from {@link com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument} to the UUID
     *                  if the sequence name should not be used
     * @return The PCI input file
     * @throws IOException if there is a problem writing the file
     */
    private static File createPciInputFile(SequenceAlignmentDocument alignment, Map<AnnotatedPluginDocument, String> renameMap) throws IOException {
        File outputFile = FileUtilities.createTempFile("alignment", ".fasta", false);
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        try {
            for (int i = 0; i < alignment.getNumberOfSequences(); i++) {
                SequenceDocument alignedSequence = alignment.getSequence(i);
                AnnotatedPluginDocument refDoc = alignment.getReferencedDocument(i);
                String uid = renameMap.get(refDoc);
                if(uid == null) {
                    uid = alignedSequence.getName();
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
     * @param genusAndSpecies The genus and species
     * @param name The name of the sequence
     * @return The UID of the sequence as it should appear in the compressed barcode format.
     *
     */
    private static String getUid(@Nullable GenusAndSpecies genusAndSpecies, @Nonnull String name) {
        String sanitizedName = name.replaceAll("[_\\s]+", "-");
        if(genusAndSpecies == null) {
            return "Unknown_Unknown_" + sanitizedName;
        } else {
            return genusAndSpecies.genus + "_" + genusAndSpecies.species + "_" + sanitizedName;
        }
    }

    public static class GenusAndSpecies {
        final String genus;
        final String species;

        public GenusAndSpecies(String genus, String species) {
            this.genus = genus;
            this.species = species;
        }
    }
}

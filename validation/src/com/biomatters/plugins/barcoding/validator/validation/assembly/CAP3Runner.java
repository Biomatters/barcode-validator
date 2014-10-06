package com.biomatters.plugins.barcoding.validator.validation.assembly;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.utilities.Execution;
import com.biomatters.geneious.publicapi.utilities.FileUtilities;
import com.biomatters.geneious.publicapi.utilities.SystemUtilities;
import com.biomatters.plugins.barcoding.validator.validation.utilities.ImportUtilities;
import jebl.util.ProgressListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Functionality for utilizing CAP3. Non-instantiable.
 *
 * @author Gen Li
 *         Created on 22/08/14 12:07 PM
 */
public class CAP3Runner {
    private static final String CAP3_ASSEMBLER_RESULT_FILE_EXTENSION = ".cap.ace";

    private static final String MIN_OVERLAP_LENGTH_COMMANDLINE_OPTION   = "-o";
    private static final String MIN_OVERLAP_IDENTITY_COMMANDLINE_OPTION = "-p";

    private CAP3Runner() {
    }

    /**
     * Assembles contigs.
     *
     * @param sequences Sequences.
     * @param executablePath CAP3 executable path.
     * @param minOverlapLength Minimum overlap length.
     * @param minOverlapIdentity Minimum overlap identity.
     * @return Contigs.
     * @throws DocumentOperationException
     */
    public static List<SequenceAlignmentDocument> assemble(List<NucleotideGraphSequenceDocument> sequences,
                                                           String executablePath,
                                                           int minOverlapLength,
                                                           int minOverlapIdentity) throws DocumentOperationException {
        try {
            String resultFilePath = runCap3Assembler(createFastaFile(sequences), executablePath, minOverlapLength, minOverlapIdentity);
            if(assemblyFailed(sequences, resultFilePath)) {
                return Collections.emptyList();
            }
            return ImportUtilities.importContigs(resultFilePath);
        } catch (DocumentOperationException e) {
            throw new DocumentOperationException("Could not assemble contigs: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            throw new DocumentOperationException("Could not assemble contigs: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new DocumentOperationException("Could not assemble contigs: " + e.getMessage(), e);
        }
    }

    /**
     * Hack method that tries to detect if assembly failed using the result file.  This is required because:
     * <ul>
     *     <li>CAP3 runs successfully and produces an almost empty Ace file when assembly fails.</li>
     *     <li>The AceDocumentImporter will throw an Exception and show a dialog when it encounters this bad file.</li>
     * </ul>
     * @param sequences The sequences that were to be assembled
     * @param resultFilePath The result file from CAP3
     * @return true iff the assembly process failed
     */
    private static boolean assemblyFailed(List<NucleotideGraphSequenceDocument> sequences, String resultFilePath) {
        File resultFile = new File(resultFilePath);
        // The result file would use at least a byte per character in the sequence name.  If it doesn't it probably
        // does not contain an assembly
        long sizeOfDocs = 0;
        for (NucleotideGraphSequenceDocument sequence : sequences) {
            sizeOfDocs += sequence.getName().length();
        }
        return !resultFile.exists() || resultFile.length() < sizeOfDocs;
    }

    /**
     * Runs CAP3.
     *
     * @param fastafilePath Fasta file path.
     * @param executablePath CAP3 executable path.
     * @param minOverlapLength Minimum overlap length.
     * @param minOverlapIdentity Minimum overlap identity.
     * @return {@value #CAP3_ASSEMBLER_RESULT_FILE_EXTENSION} output file path.
     * @throws DocumentOperationException
     * @throws InterruptedException
     * @throws IOException
     */
    private static String runCap3Assembler(String fastafilePath, String executablePath, int minOverlapLength, int minOverlapIdentity)
            throws DocumentOperationException, InterruptedException, IOException {
        Cap3OutputListener listener = new Cap3OutputListener();

        /* Run. */
        Execution exec = new Execution(
                new String[] {
                        executablePath,
                        fastafilePath,
                        MIN_OVERLAP_LENGTH_COMMANDLINE_OPTION,
                        String.valueOf(minOverlapLength),
                        MIN_OVERLAP_IDENTITY_COMMANDLINE_OPTION,
                        String.valueOf(minOverlapIdentity)
                },
                ProgressListener.EMPTY,
                listener,
                "",
                false
        );
        exec.setWorkingDirectory(fastafilePath.substring(0, fastafilePath.lastIndexOf(File.separator)));
        int exitCode = exec.execute(getEnvironmentToRunCap3());

        if (exitCode != 0) {
            throw new DocumentOperationException("CAP3 failed with exit code " + exitCode + ":\n\n" + listener.getStderrOutput());
        }

        return fastafilePath + CAP3_ASSEMBLER_RESULT_FILE_EXTENSION;
    }

    /**
     * Creates fasta file from sequences.
     *
     * @param sequences Sequences.
     * @return Fasta file path.
     */
    private static String createFastaFile(List<NucleotideGraphSequenceDocument> sequences) throws IOException {
        File fastaFile = FileUtilities.createTempFile("temp", ".fasta", false);

        BufferedWriter writer = new BufferedWriter(new FileWriter(fastaFile));
        writer.write(toFastaFormat(sequences));
        writer.close();

        return fastaFile.getAbsolutePath();
    }

    /**
     * Generates fasta output from sequences.
     *
     * @param sequences Sequences.
     * @return Fasta output.
     */
    private static String toFastaFormat(List<NucleotideGraphSequenceDocument> sequences) {
        StringBuilder fastaOutput = new StringBuilder();

        /* Generate fasta output. */
        for (NucleotideGraphSequenceDocument sequence : sequences) {
            fastaOutput.append(">").append(sequence.getName()).append(" ").append(sequence.getDescription()).append("\n")
                       .append(sequence.getSequenceString().toUpperCase()).append("\n");
        }

        /* Remove trailing new line. */
        if (fastaOutput.length() > 0) {
            fastaOutput.deleteCharAt(fastaOutput.length() - 1);
        }

        return fastaOutput.toString();
    }

    private static Map<String, String> getEnvironmentToRunCap3() throws DocumentOperationException {
        if(SystemUtilities.isWindows()) {
            String dllFileName = "cygwin1.dll";
            File cygwinDll = FileUtilities.getResourceForClass(CAP3Runner.class, dllFileName);
            if(cygwinDll == null) {
                throw new DocumentOperationException(dllFileName + " missing from plugin.  Try reinstalling.");
            }
            String pathVariableName = "Path";
            return Collections.singletonMap(pathVariableName,
                    System.getProperty(pathVariableName) + ";" + cygwinDll.getAbsolutePath());
        } else {
            return Collections.emptyMap();
        }
    }
}
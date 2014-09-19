package com.biomatters.plugins.barcoding.validator.validation.assembly;

import com.biomatters.geneious.publicapi.documents.sequence.*;
import com.biomatters.geneious.publicapi.plugin.*;
import com.biomatters.geneious.publicapi.utilities.Execution;
import com.biomatters.geneious.publicapi.utilities.FileUtilities;

import com.biomatters.plugins.barcoding.validator.validation.utilities.ImportUtilities;

import jebl.util.ProgressListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Functionality for utilizing the Cap3 assembler. Non-instantiable.
 *
 * @author Gen Li
 *         Created on 22/08/14 12:07 PM
 */
public class Cap3AssemblerRunner {
    private static final String CAP3_ASSEMBLER_WINDOWS_FILENAME = "cap3.exe";
    private static final String CAP3_ASSEMBLER_MAC_FILENAME     = "cap3.osx";
    private static final String CAP3_ASSEMBLER_LINUX_FILENAME   = "cap3.linux";

    private static final String CAP3_ASSEMBLER_RESULT_FILE_EXTENSION = ".cap.ace";

    private static final String MIN_OVERLAP_LENGTH_COMMANDLINE_OPTION   = "-o";
    private static final String MIN_OVERLAP_IDENTITY_COMMANDLINE_OPTION = "-p";

    private Cap3AssemblerRunner() {
    }

    /**
     * Assembles contigs.
     *
     * @param sequences Sequences for assembling.
     * @param minOverlapLength Minimum overlap length value.
     * @param minOverlapIdentity Minimum overlap identity value.
     * @return Assembled contigs.
     * @throws DocumentOperationException
     */
    public static List<SequenceAlignmentDocument> assemble(List<NucleotideSequenceDocument> sequences,
                                                           int minOverlapLength,
                                                           int minOverlapIdentity) throws DocumentOperationException {
        try {
            return ImportUtilities.importContigs(executeCap3Assembler(createFastaFile(sequences),
                                                                      minOverlapLength,
                                                                      minOverlapIdentity));
        } catch (IllegalStateException e) {
            throw new DocumentOperationException("Could not assemble contigs: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            throw new DocumentOperationException("Could not assemble contigs: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new DocumentOperationException("Could not assemble contigs: " + e.getMessage(), e);
        }
    }

    /**
     * Executes the CAP3 assembler.
     *
     * @param fastaFilePath Path of fasta file input.
     * @param minOverlapLength Minimum overlap length value.
     * @param minOverlapIdentity Minimum overlap identity value.
     * @return Path of the {@value #CAP3_ASSEMBLER_RESULT_FILE_EXTENSION} output file.
     * @throws java.lang.IllegalStateException
     * @throws InterruptedException
     * @throws IOException
     */
    private static String executeCap3Assembler(String fastaFilePath, int minOverlapLength, int minOverlapIdentity)
            throws IllegalStateException, InterruptedException, IOException {
        Execution exec = new Execution(
                new String[] {
                        getCap3AssemblerFilePath(),
                        fastaFilePath,
                        MIN_OVERLAP_LENGTH_COMMANDLINE_OPTION,
                        String.valueOf(minOverlapLength),
                        MIN_OVERLAP_IDENTITY_COMMANDLINE_OPTION,
                        String.valueOf(minOverlapIdentity)
                },
                ProgressListener.EMPTY,
                new Cap3OutputListener(),
                (String)null,
                false
        );

        exec.setWorkingDirectory(fastaFilePath.substring(0, fastaFilePath.lastIndexOf(File.separator)));

        /* Execute the CAP3 assembler. */
        exec.execute();

        return fastaFilePath + CAP3_ASSEMBLER_RESULT_FILE_EXTENSION;
    }

    /**
     * Creates fasta file from sequences.
     *
     * @param sequences Sequences.
     * @return Path of fasta file.
     */
    private static String createFastaFile(List<NucleotideSequenceDocument> sequences) throws IOException {
        File fastaFile = FileUtilities.createTempFile("temp", ".fasta", false);

        BufferedWriter writer = new BufferedWriter(new FileWriter(fastaFile));

        writer.write(toFastaFileFormat(sequences));

        writer.close();

        return fastaFile.getAbsolutePath();
    }

    /**
     * Generates fasta file output from sequences.
     *
     * @param sequences Sequences.
     * @return Fasta file output.
     */
    private static String toFastaFileFormat(List<NucleotideSequenceDocument> sequences) {
        StringBuilder fastaOutput = new StringBuilder();

        /* Generate the fasta file output. */
        for (NucleotideSequenceDocument sequence : sequences)
            fastaOutput.append(">").append(sequence.getName()).append(" ").append(sequence.getDescription())
                    .append("\n")
                    .append(sequence.getSequenceString().toUpperCase())
                    .append("\n");

        if (fastaOutput.length() > 0)
            /* Remove the trailing new line character. */
            fastaOutput.deleteCharAt(fastaOutput.length() - 1);

        return fastaOutput.toString();
    }

    /**
     * Returns the path of the CAP3 assembler executable for the current OS.
     *
     * @return The path of the CAP3 assembler executable for the current OS.
     */
    private static String getCap3AssemblerFilePath() throws IllegalStateException {
        String result = Cap3AssemblerRunner.class.getResource(getCap3AssemblerFileName()).getPath().replace("%20", " ");

        if (result == null)
            throw new IllegalStateException("Missing plugin resource: " +
                                            "Try re-installing the plugin. " +
                                            "Contact support@mooreasoftware.org if the issue still persists.");

        return result;
    }

    /**
     * Returns the file name of the CAP3 assembler executable for the current OS.
     *
     * @return The file name of the CAP3 assembler executable for the current OS.
     * @throws IllegalStateException If no CAP3 assembler executable is available for the current OS.
     */
    private static String getCap3AssemblerFileName() throws IllegalStateException {
        String operatingSystem = System.getProperty("os.name").toLowerCase();

        if (operatingSystem.contains("windows"))
            return CAP3_ASSEMBLER_WINDOWS_FILENAME;
        else if (operatingSystem.contains("mac"))
            return CAP3_ASSEMBLER_MAC_FILENAME;
        else if (operatingSystem.contains("linux"))
            return CAP3_ASSEMBLER_LINUX_FILENAME;
        else
            throw new IllegalStateException("Unsupported operating system: " + operatingSystem);
    }
}
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
import java.net.URL;
import java.util.List;

/**
 * Functionality for utilizing the CAP3 assembler. Non-instantiable.
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
     * @param sequences Sequences.
     * @param minOverlapLength Minimum overlap length.
     * @param minOverlapIdentity Minimum overlap identity.
     * @return Contigs.
     * @throws DocumentOperationException
     */
    public static List<SequenceAlignmentDocument> assemble(List<NucleotideSequenceDocument> sequences,
                                                           int minOverlapLength,
                                                           int minOverlapIdentity) throws DocumentOperationException {
        try {
            return ImportUtilities.importContigs(runCap3Assembler(createFastaFile(sequences),
                                                                  minOverlapLength,
                                                                  minOverlapIdentity));
        } catch (DocumentOperationException e) {
            throw new DocumentOperationException("Could not assemble contigs: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            throw new DocumentOperationException("Could not assemble contigs: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new DocumentOperationException("Could not assemble contigs: " + e.getMessage(), e);
        }
    }

    /**
     * Runs the CAP3 assembler.
     *
     * @param fastaFilePath Fasta file path.
     * @param minOverlapLength Minimum overlap length.
     * @param minOverlapIdentity Minimum overlap identity.
     * @return {@value #CAP3_ASSEMBLER_RESULT_FILE_EXTENSION} output file path.
     * @throws DocumentOperationException
     * @throws InterruptedException
     * @throws IOException
     */
    private static String runCap3Assembler(String fastaFilePath, int minOverlapLength, int minOverlapIdentity)
            throws DocumentOperationException, InterruptedException, IOException {
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

        /* Set working directory as input fasta file's containing directory. */
        exec.setWorkingDirectory(fastaFilePath.substring(0, fastaFilePath.lastIndexOf(File.separator)));

        /* Run. */
        exec.execute();

        return fastaFilePath + CAP3_ASSEMBLER_RESULT_FILE_EXTENSION;
    }

    /**
     * Creates a fasta file from sequences.
     *
     * @param sequences Sequences.
     * @return Fasta file path.
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

        /* Generate fasta file output. */
        for (NucleotideSequenceDocument sequence : sequences) {
            fastaOutput.append(">").append(sequence.getName()).append(" ").append(sequence.getDescription())
                    .append("\n")
                    .append(sequence.getSequenceString().toUpperCase())
                    .append("\n");
        }

        /* Remove trailing new line. */
        if (fastaOutput.length() > 0) {
            fastaOutput.deleteCharAt(fastaOutput.length() - 1);
        }

        return fastaOutput.toString();
    }

    /**
     * Returns the path of the CAP3 assembler executable for the current OS.
     *
     * @return Path of the CAP3 assembler executable for the current OS.
     * @throws DocumentOperationException If a CAP3 assembler executable is not available for the current OS.
     */
    private static String getCap3AssemblerFilePath() throws DocumentOperationException {
        URL cap3AssemblerURL = Cap3AssemblerRunner.class.getResource(getCap3AssemblerFileName());

        if (cap3AssemblerURL == null) {
            throw new DocumentOperationException("Missing plugin resource: " +
                                                 "Try re-installing the plugin. " +
                                                 "Contact support@mooreasoftware.org if the issue still persists.");
        }

        return cap3AssemblerURL.getPath().replace("%20", " ");
    }

    /**
     * Returns the file name of the CAP3 assembler executable for the current OS.
     *
     * @return File name of the CAP3 assembler executable for the current OS.
     * @throws DocumentOperationException If a CAP3 assembler executable is not available for the current OS.
     */
    private static String getCap3AssemblerFileName() throws DocumentOperationException {
        String operatingSystem = System.getProperty("os.name").toLowerCase();

        if (operatingSystem.contains("windows")) {
            return CAP3_ASSEMBLER_WINDOWS_FILENAME;
        } else if (operatingSystem.contains("mac")) {
            return CAP3_ASSEMBLER_MAC_FILENAME;
        } else if (operatingSystem.contains("linux")) {
            return CAP3_ASSEMBLER_LINUX_FILENAME;
        } else {
            throw new DocumentOperationException("Unsupported operating system: " + operatingSystem);
        }
    }
}
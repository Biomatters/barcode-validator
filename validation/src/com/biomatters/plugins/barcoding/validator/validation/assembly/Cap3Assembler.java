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
 * CAP3 assembler class. Non-instantiable.
 *
 * @author Gen Li
 *         Created on 22/08/14 12:07 PM
 */
public class Cap3Assembler {
    private static final String CAP3_ASSEMBLER_WINDOWS_FILENAME = "cap3.exe";
    private static final String CAP3_ASSEMBLER_MAC_FILENAME     = "cap3.osx";
    private static final String CAP3_ASSEMBLER_LINUX_FILENAME   = "cap3.linux";

    private static final String CAP3_ASSEMBLER_RESULT_FILE_EXTENSION       = ".cap.ace";
    private static final String CAP3_ASSEMBLER_UNUSED_READS_FILE_EXTENSION = ".cap.singlets";

    private static final String MIN_OVERLAP_LENGTH_COMMANDLINE_OPTION   = "-o";
    private static final String MIN_OVERLAP_IDENTITY_COMMANDLINE_OPTION = "-p";

    private static final char CHAR_FOR_DELETION_PLACEHOLDER = 'x';

    private Cap3Assembler() {
    }

    /**
     * Assembles sequences into contigs.
     *
     * @param sequences sequences for assembling.
     * @param minOverlapLength minimum overlap length value.
     * @param minOverlapIdentity minimum overlap identity value.
     * @return contig assembled.
     * @throws DocumentOperationException
     */
    public static List<SequenceAlignmentDocument> assemble(List<NucleotideSequenceDocument> sequences,
                                                           int minOverlapLength,
                                                           int minOverlapIdentity)
            throws DocumentOperationException {
        return ImportUtilities.importContigsCap3Assembler(runCap3Assembler(createFastaFile(sequences),
                                                                           minOverlapLength,
                                                                           minOverlapIdentity));
    }

    /**
     * Executes CAP3 assembler.
     *
     * @param fastaFilePath path of fasta file input.
     * @param minOverlapLength minimum overlap length value.
     * @param minOverlapIdentity minimum overlap identity value.
     * @return path of {@value #CAP3_ASSEMBLER_RESULT_FILE_EXTENSION} output file.
     * @throws DocumentOperationException if error occurs during execution of CAP3 assembler.
     */
    private static String runCap3Assembler(String fastaFilePath, int minOverlapLength, int minOverlapIdentity)
            throws DocumentOperationException {
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

        /* Execute CAP3 assembler. */
        try {
            exec.execute();
        } catch (InterruptedException e) {
            throw new DocumentOperationException(e.getMessage(), e);
        } catch (IOException e) {
            throw new DocumentOperationException(e.getMessage(), e);
        }

        return fastaFilePath + CAP3_ASSEMBLER_RESULT_FILE_EXTENSION;
    }

    /**
     * Creates fasta file from NucleotideSequenceDocuments.
     *
     * @param sequences sequences used to create fasta file.
     * @return path of fasta file created.
     * @throws DocumentOperationException if error occurs during fasta file creation.
     */
    private static String createFastaFile(List<NucleotideSequenceDocument> sequences)
            throws DocumentOperationException {
        File fastaFile;

        try {
            fastaFile = FileUtilities.createTempFile("temp", ".fasta", false);

            BufferedWriter out = new BufferedWriter(new FileWriter(fastaFile));

            out.write(generateFastaFileOutput(sequences));

            out.close();

            return fastaFile.getAbsolutePath();
        } catch (IOException e) {
            throw new DocumentOperationException("Failed to write FASTA file: " + e.getMessage(), e);
        }
    }

    /**
     * Generates fasta file content.
     * @param sequences sequences used to create fasta file.
     * @return contents to be written to fasta file.
     */
    private static String generateFastaFileOutput(List<NucleotideSequenceDocument> sequences) {
        StringBuilder fastaOutput = new StringBuilder();

        for (NucleotideSequenceDocument sequence : sequences) {
            StringBuilder sequenceProcessor = new StringBuilder(sequence.getSequenceString());
            StringBuilder finalSequenceBuilder = new StringBuilder();

            /* Replace chars for deletion with {@value #CHAR_FOR_DELETION_PLACEHOLDER}. */
            for (SequenceAnnotation annotation : sequence.getSequenceAnnotations())
                if (annotation.getType().equals(SequenceAnnotation.TYPE_TRIMMED)) {
                    SequenceAnnotationInterval trimInterval = annotation.getInterval();

                    for (int i = trimInterval.getFrom() - 1; i < trimInterval.getTo(); i++)
                        sequenceProcessor.setCharAt(i, CHAR_FOR_DELETION_PLACEHOLDER);
                }

            /* Generate trimmed sequence. */
            for (int i = 0; i < sequenceProcessor.length(); i++) {
                char c = sequenceProcessor.charAt(i);

                if (c != CHAR_FOR_DELETION_PLACEHOLDER)
                    finalSequenceBuilder.append(c);
            }

            /* Generate fasta file output. */
            fastaOutput.append(">").append(sequence.getName()).append(" ").append(sequence.getDescription()).append("\n")
                       .append(finalSequenceBuilder.toString().toUpperCase()).append("\n");
        }

        fastaOutput.deleteCharAt(fastaOutput.length() - 1); // Removes last new line character.

        return fastaOutput.toString();
    }

    /**
     * Returns path of CAP3 assembler executable for current operating system.
     *
     * @return path of CAP3 assembler executable for current operating system.
     * @throws DocumentOperationException if no CAP3 assembler executable available for current operating system.
     */
    private static String getCap3AssemblerFilePath() throws DocumentOperationException {
        return Cap3Assembler.class.getResource(getCap3AssemblerFilename()).getPath().replace("%20", " ");
    }

    /**
     * Returns file name of CAP3 assembler executable for current operating system.
     *
     * @return file name of CAP3 assembler executable for current operating system.
     * @throws DocumentOperationException if no CAP3 assembler executable available for current operating system.
     */
    private static String getCap3AssemblerFilename() throws DocumentOperationException {
        String operatingSystem = System.getProperty("os.name").toLowerCase();

        if (operatingSystem.contains("windows"))
            return CAP3_ASSEMBLER_WINDOWS_FILENAME;
        else if (operatingSystem.contains("mac"))
            return CAP3_ASSEMBLER_MAC_FILENAME;
        else if (operatingSystem.contains("linux"))
            return CAP3_ASSEMBLER_LINUX_FILENAME;
        else
            throw new DocumentOperationException("Unsupported operating system: " + operatingSystem);
    }
}
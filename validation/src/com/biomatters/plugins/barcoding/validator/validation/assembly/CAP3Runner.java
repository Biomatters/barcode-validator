package com.biomatters.plugins.barcoding.validator.validation.assembly;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
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
            return ImportUtilities.importContigs(runCap3Assembler(createFastaFile(sequences),
                                                                  executablePath,
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
    private static String runCap3Assembler(String fastafilePath,
                                           String executablePath,
                                           int minOverlapLength,
                                           int minOverlapIdentity)
            throws DocumentOperationException, InterruptedException, IOException {
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
                new Cap3OutputListener(),
                (String)null,
                false
        );

        /* Set working directory as input fasta file's containing directory. */
        exec.setWorkingDirectory(fastafilePath.substring(0, fastafilePath.lastIndexOf(File.separator)));

        /* Run. */
        exec.execute();

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
}
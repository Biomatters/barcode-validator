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
 * Functionality for utilizing the CAP3 assembler. Non-instantiable.
 *
 * @author Gen Li
 *         Created on 22/08/14 12:07 PM
 */
public class Cap3AssemblerRunner {
    private static final String CAP3_ASSEMBLER_RESULT_FILE_EXTENSION = ".cap.ace";

    private static final String MIN_OVERLAP_LENGTH_COMMANDLINE_OPTION   = "-o";
    private static final String MIN_OVERLAP_IDENTITY_COMMANDLINE_OPTION = "-p";

    private Cap3AssemblerRunner() {
    }

    /**
     * Assembles contigs.
     *
     * @param sequences Sequences.
     * @param executableLocation The command to execute cap3.  Either "cap3" if it is on the path or the location of the executable.
     *@param minOverlapLength Minimum overlap length.
     * @param minOverlapIdentity Minimum overlap identity.
     * @return Contigs.
     * @throws DocumentOperationException
     */
    public static List<SequenceAlignmentDocument> assemble(List<NucleotideGraphSequenceDocument> sequences,
                                                           String executableLocation, int minOverlapLength,
                                                           int minOverlapIdentity) throws DocumentOperationException {
        try {
            return ImportUtilities.importContigs(runCap3Assembler(createFastaFile(sequences),
                    executableLocation, minOverlapLength,
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
     * @param executableLocation The command to execute cap3.  Either "cap3" if it is on the path or the location of the executable.
     * @param minOverlapLength Minimum overlap length.
     * @param minOverlapIdentity Minimum overlap identity.
     * @return {@value #CAP3_ASSEMBLER_RESULT_FILE_EXTENSION} output file path.
     * @throws DocumentOperationException
     * @throws InterruptedException
     * @throws IOException
     */
    private static String runCap3Assembler(String fastaFilePath, String executableLocation, int minOverlapLength, int minOverlapIdentity)
            throws DocumentOperationException, InterruptedException, IOException {
        Execution exec = new Execution(
                new String[] {
                        executableLocation,
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
    private static String createFastaFile(List<NucleotideGraphSequenceDocument> sequences) throws IOException {
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
    private static String toFastaFileFormat(List<NucleotideGraphSequenceDocument> sequences) {
        StringBuilder fastaOutput = new StringBuilder();

        /* Generate fasta file output. */
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
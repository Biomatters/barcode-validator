package com.biomatters.plugins.barcoding.validator.validation.assembly;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.PluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotation;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationInterval;
import com.biomatters.geneious.publicapi.plugin.DocumentFileImporter;
import com.biomatters.geneious.publicapi.plugin.DocumentImportException;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.PluginUtilities;
import com.biomatters.geneious.publicapi.utilities.Execution;
import com.biomatters.geneious.publicapi.utilities.FileUtilities;
import jebl.util.ProgressListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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

    private static final String MIN_OVERLAP_LENGTH_OPTION_NAME   = "-o";
    private static final String MIN_OVERLAP_IDENTITY_OPTION_NAME = "-p";

    private static final char CHAR_FOR_DELETION_PLACEHOLDER = 'x';

    private Cap3Assembler() {
    }

    /**
     * Assembles sequences into contigs.
     *
     * @param sequences sequences for assembling.
     * @param minOverlapLength minimum overlap length value.
     * @param minOverlapIdentity minimum overlap identity value.
     * @return contigs assembled.
     * @throws DocumentOperationException
     */
    public static List<PluginDocument> assemble(List<NucleotideSequenceDocument> sequences,
                                                String minOverlapLength,
                                                String minOverlapIdentity) throws DocumentOperationException {
        return importContigs(executeCap3Assembler(createFastaFile(sequences), minOverlapLength, minOverlapIdentity));
    }

    /**
     * Imports contigs.
     *
     * @param result contigs imported.
     * @return paths of {@value #CAP3_ASSEMBLER_RESULT_FILE_EXTENSION} and
     * {@value #CAP3_ASSEMBLER_UNUSED_READS_FILE_EXTENSION} CAP3 assembler output files.
     * @throws DocumentOperationException if error occurs during contig import.
     */
    private static List<PluginDocument> importContigs(Cap3AssemblyResult result) throws DocumentOperationException {
        final List<PluginDocument> contigs = new ArrayList<PluginDocument>();

        File contigFile = new File(result.RESULT_FILEPATH);

        DocumentFileImporter importer = PluginUtilities.getDocumentFileImporter("ace");

        DocumentFileImporter.ImportCallback importCallback = new DocumentFileImporter.ImportCallback() {
            public AnnotatedPluginDocument addDocument(PluginDocument document) {
                contigs.add(document);
                return null;
            }

            public AnnotatedPluginDocument addDocument(AnnotatedPluginDocument annotatedDocument) {
                throw new IllegalStateException("CAP3Assembler expects only PluginDocuments from Ace importer");
            }
        };

        /* Imports contigs. */
        try {
            importer.importDocuments(contigFile, importCallback, ProgressListener.EMPTY);
        } catch (IOException e) {
            throw new DocumentOperationException(e.getMessage(), e);
        } catch (DocumentImportException e) {
            throw new DocumentOperationException(e.getMessage(), e);
        }

        return contigs;
    }

    /**
     * Executes CAP3 assembler.
     *
     * @param fastaFilePath path of fasta file input.
     * @param minOverlapLength minimum overlap length value.
     * @param minOverlapIdentity minimum overlap identity value.
     * @return paths of {@value #CAP3_ASSEMBLER_RESULT_FILE_EXTENSION} and
     * {@value #CAP3_ASSEMBLER_UNUSED_READS_FILE_EXTENSION} CAP3 assembler output files.
     * @throws DocumentOperationException if error occurs during execution of CAP3 assembler.
     */
    private static Cap3AssemblyResult executeCap3Assembler(String fastaFilePath, String minOverlapLength, String minOverlapIdentity)
            throws DocumentOperationException {
        Execution exec = new Execution(
                new String[] {
                    getCap3AssemblerFilePath(),
                    fastaFilePath,
                    MIN_OVERLAP_LENGTH_OPTION_NAME, minOverlapLength,
                    MIN_OVERLAP_IDENTITY_OPTION_NAME, minOverlapIdentity
                },
                new ProgressListener() {
                    @Override
                    protected void _setProgress(double v) {
                    }

                    @Override
                    protected void _setIndeterminateProgress() {
                    }

                    @Override
                    protected void _setMessage(String s) {
                    }

                    @Override
                    public boolean isCanceled() {
                        return false;
                    }
                },
                new Cap3OutputListener(),
                (String) null,
                false);

        exec.setWorkingDirectory(fastaFilePath.substring(0, fastaFilePath.lastIndexOf(File.separator)));

        /* Execute CAP3 assembler. */
        try {
            exec.execute();
        } catch (InterruptedException e) {
            throw new DocumentOperationException(e.getMessage(), e);
        } catch (IOException e) {
            throw new DocumentOperationException(e.getMessage(), e);
        }

        return new Cap3AssemblyResult(fastaFilePath + CAP3_ASSEMBLER_RESULT_FILE_EXTENSION,
                                      fastaFilePath + CAP3_ASSEMBLER_UNUSED_READS_FILE_EXTENSION);
    }

    /**
     * Creates fasta file from NucleotideSequenceDocuments.
     *
     * @param nucleotideSequences sequences used to create fasta file.
     * @return path of fasta file created.
     * @throws DocumentOperationException if error occurs during fasta file creation.
     */
    private static String createFastaFile(List<NucleotideSequenceDocument> nucleotideSequences)
            throws DocumentOperationException {
        StringBuilder fastaOutput = new StringBuilder();
        for (NucleotideSequenceDocument document : nucleotideSequences) {
            StringBuilder sequence = new StringBuilder(document.getSequenceString());
            StringBuilder finalSequence = new StringBuilder();

            /* Replaces chars for deletion with {@value #CHAR_FOR_DELETION_PLACEHOLDER}. */
            for (SequenceAnnotation annotation : document.getSequenceAnnotations()) {
                if (annotation.getName().equals("Trimmed")) {
                    SequenceAnnotationInterval interval = annotation.getInterval();
                    for (int i = interval.getFrom() - 1; i < interval.getTo(); i++) {
                        sequence.setCharAt(i, CHAR_FOR_DELETION_PLACEHOLDER);
                    }
                }
            }

            /* Generates trimmed sequence. */
            for (int i = 0; i < sequence.length(); i++) {
                char c = sequence.charAt(i);
                if (c != CHAR_FOR_DELETION_PLACEHOLDER) {
                    finalSequence.append(c);
                }
            }

            /* Generates fasta file output. */
            fastaOutput.append(">" + document.getName() + " " + document.getDescription() + "\n")
                       .append(finalSequence.toString().toUpperCase() + "\n");
        }

        fastaOutput.deleteCharAt(fastaOutput.length() - 1); // Removes last new line character.

        /* Creates fasta file. */
        File fastaFile;
        try {
            fastaFile = FileUtilities.createTempFile("temp", ".fasta", false);

            BufferedWriter out = new BufferedWriter(new FileWriter(fastaFile));
            out.write(fastaOutput.toString());
            out.close();

            return fastaFile.getAbsolutePath();
        } catch (IOException e) {
            throw new DocumentOperationException("Failed to write FASTA file: " + e.getMessage(), e);
        }
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

    /**
     * Holds paths of {@value #CAP3_ASSEMBLER_RESULT_FILE_EXTENSION} and
     * {@value #CAP3_ASSEMBLER_UNUSED_READS_FILE_EXTENSION} CAP3 assembly output files.
     */
    private static class Cap3AssemblyResult {
        private String RESULT_FILEPATH;
        private String UNUSED_READS_FILEPATH;

        private Cap3AssemblyResult(String resultFilePath, String unusedReadsFilePath) {
            RESULT_FILEPATH = resultFilePath;
            UNUSED_READS_FILEPATH = unusedReadsFilePath;
        }
    }
}
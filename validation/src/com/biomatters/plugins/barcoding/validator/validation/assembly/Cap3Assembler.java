package com.biomatters.plugins.barcoding.validator.validation.assembly;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotation;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationInterval;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.utilities.FileUtilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @author Gen Li
 *         Created on 22/08/14 12:07 PM
 */
public class Cap3Assembler {
    private static final char CHAR_FOR_DELETION_PLACEHOLDER      = 'X';
    private static final String MIN_OVERLAP_LENGTH_OPTION_NAME   = "-o";
    private static final String MIN_OVERLAP_IDENTITY_OPTION_NAME = "-p";

    private File createFasta(List<NucleotideSequenceDocument> sequences) throws DocumentOperationException {
        StringBuilder fastaOutputString = new StringBuilder();
        for (NucleotideSequenceDocument sequence : sequences) {
            StringBuilder sequenceProcessor = new StringBuilder(sequence.getSequenceString());
            StringBuilder finalSequence = new StringBuilder();

            /* Replaces chars for deletion with {CHAR_FOR_DELETION_PLACEHOLDER}. */
            for (SequenceAnnotation annotation : sequence.getSequenceAnnotations()) {
                if (annotation.getName().equals("Trimmed")) {
                    SequenceAnnotationInterval interval = annotation.getInterval();
                    for (int i = interval.getFrom() - 1; i < interval.getTo(); i++) {
                        sequenceProcessor.setCharAt(i, CHAR_FOR_DELETION_PLACEHOLDER);
                    }
                }
            }

            /* Builds trimmed sequence. */
            for (int i = 0; i < sequenceProcessor.length(); i++) {
                char c = sequenceProcessor.charAt(i);
                if (c != CHAR_FOR_DELETION_PLACEHOLDER) {
                    finalSequence.append(c);
                }
            }

            fastaOutputString.append(">" + sequence.getName() + " " + sequence.getDescription() + "\n")
                             .append(finalSequence.toString().toUpperCase() + "\n");

        }

        fastaOutputString.deleteCharAt(fastaOutputString.length() - 1);

        File fasta;
        try {
            fasta = FileUtilities.createTempFile("temp", ".fasta", false);
            BufferedWriter out = new BufferedWriter(new FileWriter(fasta));
            out.write(fastaOutputString.toString());
            out.close();
            return fasta;
        } catch (IOException e) {
            throw new DocumentOperationException("Failed to write FASTA file: " + e.getMessage(), e);
        }
    }
}

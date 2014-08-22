package com.biomatters.plugins.barcoding.validator.validation.assembly;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotation;
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
    public void assemble(List<NucleotideSequenceDocument> sequences) throws DocumentOperationException {
        File fasta = createFasta(sequences);

    }

    private void executeFasta() {

    }

    private File createFasta(List<NucleotideSequenceDocument> sequences) throws DocumentOperationException {
        StringBuilder fastaOutputString = new StringBuilder();
        for (NucleotideSequenceDocument sequence : sequences) {
            int leftTrimIndex, rightTrimIndex;
            leftTrimIndex = rightTrimIndex = -1;

            for (SequenceAnnotation annotation : sequence.getSequenceAnnotations()) {
                if (annotation.getName().equals("LeftTrim")) {
                    leftTrimIndex = annotation.getInterval().getTo() - 1;
                    if (leftTrimIndex > sequence.getSequenceLength()) {
                        leftTrimIndex = sequence.getSequenceLength();
                    }
                }
                if (annotation.getName().equals("RightTrim")) {
                    rightTrimIndex = annotation.getInterval().getFrom() - 1;
                    if (rightTrimIndex < 0) {
                        rightTrimIndex = 0;
                    }
                }
            }

            if (leftTrimIndex == -1 || rightTrimIndex == -1) {
                throw new DocumentOperationException("Document without trimmings found.");
            }

            fastaOutputString.append(">" + sequence.getName() + " " + sequence.getDescription() + "\n")
                    .append(sequence.getSequenceString().substring(leftTrimIndex, rightTrimIndex).toUpperCase() + "\n");
        }

        File fasta;
        try {
            fasta = FileUtilities.createTempFile("temp", ".fasta", true);
            BufferedWriter out = new BufferedWriter(new FileWriter(fasta));
            out.write(fastaOutputString.toString());
            out.close();
            return fasta;
        } catch (IOException e) {
            throw new DocumentOperationException("Failed to write FASTA file: " + e.getMessage(), e);
        }
    }
}
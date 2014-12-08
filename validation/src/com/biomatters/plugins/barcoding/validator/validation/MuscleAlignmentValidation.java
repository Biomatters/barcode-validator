package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.*;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.implementations.Percentage;
import com.biomatters.geneious.publicapi.implementations.SequenceExtractionUtilities;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperation;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.PluginUtilities;
import com.biomatters.plugins.barcoding.validator.validation.results.MuscleAlignmentValidationResultFact;
import com.biomatters.plugins.barcoding.validator.validation.results.ResultFact;
import jebl.util.ProgressListener;

import java.util.*;

/**
 * @author Frank Lee
 * Created by frank on 2/10/14.
 */
public class MuscleAlignmentValidation extends SequenceCompareValidation {
    private static final String MUSCLE_OPERATION_ID = "MUSCLE";

    @Override
    public ResultFact validate(SequenceDocument sequence, SequenceDocument referenceSequence, ValidationOptions options, ValidationCallback callback) {
        if (!(options instanceof MuscleAlignmentValidationOptions)) {
            throw new IllegalArgumentException(
                    "Wrong options supplied: " +
                    "Expected: ConsensusValidationOptions, " +
                    "actual: " + options.getClass().getSimpleName() + "."
            );
        }

        MuscleAlignmentValidationOptions muscleAlignmentValidationOptions = (MuscleAlignmentValidationOptions)options;
        DocumentOperation alignmentOperation = PluginUtilities.getAlignmentOperation(MUSCLE_OPERATION_ID, SequenceDocument.Alphabet.NUCLEOTIDE);
        String sequenceName = sequence.getName();
        float minimumSimilarity = muscleAlignmentValidationOptions.getMinimumSimilarity();
        Map<Float, AnnotatedPluginDocument> similarityToIntermediateDocument = new HashMap<Float, AnnotatedPluginDocument>();

        AnnotatedPluginDocument sequenceDocument = DocumentUtilities.getAnnotatedPluginDocumentThatContains(sequence);
        if (sequenceDocument == null) {
            sequenceDocument = DocumentUtilities.createAnnotatedPluginDocument(sequence);
        }
        AnnotatedPluginDocument referenceSequenceDocument = DocumentUtilities.getAnnotatedPluginDocumentThatContains(referenceSequence);
        if (referenceSequenceDocument == null) {
            referenceSequenceDocument = DocumentUtilities.createAnnotatedPluginDocument(referenceSequence);
        }
        DefaultSequenceDocument sequenceReversed = SequenceExtractionUtilities.reverseComplement(sequence);
        AnnotatedPluginDocument sequenceReversedDocument = DocumentUtilities.createAnnotatedPluginDocument(sequenceReversed);
        sequenceReversedDocument.setName(sequenceName + " (reversed)");

        MuscleAlignmentValidationResultFact result = new MuscleAlignmentValidationResultFact(false, 0.0, "", Collections.<URN>emptyList(), "");

        try {
            float similarityBetweenSequenceOneAndSequenceTwo = getSimilarity(alignmentOperation, sequenceDocument, referenceSequenceDocument, similarityToIntermediateDocument);
            float similarityBetweenSequenceOneReversedAndSequenceTwo = getSimilarity(alignmentOperation, sequenceDocument, sequenceReversedDocument, similarityToIntermediateDocument);

            float similarityOfAlignment = Math.max(similarityBetweenSequenceOneAndSequenceTwo, similarityBetweenSequenceOneReversedAndSequenceTwo);
            AnnotatedPluginDocument alignmentDocument = similarityToIntermediateDocument.get(similarityOfAlignment);

            result.setSimilarity(similarityOfAlignment);
            result.setAlignmentName(alignmentDocument.getName());
            PluginDocument pluginDocument = callback.addPluginDocument(alignmentDocument.getDocument(), ProgressListener.EMPTY);
            result.addAlignmentDocument(pluginDocument);

            if (similarityOfAlignment == -1) {
                result.setNotes("Failed to align " + sequence.getName() + " and " + referenceSequence.getName());
            } else if (similarityOfAlignment < minimumSimilarity) {
                result.setNotes("Similarity was below the minimum threshold. Minimum similarity: " + minimumSimilarity + "%, actual similarity: " + similarityOfAlignment + "%");
            } else {
                result.setPass(true);
            }
        } catch (DocumentOperationException e) {
            result.setNotes("An error occurred during the alignment: " + e.getMessage());
        }

        return result;
    }

    @Override
    public ValidationOptions getOptions() {
        return new MuscleAlignmentValidationOptions(MuscleAlignmentValidation.class);
    }

    private float getSimilarity(DocumentOperation alignmentOperation,
                                AnnotatedPluginDocument sequenceOneDocument,
                                AnnotatedPluginDocument sequenceTwoDocument,
                                Map<Float, AnnotatedPluginDocument> similarityToIntermediateDocument) throws DocumentOperationException {
        List<AnnotatedPluginDocument> annotatedPluginDocuments = alignmentOperation.performOperation(ProgressListener.EMPTY, alignmentOperation.getOptions(sequenceOneDocument, sequenceTwoDocument), sequenceOneDocument, sequenceTwoDocument);

        if (annotatedPluginDocuments == null || annotatedPluginDocuments.size() == 0) {
            return -1;
        }

        // A pairwise alignment of two sequences should only ever produce one document
        assert(annotatedPluginDocuments.size() == 1);

        AnnotatedPluginDocument alignmentDocument = annotatedPluginDocuments.get(0);

        float similarity = ((Percentage)alignmentDocument.getFieldValue(DocumentField.ALIGNMENT_SIMILARITY.getCode())).floatValue();

        similarityToIntermediateDocument.put(similarity, alignmentDocument);

        return similarity;
    }
}
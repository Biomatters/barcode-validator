package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentField;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.documents.PluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.implementations.Percentage;
import com.biomatters.geneious.publicapi.plugin.DocumentOperation;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.PluginUtilities;
import jebl.util.ProgressListener;

import java.util.List;

/**
 * Created by frank on 2/10/14.
 */
public class BarcodeConsensusValidation extends BarcodeCompareValidation {
    private static final String MUSCLE_OPERATION_ID = "MUSCLE";
    @Override
    public ValidationOptions getOptions() {
        return new BarcodeConsensusValidationOptions(BarcodeConsensusValidation.class);
    }

    @Override
    public ValidationResult validate(SequenceDocument originalSequence, SequenceDocument generatedSequence, ValidationOptions options) {
        DocumentOperation alignmentOperation = PluginUtilities.getAlignmentOperation(MUSCLE_OPERATION_ID, SequenceDocument.Alphabet.NUCLEOTIDE);
        AnnotatedPluginDocument origianlDoc = DocumentUtilities.getAnnotatedPluginDocumentThatContains(originalSequence);
        if (origianlDoc == null) {
            origianlDoc = DocumentUtilities.createAnnotatedPluginDocument(originalSequence);
        }

        AnnotatedPluginDocument generatedDoc = DocumentUtilities.getAnnotatedPluginDocumentThatContains(generatedSequence);
        if (generatedDoc == null) {
            generatedDoc = DocumentUtilities.createAnnotatedPluginDocument(generatedSequence);
        }

        try {
            BarcodeConsensusValidationOptions opt = (BarcodeConsensusValidationOptions) options;
            float matches = opt.getMatches();
            List<AnnotatedPluginDocument> annotatedPluginDocuments = alignmentOperation.performOperation(ProgressListener.EMPTY, alignmentOperation.getOptions(origianlDoc, generatedDoc), origianlDoc, generatedDoc);
            if (annotatedPluginDocuments == null || annotatedPluginDocuments.size() == 0) {
                return new ValidationResult(false, "Failed to align " + originalSequence.getName() + " and " + generatedSequence.getName());
            }

            // A pairwise alignment of two sequences should only ever produce one document
            assert(annotatedPluginDocuments.size() == 1);
            AnnotatedPluginDocument apd = annotatedPluginDocuments.get(0);
            PluginDocument alignment = apd.getDocumentOrNull();

            Percentage fieldValue = (Percentage) apd.getFieldValue(DocumentField.ALIGNMENT_PERCENTAGE_IDENTICAL.getCode());
            ValidationResult validationResult;
            if (fieldValue.floatValue() < matches) {
                validationResult = new ValidationResult(false, "Similarity was too low.  Required " + fieldValue.floatValue() + "% but was " + matches + "%");
            } else {
                validationResult = new ValidationResult(true, null);
            }

            validationResult.addIntermediateDocument(alignment);
            return validationResult;
        } catch (DocumentOperationException e) {
            e.printStackTrace();
            return new ValidationResult(false, e.getMessage());
        }
    }
}

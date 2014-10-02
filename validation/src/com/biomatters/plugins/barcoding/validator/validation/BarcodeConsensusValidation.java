package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentField;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
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
                return new ValidationResult(false, "failed to align SequenceDocument (" + originalSequence.getName() + " and " + generatedSequence.getName() + ")");
            }

            for(AnnotatedPluginDocument doc : annotatedPluginDocuments) {
                Percentage fieldValue = (Percentage)doc.getFieldValue(DocumentField.ALIGNMENT_PERCENTAGE_IDENTICAL.getCode());
                if (fieldValue.floatValue() < matches)
                    return new ValidationResult(false, "failed validation, similarity is " + fieldValue.floatValue() + "% but require up to " + matches + "%");
            }
        } catch (DocumentOperationException e) {
            e.printStackTrace();
            return new ValidationResult(false, e.getMessage());
        }

        return new ValidationResult(true, "validation success.");
    }
}

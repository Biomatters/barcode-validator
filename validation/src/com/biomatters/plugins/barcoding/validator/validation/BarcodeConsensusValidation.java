package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentField;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.documents.PluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.implementations.Percentage;
import com.biomatters.geneious.publicapi.implementations.SequenceExtractionUtilities;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperation;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.PluginUtilities;
import com.biomatters.plugins.barcoding.validator.validation.results.BarcodeValidationResult;
import com.biomatters.plugins.barcoding.validator.validation.results.StatusFact;
import jebl.util.ProgressListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Frank Lee
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

        DefaultSequenceDocument reversedSequence = SequenceExtractionUtilities.reverseComplement(generatedSequence);
        AnnotatedPluginDocument reversedGeneratedDoc = DocumentUtilities.getAnnotatedPluginDocumentThatContains(reversedSequence);
        if (reversedGeneratedDoc == null) {
            reversedGeneratedDoc = DocumentUtilities.createAnnotatedPluginDocument(reversedSequence);
        }

        try {
            BarcodeConsensusValidationOptions opt = (BarcodeConsensusValidationOptions) options;
            Map<Float, PluginDocument> interDocs = new HashMap<Float, PluginDocument>();
            float matches = opt.getMatches();
            float simi1 = getSimilarity(alignmentOperation, origianlDoc, generatedDoc, interDocs);
            float simi2 = getSimilarity(alignmentOperation, origianlDoc, reversedGeneratedDoc, interDocs);
            float simi = simi1 > simi2 ? simi1 : simi2;

            ValidationResult validationResult;
            if (simi == -1) {
                validationResult = new ValidationResult(false, "Failed to align " + originalSequence.getName() + " and " + generatedSequence.getName());
            } else if (simi < matches) {
                validationResult = new ValidationResult(false, "Similarity was too low.  Required " + matches + "% but was " + simi + "%");
            } else {
                validationResult = new ValidationResult(true, null);
            }

            validationResult.addIntermediateDocument(interDocs.get(simi));

            BarcodeValidationResult entry = new BarcodeValidationResult();
            StatusFact fact = new StatusFact();
            fact.setName(interDocs.get(simi).getName());
            fact.addLink(interDocs.get(simi).getURN());
            fact.setIdentity(simi / 100);
            fact.setStatus(validationResult.isPassed());
            entry.setConsensusFact(fact);
            validationResult.setEntry(entry);

            return validationResult;
        } catch (DocumentOperationException e) {
            e.printStackTrace();
            return new ValidationResult(false, e.getMessage());
        }
    }

    private float getSimilarity(DocumentOperation alignmentOperation, AnnotatedPluginDocument origianlDoc, AnnotatedPluginDocument generatedDoc, Map<Float, PluginDocument> interDocs) throws DocumentOperationException {
        List<AnnotatedPluginDocument> annotatedPluginDocuments = alignmentOperation.performOperation(ProgressListener.EMPTY, alignmentOperation.getOptions(origianlDoc, generatedDoc), origianlDoc, generatedDoc);
        if (annotatedPluginDocuments == null || annotatedPluginDocuments.size() == 0) {
            return -1;
        }

        // A pairwise alignment of two sequences should only ever produce one document
        assert(annotatedPluginDocuments.size() == 1);
        AnnotatedPluginDocument apd = annotatedPluginDocuments.get(0);
        Percentage fieldValue = (Percentage) apd.getFieldValue(DocumentField.ALIGNMENT_SIMILARITY.getCode());
        float ret = fieldValue.floatValue();
        interDocs.put(ret, apd.getDocumentOrNull());
        return ret;
    }
}

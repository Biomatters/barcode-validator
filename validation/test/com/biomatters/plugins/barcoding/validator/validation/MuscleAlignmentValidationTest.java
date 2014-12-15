package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.PluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.implementations.SequenceExtractionUtilities;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.TestGeneious;
import com.biomatters.plugins.barcoding.validator.validation.results.DoubleResultColumn;
import com.biomatters.plugins.barcoding.validator.validation.results.MuscleAlignmentValidationResultFact;
import com.biomatters.plugins.barcoding.validator.validation.results.ResultColumn;
import com.biomatters.plugins.barcoding.validator.validation.results.ResultFact;
import jebl.util.ProgressListener;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.List;

/**
 * @author Matthew Cheung
 *         Created on 16/12/14 9:14 AM
 */
public class MuscleAlignmentValidationTest extends Assert {

    @Test
    public void canAutomaticallyDetermineDirection() {
        TestGeneious.initializePlugins("com.biomatters.plugins.muscle.MusclePlugin");

        MuscleAlignmentValidation validation = new MuscleAlignmentValidation();
        ValidationOptions options = validation.getOptions();

        DefaultNucleotideSequence sequence = new DefaultNucleotideSequence("test", "ACTGAAACTGAGACCA");
        DefaultNucleotideSequence toTest = new DefaultNucleotideSequence(sequence, new Date());
        ResultFact result = validation.validate(sequence, toTest, options, getCallbackThatDoesNothing());
        ResultFact reverseResult = validation.validate(sequence, SequenceExtractionUtilities.reverseComplement(toTest), options, getCallbackThatDoesNothing());

        checkForNoErrorMessage(result);
        checkForNoErrorMessage(reverseResult);

        ResultColumn column = getResultColumnForName(result, MuscleAlignmentValidationResultFact.SIMILARITY_COLUMN_NAME);
        assertTrue(column instanceof DoubleResultColumn);
        ResultColumn reverseColumn = getResultColumnForName(reverseResult, MuscleAlignmentValidationResultFact.SIMILARITY_COLUMN_NAME);
        assertTrue(reverseColumn instanceof DoubleResultColumn);

        assertEquals(column.getData(), reverseColumn.getData());
        assertTrue((Double)column.getData() > 0.0);
        assertTrue((Double)reverseColumn.getData() > 0.0);
    }

    private void checkForNoErrorMessage(ResultFact result) {
        Object errorMessage = getResultColumnForName(result, MuscleAlignmentValidationResultFact.NOTES_COLUMN_NAME).getData();
        assertTrue(String.valueOf(errorMessage), errorMessage == null || errorMessage.toString().trim().length() == 0);
    }

    private ResultColumn getResultColumnForName(ResultFact fact, String similarityColumnName) {
        for (ResultColumn resultColumn : fact.getColumns()) {
            if(resultColumn.getName().equals(similarityColumnName)) {
                return resultColumn;
            }
        }
        return null;
    }

    private ValidationCallback getCallbackThatDoesNothing() {
        return new ValidationCallback() {
            @Override
            public void setInputs(NucleotideSequenceDocument barcodeSequence, List<NucleotideGraphSequenceDocument> traces, ProgressListener progressListener) throws DocumentOperationException {

            }

            @Override
            public List<NucleotideGraphSequenceDocument> addTrimmedTraces(List<NucleotideGraphSequenceDocument> traces, ProgressListener progressListener) throws DocumentOperationException {
                return null;
            }

            @Override
            public SequenceAlignmentDocument addAssembly(SequenceAlignmentDocument contigAssembly, ProgressListener progressListener) throws DocumentOperationException {
                return null;
            }

            @Override
            public NucleotideGraphSequenceDocument addConsensus(NucleotideGraphSequenceDocument consensusSequence, ProgressListener progressListener) throws DocumentOperationException {
                return null;
            }

            @Override
            public void addValidationResult(ValidationOptions options, ValidationResult validationResult, ProgressListener progressListener) throws DocumentOperationException {

            }

            @Override
            public PluginDocument addPluginDocument(PluginDocument pluginDocument, ProgressListener progressListener) throws DocumentOperationException {
                return null;
            }
        };
    }
}

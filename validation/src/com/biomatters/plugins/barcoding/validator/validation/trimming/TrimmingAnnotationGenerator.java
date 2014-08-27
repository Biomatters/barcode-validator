package com.biomatters.plugins.barcoding.validator.validation.trimming;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.PluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotation;
import com.biomatters.geneious.publicapi.plugin.*;
import com.biomatters.plugins.barcoding.validator.validation.assembly.Cap3Assembler;
import jebl.util.ProgressListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gen Li
 *         Created on 21/08/14 1:46 PM
 */
public class TrimmingAnnotationGenerator extends SequenceAnnotationGenerator {

    @Override
    public GeneiousActionOptions getActionOptions() {
        return new GeneiousActionOptions("Trim").setInMainToolbar(true);
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public DocumentSelectionSignature[] getSelectionSignatures() {
        return new DocumentSelectionSignature[] {
            DocumentSelectionSignature.forNucleotideSequences(1, Integer.MAX_VALUE)
        };
    }

    @Override
    public Options getOptions(AnnotatedPluginDocument[] documents, SelectionRange selectionRange) throws DocumentOperationException {
        return new ErrorProbabilityOptions();
    }

    @Override
    public List<AnnotationGeneratorResult> generate(AnnotatedPluginDocument[] documents, SelectionRange selectionRange, ProgressListener progressListener, Options options) throws DocumentOperationException {
        List<AnnotationGeneratorResult> result = new ArrayList<AnnotationGeneratorResult>();
        List<NucleotideSequenceDocument> nucleotideSequenceDocuments = new ArrayList<NucleotideSequenceDocument>();
        for (AnnotatedPluginDocument annotatedPluginDocument : documents) {
            PluginDocument pluginDocument = annotatedPluginDocument.getDocumentOrNull();

            if (pluginDocument == null) {
                throw new DocumentOperationException("Could not load document " + annotatedPluginDocument.getName());
            }

            if (!(pluginDocument instanceof NucleotideSequenceDocument)) {
                throw new IllegalStateException("Wrong document type, expected: NucleotideSequenceDocument, actual: " + pluginDocument.getClass().getSimpleName());
            }

            NucleotideSequenceDocument nucleotideSequenceDocument = (NucleotideSequenceDocument) pluginDocument;

            nucleotideSequenceDocuments.add(nucleotideSequenceDocument);

            Trimmage trimmage = ErrorProbabilityTrimmer.getTrimmage(nucleotideSequenceDocument, TrimmableEnds.Both, ((ErrorProbabilityOptions)options).getErrorProbabilityLimit());

            AnnotationGeneratorResult annotationGeneratorResult = new AnnotationGeneratorResult();

            SequenceAnnotation forwardSequenceAnnotation = SequenceAnnotation.createTrimAnnotation(1, trimmage.trimAtStart);
            SequenceAnnotation reverseSequenceAnnotation = SequenceAnnotation.createTrimAnnotation(
                    nucleotideSequenceDocument.getSequenceLength() - trimmage.trimAtEnd + 1,
                    nucleotideSequenceDocument.getSequenceLength());

            annotationGeneratorResult.addAnnotationToAdd(forwardSequenceAnnotation);
            annotationGeneratorResult.addAnnotationToAdd(reverseSequenceAnnotation);

            result.add(annotationGeneratorResult);
        }

        Cap3Assembler.createFasta(nucleotideSequenceDocuments);

        return result;
    }
}
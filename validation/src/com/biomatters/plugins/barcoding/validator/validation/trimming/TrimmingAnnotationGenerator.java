package com.biomatters.plugins.barcoding.validator.validation.trimming;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.plugin.*;
import jebl.util.ProgressListener;

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
    public List<AnnotationGeneratorResult> generate(AnnotatedPluginDocument[] documents, SelectionRange selectionRange, ProgressListener progressListener, Options options) throws DocumentOperationException {
        return super.generate(documents, selectionRange, progressListener, options);
    }
}

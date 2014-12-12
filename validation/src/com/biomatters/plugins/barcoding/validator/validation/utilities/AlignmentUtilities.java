package com.biomatters.plugins.barcoding.validator.validation.utilities;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.documents.PluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperation;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.geneious.publicapi.plugin.PluginUtilities;
import jebl.util.ProgressListener;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Matthew Cheung
 *         Created on 3/12/14 2:16 PM
 */
public class AlignmentUtilities {

    private AlignmentUtilities(){}

    private static final String MUSCLE_OPERATION_ID = "MUSCLE";

    /**
     * This method currently uses the Geneious Muscle Alignment plugin to perform an alignment.  However in the future
     * it needs to be changed to work independently of the Geneious run time.
     * @param toAlign List of sequences to align
     * @param progressListener
     * @return A {@link com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument} or null if there was no alignment
     * @throws DocumentOperationException
     */
    public static SequenceAlignmentDocument performAlignment(List<NucleotideSequenceDocument> toAlign, ProgressListener progressListener) throws DocumentOperationException {
        DocumentOperation alignmentOperation = PluginUtilities.getAlignmentOperation(MUSCLE_OPERATION_ID, SequenceDocument.Alphabet.NUCLEOTIDE);
        if(alignmentOperation == null) {
            throw new DocumentOperationException("The Muscle Alignment plugin must be enabled");
        }

        AnnotatedPluginDocument[] inputDocs = new AnnotatedPluginDocument[toAlign.size()];
        for(int i=0; i<toAlign.size(); i++) {
            SequenceDocument pluginDoc = toAlign.get(i);
            AnnotatedPluginDocument apd = DocumentUtilities.getAnnotatedPluginDocumentThatContains(pluginDoc);
            if(apd == null) {
                apd = DocumentUtilities.createAnnotatedPluginDocument(pluginDoc);
            }
            inputDocs[i] = apd;
        }
        Options options = alignmentOperation.getOptions(inputDocs);
        List<AnnotatedPluginDocument> annotatedPluginDocuments = alignmentOperation.performOperation(progressListener, options, inputDocs);
        if (annotatedPluginDocuments == null || annotatedPluginDocuments.isEmpty()) {
            return null;
        }

        assert annotatedPluginDocuments.size() == 1 : "Alignment operation should only produce at maximum one alignment document";

        AnnotatedPluginDocument alignmentDocument = annotatedPluginDocuments.get(0);
        PluginDocument pluginDoc = alignmentDocument.getDocumentOrNull();
        if(pluginDoc instanceof SequenceAlignmentDocument) {
            return (SequenceAlignmentDocument)pluginDoc;
        } else {
            throw new IllegalStateException("Alignment produced a " + pluginDoc.getClass());
        }
    }
}

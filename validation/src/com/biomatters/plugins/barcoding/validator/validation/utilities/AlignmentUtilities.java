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

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Matthew Cheung
 *         Created on 3/12/14 2:16 PM
 */
public class AlignmentUtilities {

    private AlignmentUtilities(){}

    private static final String MUSCLE_OPERATION_ID = "MUSCLE";

    /**
     *
     * @return Options that can be used to specify the parameters of the alignment to {@link #performAlignment(java.util.List, com.biomatters.geneious.publicapi.plugin.Options, jebl.util.ProgressListener)}
     * @throws DocumentOperationException
     */
    public static Options getOptions() throws DocumentOperationException {
        Options options = getMuscleOperation().getGeneralOptions();
        // We'll use a default of 2 iterations because that is typically sufficient for barcode data since you'll be
        // typically aligning sequences from the same locus.
        Options.Option iterationsOption = options.getOption("-maxiters");
        if(iterationsOption instanceof Options.IntegerOption) {
            ((Options.IntegerOption)iterationsOption).setDefaultValue(2);
        }

        for (Options.Option option : options.getOptions()) {
            option.setAdvanced(false);
        }

        return options;
    }

    /**
     * This method currently uses the Geneious Muscle Alignment plugin to perform an alignment.  However in the future
     * it needs to be changed to work independently of the Geneious run time.
     * @param toAlign List of sequences to align
     * @param options The options for running the alignment.  May be null to use the defaults.
     *@param progressListener  @return A {@link com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument} or null if there was no alignment
     * @throws DocumentOperationException
     */
    public static SequenceAlignmentDocument performAlignment(List<NucleotideSequenceDocument> toAlign, @Nullable Options options, ProgressListener progressListener) throws DocumentOperationException {
        DocumentOperation alignmentOperation = getMuscleOperation();

        AnnotatedPluginDocument[] inputDocs = new AnnotatedPluginDocument[toAlign.size()];
        for(int i=0; i<toAlign.size(); i++) {
            SequenceDocument pluginDoc = toAlign.get(i);
            AnnotatedPluginDocument apd = DocumentUtilities.getAnnotatedPluginDocumentThatContains(pluginDoc);
            if(apd == null) {
                apd = DocumentUtilities.createAnnotatedPluginDocument(pluginDoc);
            }
            inputDocs[i] = apd;
        }
        if(options == null) {
            options = alignmentOperation.getOptions(inputDocs);
        }
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

    private static DocumentOperation getMuscleOperation() throws DocumentOperationException {
        DocumentOperation alignmentOperation = PluginUtilities.getAlignmentOperation(MUSCLE_OPERATION_ID, SequenceDocument.Alphabet.NUCLEOTIDE);
        if(alignmentOperation == null) {
            throw new DocumentOperationException("The Muscle Alignment plugin must be enabled");
        }
        return alignmentOperation;
    }
}

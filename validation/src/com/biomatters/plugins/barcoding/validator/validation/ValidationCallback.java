package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.PluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import jebl.util.ProgressListener;

import java.util.List;

/**
 * Used to record the validation process for reporting.  Implementations are responsible for persisting the record if
 * they require persistence between instances of the JVM.
 * <p />
 * Note that the methods used to add documents to the callback also return documents.  This is because an implementation
 * may choose to create it's own copy of each of the supplied documents.  If it does so then it must do for all of
 * those documents.  Clients of this class should update references to the sequences with the values returned from those
 * add methods.
 *
 * @author Matthew Cheung
 *         Created on 30/09/14 4:17 PM
 */
public interface ValidationCallback {

    public void setInputs(NucleotideSequenceDocument barcodeSequence, List<NucleotideGraphSequenceDocument> traces, ProgressListener progressListener) throws DocumentOperationException;
    public List<NucleotideGraphSequenceDocument> addTrimmedTraces(List<NucleotideGraphSequenceDocument> traces, ProgressListener progressListener) throws DocumentOperationException;
    public SequenceAlignmentDocument addAssembly(SequenceAlignmentDocument contigAssembly, ProgressListener progressListener) throws DocumentOperationException;
    public NucleotideGraphSequenceDocument addConsensus(NucleotideGraphSequenceDocument consensusSequence, ProgressListener progressListener) throws DocumentOperationException;
    public void addValidationResult(ValidationOptions options, ValidationResult validationResult, ProgressListener progressListener) throws DocumentOperationException;
    public PluginDocument addPluginDocument(PluginDocument pluginDocument, ProgressListener progressListener) throws DocumentOperationException;
}

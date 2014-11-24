package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.PluginDocument;
import com.biomatters.geneious.publicapi.documents.URN;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import jebl.util.ProgressListener;

import java.util.List;

/**
 * Used to record the validation process for reporting
 *
 * @author Matthew Cheung
 *         Created on 30/09/14 4:17 PM
 */
public interface ValidationCallback {

    public void setInputs(NucleotideSequenceDocument barcodeSequence, List<NucleotideGraphSequenceDocument> traces, ProgressListener progressListener) throws DocumentOperationException;
    public List<NucleotideGraphSequenceDocument> addTrimmedTraces(List<NucleotideGraphSequenceDocument> traces, ProgressListener progressListener) throws DocumentOperationException;
    public void addAssembly(SequenceAlignmentDocument contigAssembly, ProgressListener progressListener) throws DocumentOperationException;
    public void addConsensus(SequenceDocument consensusSequence, ProgressListener progressListener) throws DocumentOperationException;
    public void addValidationResult(ValidationOptions options, ValidationResult validationResult, ProgressListener progressListener) throws DocumentOperationException;
    public URN getURNofSequence(PluginDocument sequence);
}

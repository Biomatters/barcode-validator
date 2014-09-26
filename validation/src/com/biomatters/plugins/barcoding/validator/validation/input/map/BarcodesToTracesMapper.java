package com.biomatters.plugins.barcoding.validator.validation.input.map;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideGraphSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;

import java.util.List;
import java.util.Map;

/**
 * @author Gen Li
 *         Created on 3/09/14 5:26 PM
 */
public abstract class BarcodesToTracesMapper {
    public abstract Map<NucleotideSequenceDocument, List<DefaultNucleotideGraphSequence>>
    map(List<NucleotideSequenceDocument> barcodes, List<DefaultNucleotideGraphSequence> traces)
            throws DocumentOperationException;
}
package com.biomatters.plugins.barcoding.validator.validation.input.map;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideGraphSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;

import java.util.List;
import java.util.Map;

/**
 * @author Gen Li
 *         Created on 5/09/14 9:57 AM
 */
public class GenbankXmlMapper extends BarcodesToTracesMapper {
    @Override
    public Map<NucleotideSequenceDocument, List<DefaultNucleotideGraphSequence>>
    map(List<NucleotideSequenceDocument> traces, List<DefaultNucleotideGraphSequence> barcodeSequences)
            throws DocumentOperationException {
        return null;
    }
}
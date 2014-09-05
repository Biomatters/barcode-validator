package com.biomatters.plugins.barcoding.validator.research.input.map;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;

import java.util.List;
import java.util.Map;

/**
 * @author Gen Li
 *         Created on 5/09/14 9:57 AM
 */
public class ByGenbankXmlMapper extends TraceToBarcodeMapper {
    @Override
    public Map<NucleotideSequenceDocument, List<NucleotideSequenceDocument>>
    map(List<NucleotideSequenceDocument> traces, List<NucleotideSequenceDocument> barcodeSequences)
            throws DocumentOperationException {
        return null;
    }
}
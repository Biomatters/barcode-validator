package com.biomatters.plugins.barcoding.validator.research.input.map;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.plugins.barcoding.validator.research.input.InputSplitterOptions;

import java.util.List;
import java.util.Map;

/**
 * @author Gen Li
 *         Created on 3/09/14 5:26 PM
 */
public abstract class TraceToBarcodeMapper {
    public abstract Map<NucleotideSequenceDocument, NucleotideSequenceDocument>
    map(List<NucleotideSequenceDocument> traces,
        List<NucleotideSequenceDocument> barcodeSequences);

}
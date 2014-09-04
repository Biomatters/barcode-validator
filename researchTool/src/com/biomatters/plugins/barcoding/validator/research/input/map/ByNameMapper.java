package com.biomatters.plugins.barcoding.validator.research.input.map;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Gen Li
 *         Created on 4/09/14 12:40 PM
 */
public class ByNameMapper extends TraceToBarcodeMapper {
    @Override
    public Map<NucleotideSequenceDocument, NucleotideSequenceDocument>
    map(List<NucleotideSequenceDocument> traces, List<NucleotideSequenceDocument> barcodeSequences) {
        Map<NucleotideSequenceDocument, NucleotideSequenceDocument> result =
                new HashMap<NucleotideSequenceDocument, NucleotideSequenceDocument>();

        for (NucleotideSequenceDocument nucleotideSequenceDocument : traces) {

        }

        return result;
    }
}
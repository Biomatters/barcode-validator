package com.biomatters.plugins.barcoding.validator.validation.input;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.plugins.barcoding.validator.validation.input.map.FileNameMapper;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * @author Gen Li
 *         Created on 8/09/14 2:36 PM
 */
public class InputTest extends Assert {
    @Test
    public void testSplitByName() throws DocumentOperationException {
        for (String separator : Arrays.asList("_", "*", "|", ":", "$", "=", ".", ",", "+", "~", " ")) {
            mappingTestWithSeparator(separator);
        }
    }

    public void mappingTestWithSeparator(String separator) throws DocumentOperationException {
        NucleotideSequenceDocument t1 = new DefaultNucleotideSequence("trace1" + separator + "1", "");
        NucleotideSequenceDocument t2 = new DefaultNucleotideSequence("trace2" + separator + "1", "");
        NucleotideSequenceDocument t3 = new DefaultNucleotideSequence("trace3" + separator + "1", "");
        NucleotideSequenceDocument t4 = new DefaultNucleotideSequence("trace4" + separator + "2", "");

        NucleotideSequenceDocument b1 = new DefaultNucleotideSequence("1" + separator + "barcode1", "");
        NucleotideSequenceDocument b2 = new DefaultNucleotideSequence("2" + separator + "barcode2", "");

        List<NucleotideSequenceDocument> traces = Arrays.asList(t1, t2, t3, t4);
        List<NucleotideSequenceDocument> barcodes = Arrays.asList(b1, b2);

        String regex = getRegularExpressionForSeparator(separator);
        FileNameMapper mapper = new FileNameMapper(regex, 1, regex, 0);

        Map<NucleotideSequenceDocument, List<NucleotideSequenceDocument>> mapped = mapper.map(barcodes, traces);

        List<NucleotideSequenceDocument> mappedToB1 = mapped.get(b1);
        List<NucleotideSequenceDocument> mappedToB2 = mapped.get(b2);

        assertEquals(2, mapped.keySet().size());

        assertEquals(3, mappedToB1.size());
        assertTrue(mappedToB1.contains(t1));
        assertTrue(mappedToB1.contains(t2));
        assertTrue(mappedToB1.contains(t3));

        assertEquals(1, mappedToB2.size());
        assertTrue(mappedToB2.contains(t4));
    }

    private static Map<String, String> separatorToRegularExpression = new HashMap<String, String>();
    static {
        separatorToRegularExpression.put("*", "\\*");
        separatorToRegularExpression.put("|", "\\|");
        separatorToRegularExpression.put("$", "\\$");
        separatorToRegularExpression.put(".", "\\.");
        separatorToRegularExpression.put("+", "\\+");
        separatorToRegularExpression.put("~", "\\~");
        separatorToRegularExpression.put(" ", "\\s+");
    }

    private static String getRegularExpressionForSeparator(String separator) {
        String regex = separatorToRegularExpression.get(separator);
        if (regex != null) {
            return regex;
        }

        return separator;
    }
}
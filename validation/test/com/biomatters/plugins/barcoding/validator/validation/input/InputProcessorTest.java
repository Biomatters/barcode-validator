package com.biomatters.plugins.barcoding.validator.validation.input;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.documents.sequence.DefaultNucleotideGraph;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideGraphSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.plugins.barcoding.validator.validation.input.map.FileNameMapper;
import com.google.common.collect.Multimap;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * @author Gen Li
 *         Created on 8/09/14 2:36 PM
 */
public class InputProcessorTest extends Assert {
    @Test
    public void testSplitByName() throws DocumentOperationException {
        for (String separator : Arrays.asList("_", "*", "|", ":", "$", "=", ".", ",", "+", "~", " ")) {
            mappingTestWithSeparator(separator);
        }
    }

    public void mappingTestWithSeparator(String separator) throws DocumentOperationException {
        AnnotatedPluginDocument t1 = createTestDocWithName("trace1" + separator + "1");
        AnnotatedPluginDocument t2 = createTestDocWithName("trace2" + separator + "1");
        AnnotatedPluginDocument t3 = createTestDocWithName("trace3" + separator + "1");
        AnnotatedPluginDocument t4 = createTestDocWithName("trace4" + separator + "2");

        List<AnnotatedPluginDocument> traces = Arrays.asList(t1, t2, t3, t4);

        AnnotatedPluginDocument b1 = createTestDocWithName("1" + separator + "barcode1");
        AnnotatedPluginDocument b2 = createTestDocWithName("2" + separator + "barcode2");
        List<AnnotatedPluginDocument> barcodes = Arrays.asList(b1, b2);

        String regex = getRegularExpressionForSeparator(separator);
        FileNameMapper mapper = new FileNameMapper(regex, 1, regex, 0);

        Multimap<AnnotatedPluginDocument, AnnotatedPluginDocument> mapped = mapper.map(barcodes, traces);
        Collection<AnnotatedPluginDocument> mappedToB1 = mapped.get(b1);
        Collection<AnnotatedPluginDocument> mappedToB2 = mapped.get(b2);

        assertEquals(2, mapped.keySet().size());

        assertEquals(3, mappedToB1.size());
        assertTrue(mappedToB1.contains(t1));
        assertTrue(mappedToB1.contains(t2));
        assertTrue(mappedToB1.contains(t3));

        assertEquals(1, mappedToB2.size());
        assertTrue(mappedToB2.contains(t4));
    }

    private AnnotatedPluginDocument createTestDocWithName(String name) {
        return DocumentUtilities.createAnnotatedPluginDocument(new DefaultNucleotideGraphSequence(name, "", "", new Date(), new DefaultNucleotideGraph(null, null, null, 0, 0)));
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
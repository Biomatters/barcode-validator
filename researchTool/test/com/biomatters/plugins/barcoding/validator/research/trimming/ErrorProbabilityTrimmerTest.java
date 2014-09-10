package com.biomatters.plugins.barcoding.validator.research.trimming;

import com.biomatters.geneious.publicapi.documents.sequence.DefaultNucleotideGraph;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraph;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideGraphSequence;
import com.biomatters.geneious.publicapi.utilities.CharSequenceUtilities;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.*;

/**
 * @author Amy Wilson
 * @version $Id$
 *          <p/>
 *          Created on 8/12/2008 1:08:53 PM
 */
public class ErrorProbabilityTrimmerTest extends TestCase {

    private void test(int[] values, Trimmage expectedTrimmage) {
        DefaultNucleotideGraphSequence graph = testGraph(values, null);
        Trimmage trimmage =  ErrorProbabilityTrimmer.getTrimmage(graph, TrimmableEnds.Both, 0.05);
        assertEquals(expectedTrimmage, trimmage);
    }

    /**
     * @param sequence or null to generate one the same length as qualities
     */
    public static DefaultNucleotideGraphSequence testGraph(final int[] qualities, CharSequence sequence) {
        if (sequence == null) {
            sequence = CharSequenceUtilities.repeatedCharSequence("C", qualities.length);
        }
        if (sequence != null && qualities.length != sequence.length()) {
            throw new IllegalArgumentException(qualities.length + " != " + sequence.length());
        }
        NucleotideGraph graph = new DefaultNucleotideGraph(null, null, qualities, sequence.length(), 0);
        return new DefaultNucleotideGraphSequence("test", "", sequence, new Date(), graph);
    }

    @Test
    public void testTrimByErrorProbability() {
        Map<List<Integer>, Trimmage> testValues = new LinkedHashMap<List<Integer>, Trimmage>();
        testValues.put(Arrays.asList(10, 10, 10, 10, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 10, 10, 10, 10), new Trimmage(4, 4));
        testValues.put(Arrays.asList(10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60), new Trimmage(10, 0));
        testValues.put(Arrays.asList(60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10), new Trimmage(0, 10));
        testValues.put(Arrays.asList(60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20), new Trimmage(0, 0));
        testValues.put(Arrays.asList(20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20), new Trimmage(0, 0));
        testValues.put(Arrays.asList(10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10), new Trimmage(30, 0));
        for (Map.Entry<List<Integer>, Trimmage> entry: testValues.entrySet()) {
            test(toArray(entry.getKey()), entry.getValue());
        }
    }

    int[] toArray(List<Integer> values) {
        int[] array = new int[values.size()];
        for (int i = 0; i < values.size(); ++i) {
            array[i] = values.get(i);
        }
        return array;
    }
}
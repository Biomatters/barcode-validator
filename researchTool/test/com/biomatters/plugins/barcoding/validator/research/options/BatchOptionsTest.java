package com.biomatters.plugins.barcoding.validator.research.options;

import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.Options;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * @author Matthew Cheung
 *         Created on 3/11/14 11:01 AM
 */
public class BatchOptionsTest extends Assert {

    @Test
    public void simpleCase() throws DocumentOperationException {

        BatchOptions<TestIntegerOptions> batchOptions = new BatchOptions<TestIntegerOptions>(new TestIntegerOptions()) {
            @Override
            protected void addFirstOptions() {
            }
        };

        setMultiOptionAndTest(TestIntegerOptions.OPTION_1, batchOptions, Arrays.asList(1));
        setMultiOptionAndTest(TestIntegerOptions.OPTION_1, batchOptions, Arrays.asList(1, 2));
        setMultiOptionAndTest(TestIntegerOptions.OPTION_1, batchOptions, Arrays.asList(1, 2, 3));
    }

    public <T extends Number> void setMultiOptionAndTest(String optionName, BatchOptions<TestIntegerOptions> batchOptions, List<T> values) throws DocumentOperationException {
        batchOptions.setValue(BatchOptions.WRAPPED_OPTIONS_KEY + "." + optionName + MultiValueOption.SUFFIX, values);
        assertEquals(values.size(), batchOptions.getBatchSize());
        Iterator<TestIntegerOptions> it = batchOptions.iterator();
        int count = 0;
        Set<Integer> valuesInOptions = new HashSet<Integer>();
        while (it.hasNext()) {
            valuesInOptions.add((Integer) it.next().getValue(optionName));
            count++;
        }
        assertEquals(values.size(), count);
        assertEquals(new HashSet<T>(values), valuesInOptions);
    }

    @Test
    public void multipleOptionsCase() throws DocumentOperationException {

        BatchOptions<TestIntegerOptions> batchOptions = new BatchOptions<TestIntegerOptions>(new TestIntegerOptions()) {
            @Override
            protected void addFirstOptions() {
            }
        };

        batchOptions.setValue(BatchOptions.WRAPPED_OPTIONS_KEY + "." + TestIntegerOptions.OPTION_1 + MultiValueOption.SUFFIX, Arrays.asList(1, 2));
        batchOptions.setValue(BatchOptions.WRAPPED_OPTIONS_KEY + "." + TestIntegerOptions.OPTION_2 + MultiValueOption.SUFFIX, Arrays.asList(1, 2));

        assertEquals(4, batchOptions.getBatchSize());
        Iterator<TestIntegerOptions> it = batchOptions.iterator();
        int count = 0;
        Set<List<Integer>> valuesInOptions = new HashSet<List<Integer>>();
        while (it.hasNext()) {
            TestIntegerOptions options = it.next();
            valuesInOptions.add(Arrays.asList(
                    (Integer) options.getValue(TestIntegerOptions.OPTION_1),
                    (Integer) options.getValue(TestIntegerOptions.OPTION_2)
            ));
            count++;
        }
        assertEquals(4, count);
        assertEquals(new HashSet<List<Integer>>() {
            {
                add(Arrays.asList(1, 1));
                add(Arrays.asList(1, 2));
                add(Arrays.asList(2, 1));
                add(Arrays.asList(2, 2));
            }
        }, valuesInOptions);
    }

    public static class TestIntegerOptions extends Options {
        static final String OPTION_1 = "int";
        static final String OPTION_2 = "int2";

        public TestIntegerOptions() {
            addIntegerOption(OPTION_1, OPTION_1, 1);
            addIntegerOption(OPTION_2, OPTION_2, 1);
        }
    }
}

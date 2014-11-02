package com.biomatters.plugins.barcoding.validator.research.options;

import com.biomatters.geneious.publicapi.plugin.Options;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author Matthew Cheung
 *         Created on 3/11/14 10:46 AM
 */
public class StepSizeTest extends Assert {

    @Test
    public void integerSteps() {
        Options options = new Options(StepSizeTest.class);
        Options.IntegerOption integerOption = options.addIntegerOption("a", "a", 1);
        IntegerMultiValueOption multiValueOption = new IntegerMultiValueOption(integerOption);

        assertEquals(Arrays.asList(1), multiValueOption.getForSteps(1, 1, 1));
        assertEquals(Arrays.asList(1), multiValueOption.getForSteps(1, 10, 0));
        assertEquals(Arrays.asList(1), multiValueOption.getForSteps(1, 10, -1));
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), multiValueOption.getForSteps(1, 10, 1));
        assertEquals(Arrays.asList(1,4,7,10), multiValueOption.getForSteps(1, 10, 3));
        assertEquals(Arrays.asList(1,5,9,10), multiValueOption.getForSteps(1, 10, 4));
    }

    @Test
    public void doubleSteps() {
        Options options = new Options(StepSizeTest.class);
        Options.DoubleOption doubleOption = options.addDoubleOption("a", "a", 1.0);
        DoubleMultiValueOption multiValueOption = new DoubleMultiValueOption(doubleOption);

        assertEquals(Arrays.asList(1.0), multiValueOption.getForSteps(1.0, 1.0, 1.0));
        assertEquals(Arrays.asList(1.0), multiValueOption.getForSteps(1.0, 1.0, -1.0));
        assertEquals(Arrays.asList(1.0), multiValueOption.getForSteps(1.0, 10.0, 0.0));
        assertEquals(Arrays.asList(0.5,1.0,1.5,2.0), multiValueOption.getForSteps(0.5, 2.0, 0.5));
        assertEquals(Arrays.asList(0.5,1.5,2.5,3.0), multiValueOption.getForSteps(0.5, 3.0, 1.0));
    }
}

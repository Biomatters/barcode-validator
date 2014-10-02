package com.biomatters.plugins.barcoding.validator.research;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Gen Li
 *         Created on 20/08/14 4:55 PM
 */
public class BarcodeValidatorTest extends Assert {
    @Test
    public void testNameIsCorrect() {
        assertEquals("Barcode Validator", new BarcodeValidatorPlugin().getName());
    }
}

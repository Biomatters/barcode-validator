package com.biomatters.plugins.barcoding.validator.validation.input.map;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Matthew Cheung
 *         Created on 25/09/14 4:27 PM
 */
public class FileNameMapperTest extends Assert {

    @Test
    public void testGetOrdinal() {
        assertEquals("1st", FileNameMapper.getOrdinalString(1));
        assertEquals("2nd", FileNameMapper.getOrdinalString(2));
        assertEquals("3rd", FileNameMapper.getOrdinalString(3));
        assertEquals("4th", FileNameMapper.getOrdinalString(4));

        assertEquals("10th", FileNameMapper.getOrdinalString(10));
        assertEquals("11th", FileNameMapper.getOrdinalString(11));
        assertEquals("12th", FileNameMapper.getOrdinalString(12));
        assertEquals("13th", FileNameMapper.getOrdinalString(13));
        assertEquals("14th", FileNameMapper.getOrdinalString(14));

        assertEquals("21st", FileNameMapper.getOrdinalString(21));
        assertEquals("22nd", FileNameMapper.getOrdinalString(22));
        assertEquals("23rd", FileNameMapper.getOrdinalString(23));

        assertEquals("111th", FileNameMapper.getOrdinalString(111));
        assertEquals("112th", FileNameMapper.getOrdinalString(112));
        assertEquals("113th", FileNameMapper.getOrdinalString(113));
    }
}

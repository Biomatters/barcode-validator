package com.biomatters.plugins.barcoding.validator.validation.utilities;

import com.biomatters.plugins.barcoding.validator.validation.*;
import junit.framework.TestCase;

import java.util.List;

/**
 * TODO: Write some javadoc
 *
 * @author Frank Lee
 *         Created on 3/11/14 10:45 AM
 */
public class ClassUtilsTest extends TestCase {

    public void testFindClass_null() {
        List<Class> list = ClassUtils.findClass(null, new Class[]{BarcodeValidation.class});
        assertEquals(0, list.size());
    }

    public void testFindClass_empty() {
        List<Class> list = ClassUtils.findClass("", new Class[]{BarcodeValidation.class});
        assertEquals(0, list.size());
    }

    public void testFindClass_cannotFound() {
        List<Class> list = ClassUtils.findClass("com.biomatters.plugins.barcoding.validator.validation.assembly", new Class[]{BarcodeValidation.class});
        assertEquals(0, list.size());
    }

    public void testFindClass_packageDoseExist() {
        List<Class> list = ClassUtils.findClass("com.biomatters.plugins.barcoding.validator.validation.nopackage", new Class[]{BarcodeValidation.class});
        assertEquals(0, list.size());
    }

    public void testFindClass_fromParentPackage() {
        List<Class> list = ClassUtils.findClass("com.biomatters", new Class[]{BarcodeValidation.class});
        assertEquals(1, list.size());
        assertTrue(list.get(0).equals(BarcodeConsensusValidation.class));
    }

    public void testFindClass() {
        List<Class> list = ClassUtils.findClass("com.biomatters.plugins.barcoding.validator.validation", new Class[]{BarcodeValidation.class});
        assertEquals(1, list.size());
        assertTrue(list.get(0).equals(BarcodeConsensusValidation.class));

        list = ClassUtils.findClass("com.biomatters.plugins.barcoding.validator.validation", new Class[]{TraceValidation.class});
        assertEquals(1, list.size());
        assertTrue(list.get(0).equals(SlidingWindowTraceValidation.class));
    }

    public void testFindClass_interface() {
        List<Class> list = ClassUtils.findClass("com.biomatters", new Class[]{Validation.class});
        assertEquals(2, list.size());
    }

    public void testFindClass_multipleSuper() {
        List<Class> list = ClassUtils.findClass("com.biomatters", new Class[]{Validation.class, BarcodeValidation.class});
        assertEquals(1, list.size());
        assertTrue(list.get(0).equals(BarcodeConsensusValidation.class));
    }
}

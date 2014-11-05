package com.biomatters.plugins.barcoding.validator.validation.utilities;

import com.biomatters.plugins.barcoding.validator.validation.*;
import jebl.evolution.io.ImportException;
import junit.framework.TestCase;

import java.util.List;

/**
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
        boolean pass = false;
        List<Class> list = ClassUtils.findClass("com.biomatters", new Class[]{BarcodeValidation.class});

        for (Class cl : list) {
            if (cl.equals(BarcodeConsensusValidation.class)) {
                pass = true;
            }
        }
        assertTrue(pass);
    }

    public void testFindClass() {
        boolean pass = false;
        List<Class> list = ClassUtils.findClass("com.biomatters.plugins.barcoding.validator.validation", new Class[]{BarcodeValidation.class});
        for (Class cl : list) {
            if (cl.equals(BarcodeConsensusValidation.class)) {
                pass = true;
            }
        }

        assertTrue(pass);

        pass =false;
        list = ClassUtils.findClass("com.biomatters.plugins.barcoding.validator.validation", new Class[]{TraceValidation.class});
        for (Class cl : list) {
            if (cl.equals(SlidingWindowTraceValidation.class)) {
                pass = true;
            }
        }
        assertTrue(pass);
    }

    public void testFindClass_interface() {
        int count = 0;
        List<Class> list = ClassUtils.findClass("com.biomatters", new Class[]{Validation.class});
        for (Class cl : list) {
            if (cl.equals(BarcodeConsensusValidation.class) || cl.equals(SlidingWindowTraceValidation.class)) {
                count++;
            }

        }

        assertEquals(2, count);
    }

    public void testFindClass_multipleSuper() {
        boolean pass = false;
        List<Class> list = ClassUtils.findClass("com.biomatters", new Class[]{Validation.class, BarcodeValidation.class});
        for (Class cl : list) {
            if (cl.equals(BarcodeConsensusValidation.class)) {
                pass = true;
            }
        }
        assertTrue(pass);
    }

    public void testFindClass_fromJar() {
        boolean pass = false;
        List<Class> list = ClassUtils.findClass("jebl.evolution.io", new Class[]{Exception.class});
        for (Class cl : list) {
            if (cl.equals(ImportException.class)) {
                pass = true;
            }
        }
        assertTrue(pass);
    }
}

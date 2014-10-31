package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.plugins.barcoding.validator.validation.utilities.ClassUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a task that validates a collection of barcode sequences
 *
 * @author Matthew Cheung
 *         Created on 21/08/14 4:52 PM
 */
public abstract class BarcodeValidation implements Validation {
    public static final String IMPLEMENTS_PAKCAGE = BarcodeValidation.class.getPackage().getName();
    private static final List<BarcodeValidation> impls;

    static {
        impls = new ArrayList<BarcodeValidation>();

        try {
            List<Class> ret = ClassUtils.findClass(IMPLEMENTS_PAKCAGE, new Class[] {BarcodeValidation.class});
            for (Class cl : ret) {
                impls.add((BarcodeValidation)cl.newInstance());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return List of BarcodeValidation objects.
     */
    public static List<BarcodeValidation> getBarcodeValidations() {
        return impls;
    }
}
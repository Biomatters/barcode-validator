package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.components.Dialogs;
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
    private static List<BarcodeValidation> impls;

    /**
     * @return List of BarcodeValidation objects.
     */
    public static synchronized List<BarcodeValidation> getBarcodeValidations() {
        if (impls == null) {
            impls = new ArrayList<BarcodeValidation>();

            List<Class> ret = ClassUtils.findClass(IMPLEMENTS_PAKCAGE, new Class[] {BarcodeValidation.class});
            for (Class cl : ret) {
                try {
                    impls.add((BarcodeValidation)cl.newInstance());
                } catch (InstantiationException e) {
                    Dialogs.showMessageDialog("Failed to initialize class " + cl.getName(), " because of " + e.getMessage());
                } catch (IllegalAccessException e) {
                    Dialogs.showMessageDialog("Failed to access class " + cl.getName(), " because of " + e.getMessage());
                }
            }
        }
        return impls;
    }
}
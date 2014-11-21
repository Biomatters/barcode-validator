package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.plugins.barcoding.validator.validation.results.ValidationResultEntry;
import com.biomatters.plugins.barcoding.validator.validation.utilities.ClassUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gen Li
 *         Created on 29/09/14 2:47 PM
 */
public abstract class Validation {
    private static final String IMPLEMENTS_PACKAGE = Validation.class.getPackage().getName();
    private static List<Validation> validations = null;

    /**
     * @return List of available Validation objects.
     */
    public synchronized static List<Validation> getValidations() {
        if (validations == null) {
            validations = new ArrayList<Validation>();

            List<Class> validationObjectClasses = ClassUtils.findClass(IMPLEMENTS_PACKAGE, new Class[]{Validation.class});
            for (Class cl : validationObjectClasses) {
                try {
                    validations.add((Validation)cl.newInstance());
                } catch (InstantiationException e) {
                    Dialogs.showMessageDialog("Failed to initialize class " + cl.getName(), " because of " + e.getMessage());
                } catch (IllegalAccessException e) {
                    Dialogs.showMessageDialog("Failed to access class " + cl.getName(), " because of " + e.getMessage());
                }
            }
        }

        return validations;
    }

    public abstract ValidationOptions getOptions();

    public abstract ValidationResultEntry getValidationResultEntry();
}
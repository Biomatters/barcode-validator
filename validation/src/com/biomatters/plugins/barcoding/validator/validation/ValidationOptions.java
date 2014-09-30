package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.plugin.Options;

/**
 * Options associated with Validation objects.
 *
 * @author Gen Li
 *         Created on 29/09/14 3:53 PM
 */
public abstract class ValidationOptions extends Options {
    /**
     * @return The option's identifier.
     */
    public abstract String getIdentifier();

    /**
     * @return A label associated with the option.
     */
    public abstract String getLabel();

    /**
     * @return A description of the option.
     */
    public abstract String getDescription();
}

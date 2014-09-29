package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.plugin.Options;

/**
 * todo: add javadoc for methods.
 * @author Gen Li
 *         Created on 29/09/14 3:53 PM
 */
public abstract class ValidationOptions extends Options {
    public abstract String getName();

    public abstract String getLabel();

    public abstract String getDescription();
}

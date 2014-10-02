package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.plugin.Options;

/**
 * Options used to run {@link com.biomatters.plugins.barcoding.validator.validation.Validation} tasks.
 * <br/><br/>
 * All implementing subclasses must support serialization to and from XML
 * using {@link com.biomatters.geneious.publicapi.documents.XMLSerializer}.  Generally this means that they need to
 * avoid storing internal state outside of the {@link com.biomatters.geneious.publicapi.documents.XMLSerializable#toXML()}
 * {@link com.biomatters.geneious.publicapi.documents.XMLSerializable#fromXML(org.jdom.Element)} methods.
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

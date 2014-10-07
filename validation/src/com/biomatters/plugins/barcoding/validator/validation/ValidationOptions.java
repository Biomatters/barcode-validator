package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import com.biomatters.geneious.publicapi.plugin.Options;
import org.jdom.Element;

/**
 * Options used to run {@link com.biomatters.plugins.barcoding.validator.validation.Validation} tasks.
 * <br/><br/>
 * All implementing subclasses must support serialization to and from XML
 * using {@link com.biomatters.geneious.publicapi.documents.XMLSerializer}.  The easiest way to achieve this is to:
 * <ul>
 *     <li>
 *         Avoid storing internal state outside of the
 *         {@link com.biomatters.geneious.publicapi.documents.XMLSerializable#toXML()}
 *         {@link com.biomatters.geneious.publicapi.documents.XMLSerializable#fromXML(org.jdom.Element)} methods.
 *     </li>
 *     <li>Implement a constructor that takes an {@link org.jdom.Element} and calls super({@link org.jdom.Element})</li>
 * </ul>
 *
 * @author Gen Li
 *         Created on 29/09/14 3:53 PM
 */
public abstract class ValidationOptions extends Options {
    public static final String TRACE_VALIDATION_GROUP = "Trace Validation";
    public static final String BARCODE_VALIDATION_GROUP = "Barcode Validation";

    public ValidationOptions(Element element) throws XMLSerializationException {
        super(element);
    }

    public ValidationOptions(Class cls) {
        super(cls);
    }
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

    /**
     * @return A group of the option.
     */
    public abstract String getGroup();
}

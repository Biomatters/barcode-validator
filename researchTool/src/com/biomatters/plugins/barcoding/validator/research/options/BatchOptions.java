package com.biomatters.plugins.barcoding.validator.research.options;

import com.biomatters.geneious.publicapi.documents.XMLSerializable;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.Options;

import java.util.Iterator;

/**
 * <p>
 * A wrapper for an Options that provides the ability to run batches of parameters.  Replaces any numerical options
 * with {@link com.biomatters.plugins.barcoding.validator.research.options.MultiValueOption} so the user can specify
 * multiple values.
 * </p>
 * <p>
 * Use {@link #iterator()} to iterate over the combinations.
 * </p>
 *
 * @author Matthew Cheung
 *         Created on 31/10/14 2:55 PM
 */
public abstract class BatchOptions<T extends Options> extends Options {
    private T options;

    public BatchOptions(T options) {
        super(options.getClass());
        this.options = options;
        addFirstOptions();
        addChildOptions("child", "", null, options);
        addMinMax(options);
    }

    protected abstract void addFirstOptions();

    private static void addMinMax(Options options) {
        for (Options.Option option : options.getOptions()) {
            MultiValueOption multiValueOption = null;
            if(option instanceof Options.IntegerOption) {
                multiValueOption = new IntegerMultiValueOption((Options.IntegerOption) option);
            } else if(option instanceof Options.DoubleOption) {
                multiValueOption = new DoubleMultiValueOption((Options.DoubleOption)option);
            }

            if(multiValueOption != null) {
                option.setVisible(false);
                options.addCustomOption(multiValueOption);
            }
        }

        for (Options childOptions : options.getChildOptions().values()) {
            addMinMax(childOptions);
        }
    }

    public int getBatchSize() {
        return getBatchSize(this);
    }

    private static int getBatchSize(Options options) {
        int total = 1;
        for (Option option : options.getOptions()) {
            if(option instanceof MultiValueOption<?>) {
                total *= ((MultiValueOption<?>)option).getValue().size();
            }
        }
        for (Options childOptions : options.getChildOptions().values()) {
            total *= getBatchSize(childOptions);
        }
        return total;
    }

    public Iterator<T> iterator() throws DocumentOperationException {
        try {
            final T toReturn = (T)options.getClass().newInstance();
            toReturn.valuesFromXML(this.options.valuesToXML(XMLSerializable.ROOT_ELEMENT_NAME));

            return new Iterator<T>() {

                int count = 0;
                @Override
                public boolean hasNext() {
                    count++;
                    return count < 2;
                }

                @Override
                public T next() {
                    return toReturn;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        } catch (InstantiationException e) {
            throw new DocumentOperationException("Failed to create Options: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new DocumentOperationException("Failed to create Options: " + e.getMessage(), e);
        }

    }
}

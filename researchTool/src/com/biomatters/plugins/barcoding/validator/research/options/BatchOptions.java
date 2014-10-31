package com.biomatters.plugins.barcoding.validator.research.options;

import com.biomatters.geneious.publicapi.documents.XMLSerializable;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.google.common.collect.Sets;

import java.util.*;

/**
 * <p>
 * A wrapper for an Options that provides the ability to run batches of parameters.  Replaces any numerical options
 * with {@link com.biomatters.plugins.barcoding.validator.research.options.MultiValueOption} so the user can specify
 * multiple values.
 * </p>
 * <p>
 * Use {@link #iterator()} to iterate over the combinations.  The iterator will always return the same {@link Options}
 * object but will iterate through the parameter combinations when {@link java.util.Iterator#next()} is called.
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
        List<Set<OptionToSet>> possibleValues = new ArrayList<Set<OptionToSet>>();

        Map<String, MultiValueOption<?>> multiValueOptions = getMultiValueOptions("", options);
        for (Map.Entry<String, MultiValueOption<?>> entry : multiValueOptions.entrySet()) {
            possibleValues.add(getOptionsToSet(entry.getKey(), entry.getValue()));
        }
        Set<List<OptionToSet>> lists = Sets.cartesianProduct(possibleValues);
        final Iterator<List<OptionToSet>> possibilityIterator = lists.iterator();

        final T template;
        try {
            //noinspection unchecked
            template = (T)options.getClass().newInstance();  // getClass() doesn't return Class<T> :(
            template.valuesFromXML(options.valuesToXML(XMLSerializable.ROOT_ELEMENT_NAME));
        } catch (InstantiationException e) {
            throw new DocumentOperationException("Failed to create Options: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new DocumentOperationException("Failed to create Options: " + e.getMessage(), e);
        }

        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return possibilityIterator.hasNext();
            }

            @Override
            public T next() {
                List<OptionToSet> possibility = possibilityIterator.next();
                for (OptionToSet optionToSet : possibility) {
                    template.setValue(optionToSet.name, optionToSet.value);
                }
                return template;
            }

            @Override
            public void remove() {
                new UnsupportedOperationException();
            }
        };
    }

    private static <T extends Number> Set<OptionToSet> getOptionsToSet(String fullOptionName, MultiValueOption<T> multiValueOption) {
        Set<OptionToSet> set = new HashSet<OptionToSet>();
        for (T value : multiValueOption.getValue()) {
            set.add(new OptionToSet<T>(fullOptionName, value));
        }
        return set;
    }

    private static class OptionToSet<T> {
        String name;
        T value;

        private OptionToSet(String name, T value) {
            this.name = name;
            this.value = value;
        }
    }

    private static Map<String, MultiValueOption<?>> getMultiValueOptions(String prefix, Options options) {
        Map<String, MultiValueOption<?>> result = new HashMap<String, MultiValueOption<?>>();
        for (Option option : options.getOptions()) {
            if(option instanceof MultiValueOption) {
                result.put(prefix + option.getName().replace(MultiValueOption.SUFFIX, ""), (MultiValueOption)option);
            }
        }
        for (Map.Entry<String, Options> entry : options.getChildOptions().entrySet()) {
            result.putAll(getMultiValueOptions(prefix + entry.getKey() + ".", entry.getValue()));
        }
        return result;
    }
}

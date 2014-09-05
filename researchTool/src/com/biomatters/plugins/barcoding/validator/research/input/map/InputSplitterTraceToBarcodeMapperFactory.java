package com.biomatters.plugins.barcoding.validator.research.input.map;

import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.plugins.barcoding.validator.research.input.InputSplitterOptions;

/**
 * @author Gen Li
 *         Created on 4/09/14 12:58 PM
 */
public class InputSplitterTraceToBarcodeMapperFactory {
    private InputSplitterTraceToBarcodeMapperFactory() {
    }

    public static TraceToBarcodeMapper getTraceToBarcodeMapper(InputSplitterOptions options) {
        Options traceToBarcodeMapperOptions = options.getMatchTraceToBarcodeMethodOption();
//        if (ByBoldListMapperOptions.class.isAssignableFrom(traceToBarcodeMapperOptions.getClass())) {
//            ByBoldListMapperOptions byBoldListMapperOptions = (ByBoldListMapperOptions)traceToBarcodeMapperOptions;
//        }
//        if (ByGenbankXmlMapperOptions.class.isAssignableFrom(traceToBarcodeMapperOptions.getClass())) {
//            ByGenbankXmlMapperOptions byGenbankXmlMapperOptions = (ByGenbankXmlMapperOptions)traceToBarcodeMapperOptions;
//        }
        if (ByFileNameMapperOptions.class.isAssignableFrom(traceToBarcodeMapperOptions.getClass())) {
            ByFileNameMapperOptions byFileNameMapperOptions = (ByFileNameMapperOptions)traceToBarcodeMapperOptions;

            return new ByFileNameMapper(byFileNameMapperOptions.getTraceSeparator(),
                                        byFileNameMapperOptions.getTraceNamePartNumber(),
                                        byFileNameMapperOptions.getSequenceSeparator(),
                                        byFileNameMapperOptions.getSequenceNamePartNumber());
        }
        throw new IllegalArgumentException("Unrecognized mapper name: " + options.getMatchTraceToBarcodeMethodOption());
    }
}
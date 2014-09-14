package com.biomatters.plugins.barcoding.validator.validation.input.map;

/**
 * @author Gen Li
 *         Created on 4/09/14 12:58 PM
 */
public class BarcodesToTracesMapperFactory {
    private BarcodesToTracesMapperFactory() {
    }

    public static BarcodesToTracesMapper getBarcodesToTracesMapper(BarcodesToTracesMapperOptions options) {
//        if (BoldListMapperOptions.class.isAssignableFrom(options.getClass())) {
//            BoldListMapperOptions boldListMapperOptions = (ByBoldListMapperOptions)options;
//        }
//        if (GenbankXmlMapperOptions.class.isAssignableFrom(options.getClass())) {
//            GenbankXmlMapperOptions genbankXmlMapperOptions = (GenbankXmlMapperOptions)options;
//        }
        if (FileNameMapperOptions.class.isAssignableFrom(options.getClass())) {
            FileNameMapperOptions fileNameMapperOptions = (FileNameMapperOptions)options;

            return new FileNameMapper(fileNameMapperOptions.getTraceSeparator(),
                                      fileNameMapperOptions.getTraceNamePartNumber(),
                                      fileNameMapperOptions.getSequenceSeparator(),
                                      fileNameMapperOptions.getSequenceNamePartNumber());
        }

        throw new IllegalArgumentException("Unrecognized mapper name: " + options.getClass().getSimpleName());
    }
}
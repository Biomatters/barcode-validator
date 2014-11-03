package com.biomatters.plugins.barcoding.validator.validation.trimming;

import com.biomatters.geneious.publicapi.plugin.Options;

/**
 * @author Gen Li
 *         Created on 21/10/14 3:10 PM
 */
public class TrimmingOptions extends Options {
    private static final String QUALITY_TRIMMING_OPTIONS_NAME = "quality";
    private static final String PRIMER_TRIMMING_OPTIONS_NAME  = "primer";

    public TrimmingOptions(Class cls) {
        super(cls);

        init();
    }

    public ErrorProbabilityOptions getQualityTrimmingOptions() {
        return (ErrorProbabilityOptions)getChildOptions().get(QUALITY_TRIMMING_OPTIONS_NAME);
    }

    public PrimerTrimmingOptions getPrimerTrimmingOptions() {
        return (PrimerTrimmingOptions)getChildOptions().get(PRIMER_TRIMMING_OPTIONS_NAME);
    }

    private void init() {
        addChildOptions(QUALITY_TRIMMING_OPTIONS_NAME, "By Quality", "Quality trimming", new ErrorProbabilityOptions(), true);
        addChildOptions(PRIMER_TRIMMING_OPTIONS_NAME, "By Primer", "Primer trimming", new PrimerTrimmingOptions(TrimmingOptions.class), true);
    }
}
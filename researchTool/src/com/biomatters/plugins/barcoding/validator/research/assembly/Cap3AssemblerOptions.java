package com.biomatters.plugins.barcoding.validator.research.assembly;

import com.biomatters.geneious.publicapi.plugin.Options;

/**
 * @author Gen Li
 *         Created on 3/09/14 3:42 PM
 */
public class Cap3AssemblerOptions extends Options {
    private IntegerOption minOverlapLengthOption;
    private IntegerOption minOverlapIdentityOption;

    public Cap3AssemblerOptions() {
        super(Cap3AssemblerOptions.class);

        addMinOverlapLengthOption();

        addMinOverlapIdentityOption();
    }

    public int getMinOverlapLength() {
        return minOverlapLengthOption.getValue();
    }

    public int getMinOverlapIdentity() {
        return minOverlapIdentityOption.getValue();
    }

    private void addMinOverlapLengthOption() {
        minOverlapLengthOption = addIntegerOption("minOverlapLength", "Min overlap length:", 40, 16, 1000);
    }

    private void addMinOverlapIdentityOption() {
        minOverlapIdentityOption = addIntegerOption("minOverlapIdentity", "Min overlap identity:", 90, 66, 1000);
    }
}
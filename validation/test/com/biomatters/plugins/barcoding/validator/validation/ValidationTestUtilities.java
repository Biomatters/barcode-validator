package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.sequence.DefaultNucleotideGraph;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideGraphSequence;
import jebl.evolution.sequences.Nucleotides;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

/**
 * @author Matthew Cheung
 *         Created on 14/11/14 10:38 AM
 */
public class ValidationTestUtilities {


    public static String getRandomString(int length) {
        StringBuilder seqBuilder = new StringBuilder(length);
        for (int j = 0; j < length; j++) {
            seqBuilder.append(Nucleotides.getCanonicalStates().get((int)(Math.random()*4)));
        }
        return seqBuilder.toString();
    }

    public static DefaultNucleotideGraphSequence getTestSequenceWithConsistentQuality(CharSequence charSequence, int qualityValue) {
        int[] qualities = new int[charSequence.length()];
        Arrays.fill(qualities, qualityValue);
        return getTestSequence(charSequence, qualities);
    }

    public static DefaultNucleotideGraphSequence getTestSequence(CharSequence charSequence, int[] qualities) {
        return new DefaultNucleotideGraphSequence(UUID.randomUUID().toString(), null, charSequence, new Date(),
                new DefaultNucleotideGraph(null, null, qualities, charSequence.length(), 0));
    }
}

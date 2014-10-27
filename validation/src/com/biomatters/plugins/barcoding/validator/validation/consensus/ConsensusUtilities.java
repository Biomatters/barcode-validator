package com.biomatters.plugins.barcoding.validator.validation.consensus;

import com.biomatters.geneious.publicapi.documents.sequence.DefaultNucleotideGraph;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideGraphSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.utilities.StringUtilities;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import jebl.evolution.sequences.NucleotideState;
import jebl.evolution.sequences.Nucleotides;
import jebl.evolution.sequences.State;

import java.util.*;

/**
 * @author Matthew Cheung
 *         Created on 24/10/14 4:03 PM
 */
public class ConsensusUtilities {

    /**
     * Generates a consensus from a contig assembly using the quality values of the aligned sequences.
     *
     * @param contigAssembly The assembly to get the consensus of.  MUST have quality values for sequences.
     * @return The consensus.
     */
    public static NucleotideGraphSequenceDocument getConsensus(SequenceAlignmentDocument contigAssembly) throws DocumentOperationException {

        int totalLength = contigAssembly.getSequence(0).getSequenceLength();

        StringBuilder sequenceBuilder = new StringBuilder(totalLength);
        int[] quality = new int[totalLength];

        for (int i = 0; i < totalLength; i++) {
            ConsensusPosition position = getBaseForPosition(contigAssembly, i);
            sequenceBuilder.append(position.base);
            quality[i] = position.quality;
        }

        DefaultNucleotideGraph graph = new DefaultNucleotideGraph(null, null, quality, sequenceBuilder.length(), 0);

        return new DefaultNucleotideGraphSequence("Consensus", "", sequenceBuilder.toString(), new Date(), graph);
    }

    private static class ConsensusPosition {
        String base;
        int quality;

        private ConsensusPosition(String base, int quality) {
            this.base = base;
            this.quality = quality;
        }
    }

    private static ConsensusPosition getBaseForPosition(SequenceAlignmentDocument contigAssembly, int index) throws DocumentOperationException {
        Map<NucleotideState, Integer> stateToTotalQuality = getStateToTotalQualityMapForPosition(contigAssembly, index);

        Set<NucleotideState> stateWithMax = new HashSet<NucleotideState>();
        Integer max = 0;
        for (Map.Entry<NucleotideState, Integer> entry : stateToTotalQuality.entrySet()) {
            int total = entry.getValue();
            if (total > max) {
                stateWithMax.clear();
                max = total;
                stateWithMax.add(entry.getKey());
            } else if (total == max) {
                stateWithMax.add(entry.getKey());
            }
        }

        int quality = 0;
        for (Map.Entry<NucleotideState, Integer> entry : stateToTotalQuality.entrySet()) {
            if(stateWithMax.contains(entry.getKey())) {
                quality += entry.getValue();
            } else {
                quality -= entry.getValue();
            }
        }

        return new ConsensusPosition(getStateForStates(stateWithMax).toString(), quality);
    }

    private static Map<NucleotideState, Integer> getStateToTotalQualityMapForPosition(SequenceAlignmentDocument contigAssembly, int indexInAssembly) throws DocumentOperationException {
        int numSeqs = contigAssembly.getNumberOfSequences();
        Multimap<NucleotideState, Integer> stateToQuality = ArrayListMultimap.create();
        for (int j = 0; j < numSeqs; j++) {
            SequenceDocument sequence = contigAssembly.getSequence(j);
            if (sequence instanceof NucleotideGraphSequenceDocument && ((NucleotideGraphSequenceDocument) sequence).hasSequenceQualities()) {
                int qualityValue = ((NucleotideGraphSequenceDocument) sequence).getSequenceQuality(indexInAssembly);
                NucleotideState state = Nucleotides.getState(sequence.getCharSequence().charAt(indexInAssembly));
                if(!state.isGap()) {
                    for (State canonicalState : state.getCanonicalStates()) {
                        stateToQuality.put((NucleotideState)canonicalState, qualityValue);
                    }
                }
            } else {
                throw new DocumentOperationException("Alignment is missing quality values for " + sequence.getName() + " (index = " + j + ")");
            }
        }
        Map<NucleotideState, Integer> stateToTotalQuality = new HashMap<NucleotideState, Integer>();
        for (Map.Entry<NucleotideState, Collection<Integer>> entry : stateToQuality.asMap().entrySet()) {
            int total = 0;
            for (Integer toAdd : entry.getValue()) {
                total += toAdd;
            }
            stateToTotalQuality.put(entry.getKey(), total);
        }
        return stateToTotalQuality;
    }

    /**
     *
     * @param states The canonical states.  Must be one of {@link jebl.evolution.sequences.Nucleotides#getCanonicalStates()}.
     * @return a {@link jebl.evolution.sequences.State} representing the supplied canonical states.
     */
    private static State getStateForStates(Set<NucleotideState> states) {
        if(states.size() == 1) {
            return states.iterator().next();
        } else {
            for (State possible : Nucleotides.getStates()) {
                if(possible.getCanonicalStates().containsAll(states)) {
                    return possible;
                }
            }
        }
        throw new IllegalStateException("Didn't find valid nucleotide state to represent (" + StringUtilities.join(",", states) + ")");
    }
}

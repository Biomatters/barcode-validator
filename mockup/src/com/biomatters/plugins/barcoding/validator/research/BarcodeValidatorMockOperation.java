package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.documents.XMLSerializerImplementation;
import com.biomatters.geneious.publicapi.documents.sequence.DefaultSequenceListDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideSequence;
import com.biomatters.geneious.publicapi.plugin.*;
import com.biomatters.geneious.publicapi.utilities.ImportUtiltiesThatImportsFastaFiles;
import com.biomatters.plugins.cap3.Cap3Assembler;
import com.biomatters.plugins.cap3.Cap3AssemblerOptions;
import jebl.util.ProgressListener;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Matthew Cheung
 *         Created on 15/07/14 3:01 PM
 */
public class BarcodeValidatorMockOperation extends DocumentOperation {
    @Override
    public GeneiousActionOptions getActionOptions() {
        return new GeneiousActionOptions("Barcode Validator (Mockup)", "Use to test out validation parameters")
                .setInMainToolbar(true);
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public DocumentSelectionSignature[] getSelectionSignatures() {
        return new DocumentSelectionSignature[0];
    }

    @Override
    public Options getOptions(DocumentOperationInput operationInput) throws DocumentOperationException {
        return new BarcodeValidatorMockOptions();
    }

    @Override
    public List<AnnotatedPluginDocument> performOperation(AnnotatedPluginDocument[] annotatedDocuments, ProgressListener progressListener, Options options) throws DocumentOperationException {
        return Collections.singletonList(DocumentUtilities.createAnnotatedPluginDocument(new MockupReport("Report")));
    }


    public static void main(String[] args) throws DocumentOperationException {
        // A test to see if it is possible to run the Biomatters CAP3 plugin with just the Public API

        XMLSerializerImplementation.setup();
        ImportUtiltiesThatImportsFastaFiles.setupImporter();
        PluginUtilitiesImplementationThatReturnsAceImporter.set();

        Cap3AssemblerOptions options = new Cap3AssemblerOptions(null);
        options.setValue("EXE", "/home/matthew/Downloads/CAP3/cap3");
        Cap3Assembler assembler = new Cap3Assembler();

        DefaultNucleotideSequence seq1 = new DefaultNucleotideSequence("test", "GTGGACTAGATTCGGTACACTGTATATTTTATTCGGGATATGATCTGGATTAGTTGGAACGGCTTTGAGGCTCCTGATTCGAGCAGAGCTCGGGCAGCCGGGAGCCCTACTGGGTGATGATCAATTATATAACGTAATTGTAACAGCACATGCTTTTGTAATAATTTTTTTCTTAGTAATGCCTATGATAATTGGAGGATTTGGTAACTGGTTAGTTCCTCTTATATTAGGGGCTCCTGATATGGCTTTCCCTCGACTGAATAACATGAGTTTTTGACTTCTACCTCCTGCTTTATTGCTTTTATTATCTTCTGCAGCAGTTGAGAGAGGGGTAGGAACAGGCTGAACAGTCTATCCTCCGTTAGCGGGAAATCTTGCACATGCCGGAGGTTCAGTAGATCTTGCAATTTTTTCTCTCCACTTAGCGGGGGTGTCTTCAATTTTAGGTGCAGTAAATTTTATTACAACTATTATTAATATGCGATGACAGGGAATACAGTTTGAACGGCTCCCTCTGTTCGTTTGATCCGTAAAGATTACAGCTGTTCTCTTGTTACTTTCTCTACCCGTCTTAGCCGGAGCCATTACCATGCTTTTGACTGACCGTAACTTTAATACTGCTTTCTTCGATCCAGCAGGAGGTGGTGATCCTATTTTGTATCAGCATTTATTTTGATTCTTTGGTCACCCTGAAGTTTACCTGTGTGAAAGTGTTGTCCCA");
        DefaultNucleotideSequence seq2 = new DefaultNucleotideSequence("test", "CCACGGAGAAAGATCAAATAAATGCTGATACAAAATAGGATCACCACCTCCTGCTGGATCGAAGAAAGCAGTATTAAAGTTACGGTCAGTCAAAAGCATGGTAATGGCTCCGGCTAAGACGGGTAGAGAAAGTAACAAGAGAACAGCTGTAATCTTTACGGATCAAACGAACAGAGGGAGCCGTTCAAACTGTATTCCCTGTCATCGCATATTAATAATAGTTGTAATAAAATTTACTGCACCTAAAATTGAAGACACCCCCGCTAAGTGGAGAGAAAAAATTGCAAGATCTACTGAACCTCCGGCATGTGCAAGATTTCCCGCTAACGGAGGATAGACTGTTCAGCCTGTTCCTACCCCTCTCTCAACTGCTGCAGAAGATAATAAAAGCAATAAAGCAGGAGGTAGAAGTCAAAAACTCATGTTATTCAGTCGAGGGAAAGCCATATCAGGAGCCCCTAATATAAGAGGAACTAACCAGTTACCAAATCCTCCAATTATCATAGGCATTACTAAGAAAAAAATTATTACAAAAGCATGTGCTGTTACAATTACGTTATATAATTGATCATCACCCAGTAGGGCTCCCGGCTGCCCGAGCTCTGCTCGAATCAGGAGCCTCAAAGCCGTTCCAACTAATCCAGATCATATCCCGAATAAAATATACAGTGTACCGATGTCTTTATGATTTGTTGACCGTCGTTTTAAAACGTCGTGAGGG");
        DefaultSequenceListDocument list = DefaultSequenceListDocument.forNucleotideSequences(Arrays.<NucleotideSequenceDocument>asList(seq1, seq2));
        AssemblerInput input = new AssemblerInput(Collections.<NucleotideSequenceDocument>emptyList(), list);
        input.setGenerateContigs(false);

        assembler.assemble(options, input, ProgressListener.EMPTY, new Assembler.Callback() {
            @Override
            public void addContigDocument(SequenceAlignmentDocument contig, NucleotideSequenceDocument contigConsensus, boolean isThisTheOnlyContigGeneratedByDeNovoAssembly, ProgressListener progressListener) throws DocumentOperationException {
                System.out.println("Generated! " + contig.getName());
                for (SequenceDocument sequenceDocument : contig.getSequences()) {
                    System.out.println(sequenceDocument.getName() + ": " + sequenceDocument.getCharSequence());
                }
            }

            @Override
            public void addUnusedRead(AssemblerInput.Read read, ProgressListener progressListener) throws DocumentOperationException {
                NucleotideSequenceDocument notUsed = read.getRead();
                System.out.println("Read not used! " + notUsed.getName() + ": " + notUsed.getCharSequence());
            }
        });
        System.exit(0);

    }
}

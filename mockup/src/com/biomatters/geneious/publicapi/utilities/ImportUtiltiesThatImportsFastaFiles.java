package com.biomatters.geneious.publicapi.utilities;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentFileImporter;
import com.biomatters.geneious.publicapi.plugin.DocumentImportException;
import jebl.util.ProgressListener;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Matthew Cheung
 *         Created on 15/08/14 3:14 PM
 */
public class ImportUtiltiesThatImportsFastaFiles {
    public static void setupImporter() {
        ImportUtilities.setImplementation(new ImportUtilities.Implementation() {
            @Override
            public List<SequenceDocument> importFastaSequences(File fastaFile, SequenceDocument.Alphabet alphabet, ProgressListener progressListener) throws IOException, DocumentImportException {


                return Arrays.<SequenceDocument>asList(
                        new DefaultNucleotideSequence("test", "GTGGACTAGATTCGGTACACTGTATATTTTATTCGGGATATGATCTGGATTAGTTGGAACGGCTTTGAGGCTCCTGATTCGAGCAGAGCTCGGGCAGCCGGGAGCCCTACTGGGTGATGATCAATTATATAACGTAATTGTAACAGCACATGCTTTTGTAATAATTTTTTTCTTAGTAATGCCTATGATAATTGGAGGATTTGGTAACTGGTTAGTTCCTCTTATATTAGGGGCTCCTGATATGGCTTTCCCTCGACTGAATAACATGAGTTTTTGACTTCTACCTCCTGCTTTATTGCTTTTATTATCTTCTGCAGCAGTTGAGAGAGGGGTAGGAACAGGCTGAACAGTCTATCCTCCGTTAGCGGGAAATCTTGCACATGCCGGAGGTTCAGTAGATCTTGCAATTTTTTCTCTCCACTTAGCGGGGGTGTCTTCAATTTTAGGTGCAGTAAATTTTATTACAACTATTATTAATATGCGATGACAGGGAATACAGTTTGAACGGCTCCCTCTGTTCGTTTGATCCGTAAAGATTACAGCTGTTCTCTTGTTACTTTCTCTACCCGTCTTAGCCGGAGCCATTACCATGCTTTTGACTGACCGTAACTTTAATACTGCTTTCTTCGATCCAGCAGGAGGTGGTGATCCTATTTTGTATCAGCATTTATTTTGATTCTTTGGTCACCCTGAAGTTTACCTGTGTGAAAGTGTTGTCCCA"),
                        new DefaultNucleotideSequence("test2", "CCACGGAGAAAGATCAAATAAATGCTGATACAAAATAGGATCACCACCTCCTGCTGGATCGAAGAAAGCAGTATTAAAGTTACGGTCAGTCAAAAGCATGGTAATGGCTCCGGCTAAGACGGGTAGAGAAAGTAACAAGAGAACAGCTGTAATCTTTACGGATCAAACGAACAGAGGGAGCCGTTCAAACTGTATTCCCTGTCATCGCATATTAATAATAGTTGTAATAAAATTTACTGCACCTAAAATTGAAGACACCCCCGCTAAGTGGAGAGAAAAAATTGCAAGATCTACTGAACCTCCGGCATGTGCAAGATTTCCCGCTAACGGAGGATAGACTGTTCAGCCTGTTCCTACCCCTCTCTCAACTGCTGCAGAAGATAATAAAAGCAATAAAGCAGGAGGTAGAAGTCAAAAACTCATGTTATTCAGTCGAGGGAAAGCCATATCAGGAGCCCCTAATATAAGAGGAACTAACCAGTTACCAAATCCTCCAATTATCATAGGCATTACTAAGAAAAAAATTATTACAAAAGCATGTGCTGTTACAATTACGTTATATAATTGATCATCACCCAGTAGGGCTCCCGGCTGCCCGAGCTCTGCTCGAATCAGGAGCCTCAAAGCCGTTCCAACTAATCCAGATCATATCCCGAATAAAATATACAGTGTACCGATGTCTTTATGATTTGTTGACCGTCGTTTTAAAACGTCGTGAGGG")
                );
            }

            @Override
            public SequenceAlignmentDocument importFastaAlignment(File fastaFile, SequenceDocument.Alphabet alphabet, ProgressListener progressListener) throws IOException, DocumentImportException {
                return null;
            }

            @Override
            public List<AnnotatedPluginDocument> importDocuments(File file, DocumentFileImporter importer, ImportUtilities.ActionWhenInvalid actionWhenInvalid, ProgressListener progressListener) throws IOException, DocumentImportException {
                return null;
            }

            @Override
            public void importDocuments(File file, DocumentFileImporter.ImportCallback callback, ImportUtilities.ActionWhenInvalid actionWhenInvalid, ImportUtilities.ImportDocumentType importDocumentType, SequenceDocument.Alphabet alphabet, ProgressListener progressListener) throws IOException, DocumentImportException {

            }
        });
    }
}

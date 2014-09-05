package com.biomatters.plugins.barcoding.validator.research.common;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.PluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.DefaultSequenceListDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideGraphSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentImportException;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.PluginUtilities;
import com.biomatters.plugins.barcoding.validator.research.assembly.Cap3AssemblerResult;
import jebl.util.ProgressListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Contains functionality for importing documents.
 * @author Gen Li
 *         Created on 5/09/14 3:09 PM
 */
public class ImportUtilities {
    private ImportUtilities() {
    }

    public static List<NucleotideSequenceDocument> importTraces(List<String> filePathsOfTraces)
            throws DocumentOperationException {
        List<NucleotideSequenceDocument> traces = new ArrayList<NucleotideSequenceDocument>();

        List<Class> validDocumentFormats = Arrays.asList((Class)DefaultNucleotideGraphSequence.class);

        List<AnnotatedPluginDocument> importedDocuments = importDocuments(filePathsOfTraces,
                                                                          validDocumentFormats,
                                                                          "traces");

        for (AnnotatedPluginDocument importedDocument : importedDocuments) {
            for (Class validDocumentFormat : validDocumentFormats) {
                if (NucleotideSequenceDocument.class.isAssignableFrom(validDocumentFormat)) {
                    traces.add((NucleotideSequenceDocument)importedDocument.getDocument());
                }
            }
        }

        return traces;
    }

    public static List<NucleotideSequenceDocument> importBarcodes(List<String> filePathsOfBarcodes)
            throws DocumentOperationException {
        List<NucleotideSequenceDocument> barcodes = new ArrayList<NucleotideSequenceDocument>();

        List<Class> validDocumentFormats = Arrays.asList((Class)DefaultSequenceListDocument.class);

        List<AnnotatedPluginDocument> importedDocuments = importDocuments(filePathsOfBarcodes,
                                                                          validDocumentFormats,
                                                                          "barcodes");

        for (AnnotatedPluginDocument importedDocument : importedDocuments) {
            for (Class validDocumentFormat : validDocumentFormats) {
                if (DefaultSequenceListDocument.class.isAssignableFrom(validDocumentFormat)) {
                    barcodes.addAll(
                            ((DefaultSequenceListDocument)importedDocument.getDocument()).getNucleotideSequences()
                    );
                }
            }
        }

        return barcodes;
    }

    /**
     * Imports contigs.
     *
     * @param result contigs imported.
     * @return paths of {@value com.biomatters.plugins.barcoding.validator.research.assembly.Cap3Assembler#
     * CAP3_ASSEMBLER_RESULT_FILE_EXTENSION} and
     * {@value com.biomatters.plugins.barcoding.validator.research.assembly.Cap3Assembler#
     * CAP3_ASSEMBLER_UNUSED_READS_FILE_EXTENSION} CAP3 assembler output files.
     * @throws DocumentOperationException if error occurs during contig import.
     */
    public static List<PluginDocument> importContigsForCap3Assembler(Cap3AssemblerResult result)
            throws DocumentOperationException {
        final List<PluginDocument> contigs = new ArrayList<PluginDocument>();

        List<Class> validDocumentFormats = Arrays.asList((Class)DefaultSequenceListDocument.class,
                                                         (Class)SequenceAlignmentDocument.class);

        List<AnnotatedPluginDocument> importedDocuments = ImportUtilities.importDocuments(
                Collections.singletonList(result.getPathOfResultFile()),
                validDocumentFormats,
                "contigs"
        );

        return contigs;
    }

    private static List<AnnotatedPluginDocument> importDocuments(List<String> filePathsOfDocuments,
                                                                 List<Class> validDocumentTypes,
                                                                 String simpleNameOfDocument)
            throws DocumentOperationException {
        if (validDocumentTypes.isEmpty()) {
            throw new DocumentOperationException("Could not import " + simpleNameOfDocument + ": " +
                                                 "Please supply at least one document type as.");
        }
        List<AnnotatedPluginDocument> result = new ArrayList<AnnotatedPluginDocument>();

        try {
            for (String path : filePathsOfDocuments) {
                List<AnnotatedPluginDocument> importedDocuments = PluginUtilities.importDocuments(
                        new File(path),
                        ProgressListener.EMPTY
                );

                for (AnnotatedPluginDocument importedDocument : importedDocuments) {
                    if (!isImportedDocumentOfAValidFormat(validDocumentTypes, importedDocument.getDocumentClass())) {
                        throw new DocumentOperationException(
                                importedDocumentIsOfAnInvalidFormatMessage(validDocumentTypes,
                                                                           importedDocument.getDocumentClass(),
                                                                           simpleNameOfDocument)
                        );
                    }
                }
                result.addAll(importedDocuments);
            }
        } catch (IOException e) {
            throw new DocumentOperationException("Could not import " + simpleNameOfDocument + ": " + e.getMessage(), e);
        } catch (DocumentImportException e) {
            throw new DocumentOperationException("Could not import " + simpleNameOfDocument + ": " + e.getMessage(), e);
        }
        return result;
    }

    private static boolean isImportedDocumentOfAValidFormat(List<Class> validDocumentTypes,
                                                            Class typeOfImportedDocument) {
        for (Class validDocumentType : validDocumentTypes) {
            if (!validDocumentType.isAssignableFrom(typeOfImportedDocument)) {
                return false;
            }
        }
        return true;
    }

    private static String importedDocumentIsOfAnInvalidFormatMessage(List<Class> validDocumentTypes,
                                                                     Class typeOfImportedDocument,
                                                                     String simpleNameOfImportedDocument) {
        StringBuilder invalidImportedDocumentFormatMessageBuilder = new StringBuilder();

        invalidImportedDocumentFormatMessageBuilder
                .append("Could not import ").append(simpleNameOfImportedDocument).append(": ")
                .append("Document of invalid type imported, ")
                .append("expected types: ");

        for (Class validDocumentType : validDocumentTypes) {
            invalidImportedDocumentFormatMessageBuilder
                    .append("<? extends ").append(validDocumentType.getSimpleName()).append(">, ");
        }

        invalidImportedDocumentFormatMessageBuilder
                .append("actual type: ")
                .append("<? extends ").append(typeOfImportedDocument.getSimpleName()).append(">.");

        return invalidImportedDocumentFormatMessageBuilder.toString();
    }
}
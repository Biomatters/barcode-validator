package com.biomatters.plugins.barcoding.validator.research.common;

import com.biomatters.geneious.publicapi.documents.*;
import com.biomatters.geneious.publicapi.documents.sequence.DefaultSequenceListDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideGraphSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentImportException;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.PluginUtilities;
import jebl.util.ProgressListener;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Contains methods for importing documents.
 * @author Gen Li
 *         Created on 5/09/14 3:09 PM
 */
public class ImportUtilities {
    private ImportUtilities() {
    }

    public static List<NucleotideSequenceDocument> importTraces(List<String> filePaths)
            throws DocumentOperationException {
        List<NucleotideSequenceDocument> result = new ArrayList<NucleotideSequenceDocument>();

        List<AnnotatedPluginDocument> importedDocuments = importDocuments(
                filePaths,
                Arrays.asList((Class) DefaultNucleotideGraphSequence.class)
        );

        for (AnnotatedPluginDocument importedDocument : importedDocuments)
            result.add((NucleotideSequenceDocument)importedDocument.getDocument());

        return result;
    }

    public static List<NucleotideSequenceDocument> importBarcodes(List<String> filePaths)
            throws DocumentOperationException {
        List<NucleotideSequenceDocument> result = new ArrayList<NucleotideSequenceDocument>();

        List<AnnotatedPluginDocument> importedDocuments = importDocuments(
                filePaths,
                Arrays.asList((Class)DefaultSequenceListDocument.class)
        );

        for (AnnotatedPluginDocument importedDocument : importedDocuments)
            result.addAll(((DefaultSequenceListDocument)importedDocument.getDocument()).getNucleotideSequences());

        return result;
    }

    public static List<SequenceAlignmentDocument> importContigsCap3Assembler(String filePath)
            throws DocumentOperationException {
        List<SequenceAlignmentDocument> result = new ArrayList<SequenceAlignmentDocument>();

        List<AnnotatedPluginDocument> importedDocuments = importDocuments(
                Collections.singletonList(filePath),
                Arrays.asList((Class)DefaultSequenceListDocument.class, (Class)SequenceAlignmentDocument.class)
        );

        for (AnnotatedPluginDocument importedDocument : importedDocuments)
            if (SequenceAlignmentDocument.class.isAssignableFrom(importedDocument.getDocumentClass()))
                result.add((SequenceAlignmentDocument) importedDocument.getDocument());

        return result;
    }

    private static List<AnnotatedPluginDocument> importDocuments(List<String> filePaths,
                                                                 List<Class> expectedDocumentTypes)
            throws DocumentOperationException {
        List<AnnotatedPluginDocument> result = importDocuments(filePaths);

        checkDocumentsAreOfTypes(result, expectedDocumentTypes);

        return result;
    }

    private static List<AnnotatedPluginDocument> importDocuments(List<String> documentPaths)
            throws DocumentOperationException {
        List<AnnotatedPluginDocument> result = new ArrayList<AnnotatedPluginDocument>();

        try {
            for (String path : documentPaths) {
                File importFile = new File(path);

                if (!importFile.exists())
                    throw new DocumentOperationException("Could not import document: " +
                                                         "File '" + path + "' does not exist.");

                result.addAll(PluginUtilities.importDocuments(new File(path), ProgressListener.EMPTY));
            }
            
            return result;
        } catch (IOException e) {
            throw new DocumentOperationException("Could not import documents: " + e.getMessage(), e);
        } catch (DocumentImportException e) {
            throw new DocumentOperationException("Could not import documents: " + e.getMessage(), e);
        }
    }

    private static void checkDocumentsAreOfTypes(List<AnnotatedPluginDocument> documents, List<Class> types)
            throws DocumentOperationException {
        for (AnnotatedPluginDocument document : documents)
            if (!isDocumentOfTypes(document, types))
                throw new DocumentOperationException(importedDocumentUnexpectedTypeMessage(
                        types,
                        document.getDocumentClass(),
                        document.getDocument().getName())
                );
    }
    
    private static boolean isDocumentOfTypes(AnnotatedPluginDocument document, List<Class> types) {
        for (Class type : types)
            if (type.isAssignableFrom(document.getDocumentClass()))
                return true;

        return false;
    }

    private static String importedDocumentUnexpectedTypeMessage(List<Class> expectedTypes,
                                                                Class importedDocumentType,
                                                                String importedDocumentName) {
        StringBuilder messageBuilder = new StringBuilder();

        messageBuilder.append("Could not import ").append(importedDocumentName).append(": ")
                      .append("Document of unexpected type imported, ")
                      .append("expected types: ");

        for (Class validDocumentType : expectedTypes)
            messageBuilder.append("<? extends ").append(validDocumentType.getSimpleName()).append(">, ");

        messageBuilder.append("actual type: ")
                      .append("<? extends ").append(importedDocumentType.getSimpleName()).append(">.");

        return messageBuilder.toString();
    }
}
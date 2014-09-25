package com.biomatters.plugins.barcoding.validator.validation.utilities;

import com.biomatters.geneious.publicapi.documents.*;
import com.biomatters.geneious.publicapi.documents.sequence.DefaultSequenceListDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideGraphSequence;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentImportException;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.PluginUtilities;
import jebl.util.ProgressListener;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Functionality for importing documents in Geneious-native formats. Non-instantiable.
 *
 * @author Gen Li
 *         Created on 5/09/14 3:09 PM
 */
public class ImportUtilities {
    private final static Set<String> TRACE_ALLOWED_FILE_EXTENSIONS
            = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("ab1")));
    private final static Set<String> BARCODE_ALLOWED_FILE_EXTENSIONS
            = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("fasta")));
    private final static Set<String> CONTIGS_ALLOWED_FILE_EXTENSIONS
            = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("ace")));

    private ImportUtilities() {
    }

    /**
     * Imports traces.
     *
     * @param sourcePaths Paths of source files or folders containing source files, from which traces are imported.
     * @return Traces.
     * @throws DocumentOperationException
     */
    public static List<NucleotideSequenceDocument> importTraces(List<String> sourcePaths)
            throws DocumentOperationException {
        List<NucleotideSequenceDocument> result = new ArrayList<NucleotideSequenceDocument>();

        List<AnnotatedPluginDocument> importedDocuments;
        try {
            /* Import traces. */
            importedDocuments = importDocuments(
                    sourcePaths,
                    Arrays.asList((Class) DefaultNucleotideGraphSequence.class),
                    TRACE_ALLOWED_FILE_EXTENSIONS
            );
        } catch (DocumentOperationException e) {
            throw new DocumentOperationException("Could not import traces: " + e.getMessage(), e);
        }

        /* Filter traces. */
        for (AnnotatedPluginDocument importedDocument : importedDocuments) {
            result.add((NucleotideSequenceDocument) importedDocument.getDocument());
        }

        return result;
    }

    /**
     * Imports barcodes.
     *
     * @param sourcePaths Paths of source files or folders containing source files, from which barcodes are imported.
     * @return Barcodes.
     * @throws DocumentOperationException
     */
    public static List<NucleotideSequenceDocument> importBarcodes(List<String> sourcePaths)
            throws DocumentOperationException {
        List<NucleotideSequenceDocument> result = new ArrayList<NucleotideSequenceDocument>();

        List<AnnotatedPluginDocument> importedDocuments;
        try {
            /* Import barcodes. */
            importedDocuments = importDocuments(
                    sourcePaths,
                    Arrays.asList((Class)DefaultSequenceListDocument.class, (Class)DefaultNucleotideSequence.class),
                    BARCODE_ALLOWED_FILE_EXTENSIONS
            );
        } catch (DocumentOperationException e) {
            throw new DocumentOperationException("Could not import barcodes: " + e.getMessage(), e);
        }

        /* Filter barcodes. */
        for (AnnotatedPluginDocument importedDocument : importedDocuments) {
            if (DefaultSequenceListDocument.class.isAssignableFrom(importedDocument.getDocumentClass())) {
                result.addAll(((DefaultSequenceListDocument)importedDocument.getDocument()).getNucleotideSequences());
            } else if (DefaultNucleotideSequence.class.isAssignableFrom(importedDocument.getDocumentClass())) {
                result.add((NucleotideSequenceDocument) importedDocument.getDocument());
            }
        }

        return result;
    }

    /**
     * Imports contigs.
     *
     * @param sourcePath Paths of source files or folders containing source files, from which contigs are imported.
     * @return Contigs.
     * @throws DocumentOperationException
     */
    public static List<SequenceAlignmentDocument> importContigs(String sourcePath)
            throws DocumentOperationException {
        List<SequenceAlignmentDocument> result = new ArrayList<SequenceAlignmentDocument>();

        List<AnnotatedPluginDocument> importedDocuments;
        try {
            /* Import contigs. */
            importedDocuments = importDocuments(
                    Collections.singletonList(sourcePath),
                    Arrays.asList((Class)DefaultSequenceListDocument.class, (Class)SequenceAlignmentDocument.class),
                    CONTIGS_ALLOWED_FILE_EXTENSIONS
            );
        } catch (DocumentOperationException e) {
            throw new DocumentOperationException("Could not import contigs: " + e.getMessage(), e);
        }

        /* Filter contigs. */
        for (AnnotatedPluginDocument importedDocument : importedDocuments) {
            if (SequenceAlignmentDocument.class.isAssignableFrom(importedDocument.getDocumentClass())) {
                result.add((SequenceAlignmentDocument) importedDocument.getDocument());
            }
        }

        return result;
    }

    /**
     * Imports documents and checks the correctness of their types.
     *
     * @param sourcePaths Paths of source files or folders containing source files, from which documents are imported.
     * @param expectedDocumentTypes Expected types of imported documents.
     * @param allowedFileExtensions Allowed source file extensions.
     * @return Documents.
     * @throws DocumentOperationException
     */
    private static List<AnnotatedPluginDocument> importDocuments(List<String> sourcePaths,
                                                                 List<Class> expectedDocumentTypes,
                                                                 Set<String> allowedFileExtensions)
            throws DocumentOperationException {
        List<AnnotatedPluginDocument> result;

        List<File> files = new ArrayList<File>();

        for (String sourcePath : sourcePaths) {
            File file = new File(sourcePath);

            if (!file.exists()) {
                throw new DocumentOperationException("File or directory '" + sourcePath + "' does not exist.");
            }

            files.add(file);
        }

        result = importDocuments(files, allowedFileExtensions);

        checkDocumentsAreOfTypes(result, expectedDocumentTypes);

        return result;
    }

    /**
     * Imports documents. Folders are recursively scanned and their containing files accumulated.
     *
     * @param sources Source files or folders containing source files, from which documents are imported.
     * @param allowedFileExtensions Allowed file extensions for source files..
     * @return Imported documents.
     * @throws DocumentOperationException
     */
    private static List<AnnotatedPluginDocument> importDocuments(List<File> sources,
                                                                 Set<String> allowedFileExtensions)
            throws DocumentOperationException {
        List<AnnotatedPluginDocument> result = new ArrayList<AnnotatedPluginDocument>();

        try {
            for (File source : sources) {
                if (source.isDirectory()) {
                    File[] subSource = source.listFiles();

                    if (subSource == null) {
                        throw new DocumentOperationException("Could not list files under directory '" +
                                                             source.getAbsolutePath() + "'.");
                    }

                    result.addAll(importDocuments(Arrays.asList(subSource), allowedFileExtensions));
                } else if (fileNameHasOneOfExtensions(source.getName(), allowedFileExtensions)) {
                    result.addAll(PluginUtilities.importDocuments(source, ProgressListener.EMPTY));
                }
            }
        } catch (DocumentImportException e) {
            throw new DocumentOperationException(e.getMessage(), e);
        } catch (IOException e) {
            throw new DocumentOperationException(e.getMessage(), e);
        }

        return result;
    }

    /**
     * Checks for the presence of the types of a group of document types in a group of types.
     *
     * @param documents Documents.
     * @param types Types.
     * @throws DocumentOperationException If the type(s) of one or more documents are not present in the group of types.
     */
    private static void checkDocumentsAreOfTypes(List<AnnotatedPluginDocument> documents, List<Class> types)
            throws DocumentOperationException {
        for (AnnotatedPluginDocument document : documents) {
            if (!isDocumentOfTypes(document, types)) {
                throw new DocumentOperationException(importedDocumentUnexpectedTypeMessage(
                        types,
                        document.getDocumentClass(),
                        document.getDocument().getName())
                );
            }
        }
    }

    /**
     * Checks for the presence of a document's type among a group of types.
     *
     * @param document Document.
     * @param types Types.
     * @return True if the document's type is among the group of types; false if not.
     */
    private static boolean isDocumentOfTypes(AnnotatedPluginDocument document, List<Class> types) {
        for (Class<?> type : types) {
            if (type.isAssignableFrom(document.getDocumentClass())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns an error message for when an imported document has the wrong type.
     *
     * @param expectedTypes Expected types.
     * @param importedDocumentType Imported document type.
     * @param importedDocumentName Imported document name.
     * @return Error message.
     */
    private static String importedDocumentUnexpectedTypeMessage(List<Class> expectedTypes,
                                                                Class importedDocumentType,
                                                                String importedDocumentName) {
        StringBuilder messageBuilder = new StringBuilder();

        messageBuilder.append("Imported document '").append(importedDocumentName).append(" is of an unexpected type, ")
                      .append("expected types: ");

        for (Class validDocumentType : expectedTypes) {
            messageBuilder.append("<? extends ").append(validDocumentType.getSimpleName()).append(">, ");
        }

        messageBuilder.append("actual type: ").append(importedDocumentType.getSimpleName());

        return messageBuilder.toString();
    }

    /**
     * Checks for the presence of a filename's extension among a group of extensions.
     *
     * @param fileName Filename.
     * @param extensions Extensions.
     * @return True if the filename has an extension that is among the group of extensions; false if not.
     */
    private static boolean fileNameHasOneOfExtensions(String fileName, Set<String> extensions) {
        for (String extension : extensions) {
            if (fileName.endsWith("." + extension)) {
                return true;
            }
        }

        return false;
    }
}
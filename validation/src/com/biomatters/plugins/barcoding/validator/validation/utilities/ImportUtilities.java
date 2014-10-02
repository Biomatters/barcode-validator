package com.biomatters.plugins.barcoding.validator.validation.utilities;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.DefaultSequenceListDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
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
    /* Allowed file extensions. */
    public final static Set<String> TRACE_ALLOWED_FILE_EXTENSIONS =  Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("abi", "ab1", "scf")));
    private final static Set<String> BARCODE_ALLOWED_FILE_EXTENSIONS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("fasta", "fas")));
    private final static Set<String> CONTIGS_ALLOWED_FILE_EXTENSIONS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("ace")));

    private ImportUtilities() {
    }

    /**
     * Imports traces.
     *
     * @param sourcePaths Paths of source files and/or folders containing source files.
     * @return Traces.
     * @throws DocumentOperationException
     */
    public static List<NucleotideGraphSequenceDocument> importTraces(List<String> sourcePaths) throws DocumentOperationException {
        List<NucleotideGraphSequenceDocument> result = new ArrayList<NucleotideGraphSequenceDocument>();
        List<AnnotatedPluginDocument> importedDocuments;

        /* Import documents. */
        try {
            importedDocuments = importDocuments(sourcePaths, Arrays.asList((Class)NucleotideGraphSequenceDocument.class), TRACE_ALLOWED_FILE_EXTENSIONS);
        } catch (DocumentOperationException e) {
            throw new DocumentOperationException("Could not import traces: " + e.getMessage(), e);
        }

        for (AnnotatedPluginDocument importedDocument : importedDocuments) {
            result.add((NucleotideGraphSequenceDocument)importedDocument.getDocument());
        }

        return result;
    }

    /**
     * Imports barcodes.
     *
     * @param sourcePaths Paths of source files and/or folders containing source files.
     * @return Barcodes.
     * @throws DocumentOperationException
     */
    public static List<NucleotideSequenceDocument> importBarcodes(List<String> sourcePaths) throws DocumentOperationException {
        List<NucleotideSequenceDocument> result = new ArrayList<NucleotideSequenceDocument>();
        List<AnnotatedPluginDocument> importedDocuments;

        /* Import documents. */
        try {
            importedDocuments = importDocuments(
                    sourcePaths, Arrays.asList((Class)DefaultSequenceListDocument.class, (Class)DefaultNucleotideSequence.class), BARCODE_ALLOWED_FILE_EXTENSIONS
            );
        } catch (DocumentOperationException e) {
            throw new DocumentOperationException("Could not import barcodes: " + e.getMessage(), e);
        }

        /* Filter out barcodes. */
        for (AnnotatedPluginDocument importedDocument : importedDocuments) {
            if (DefaultSequenceListDocument.class.isAssignableFrom(importedDocument.getDocumentClass())) {
                result.addAll(((DefaultSequenceListDocument)importedDocument.getDocument()).getNucleotideSequences());
            } else if (DefaultNucleotideSequence.class.isAssignableFrom(importedDocument.getDocumentClass())) {
                result.add((NucleotideSequenceDocument)importedDocument.getDocument());
            }
        }

        return result;
    }

    /**
     * Imports contigs.
     *
     * @param sourcePath Paths of source files and/or folders containing source files.
     * @return Contigs.
     * @throws DocumentOperationException
     */
    public static List<SequenceAlignmentDocument> importContigs(String sourcePath) throws DocumentOperationException {
        List<SequenceAlignmentDocument> result = new ArrayList<SequenceAlignmentDocument>();
        List<AnnotatedPluginDocument> importedDocuments;

        /* Import documents. */
        try {
            importedDocuments = importDocuments(
                    Collections.singletonList(sourcePath), Arrays.asList((Class)DefaultSequenceListDocument.class, (Class)SequenceAlignmentDocument.class), CONTIGS_ALLOWED_FILE_EXTENSIONS
            );
        } catch (DocumentOperationException e) {
            throw new DocumentOperationException("Could not import contigs: " + e.getMessage(), e);
        }

        /* Filter out contigs. */
        for (AnnotatedPluginDocument importedDocument : importedDocuments) {
            if (SequenceAlignmentDocument.class.isAssignableFrom(importedDocument.getDocumentClass())) {
                result.add((SequenceAlignmentDocument) importedDocument.getDocument());
            }
        }

        return result;
    }

    /**
     * Imports documents.
     *
     * @param sourcePaths Paths of source files and/or folders containing source files.
     * @param expectedDocumentTypes Expected document types.
     * @param allowedFileExtensions Allowed file extensions.
     * @return Documents.
     * @throws DocumentOperationException
     */
    private static List<AnnotatedPluginDocument> importDocuments(List<String> sourcePaths, List<Class> expectedDocumentTypes, Set<String> allowedFileExtensions)
            throws DocumentOperationException {
        List<AnnotatedPluginDocument> result;
        List<File> files = new ArrayList<File>();

        /* Check existence of source paths. */
        for (String sourcePath : sourcePaths) {
            File file = new File(sourcePath);
            if (!file.exists()) {
                throw new DocumentOperationException("File or directory '" + sourcePath + "' does not exist.");
            }
            files.add(file);
        }

        /* Import. */
        result = importDocuments(files, allowedFileExtensions);

        /* Check types of documents. */
        checkDocumentsAreOfTypes(result, expectedDocumentTypes);

        return result;
    }

    /**
     * Imports documents. Folders are recursively scanned and their containing files accumulated.
     *
     * @param sources Source files or folders containing source files.
     * @param allowedFileExtensions Allowed file extensions.
     * @return Documents.
     * @throws DocumentOperationException
     */
    private static List<AnnotatedPluginDocument> importDocuments(List<File> sources, Set<String> allowedFileExtensions) throws DocumentOperationException {
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
     * Given documents D and types T, verify T contains type of each document in D.
     *
     * @param documents Documents.
     * @param types Types.
     * @throws DocumentOperationException If verification fails.
     */
    private static void checkDocumentsAreOfTypes(List<AnnotatedPluginDocument> documents, List<Class> types) throws DocumentOperationException {
        for (AnnotatedPluginDocument document : documents) {
            if (!isDocumentOfTypes(document, types)) {
                throw new DocumentOperationException(buildDocumentUnexpectedTypeMessage(types, document));
            }
        }
    }

    /**
     * Given document D and types T, verify T contains type of D.
     *
     * @param document Document.
     * @param types Types.
     * @return True if verification succeeds and false if not.
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
     * Returns error message for when document has wrong type.
     *
     * @param expectedTypes Expected types.
     * @param document Document.
     * @return Error message.
     */
    private static String buildDocumentUnexpectedTypeMessage(List<Class> expectedTypes, AnnotatedPluginDocument document) {
        StringBuilder messageBuilder = new StringBuilder();

        messageBuilder.append("Imported document '").append(document.getName()).append(" is of an unexpected type, ")
                      .append("expected types: ");

        for (Class validDocumentType : expectedTypes) {
            messageBuilder.append("<? extends ").append(validDocumentType.getSimpleName()).append(">, ");
        }

        messageBuilder.append("actual type: ").append(document.getDocumentClass().getSimpleName());

        return messageBuilder.toString();
    }

    /**
     * Given file name N and file extensions G, verify G contains N's extension.
     *
     * @param fileName Filename.
     * @param extensions File extensions.
     * @return True if verification succeeds and false if not.
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
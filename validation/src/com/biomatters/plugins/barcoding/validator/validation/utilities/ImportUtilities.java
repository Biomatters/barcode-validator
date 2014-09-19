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
import com.biomatters.geneious.publicapi.utilities.StringUtilities;
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
     * @param sourcePath Paths of source files or folders containing source files from which traces are imported.
     * @return Imported traces.
     * @throws DocumentOperationException
     */
    public static List<NucleotideSequenceDocument> importTraces(List<String> sourcePath)
            throws DocumentOperationException {
        List<NucleotideSequenceDocument> result = new ArrayList<NucleotideSequenceDocument>();

        List<AnnotatedPluginDocument> importedDocuments;

        try {
            /* Import traces. */
            importedDocuments = importDocuments(
                    sourcePath,
                    Arrays.asList((Class) DefaultNucleotideGraphSequence.class),
                    TRACE_ALLOWED_FILE_EXTENSIONS
            );
        } catch (DocumentOperationException e) {
            throw new DocumentOperationException("Could not import traces: " + e.getMessage(), e);
        }

        /* Filter traces. */
        for (AnnotatedPluginDocument importedDocument : importedDocuments)
            result.add((NucleotideSequenceDocument)importedDocument.getDocument());

        return result;
    }

    /**
     * Imports barcodes.
     *
     * @param sourcePath Paths of source files or folders containing source files from which barcodes are imported.
     * @return Imported barcodes.
     * @throws DocumentOperationException
     */
    public static List<NucleotideSequenceDocument> importBarcodes(List<String> sourcePath)
            throws DocumentOperationException {
        List<NucleotideSequenceDocument> result = new ArrayList<NucleotideSequenceDocument>();

        List<AnnotatedPluginDocument> importedDocuments;

        try {
            /* Import barcodes. */
            importedDocuments = importDocuments(
                    sourcePath,
                    Arrays.asList((Class) DefaultSequenceListDocument.class, (Class) DefaultNucleotideSequence.class),
                    BARCODE_ALLOWED_FILE_EXTENSIONS
            );
        } catch (DocumentOperationException e) {
            throw new DocumentOperationException("Could not import barcodes: " + e.getMessage(), e);
        }

        /* Filter barcodes. */
        for (AnnotatedPluginDocument importedDocument : importedDocuments)
            if (DefaultSequenceListDocument.class.isAssignableFrom(importedDocument.getDocumentClass()))
                result.addAll(((DefaultSequenceListDocument)importedDocument.getDocument()).getNucleotideSequences());
            else if (DefaultNucleotideSequence.class.isAssignableFrom(importedDocument.getDocumentClass()))
                result.add((NucleotideSequenceDocument)importedDocument.getDocument());

        return result;
    }

    /**
     * Imports contigs.
     *
     * @param sourcePath Paths of source files or folders containing source files from which contigs are imported.
     * @return Imported contigs.
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
                    Arrays.asList((Class) DefaultSequenceListDocument.class, (Class) SequenceAlignmentDocument.class),
                    CONTIGS_ALLOWED_FILE_EXTENSIONS
            );
        } catch (DocumentOperationException e) {
            throw new DocumentOperationException("Could not import contigs: " + e.getMessage(), e);
        }

        /* Filter contigs. */
        for (AnnotatedPluginDocument importedDocument : importedDocuments)
            if (SequenceAlignmentDocument.class.isAssignableFrom(importedDocument.getDocumentClass()))
                result.add((SequenceAlignmentDocument)importedDocument.getDocument());

        return result;
    }

    /**
     * Imports documents and checks the correctness of their types.
     *
     * @param filePaths Paths of source files or folders containing source files from which documents are imported.
     * @param expectedDocumentTypes Types among which the imported documents' types must be.
     * @param allowedFileExtensions File extensions that each document must associate with one of.
     * @return Imported documents.
     * @throws DocumentOperationException
     */
    private static List<AnnotatedPluginDocument> importDocuments(List<String> filePaths,
                                                                 List<Class> expectedDocumentTypes,
                                                                 Set<String> allowedFileExtensions)
            throws DocumentOperationException {
        List<AnnotatedPluginDocument> result;

        List<File> files = new ArrayList<File>();

        for (String filePath : filePaths) {
            File file = new File(filePath);

            if (!file.exists())
                throw new DocumentOperationException("File or directory '" + filePath + "' does not exist.");

            files.add(file);
        }

        result = importDocuments(files, allowedFileExtensions);

        checkDocumentsAreOfTypes(result, expectedDocumentTypes);

        return result;
    }

    /**
     * Imports documents. Folders are recursively scanned and their containing files accumulated.
     *
     * @param sourcefiles Source files or folders containing source files from which documents are imported.
     * @param allowedFileExtensions Extensions among which the extensions of the source files must be.
     * @return Imported documents.
     * @throws DocumentOperationException
     */
    private static List<AnnotatedPluginDocument> importDocuments(List<File> sourcefiles,
                                                                 Set<String> allowedFileExtensions)
            throws DocumentOperationException {
        List<AnnotatedPluginDocument> result = new ArrayList<AnnotatedPluginDocument>();

        try {
            for (File sourceFile : sourcefiles)
                if (sourceFile.isDirectory())
                    result.addAll(importDocuments(Arrays.asList(sourceFile.listFiles()), allowedFileExtensions));
                else {
                    if (!fileNameHasOneOfExtensions(sourceFile.getName(), allowedFileExtensions))
                        throw new DocumentOperationException(fileNameInvalidExtensionMessage(sourceFile.getName(),
                                                                                             allowedFileExtensions));

                    result.addAll(PluginUtilities.importDocuments(sourceFile, ProgressListener.EMPTY));
                }
        } catch (DocumentImportException e) {
            throw new DocumentOperationException(e.getMessage(), e);
        } catch (IOException e) {
            throw new DocumentOperationException(e.getMessage(), e);
        }

        return result;
    }

    /**
     * Checks for the presence of a group of documents' types in a group of types.
     *
     * @param documents Documents.
     * @param types Types.
     * @throws DocumentOperationException If the type of one or more documents aren't present in the group of types.
     */
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

    /**
     * Checks for the presence of a document's type in a group of types.
     *
     * @param document Document.
     * @param types Types.
     * @return True if the document's type is present in the group of types; false if not.
     */
    private static boolean isDocumentOfTypes(AnnotatedPluginDocument document, List<Class> types) {
        for (Class type : types)
            if (type.isAssignableFrom(document.getDocumentClass()))
                return true;

        return false;
    }

    /**
     * Returns an error message for when an imported document has the wrong type.
     *
     * @param expectedTypes Expected types.
     * @param importedDocumentType Type of the imported document.
     * @param importedDocumentName Name of the imported document.
     * @return Error message.
     */
    private static String importedDocumentUnexpectedTypeMessage(List<Class> expectedTypes,
                                                                Class importedDocumentType,
                                                                String importedDocumentName) {
        StringBuilder messageBuilder = new StringBuilder();

        messageBuilder.append("Imported document '").append(importedDocumentName).append(" is of an unexpected type, ")
                      .append("expected types: ");

        for (Class validDocumentType : expectedTypes)
            messageBuilder.append("<? extends ").append(validDocumentType.getSimpleName()).append(">, ");

        messageBuilder.append("actual type: ").append(importedDocumentType.getSimpleName());

        return messageBuilder.toString();
    }

    /**
     * Checks for the presence of a filename's extension in a group of extensions.
     *
     * @param fileName Filename.
     * @param extensions Extensions.
     * @return True if the filename has an extension that is among the supplied extensions; false if not.
     */
    private static boolean fileNameHasOneOfExtensions(String fileName, Set<String> extensions) {
        for (String extension : extensions)
            if (fileName.endsWith("." + extension))
                return true;

        return false;
    }

    /**
     * Returns an error message for when a file has the wrong extension.
     *
     * @param fileName Filename.
     * @param allowedExtensions Allowed extensions.
     * @return Error message.
     */
    private static String fileNameInvalidExtensionMessage(String fileName, Set<String> allowedExtensions) {
        StringBuilder messageBuilder = new StringBuilder();

        messageBuilder.append("File name '").append(fileName).append("' has an incorrect extension, ")
                      .append("allowed extensions: ")
                      .append(StringUtilities.join(", ", allowedExtensions)).append(".");

        return messageBuilder.toString();
    }
}
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
 * Contains methods for importing documents.
 * @author Gen Li
 *         Created on 5/09/14 3:09 PM
 */
public class ImportUtilities {
    private final static List<String> TRACE_ALLOWED_FILE_EXTENSIONS
            = Collections.unmodifiableList(Arrays.asList("ab1"));
    private final static List<String> BARCODE_ALLOWED_FILE_EXTENSIONS
            = Collections.unmodifiableList(Arrays.asList("fasta"));
    private final static List<String> CONTIGS_CAP3ASSEMBLER_ALLOWED_FILE_EXTENSIONS
            = Collections.unmodifiableList(Arrays.asList("ace"));

    private ImportUtilities() {
    }

    public static List<NucleotideSequenceDocument> importTraces(List<String> filePaths)
            throws DocumentOperationException {
        List<NucleotideSequenceDocument> result = new ArrayList<NucleotideSequenceDocument>();

        List<AnnotatedPluginDocument> importedDocuments = importDocuments(
                filePaths,
                Arrays.asList((Class)DefaultNucleotideGraphSequence.class),
                TRACE_ALLOWED_FILE_EXTENSIONS
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
                Arrays.asList((Class)DefaultSequenceListDocument.class, (Class)DefaultNucleotideSequence.class),
                BARCODE_ALLOWED_FILE_EXTENSIONS
        );

        for (AnnotatedPluginDocument importedDocument : importedDocuments)
            if (DefaultSequenceListDocument.class.isAssignableFrom(importedDocument.getDocumentClass()))
                result.addAll(((DefaultSequenceListDocument)importedDocument.getDocument()).getNucleotideSequences());
            else if (DefaultNucleotideSequence.class.isAssignableFrom(importedDocument.getDocumentClass()))
                result.add((NucleotideSequenceDocument)importedDocument.getDocument());

        return result;
    }

    public static List<SequenceAlignmentDocument> importContigsCap3Assembler(String filePath)
            throws DocumentOperationException {
        List<SequenceAlignmentDocument> result = new ArrayList<SequenceAlignmentDocument>();

        List<AnnotatedPluginDocument> importedDocuments = importDocuments(
                Collections.singletonList(filePath),
                Arrays.asList((Class)DefaultSequenceListDocument.class, (Class)SequenceAlignmentDocument.class),
                CONTIGS_CAP3ASSEMBLER_ALLOWED_FILE_EXTENSIONS
        );

        for (AnnotatedPluginDocument importedDocument : importedDocuments)
            if (SequenceAlignmentDocument.class.isAssignableFrom(importedDocument.getDocumentClass()))
                result.add((SequenceAlignmentDocument)importedDocument.getDocument());

        return result;
    }

    private static List<AnnotatedPluginDocument> importDocuments(List<String> filePaths,
                                                                 List<Class> expectedDocumentTypes,
                                                                 List<String> allowedFileExtensions)
            throws DocumentOperationException {
        List<AnnotatedPluginDocument> result;

        List<File> files = new ArrayList<File>();

        for (String filePath : filePaths) {
            File file = new File(filePath);

            if (!file.exists())
                throw new DocumentOperationException("Could not import documents: " +
                                                     "File or directory '" + filePath + "' does not exist.");

            files.add(file);
        }

        result = importDocuments(files, allowedFileExtensions);

        checkDocumentsAreOfTypes(result, expectedDocumentTypes);

        return result;
    }

    private static List<AnnotatedPluginDocument> importDocuments(List<File> files, List<String> allowedFileExtensions)
            throws DocumentOperationException {
        List<AnnotatedPluginDocument> result = new ArrayList<AnnotatedPluginDocument>();

        try {
            for (File file : files)
                if (file.isDirectory())
                    result.addAll(importDocuments(Arrays.asList(file.listFiles()), allowedFileExtensions));
                else {
                    if (fileNameHasOneOfExtensions(file.getName(), allowedFileExtensions))
                        throw new DocumentOperationException(fileNameInvalidExtensionMessage(file.getName(),
                                                                                             allowedFileExtensions));
                    
                    result.addAll(PluginUtilities.importDocuments(file, ProgressListener.EMPTY));
                }
        } catch (DocumentImportException e) {
            throw new DocumentOperationException("Could not import documents: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new DocumentOperationException("Could not import documents: " + e.getMessage(), e);
        }

        return result;
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

    private static boolean fileNameHasOneOfExtensions(String fileName, List<String> extensions) {
        for (String extension : extensions)
            if (fileName.endsWith("." + extension))
                return true;

        return false;
    }

    private static String fileNameInvalidExtensionMessage(String fileName, List<String> allowedExtensions) {
        StringBuilder messageBuilder = new StringBuilder();

        messageBuilder.append("Could not import documents: ")
                      .append("File name '").append(fileName).append("' has an incorrect extension, ")
                      .append("allowed extensions: ")
                      .append(StringUtilities.join(", ", allowedExtensions)).append(".");

        return messageBuilder.toString();
    }
}
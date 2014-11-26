package com.biomatters.plugins.barcoding.validator.validation.utilities;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.DefaultSequenceListDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentImportException;
import com.biomatters.geneious.publicapi.plugin.DocumentOperation;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.PluginUtilities;
import jebl.evolution.io.FastaImporter;
import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.GaplessSequence;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.util.Cancelable;
import jebl.util.ProgressListener;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
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
     * @param operationCallback
     * @param cancelable
     * @return Traces.
     * @throws DocumentOperationException
     */
    public static List<AnnotatedPluginDocument> importTraces(List<String> sourcePaths, DocumentOperation.OperationCallback operationCallback, Cancelable cancelable) throws DocumentOperationException {
        try {
            return importDocuments(sourcePaths, Arrays.asList((Class)NucleotideGraphSequenceDocument.class), TRACE_ALLOWED_FILE_EXTENSIONS, operationCallback, cancelable);
        } catch (DocumentOperationException e) {
            throw new DocumentOperationException("Could not import traces: " + e.getMessage(), e);
        }
    }

    /**
     * Imports barcodes.
     *
     * @param sourcePaths Paths of source files and/or folders containing source files.
     * @param operationCallback
     * @param cancelable
     * @return Barcodes.
     * @throws DocumentOperationException
     */
    public static List<AnnotatedPluginDocument> importBarcodes(List<String> sourcePaths, DocumentOperation.OperationCallback operationCallback, Cancelable cancelable) throws DocumentOperationException {
        List<AnnotatedPluginDocument> result = new ArrayList<AnnotatedPluginDocument>();

        /* Check existence of source paths, and import */
        for (String sourcePath : sourcePaths) {
            if(cancelable.isCanceled()) {
                throw new DocumentOperationException.Canceled();
            }

            File file = new File(sourcePath);
            if (!file.exists()) {
                throw new DocumentOperationException("File or directory '" + sourcePath + "' does not exist.");
            }

            if (file.isDirectory()) {
                File[] subSource = file.listFiles();
                if (subSource == null) {
                    throw new DocumentOperationException("Could not list files under directory '" + file.getAbsolutePath() + "'.");
                }

                List<String> subFiles = new ArrayList<String>();
                for (File f : subSource) {
                    subFiles.add(f.getPath());
                }

                result.addAll(importBarcodes(subFiles, operationCallback, cancelable));
            } else if (fileNameHasOneOfExtensions(file.getName(), BARCODE_ALLOWED_FILE_EXTENSIONS)) {
                try {
                    FastaImporter importer = new FastaImporter(file, SequenceType.NUCLEOTIDE);
                    List<Sequence> sequences = importer.importSequences();
                    for (Sequence seq : sequences) {
                        DefaultNucleotideSequence seqDoc = new DefaultNucleotideSequence(new GaplessSequence(seq));
                        String sequenceName = seqDoc.getName();
                        AnnotatedPluginDocument apd = operationCallback.addDocument(seqDoc, true, ProgressListener.EMPTY);
                        if(!sequenceName.equals(apd.getName())) {
                            apd.setName(sequenceName);
                            apd.save();
                        }
                        result.add(apd);
                    }
                } catch (FileNotFoundException e) {
                    throw new DocumentOperationException("Could not import barcodes: " + e.getMessage(), e);
                } catch (ImportException e) {
                    throw new DocumentOperationException("Could not import barcodes: " + e.getMessage(), e);
                } catch (IOException e) {
                    throw new DocumentOperationException("Could not import barcodes: " + e.getMessage(), e);
                }
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
                    Collections.singletonList(sourcePath), Arrays.asList((Class)DefaultSequenceListDocument.class, (Class)SequenceAlignmentDocument.class), CONTIGS_ALLOWED_FILE_EXTENSIONS,
                    null, ProgressListener.EMPTY);
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
     * @param operationCallback
     * @param cancelable
     * @return Documents.
     * @throws DocumentOperationException
     */
    private static List<AnnotatedPluginDocument> importDocuments(List<String> sourcePaths, List<Class> expectedDocumentTypes,
                                                                 Set<String> allowedFileExtensions,
                                                                 @Nullable DocumentOperation.OperationCallback operationCallback, Cancelable cancelable)
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
        result = importDocuments(files, allowedFileExtensions, operationCallback, cancelable);

        /* Check types of documents. */
        checkDocumentsAreOfTypes(result, expectedDocumentTypes);

        return result;
    }

    /**
     * Imports documents. Folders are recursively scanned and their containing files accumulated.
     *
     * @param sources Source files or folders containing source files.
     * @param allowedFileExtensions Allowed file extensions.
     * @param operationCallback
     * @param cancelable
     * @return Documents.
     * @throws DocumentOperationException
     */
    private static List<AnnotatedPluginDocument> importDocuments(List<File> sources, Set<String> allowedFileExtensions,
                                                                 @Nullable DocumentOperation.OperationCallback operationCallback, Cancelable cancelable) throws DocumentOperationException {
        List<AnnotatedPluginDocument> result = new ArrayList<AnnotatedPluginDocument>();

        try {
            for (File source : sources) {
                if(cancelable.isCanceled()) {
                    throw new DocumentOperationException.Canceled();
                }

                if (source.isDirectory()) {
                    File[] subSource = source.listFiles();

                    if (subSource == null) {
                        throw new DocumentOperationException("Could not list files under directory '" +
                                                             source.getAbsolutePath() + "'.");
                    }

                    result.addAll(importDocuments(Arrays.asList(subSource), allowedFileExtensions, operationCallback, cancelable));
                } else if (fileNameHasOneOfExtensions(source.getName(), allowedFileExtensions)) {
                    List<AnnotatedPluginDocument> imported = PluginUtilities.importDocuments(source, ProgressListener.EMPTY);
                    String originalFilename = source.getName();
                    for (AnnotatedPluginDocument annotatedPluginDocument : imported) {
                        if(operationCallback != null) {
                            annotatedPluginDocument = operationCallback.addDocument(annotatedPluginDocument, true, ProgressListener.EMPTY);
                            if(!originalFilename.equals(annotatedPluginDocument.getName())) {
                                annotatedPluginDocument.setName(originalFilename);
                                annotatedPluginDocument.save();
                            }
                        }
                        result.add(annotatedPluginDocument);
                    }
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

        messageBuilder.append("Imported document '").append(document.getName()).append("' is of an unexpected type, ")
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
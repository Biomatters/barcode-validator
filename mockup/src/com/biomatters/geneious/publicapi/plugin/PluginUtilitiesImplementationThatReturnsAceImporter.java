package com.biomatters.geneious.publicapi.plugin;

import com.biomatters.geneious.publicapi.databaseservice.WritableDatabaseService;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentField;
import com.biomatters.geneious.publicapi.documents.DocumentSearchCache;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.plugins.fileimportexport.AceImporter.AceDocumentImporter;
import jebl.util.ProgressListener;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @author Matthew Cheung
 *         Created on 15/08/14 3:21 PM
 */
public class PluginUtilitiesImplementationThatReturnsAceImporter {
    public static void set() {
        PluginUtilities.setImplementation(new PluginUtilities.Implementation() {
            @Override
            public List<SequenceAnnotationGenerator> getSequenceAnnotationGenerators() {
                return null;
            }

            @Override
            public SequenceAlignmentDocument doAlignment(List<SequenceDocument> sequences, ProgressListener progressListener) throws DocumentOperationException {
                return null;
            }

            @Override
            public void exportDocumentsInGeneiousFormat(File outputFile, ProgressListener progressListener, AnnotatedPluginDocument[] documents) throws IOException {

            }

            @Override
            public void exportDocumentsInGeneiousFormat(OutputStream outputStream, ProgressListener progressListener, AnnotatedPluginDocument[] documents) throws IOException {

            }

            @Override
            public List<AnnotatedPluginDocument> importDocuments(File file, ProgressListener progress) throws IOException, DocumentImportException {
                return null;
            }

            @Override
            public List<AnnotatedPluginDocument> importDocumentsToDatabase(File file, WritableDatabaseService destination, ProgressListener progress) throws DocumentImportException, IOException {
                return null;
            }

            @Override
            public GeneiousService getGeneiousService(String uniqueID) {
                return null;
            }

            @Override
            public List<GeneiousService> getGeneiousServices() {
                return null;
            }

            @Override
            public List<DocumentFileExporter> getDocumentFileExporters() {
                return null;
            }

            @Override
            public List<DocumentFileImporter> getDocumentFileImporters() {
                return null;
            }

            @Override
            public List<SequenceGraphFactory> getSequenceGraphFactories() {
                return null;
            }

            @Override
            public List<TreeViewerExtension.Factory> getTreeViewerExtensionFactories() {
                return null;
            }

            @Override
            public List<SequenceViewerExtension.Factory> getSequenceViewerExtensionFactories() {
                return null;
            }

            @Override
            public List<DocumentOperation> getDocumentOperations() {
                return null;
            }

            @Override
            public DocumentOperation getCategoryOperation(GeneiousActionOptions.Category category) {
                return null;
            }

            @Override
            public DocumentOperation getDocumentOperation(String uniqueID) {
                return null;
            }

            @Override
            public SequenceAnnotationGenerator getSequenceAnnotationGenerator(String uniqueID) {
                return null;
            }

            @Override
            public DocumentFileImporter getDocumentFileImporter(String uniqueID) {
                return new AceDocumentImporter();
            }

            @Override
            public DocumentFileExporter getDocumentFileExporter(String uniqueID) {
                return null;
            }

            @Override
            public List<DocumentViewerFactory> getDocumentViewerFactories(AnnotatedPluginDocument... documents) {
                return null;
            }

            @Override
            public void waitForGeneiousServiceInitializationToComplete() {

            }

            @Override
            public List<WritableDatabaseService> getWritableDatabaseServiceRoots() {
                return null;
            }

            @Override
            public void addWritableDatabaseServiceRootListener(PluginUtilities.WritableDatabaseServicesListener listener) {

            }

            @Override
            public void removeWritableDatabaseServiceRootListener(PluginUtilities.WritableDatabaseServicesListener listener) {

            }

            @Override
            public List<Assembler> getAssemblers() {
                return null;
            }

            @Override
            public DocumentSelectionOption.FolderOrDocuments displayDocumentSearchDialog(DocumentSearchCache searchCache, List<AnnotatedPluginDocument> extraDocuments, DocumentSelectionOption.FolderOrDocuments value, List<DocumentField> fieldsToDisplay, boolean allowMultipleSelection, boolean showUseAllButton, JComponent owner) {
                return null;
            }

            @Override
            public List<GeneiousPlugin> getActiveGeneiousPlugins() {
                return null;
            }

            @Override
            public GeneiousPlugin getPluginForOperation(DocumentOperation operation) {
                return null;
            }

            @Override
            public GeneiousPlugin getPluginForAssembler(Assembler assembler) {
                return null;
            }

            @Override
            public Map<GeneiousService, Options> getGeneiousRemoteServicesThatCanRunOperation(DocumentOperation operation, AnnotatedPluginDocument[] documents) {
                return null;
            }

            @Override
            public void runJobForGeneiousGridOperation(GeneiousGridDocumentOperation operation, AnnotatedPluginDocument[] documents, ProgressListener progress, Options options, SequenceSelection sequenceSelection, DocumentOperation.OperationCallback callback) throws DocumentOperationException {

            }

            @Override
            public GeneiousPlugin getPluginForSequenceAnnotationGenerator(SequenceAnnotationGenerator sequenceAnnotationGenerator) {
                return null;
            }

            @Override
            public boolean isRunningFromScript() {
                return false;
            }
        });
    }
}

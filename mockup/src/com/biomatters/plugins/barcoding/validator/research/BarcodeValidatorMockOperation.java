package com.biomatters.plugins.barcoding.validator.research;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.documents.URN;
import com.biomatters.geneious.publicapi.documents.sequence.*;
import com.biomatters.geneious.publicapi.plugin.*;
import com.biomatters.geneious.publicapi.utilities.FileUtilities;
import com.biomatters.geneious.publicapi.utilities.ThreadUtilities;
import com.biomatters.plugins.barcoding.validator.research.data.Set;
import com.biomatters.plugins.barcoding.validator.research.report.MockupReport;
import jebl.util.CompositeProgressListener;
import jebl.util.ProgressListener;

import java.io.IOException;
import java.util.*;

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
        progressListener.setMessage("Running through validation steps...");
        CompositeProgressListener composite = new CompositeProgressListener(progressListener, 8);
        List<Set> passed = new ArrayList<Set>();
        composite.beginSubtask();
        passed.add(getExampleSet("good.geneious"));
        composite.beginSubtask();
        passed.add(getExampleSet("good.geneious"));
        composite.beginSubtask();
        passed.add(getExampleSet("good.geneious"));

        Map<Set, String> failed = new LinkedHashMap<Set, String>();
        composite.beginSubtask();
        failed.put(getExampleSet("WrongBarcode.geneious"), " ,Assembled traces do not match barcode, , ");
        composite.beginSubtask();
        failed.put(getExampleSet("BadQuality.geneious"), "Traces don't meet quality criteria,Couldn't assemble,Couldn't assemble, ");
        composite.beginSubtask();
        failed.put(getExampleSet("WrongBarcode.geneious"), " ,Assembled traces do not match barcode, , ");
        composite.beginSubtask();
        failed.put(getExampleSet("BadQuality.geneious"), "Traces don't meet quality criteria,Couldn't assemble,Couldn't assemble, ");

        composite.beginSubtask("Creating mockup reports...");
        ThreadUtilities.sleep(500);
        return Collections.singletonList(DocumentUtilities.createAnnotatedPluginDocument(new MockupReport("Report", passed, failed)));
    }

    public Set getExampleSet(String name) throws DocumentOperationException {
        try {
            List<AnnotatedPluginDocument> docs = PluginUtilities.importDocuments(FileUtilities.getResourceForClass(BarcodeValidatorMockOperation.class, name), ProgressListener.EMPTY);
            List<AnnotatedPluginDocument> added = DocumentUtilities.addAndReturnGeneratedDocuments(docs, false, Collections.<AnnotatedPluginDocument>emptyList());
            URN barcode = null;
            List<URN> traces = new ArrayList<URN>();
            for (AnnotatedPluginDocument toCheck : added) {
                if(((NucleotideGraphSequenceDocument)toCheck.getDocument()).getChromatogramLength() > 0) {
                    traces.add(toCheck.getURN());
                } else {
                    barcode = toCheck.getURN();
                }
            }
            return new Set(barcode, traces);
        } catch (IOException e) {
            throw new DocumentOperationException(e.getMessage(), e);
        } catch (DocumentImportException e) {
            throw new DocumentOperationException(e.getMessage(), e);
        }
    }
}

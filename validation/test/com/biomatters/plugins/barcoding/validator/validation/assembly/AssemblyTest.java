package com.biomatters.plugins.barcoding.validator.validation.assembly;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideGraphSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.TestGeneious;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gen Li
 *         Created on 28/08/14 3:37 PM
 */
public class AssemblyTest extends Assert {
    @Test
    public void testContigAssembled() throws DocumentOperationException {
        TestGeneious.initializePlugins(
                "com.biomatters.plugins.fileimportexport.AceImporter.AceImporterPlugin", // Required to process Cap3 results
                "com.biomatters.plugins.local.LocalDatabasePlugin" // Required because Ace importer requires a WritableDatabaseService
        );
        final String theSequence = "ACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTG";

        NucleotideGraphSequenceDocument document = new DefaultNucleotideGraphSequence(null,
                                                                                      null,
                                                                                      theSequence,
                                                                                      null,
                                                                                      null);


        List<NucleotideGraphSequenceDocument> documents = new ArrayList<NucleotideGraphSequenceDocument>();
        documents.add(document);
        documents.add(document);

        List<SequenceAlignmentDocument> result = Cap3AssemblerRunner.assemble(documents, 40, 90);
        assertEquals(1, result.size());

        List<SequenceDocument> sequences = result.get(0).getSequences();
        assertEquals(3, sequences.size());

        for (int i = 1; i < sequences.size(); i++) {
            String withNoGaps = sequences.get(i).getSequenceString().replace("-", "");
            assertEquals(theSequence, withNoGaps);
        }
    }
}

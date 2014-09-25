package com.biomatters.plugins.barcoding.validator.validation.assembly;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.TestGeneious;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Gen Li
 *         Created on 28/08/14 3:37 PM
 */
public class AssemblyTest extends Assert {
    @Test
    public void testContigAssembled() throws DocumentOperationException {
        TestGeneious.initializePlugins(
                "com.biomatters.plugins.fileimportexport.AceImporter.AceImporterPlugin",  // Required to process Cap3 results
                "com.biomatters.plugins.local.LocalDatabasePlugin"  // Required becasue Ace importer requires a WritableDatabaseService
        );
        final String theSequence = "ACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTG";

        NucleotideSequenceDocument document = new DefaultNucleotideSequence("testDoc",
                                                                            "Test document",
                                                                            theSequence,
                                                                            new Date());

        List<NucleotideSequenceDocument> documents = new ArrayList<NucleotideSequenceDocument>();
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

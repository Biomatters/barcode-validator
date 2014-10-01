package com.biomatters.plugins.barcoding.validator.validation.assembly;

import com.biomatters.geneious.publicapi.documents.sequence.DefaultNucleotideGraph;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideGraphSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.TestGeneious;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Gen Li
 *         Created on 28/08/14 3:37 PM
 */
public class AssemblyTest extends Assert {
    @Test
    public void testContigAssembly() throws DocumentOperationException {
        Assume.assumeTrue(canRun(CAP3Options.getDefaultCap3ExecutableName()));

        // "com.biomatters.plugins.fileimportexport.AceImporter.AceImporterPlugin" required to process Cap3 results.
        // "com.biomatters.plugins.local.LocalDatabasePlugin" required because Ace importer requires a
        // WritableDatabaseService.
        TestGeneious.initializePlugins(
                "com.biomatters.plugins.fileimportexport.AceImporter.AceImporterPlugin",
                "com.biomatters.plugins.local.LocalDatabasePlugin"
        );

        final String theSequence = "ACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTG";

        NucleotideGraphSequenceDocument document = new DefaultNucleotideGraphSequence("",
                                                                                      "",
                                                                                      "",
                                                                                      new Date(),
                                                                                      new DefaultNucleotideGraph(null,
                                                                                                                 null,
                                                                                                                 null,
                                                                                                                 0,
                                                                                                                 0));

        List<NucleotideGraphSequenceDocument> documents = new ArrayList<NucleotideGraphSequenceDocument>();
        documents.add(document);
        documents.add(document);

        List<SequenceAlignmentDocument> result = CAP3Runner.assemble(documents,
                                                                     CAP3Options.getDefaultCap3ExecutableName(),
                                                                     40,
                                                                     90);
        assertEquals(1, result.size());

        List<SequenceDocument> sequences = result.get(0).getSequences();
        assertEquals(3, sequences.size());

        for (int i = 1; i < sequences.size(); i++) {
            String withNoGaps = sequences.get(i).getSequenceString().replace("-", "");

            assertEquals(theSequence, withNoGaps);
        }
    }

    public static boolean canRun(String executablePath) {
        try {
            Runtime.getRuntime().exec(executablePath);

            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
package com.biomatters.plugins.barcoding.validator.validation.assembly;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.documents.sequence.*;
import com.biomatters.geneious.publicapi.implementations.DefaultAlignmentDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.utilities.*;
import com.biomatters.plugins.barcoding.validator.validation.utilities.ImportUtilities;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import jebl.util.ProgressListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Functionality for utilizing CAP3. Non-instantiable.
 *
 * @author Gen Li
 *         Created on 22/08/14 12:07 PM
 */
public class CAP3Runner {
    private static final String CAP3_ASSEMBLER_RESULT_FILE_EXTENSION = ".cap.ace";

    private static final String MIN_OVERLAP_LENGTH_COMMANDLINE_OPTION   = "-o";
    private static final String MIN_OVERLAP_IDENTITY_COMMANDLINE_OPTION = "-p";

    private CAP3Runner() {
    }

    /**
     * Assembles contigs.
     *
     * @param sequences Sequences.
     * @param executablePath CAP3 executable path.
     * @param minOverlapLength Minimum overlap length.
     * @param minOverlapIdentity Minimum overlap identity.
     * @param contigName The name to use for the resulting assembly
     * @param progressListener To report progress to and for cancelling the assembly
     * @return Contigs.
     * @throws DocumentOperationException
     */
    public static List<SequenceAlignmentDocument> assemble(List<NucleotideGraphSequenceDocument> sequences,
                                                           String executablePath,
                                                           int minOverlapLength,
                                                           int minOverlapIdentity,
                                                           String contigName,
                                                           ProgressListener progressListener) throws DocumentOperationException {
        if (sequences.size() < 2) { // There must be at least two traces to produce an assembly
            return Collections.emptyList();
        }

        BiMap<String, NucleotideGraphSequenceDocument> nameSequenceMapping = HashBiMap.create();

        for (NucleotideGraphSequenceDocument seq : sequences) {
            String tmpName = UUID.randomUUID().toString();

            if (nameSequenceMapping.containsValue(seq)) {
                throw new DocumentOperationException("Cannot assemble sequence to itself.  Input list contains multiple copies of " + seq.getName() +".");
            }

            nameSequenceMapping.put(tmpName, seq);
        }

        try {
            String resultFilePath = runCap3Assembler(createFastaFile(sequences, nameSequenceMapping.inverse()), executablePath, minOverlapLength, minOverlapIdentity, progressListener);

            if (assemblyFailed(sequences, resultFilePath)) {
                return Collections.emptyList();
            }

            List<SequenceAlignmentDocument> alignments = ImportUtilities.importContigs(resultFilePath);

            List<SequenceAlignmentDocument> results = new ArrayList<SequenceAlignmentDocument>();
            for (SequenceAlignmentDocument align : alignments) {
                int expectedSequences = align.getNumberOfSequences() - 1;
                List<CharSequence> alignedCharSequences = new ArrayList<CharSequence>(expectedSequences);
                List<SequenceDocument> unalignedSeqDocs = new ArrayList<SequenceDocument>(expectedSequences);
                List<SequenceAlignmentDocument.ReferencedSequence> referencedSequences = new ArrayList<SequenceAlignmentDocument.ReferencedSequence>(expectedSequences);

                for (SequenceDocument alignedSeq : align.getSequences()) {
                    NucleotideGraphSequenceDocument originalDoc = getStartFromMap(nameSequenceMapping, alignedSeq.getName());

                    if (originalDoc != null) {
                        alignedCharSequences.add(alignedSeq.getCharSequence());

                        unalignedSeqDocs.add(originalDoc);

                        AnnotatedPluginDocument apd = DocumentUtilities.getAnnotatedPluginDocumentThatContains(originalDoc);
                        if (apd != null) {
                            referencedSequences.add(new SequenceAlignmentDocument.ReferencedSequence(apd));
                        } else {
                            referencedSequences.add(null);
                        }
                    }
                }

                List<CharSequence> alignedCharSequencesWithAnnotation = restoreTrim(unalignedSeqDocs, alignedCharSequences);

                DefaultAlignmentDocument assembly = new DefaultAlignmentDocument(
                        unalignedSeqDocs.toArray(new SequenceDocument[expectedSequences]),
                        referencedSequences,
                        alignedCharSequencesWithAnnotation.toArray(new CharSequence[expectedSequences]),
                        null, null, contigName == null ? "Assembly" : contigName,
                        ProgressListener.EMPTY
                );

                assembly.setContig(true);

                results.add(assembly);
            }
            return results;
        } catch (DocumentOperationException e) {
            throw new DocumentOperationException("Could not assemble contigs: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            throw new DocumentOperationException("Could not assemble contigs: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new DocumentOperationException("Could not assemble contigs: " + e.getMessage(), e);
        }
    }

    private static List<CharSequence> restoreTrim(List<SequenceDocument> unalignedSeqDocs, List<CharSequence> alignedCharSequences) throws DocumentOperationException {
        List<CharSequence> ret = new ArrayList<CharSequence>();
        List<TempSequence> sequenceList = new ArrayList<TempSequence>();
        int start = Integer.MAX_VALUE;
        int end = Integer.MIN_VALUE;

        for (int i = 0; i < unalignedSeqDocs.size(); i++) {
            TempSequence tempSequence = new TempSequence(unalignedSeqDocs.get(i), alignedCharSequences.get(i));

            start = start > tempSequence.getPredictStartIndex() ? tempSequence.getPredictStartIndex() : start;
            end = end > tempSequence.getPredictEndIndex() ? end : tempSequence.getPredictEndIndex();

            sequenceList.add(tempSequence);
        }

        for (TempSequence tempSequence : sequenceList) {
            ret.add(tempSequence.restoreTrimSequence(start, end));
        }

        return ret;
    }

    private static class TempSequence {
        private SequenceDocument sequenceDocument;
        private CharSequence charSequence;
        private CharSequence prefix = SequenceCharSequence.EMPTY;
        private CharSequence suffix = SequenceCharSequence.EMPTY;
        private int nonGapStart;
        private int nonGapEnd;
        private boolean isReversed;

        private TempSequence(SequenceDocument sequenceDocument, CharSequence charSequence) {
            this.sequenceDocument = sequenceDocument;
            this.charSequence = charSequence;

            nonGapStart = -1;
            int i = 0;
            while (charSequence.charAt(i) == '-' && i < charSequence.length()) {
                i++;
            }
            nonGapStart = i;

            i = charSequence.length() - 1;
            while (charSequence.charAt(i) == '-' && i >= 0) {
                i--;
            }
            nonGapEnd = i;

            CharSequence withoutGaps = SequenceUtilities.removeGaps(charSequence);

            SequenceCharSequence originCharSeq = sequenceDocument.getCharSequence();

            if (originCharSeq.toString().toUpperCase().contains(withoutGaps.toString())) {
                isReversed = false;
            } else {
                CharSequence reverseComplement = SequenceUtilities.reverseComplement(withoutGaps);

                if (originCharSeq.toString().toUpperCase().contains(reverseComplement.toString())) {
                    isReversed = true;
                } else {
                    System.out.println("sequences do not match");
                    System.out.println("sequence document is : " + originCharSeq);
                    System.out.println("sequence from alignment is : " + charSequence);

                    throw new IllegalArgumentException("sequences do not match");
                }
            }

            for (SequenceAnnotation annotation : sequenceDocument.getSequenceAnnotations()) {
                if (SequenceAnnotation.TYPE_TRIMMED.equals(annotation.getType())) {
                    for (SequenceAnnotationInterval interval : annotation.getIntervals()) {
                        if (interval.getFrom() == 1) {
                            prefix = originCharSeq.subSequence(0, interval.getTo());
                        }

                        if (interval.getTo() == originCharSeq.length()) {
                            suffix = originCharSeq.subSequence(interval.getFrom() - 1, interval.getTo());
                        }
                    }
                }
            }

            if (isReversed) {
                CharSequence tmp = SequenceUtilities.reverseComplement(prefix);

                prefix = SequenceUtilities.reverseComplement(suffix);

                suffix = tmp;
            }
        }

        public SequenceDocument getSequenceDocument() {
            return sequenceDocument;
        }

        public void setSequenceDocument(SequenceDocument sequenceDocument) {
            this.sequenceDocument = sequenceDocument;
        }

        public CharSequence getCharSequence() {
            return charSequence;
        }

        public void setCharSequence(CharSequence charSequence) {
            this.charSequence = charSequence;
        }

        public int getPredictStartIndex (){
            return nonGapStart - prefix.length();
        }

        public int getPredictEndIndex (){
            return nonGapEnd + suffix.length();
        }

        public CharSequence restoreTrimSequence(int start, int end) {
            List<CharSequence> sequences = new ArrayList<CharSequence>();

            sequences.add(prefix);
            sequences.add(charSequence.subSequence(nonGapStart, nonGapEnd + 1));
            sequences.add(suffix);

            CharSequence concatenate = CharSequenceUtilities.concatenate(sequences);

            return SequenceCharSequence.withTerminalGaps(getPredictStartIndex() - start, concatenate, end - getPredictEndIndex());
        }
    }

    private static NucleotideGraphSequenceDocument getStartFromMap(Map<String, NucleotideGraphSequenceDocument> nameSequenceMapping, String tmpName) {
        for (Map.Entry<String, NucleotideGraphSequenceDocument> entry : nameSequenceMapping.entrySet()) {
            String key = entry.getKey();

            if (tmpName.startsWith(key)) {
                return entry.getValue();
            }
        }

        return null;
    }


    /**
     * Hack method that tries to detect if assembly failed using the result file.  This is required because:
     * <ul>
     *     <li>CAP3 runs successfully and produces an almost empty Ace file when assembly fails.</li>
     *     <li>The AceDocumentImporter will throw an Exception and show a dialog when it encounters this bad file.</li>
     * </ul>
     * @param sequences The sequences that were to be assembled
     * @param resultFilePath The result file from CAP3
     * @return true iff the assembly process failed
     */
    private static boolean assemblyFailed(List<NucleotideGraphSequenceDocument> sequences, String resultFilePath) {
        File resultFile = new File(resultFilePath);
        // The result file would use at least a byte per character in the sequence name.  If it doesn't it probably
        // does not contain an assembly
        long sizeOfDocs = 0;

        for (NucleotideGraphSequenceDocument sequence : sequences) {
            sizeOfDocs += sequence.getName().length();
        }

        return !resultFile.exists() || resultFile.length() < sizeOfDocs;
    }

    /**
     * Runs CAP3.
     *
     * @param fastafilePath Fasta file path.
     * @param executablePath CAP3 executable path.
     * @param minOverlapLength Minimum overlap length.
     * @param minOverlapIdentity Minimum overlap identity.
     * @param progressListener to provide to the {@link com.biomatters.geneious.publicapi.utilities.Execution} for cancelling the external CAP3 process
     * @return {@value #CAP3_ASSEMBLER_RESULT_FILE_EXTENSION} output file path.
     * @throws DocumentOperationException
     * @throws InterruptedException
     * @throws IOException
     */
    private static String runCap3Assembler(String fastafilePath, String executablePath, int minOverlapLength, int minOverlapIdentity, ProgressListener progressListener)
            throws DocumentOperationException, InterruptedException, IOException {
        Cap3OutputListener listener = new Cap3OutputListener();

        Execution exec = new Execution(
                new String[] {
                        executablePath,
                        fastafilePath,
                        MIN_OVERLAP_LENGTH_COMMANDLINE_OPTION,
                        String.valueOf(minOverlapLength),
                        MIN_OVERLAP_IDENTITY_COMMANDLINE_OPTION,
                        String.valueOf(minOverlapIdentity)
                },
                progressListener,
                listener,
                "",
                false
        );
        exec.setWorkingDirectory(fastafilePath.substring(0, fastafilePath.lastIndexOf(File.separator)));

        /* Run. */
        int exitCode = exec.execute(getEnvironmentToRunCap3());

        if (exitCode != 0) {
            throw new DocumentOperationException("CAP3 failed with exit code " + exitCode + ":\n\n" + listener.getStderrOutput());
        }

        return fastafilePath + CAP3_ASSEMBLER_RESULT_FILE_EXTENSION;
    }

    /**
     * Creates fasta file from sequences.
     *
     * @param sequences Sequences.
     * @return Fasta file path.
     */
    private static String createFastaFile(List<NucleotideGraphSequenceDocument> sequences, Map<NucleotideGraphSequenceDocument, String> renameMap) throws IOException {
        File fastaFile = FileUtilities.createTempFile("temp", ".fasta", false);

        BufferedWriter writer = new BufferedWriter(new FileWriter(fastaFile));
        writer.write(toFastaFormat(sequences, renameMap));
        writer.close();

        return fastaFile.getAbsolutePath();
    }

    /**
     * Generates fasta output from sequences.
     *
     * @param sequences Sequences.
     * @return Fasta output.
     */
    private static String toFastaFormat(List<NucleotideGraphSequenceDocument> sequences, Map<NucleotideGraphSequenceDocument, String> renameMap) {
        StringBuilder fastaOutput = new StringBuilder();

        /* Generate fasta output. */
        for (NucleotideGraphSequenceDocument seq : sequences) {
            String name = renameMap.get(seq);
            if (name == null) {
                name = seq.getName();
            }

            char[] chars = seq.getSequenceString().toUpperCase().toCharArray();
            List<SequenceAnnotation> sequenceAnnotations = seq.getSequenceAnnotations();
            for (SequenceAnnotation annotation : sequenceAnnotations) {
                String type = annotation.getType();

                if (SequenceAnnotation.TYPE_TRIMMED.endsWith(type)) {
                    for (SequenceAnnotationInterval interval : annotation.getIntervals()) {
                        replaceChars(chars, interval, '_');
                    }
                }
            }

            String ret = new String(chars).replaceAll("_", "");
            fastaOutput.append(">").append(name).append(" ").append(seq.getDescription()).append("\n")
                       .append(ret).append("\n");
        }

        /* Remove trailing new line. */
        if (fastaOutput.length() > 0) {
            fastaOutput.deleteCharAt(fastaOutput.length() - 1);
        }

        return fastaOutput.toString();
    }

    private static void replaceChars(char[] chars, SequenceAnnotationInterval interval, char c) {
        for (int i = interval.getFrom() - 1; i <= interval.getTo() - 1; i++) {
            chars[i] = c;
        }
    }

    private static Map<String, String> getEnvironmentToRunCap3() throws DocumentOperationException {
        String dllFileName = "cygwin1.dll";
        String pathVariableName = "Path";

        if (SystemUtilities.isWindows()) {
            File cygwinDll = FileUtilities.getResourceForClass(CAP3Runner.class, dllFileName);

            if (cygwinDll == null) {
                throw new DocumentOperationException(dllFileName + " missing from plugin.  Try reinstalling.");
            }

            return Collections.singletonMap(pathVariableName, System.getProperty(pathVariableName) + ";" + cygwinDll.getParent());
        } else {
            return Collections.emptyMap();
        }
    }
}
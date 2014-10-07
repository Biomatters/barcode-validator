package com.biomatters.plugins.barcoding.validator.validation.input.map;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.google.common.collect.ArrayListMultimap;

import java.io.*;
import java.util.*;

/**
 * Maps barcodes to traces via bold file.
 *
 * @author Gen Li
 *         Created on 5/09/14 9:54 AM
 */
public class BoldListMapper extends BarcodesToTracesMapper {
    private static final String BOLD_TSV_PROCESS_ID_COLUMN_HEADER = "PROCESSID";
    private static final String BOLD_TSV_TRACE_FILE_COLUMN_HEADER = "TRACEFILE";
    private static final String BOLD_TSV_SEPARATOR = "\t";

    private static final String BOLD_BARCODE_DESCRIPTION_SEPARATOR = "\\|";

    private String boldFilePath;

    public BoldListMapper(String boldFilePath) {
        this.boldFilePath = boldFilePath;
    }

    @Override
    public Map<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>> map(List<NucleotideSequenceDocument> barcodes,
                                                                                      List<NucleotideGraphSequenceDocument> traces)
            throws DocumentOperationException {
        try {
            /* Get map of process ids to trace file names */
            Map<String, Collection<String>> processIdToTraceFileName = getProcessIdToTraceFileName();

            /* Map and return. */
            return map(barcodes, traces, processIdToTraceFileName);
        } catch (BoldListMapperException e) {
            throw new DocumentOperationException("Could not map barcodes to traces: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new DocumentOperationException("Could not map barcodes to traces: " + e.getMessage(), e);
        }
    }

    private Map<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>> map(List<NucleotideSequenceDocument> barcodes,
                                                                                       List<NucleotideGraphSequenceDocument> traces,
                                                                                       Map<String, Collection<String>> processIdToTraceFileName)
            throws DocumentOperationException {
        Map<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>> result =
                new HashMap<NucleotideSequenceDocument, List<NucleotideGraphSequenceDocument>>();

        Map<String, NucleotideSequenceDocument> processIdToBarcode = getProcessIdToBarcode(barcodes);

        /* Map. */
        for (NucleotideSequenceDocument barcode : barcodes) {
            result.put(barcode, new ArrayList<NucleotideGraphSequenceDocument>());
        }

        for (NucleotideGraphSequenceDocument trace : traces) {
            String traceName = trace.getName();
            String processId = null;

            for (Map.Entry<String, Collection<String>> entry : processIdToTraceFileName.entrySet()) {
                if (entry.getValue().contains(traceName)) {
                    processId = entry.getKey();
                }
            }

            if (processId == null) {
                throw new DocumentOperationException("No associated barcode was found for trace '" + traceName + "'.");
            }

            NucleotideSequenceDocument barcode = processIdToBarcode.get(processId);

            if (processIdToBarcode.get(processId) == null) {
                throw new DocumentOperationException("No associated barcode was found for trace '" + traceName + "'.");
            }

            result.get(barcode).add(trace);
        }

        return result;
    }

    /**
     * Returns map of process ids to barcodes.
     * @param barcodes Barcodes.
     * @return Map of process ids to barcodes.
     * @throws DocumentOperationException
     */
    private Map<String, NucleotideSequenceDocument> getProcessIdToBarcode(List<NucleotideSequenceDocument> barcodes) {
        Map<String, NucleotideSequenceDocument> result = new HashMap<String, NucleotideSequenceDocument>();

        for (NucleotideSequenceDocument barcode : barcodes) {
            result.put(barcode.getDescription().split(BOLD_BARCODE_DESCRIPTION_SEPARATOR)[0], barcode);
        }

        return result;
    }

    /**
     * Returns map of process ids to traces from bold file.
     *
     * @return Map of process ids to traces.
     * @throws BoldListMapperException
     * @throws IOException
     */
    private Map<String, Collection<String>> getProcessIdToTraceFileName() throws BoldListMapperException, IOException {
        ArrayListMultimap<String, String> result = ArrayListMultimap.create();

        List<List<String>> contents = readBoldFile();

        int processIdRowIndex = getProcessIdRowIndex(contents);
        int traceFileRowIndex = getTraceFileRowIndex(contents);

        for (int i = 1; i < contents.size(); i++) {
            List<String> row = contents.get(i);

            result.put(row.get(processIdRowIndex), parseTraceFileName(row.get(traceFileRowIndex)));
        }

        return result.asMap();
    }

    /**
     * Parses trace file name from bold file to actual trace file name.
     *
     * @param traceFileName Trace file name from bold file.
     * @return Actual trace file name.
     */
    private String parseTraceFileName(String traceFileName) {
        traceFileName = traceFileName.substring(traceFileName.lastIndexOf("/") + 1);
        return traceFileName.substring(0, traceFileName.indexOf("+")) + traceFileName.substring(traceFileName.indexOf("."));
    }

    /**
     * Returns the contents of the bold file.
     *
     * @return Contents.
     */
    private List<List<String>> readBoldFile() throws BoldListMapperException, IOException {
        List<List<String>> result = new ArrayList<List<String>>();
        File file = new File(boldFilePath);
        BufferedReader reader = new BufferedReader(new FileReader(file));

        if (!file.isFile()) {
            throw new BoldListMapperException("The path '" + boldFilePath + "' is not of a file.");
        }

        String line = reader.readLine();
        while (line != null && !line.isEmpty()) {
            result.add(Arrays.asList(line.split(BOLD_TSV_SEPARATOR)));

            line = reader.readLine();
        }

        validateBoldFileDimensions(result);

        return result;
    }

    /**
     * Validates the correctness of bold file content dimensions.
     *
     * @param contents Bold file contents.
     * @return True if the contents of the bold file contains two or more rows and the length of each and every row are
     *         equal to each other.
     *         False if not.
     */
    private boolean validateBoldFileDimensions(List<List<String>> contents) {
        if (contents.size() < 2) {
            return false;
        }

        for (int i = 0; i < contents.size() - 1; i++) {
            if (contents.get(i).size() != contents.get(i + 1).size()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the row index of a specified column.
     *
     * @param contents Bold file contents.
     * @return Row index of the process id column.
     * @throws BoldListMapperException If no or more than 1 of the column was found.
     */
    private int getRowIndex(List<List<String>> contents, String columnName) throws BoldListMapperException{
        List<String> header = contents.get(0);

        int index = header.indexOf(columnName);

        if (index == -1) {
            throw new BoldListMapperException("'" + columnName + "' column was not found");
        }

        if (index != header.size() && header.subList(index + 1, header.size()).indexOf(columnName) != -1) {
            throw new BoldListMapperException("More than 1 '" + columnName + "' column was found.");
        }

        return index;
    }

    /**
     * Gets the row index of the process id column.
     *
     * @param contents Bold file contents.
     * @return Row index of the process id column.
     * @throws BoldListMapperException If no or more than 1 process id column was found.
     */
    private int getProcessIdRowIndex(List<List<String>> contents) throws BoldListMapperException {
       return getRowIndex(contents, BOLD_TSV_PROCESS_ID_COLUMN_HEADER);
    }

    /**
     * Gets the row index of the trace file column.
     *
     * @param contents Bold file contents.
     * @return Row index of the trace file column.
     * @throws BoldListMapperException If no or more than 1 trace file column was found.
     */
    private int getTraceFileRowIndex(List<List<String>> contents) throws BoldListMapperException {
        return getRowIndex(contents, BOLD_TSV_TRACE_FILE_COLUMN_HEADER);
    }

    /**
     * Exception representing errors that occur in the BoldListMapper class.
     */
    private static class BoldListMapperException extends Exception {
        private String message;

        public BoldListMapperException(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
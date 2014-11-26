package com.biomatters.plugins.barcoding.validator.validation.input.map;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.google.common.collect.ArrayListMultimap;

import java.io.*;
import java.util.*;

/**
 * Maps barcodes to traces via trace list file from BOLD.
 *
 * @author Gen Li
 *         Created on 5/09/14 9:54 AM
 */
public class BOLDTraceListMapper extends BarcodesToTracesMapper {
    private static final String PROCESS_ID_COLUMN_NAME = "PROCESSID";
    private static final String TRACE_FILE_COLUMN_NAME = "TRACEFILE";

    private static final String BOLD_BARCODE_DESCRIPTION_SEPARATOR = "\\|";

    private String boldTraceListFilePath;

    public BOLDTraceListMapper(String boldTraceListFilePath) {
        this.boldTraceListFilePath = boldTraceListFilePath;
    }

    /**
     * Returns a map of the supplied barcodes to the supplied traces.
     *
     * @param barcodes The supplied barcodes.
     * @param traces The supplied traces.
     * @return A map of the supplied barcodes to the supplied traces.
     * @throws DocumentOperationException If an error occurs during the mapping process.
     */
    @Override
    public Map<AnnotatedPluginDocument, List<AnnotatedPluginDocument>> map(List<AnnotatedPluginDocument> barcodes,
                                                                           List<AnnotatedPluginDocument> traces)
            throws DocumentOperationException {
        try {
            /* Get a map of process ids to names of trace files via the trace list file associated with the instance. */
            Map<String, Collection<String>> processIdToTraceFileName = getProcessIdToTraceFileNameMap();

            /* Map and return. */
            return map(barcodes, traces, processIdToTraceFileName);
        } catch (BoldTraceListMapperException e) {
            throw new DocumentOperationException("Could not map barcodes to traces: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new DocumentOperationException("Could not map barcodes to traces: " + e.getMessage(), e);
        }
    }

    /**
     * Returns a map of the supplied barcodes to the supplied traces using the supplied map of process ids to the names
     * of trace files.
     *
     * @param barcodes The supplied barcodes.
     * @param traces The supplied traces.
     * @param processIdToTraceFileName The supplied map of process ids to the names of trace files.
     * @return A map of the supplied barcodes to the supplied traces.
     * @throws DocumentOperationException If one or more of the supplied traces is not associated with a supplied
     *                                    barcode.
     */
    private Map<AnnotatedPluginDocument, List<AnnotatedPluginDocument>> map(List<AnnotatedPluginDocument> barcodes,
                                                                           List<AnnotatedPluginDocument> traces,
                                                                           Map<String, Collection<String>> processIdToTraceFileName)
            throws DocumentOperationException {
        Map<AnnotatedPluginDocument, List<AnnotatedPluginDocument>> result =
                new HashMap<AnnotatedPluginDocument, List<AnnotatedPluginDocument>>();

        Map<String, AnnotatedPluginDocument> processIdToBarcode = getProcessIdToBarcode(barcodes);

        /* Map. */
        for (AnnotatedPluginDocument barcode : barcodes) {
            result.put(barcode, new ArrayList<AnnotatedPluginDocument>());
        }

        Map<AnnotatedPluginDocument, String> tracesWithoutBarcode = new LinkedHashMap<AnnotatedPluginDocument, String>();
        for (AnnotatedPluginDocument trace : traces) {
            String traceName = trace.getName();
            String processId = null;

            for (Map.Entry<String, Collection<String>> entry : processIdToTraceFileName.entrySet()) {
                if (entry.getValue().contains(traceName)) {
                    processId = entry.getKey();
                }
            }

            if (processId == null) {
                throw new DocumentOperationException("The trace " + traceName + " was not found in the TRACE_FILE_INFO.txt mapping file.");
            }

            AnnotatedPluginDocument barcode = processIdToBarcode.get(processId);

            if (processIdToBarcode.get(processId) == null) {
                tracesWithoutBarcode.put(trace, processId);
                continue;
            }

            result.get(barcode).add(trace);
        }
        if(!tracesWithoutBarcode.isEmpty()) {
            String message = "No barcode sequences were found for <strong>" + tracesWithoutBarcode.size() + "</strong> traces:\n\n";
            int count = 0;
            StringBuilder list = new StringBuilder();
            for (Map.Entry<AnnotatedPluginDocument, String> entry : tracesWithoutBarcode.entrySet()) {
                count++;
                if(count <= 100) {
                    list.append(entry.getKey().getName()).append(" (BOLD process ID: ").append(entry.getValue()).append(")\n");
                }
            }
            message += list.toString();
            if(count > 100) {
                message += "Too many to list...";
            }

            throw new DocumentOperationException(message);
        }

        return result;
    }

    /**
     * Returns a map of the process ids of the supplied barcodes to the supplied barcodes.
     *
     * @param barcodes The supplied barcodes.
     * @return A map of process ids to barcodes.
     */
    private Map<String, AnnotatedPluginDocument> getProcessIdToBarcode(List<AnnotatedPluginDocument> barcodes) {
        Map<String, AnnotatedPluginDocument> result = new HashMap<String, AnnotatedPluginDocument>();

        for (AnnotatedPluginDocument barcode : barcodes) {
            result.put(barcode.getName().split(BOLD_BARCODE_DESCRIPTION_SEPARATOR)[0], barcode);
        }

        return result;
    }

    /**
     * Returns a map of process ids to trace file names via the trace list file associated with the instance.
     *
     * @return A map of process ids to trace file names via the trace list file associated with the instance.
     * @throws BoldTraceListMapperException
     * @throws IOException
     */
    private Map<String, Collection<String>> getProcessIdToTraceFileNameMap() throws BoldTraceListMapperException, IOException {
        ArrayListMultimap<String, String> result = ArrayListMultimap.create();

        List<List<String>> contents = getTraceListFileContent();

        int processIdRowIndex = getProcessIdIndex(contents);
        int traceFileRowIndex = getTraceFileIndex(contents);

        for (int i = 1; i < contents.size(); i++) {
            List<String> row = contents.get(i);

            result.put(row.get(processIdRowIndex), parseTraceFileName(row.get(traceFileRowIndex)));
        }

        return result.asMap();
    }

    /**
     * Parses the name of a trace file from a trace list file to the actual name of the trace file.  Traces from BOLD
     * can be named NNNN/traceName+dddddd.ab1 rather than just traceName
     *
     * @param traceFileName The name of a trace file from a trace list file.
     * @return The actual name of the trace file.
     */
    private String parseTraceFileName(String traceFileName) {
        int indexOfSeparator = traceFileName.lastIndexOf("/");
        if(indexOfSeparator > -1) {
            traceFileName = traceFileName.substring(indexOfSeparator + 1);
        }
        int indexOfPlus = traceFileName.indexOf("+");
        if(indexOfPlus > -1) {
            return traceFileName.substring(0, indexOfPlus) + traceFileName.substring(traceFileName.indexOf(".", indexOfPlus));
        } else {
            return traceFileName;
        }
    }

    /**
     * Returns the contents of the trace list file associated with the instance.
     *
     * @return The contents of the trace list file as a List of List of Strings where the sub Lists represent
     *         rows and the Strings represent cells.
     */
    private List<List<String>> getTraceListFileContent() throws BoldTraceListMapperException, IOException {
        List<List<String>> result = new ArrayList<List<String>>();
        File file = new File(boldTraceListFilePath);
        BufferedReader reader = new BufferedReader(new FileReader(file));

        if (!file.isFile()) {
            throw new BoldTraceListMapperException("The path '" + boldTraceListFilePath + "' is not of a file.");
        }

        String line = reader.readLine();
        while (line != null && !line.isEmpty()) {
            result.add(Arrays.asList(line.split("\t")));

            line = reader.readLine();
        }

        validateTraceList(result);

        return result;
    }

    /**
     * Validates the number of rows of a trace list being greater than or equal to 2 and the equality of the lengths
     * of each and every pair of rows.
     *
     * @param contents Trace list contents.
     * @throws BoldTraceListMapperException if
     */
    private void validateTraceList(List<List<String>> contents) throws BoldTraceListMapperException {
        if (!validateTraceListNumRows(contents)) {
            throw new BoldTraceListMapperException("The number of rows of the trace list is < 2.");
        }
        if (!validateTraceListRowsEqualLength(contents)) {
            throw new BoldTraceListMapperException("The lengths of the rows of the trace list are not all equal.");
        }
    }

    /**
     * Validates the number of rows of a trace list being greater than or equal to 2.
     *
     * @param contents Trace list contents.
     * @return True if the number of rows of the trace list is greater than or equal to 2, and false if not.
     */
    private boolean validateTraceListNumRows(List<List<String>> contents) {
        if (contents.size() < 2) {
            return false;
        }
        return true;
    }

    /**
     * Validates the equality of the lengths of each and every pair of rows of a trace list.
     *
     * @param contents Trace list contents.
     * @return True if the the lengths of each and every row of the trace list are equal, and false if not.
     */
    private boolean validateTraceListRowsEqualLength(List<List<String>> contents) {
        for (int i = 0; i < contents.size() - 1; i++) {
            if (contents.get(i).size() != contents.get(i + 1).size()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the index of a specified column of a trace list file.
     *
     * @param contents Trace list contents.
     * @return Index of the specified column.
     * @throws BoldTraceListMapperException If no or more than 1 of the specified column was found.
     */
    private int getRowIndex(List<List<String>> contents, String columnName) throws BoldTraceListMapperException {
        List<String> header = contents.get(0);

        int index = header.indexOf(columnName);

        if (index == -1) {
            throw new BoldTraceListMapperException("'" + columnName + "' column was not found");
        }

        if (index != header.size() && header.subList(index + 1, header.size()).indexOf(columnName) != -1) {
            throw new BoldTraceListMapperException("More than 1 '" + columnName + "' column was found.");
        }

        return index;
    }

    /**
     * Returns the index of the process id column of a trace list file.
     *
     * @param contents Trace list contents.
     * @return Index of the process id column.
     * @throws BoldTraceListMapperException If no or more than 1 process id column was found.
     */
    private int getProcessIdIndex(List<List<String>> contents) throws BoldTraceListMapperException {
        return getRowIndex(contents, PROCESS_ID_COLUMN_NAME);
    }

    /**
     * Returns the index of the trace file column of a trace list file.
     *
     * @param contents Trace list contents.
     * @return Index of the trace file column.
     * @throws BoldTraceListMapperException If no or more than 1 trace file column was found.
     */
    private int getTraceFileIndex(List<List<String>> contents) throws BoldTraceListMapperException {
        return getRowIndex(contents, TRACE_FILE_COLUMN_NAME);
    }

    /**
     * Represents errors that occur internally to the class.
     */
    private static class BoldTraceListMapperException extends Exception {
        private String message;

        public BoldTraceListMapperException(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
package com.biomatters.plugins.barcoding.validator.validation;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.plugins.barcoding.validator.validation.assembly.CAP3Options;
import com.biomatters.plugins.barcoding.validator.validation.assembly.CAP3Runner;
import com.biomatters.plugins.barcoding.validator.validation.consensus.ConsensusUtilities;
import com.biomatters.plugins.barcoding.validator.validation.results.BarcodeValidationResult;
import com.biomatters.plugins.barcoding.validator.validation.trimming.PrimerTrimmingOptions;
import com.biomatters.plugins.barcoding.validator.validation.trimming.SequenceTrimmer;
import com.biomatters.plugins.barcoding.validator.validation.trimming.TrimmingOptions;
import jebl.util.CompositeProgressListener;
import jebl.util.ProgressListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Matthew Cheung
 *         Created on 24/10/14 3:57 PM
 */
public class Pipeline {
    public static void runValidationPipeline(NucleotideSequenceDocument barcode,
                                             List<NucleotideGraphSequenceDocument> traces,
                                             TrimmingOptions trimmingOptions,
                                             CAP3Options CAP3Options,
                                             Map<String, ValidationOptions> traceValidationOptions,
                                             Map<String, ValidationOptions> barcodeValidationOptions,
                                             ValidationCallback callback,
                                             ProgressListener progressListener) throws DocumentOperationException {
        CompositeProgressListener stepsProgress = new CompositeProgressListener(progressListener, 4);

        stepsProgress.beginSubtask("Trimming Traces");
        List<NucleotideGraphSequenceDocument> trimmedTraces = trimTraces(traces, trimmingOptions, true, stepsProgress);

        stepsProgress.beginSubtask("Validating Traces...");
        CompositeProgressListener traceValidationProgress = new CompositeProgressListener(stepsProgress, 3);
        trimmedTraces = callback.addTrimmedTraces(trimmedTraces, traceValidationProgress);

        traceValidationProgress.beginSubtask();
        List<ValidationRun> traceValidationResults = runValidationTasks(traceValidationOptions, traceValidationProgress,
                TraceValidation.getTraceValidations(), createTraceValiationRunnerForTraces(trimmedTraces)
        );

        traceValidationProgress.beginSubtask();
        addValidationResultsToCallback(callback, traceValidationResults, traceValidationProgress);

        stepsProgress.beginSubtask("Assembling...");
        CompositeProgressListener assembleTracesProgress = new CompositeProgressListener(stepsProgress, 3);
        assembleTracesProgress.beginSubtask();
        List<SequenceAlignmentDocument> contigs = assembleTraces(
                trimmedTraces, CAP3Options, barcode.getName(), assembleTracesProgress);
        assembleTracesProgress.beginSubtask();
        if(!contigs.isEmpty()) {
            CompositeProgressListener progressForAddingAssembly = new CompositeProgressListener(assembleTracesProgress, contigs.size());
            for (SequenceAlignmentDocument contig : contigs) {
                progressForAddingAssembly.beginSubtask();
                callback.addAssembly(contig, progressForAddingAssembly);
            }
        }
        assembleTracesProgress.beginSubtask();
        List<NucleotideSequenceDocument> consensusSequences = new ArrayList<NucleotideSequenceDocument>();
        if(!contigs.isEmpty()) {
            CompositeProgressListener progressForEachContig = new CompositeProgressListener(assembleTracesProgress, contigs.size());

            for (SequenceAlignmentDocument contig : contigs) {
                progressForEachContig.beginSubtask();

                CompositeProgressListener progressForEachConsensusStep = new CompositeProgressListener(progressForEachContig, 2);

                progressForEachConsensusStep.beginSubtask();
                NucleotideGraphSequenceDocument consensus = ConsensusUtilities.getConsensus(contig);
                List<NucleotideGraphSequenceDocument> resultConsusList = trimTraces(
                        Collections.singletonList(consensus), trimmingOptions, true, progressForEachConsensusStep);
                assert resultConsusList.size() == 1;
                NucleotideGraphSequenceDocument retConsus = resultConsusList.get(0);

                progressForEachConsensusStep.beginSubtask();
                callback.addConsensus(retConsus, progressForEachConsensusStep);
                consensusSequences.add(retConsus);
            }
        }

        stepsProgress.beginSubtask("Validating Barcode Sequences...");
        CompositeProgressListener validateBarcodeProgress = new CompositeProgressListener(stepsProgress, 2);
        validateBarcodeProgress.beginSubtask();
        List<ValidationRun> barcodeValidationResults = runValidationTasks(barcodeValidationOptions, validateBarcodeProgress,
                BarcodeValidation.getBarcodeValidations(), createBarcodeValidationRunnerForInput(barcode, consensusSequences)
        );
        validateBarcodeProgress.beginSubtask();
        addValidationResultsToCallback(callback, barcodeValidationResults, validateBarcodeProgress);
    }

    private static ValidationRunner<TraceValidation> createTraceValiationRunnerForTraces(final List<NucleotideGraphSequenceDocument> trimmedTraces) {
        return new ValidationRunner<TraceValidation>() {
            @Override
            ValidationResult run(TraceValidation validation, ValidationOptions options) throws DocumentOperationException {
                return validation.validate(trimmedTraces, options);
            }
        };
    }

    private static ValidationRunner<BarcodeValidation> createBarcodeValidationRunnerForInput(final NucleotideSequenceDocument barcode,
                                                                                             final List<NucleotideSequenceDocument> consensus) {
        return new ValidationRunner<BarcodeValidation>() {
            @Override
            ValidationResult run(BarcodeValidation validation, ValidationOptions options) throws DocumentOperationException {
                if(consensus.isEmpty()) {
                    ValidationResult result = new ValidationResult(false, "Assembly failed.");
                    result.setEntry(new BarcodeValidationResult());
                    return result;
                } else if(consensus.size() > 1) {
                    ValidationResult result = new ValidationResult(false, "Assembly produced more than one contig.");
                    result.setEntry(new BarcodeValidationResult());
                    return result;
                }

                if (validation instanceof BarcodeCompareValidation) {
                    return ((BarcodeCompareValidation) validation).validate(barcode, consensus.get(0), options);
                } else if(validation instanceof SingleBarcodeValidaton) {
                    throw new DocumentOperationException("Single barcode validation not implemented yet");
                } else {
                    throw new DocumentOperationException("Invalid validation options.");
                }
            }
        };
    }

    private static void addValidationResultsToCallback(ValidationCallback callback,
                                                       List<ValidationRun> runs,
                                                       ProgressListener progressListener) throws DocumentOperationException {
        CompositeProgressListener addTraceValidationResultsProgress = new CompositeProgressListener(progressListener, runs.size());

        for (ValidationRun run : runs) {
            addTraceValidationResultsProgress.beginSubtask();
            callback.addValidationResult(run.options, run.result, addTraceValidationResultsProgress);
        }
    }

    /**
     * Runs a list of {@link com.biomatters.plugins.barcoding.validator.validation.Validation}s using the specified
     * {@link ValidationRunner}
     * and {@link ValidationOptions}
     *
     * @param options Map of {@link ValidationOptions#getIdentifier()} to
     * {@link ValidationOptions} of tasks to run.
     * @param progressListener to report progress to
     * @return a list of {@link ValidationRun}
     * describing each run of the specified {@link Validation}s.
     *
     * @throws com.biomatters.geneious.publicapi.plugin.DocumentOperationException if a problem occurs during validation
     */
    private static <T extends Validation> List<ValidationRun> runValidationTasks(Map<String, ValidationOptions> options,
                                                                                 ProgressListener progressListener,
                                                                                 List<T> validationTasks,
                                                                                 ValidationRunner<T> runner) throws DocumentOperationException {
        List<ValidationRun> result = new ArrayList<ValidationRun>();
        CompositeProgressListener validationProgress = new CompositeProgressListener(progressListener, validationTasks.size());

        for (T validation : validationTasks) {
            validationProgress.beginSubtask();
            ValidationOptions templateOptionsForValidation = validation.getOptions();
            ValidationOptions optionsToRunWith = options.get(templateOptionsForValidation.getIdentifier());
            if (optionsToRunWith == null) {
                throw new DocumentOperationException("Could not find validation module for identifier: '" +
                        templateOptionsForValidation.getIdentifier() + "'.");
            }
            result.add(new ValidationRun(optionsToRunWith, runner.run(validation, optionsToRunWith)));
        }

        return result;
    }

    private static List<SequenceAlignmentDocument> assembleTraces(List<NucleotideGraphSequenceDocument> traces,
                                                                  CAP3Options options,
                                                                  String contigName,
                                                                  ProgressListener progressListener) throws DocumentOperationException {
        return CAP3Runner.assemble(traces, options.getExecutablePath(), options.getMinOverlapLength(), options.getMinOverlapIdentity(), contigName, progressListener);
    }

    private static List<NucleotideGraphSequenceDocument> trimTraces(List<NucleotideGraphSequenceDocument> traces,
                                                                    TrimmingOptions options,
                                                                    boolean addAnnotation,
                                                                    ProgressListener progressListener) throws DocumentOperationException {
        List<NucleotideGraphSequenceDocument> trimmedTraces = new ArrayList<NucleotideGraphSequenceDocument>();
        CompositeProgressListener trimmingProgress = new CompositeProgressListener(progressListener, traces.size());

        PrimerTrimmingOptions primerTrimmingOptions = options.getPrimerTrimmingOptions();

        for (NucleotideGraphSequenceDocument trace : traces) {
            trimmingProgress.beginSubtask("Trimming " + trace.getName());

            trimmedTraces.add(SequenceTrimmer.trimSequenceByQualityAndPrimers(trace,
                                                                              options.getQualityTrimmingOptions().getErrorProbabilityLimit(),
                                                                              primerTrimmingOptions.getPrimers(),
                                                                              (float)primerTrimmingOptions.getGapOptionPenalty(),
                                                                              (float)primerTrimmingOptions.getGapExtensionPenalty(),
                                                                              primerTrimmingOptions.getScores(),
                                                                              primerTrimmingOptions.getMaximumMismatches(),
                                                                              primerTrimmingOptions.getMinimumMatchLength(),
                                                                              addAnnotation));
        }

        return trimmedTraces;
    }

    private static abstract class ValidationRunner<T extends Validation> {
        /**
         * Can be used to run a series of {@link Validation}s
         * on the same input data.
         * <br/><br/>
         * <strong>Note</strong>: Implementations are responsible for keeping track of their own input data and how
         * each {@link Validation} is run.
         *
         * @param validation The validation to run.
         * @param options The options to run the validation with.
         * @return a {@link ValidationResult}
         * @throws com.biomatters.geneious.publicapi.plugin.DocumentOperationException
         */
        abstract ValidationResult run(T validation, ValidationOptions options) throws DocumentOperationException;
    }

    private static class ValidationRun {
        private ValidationOptions options;
        private ValidationResult result;

        private ValidationRun(ValidationOptions options, ValidationResult result) {
            this.options = options;
            this.result = result;
        }
    }
}
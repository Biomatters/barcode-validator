#! /usr/bin/perl

use strict;
use warnings;

BEGIN 
{

    unshift @INC, '../modules/';
}

use File::Basename;
use Getopt::Std;

use Jls::Array;
use Jls::File;
use Jls::Fasta;
use Jls::BarcodeValidator;
use Jls::Set;

# Check command line and retrieve the user-specified arguments

my $opts = {};

my $prog = basename($0);
getopts('hci:o:s:', $opts); # The hidden option 'c' turns off compression.

if ($opts->{'h'}) { usage($prog); exit(0); } # help
elsif (! $opts->{'i'} || ! $opts->{'o'}) { 
   usage($prog); exit(0); # help
}

# Inputs and checks the Fasta data files.

my $fas = Jls::BarcodeValidator::lc_unambiguous_nucleotides( # Sequences are {a,c,g,t,-}.
          Jls::BarcodeValidator::string2fastas( # Dies on invalid Fasta format.
          Jls::File::file2string($opts->{'i'}))); 
my $all_sample_uids = Jls::BarcodeValidator::cfas2barcode_uids($fas); # Fasta file order of barcodes
           
# In the canonical barcode Fasta array, the defline is >barcode_uid 
#     and now each object's sequence:
#         is in lower case, 
#         lacks white space, 
#         and is on a single line.

# Checks the multiple alignment lengths and the barcode formats in the data file.

die '"' . $opts->{'i'} . '" must contain at least two multiple alignment sequences.'
    unless (@$fas > 1);

die '"' . $opts->{'i'} . '" must be a multiple sequence alignment file.'
            . "\n" . 'Including their padding with "-", the sequences must have equal length.'
    unless Jls::BarcodeValidator::seqs_are_same_length($fas);

die '"' . $opts->{'i'} . '" must have deflines with a single valid barcode identifier.'
    unless (Jls::BarcodeValidator::barcode_uids_are_valid($fas));

die '"' . $opts->{'i'} . '" must have deflines with a unique valid barcode identifier.'
    unless (Jls::BarcodeValidator::barcode_uids_are_distinct($fas));

# Alphabetizes and compresses the barcode deflines (concatenates deflines with the same sequence).

my $cfas = $opts->{'c'} ? $fas : Jls::BarcodeValidator::compress($fas); 
my $all_new_sample_uids; # array reference to barcode uids for the new samples
                     
if ($opts->{'s'}) { 

    my @ss = split("\n", Jls::File::file2string($opts->{'s'})); 
    $all_new_sample_uids = \@ss; 
    
    my $unique_new_sample_uids = Jls::Array::unique($all_new_sample_uids);
    die 'Some new samples have duplicated barcode UIDs.'
        unless (@$unique_new_sample_uids == @$all_new_sample_uids);
    
    my $intersection = Jls::Set::intersection($all_new_sample_uids, $all_sample_uids);
    die 'Some new samples are not in the multiple alignment Fasta barcode file.'
        unless (@$intersection == @$all_new_sample_uids);
}

my $new_sample_uids = Jls::BarcodeValidator::samples_with_nonunique_species($all_sample_uids, 
                                                                        $all_new_sample_uids);
my $sample_query_uid2pci_h = {};

foreach my $cfa (@$cfas) {

    my $uids = Jls::BarcodeValidator::cfa_def2barcode_uids($cfa->{'def'});
    my $sample_query_uids = Jls::Set::intersection($uids, $new_sample_uids);

    if (! @$sample_query_uids) { next; }
    
    my $nearest_neighbor_uids = Jls::BarcodeValidator::cfa_defs2barcode_uids(
                                Jls::BarcodeValidator::nearest_neighbors_in_key2distance_h(
                                Jls::BarcodeValidator::key2distance_h(
                                Jls::BarcodeValidator::cfa_def2distance_statistics_h($cfa, $cfas)),
                                @$uids == 1 ? $cfa->{'def'} : undef));

    foreach my $sample_query_uid (@$sample_query_uids) {
    
        my $fraction_of_same_species = 
            Jls::BarcodeValidator::fraction_of_same_species ($sample_query_uid, 
                                                             $nearest_neighbor_uids);
        $sample_query_uid2pci_h->{$sample_query_uid} = $fraction_of_same_species;
    }
}

my $pci_stats = Jls::BarcodeValidator::pci_stats_h($sample_query_uid2pci_h);

my @keys = keys %$sample_query_uid2pci_h;
my $sample_query_uids = Jls::Array::reorder(\@keys, $new_sample_uids);
    
my $o_s = $pci_stats->{'average_pci'} . "\t" . $pci_stats->{'st_dev_pci'};

foreach my $sample_uid (@$sample_query_uids) {

    $o_s .= "\n" . $sample_uid . "\t" . $sample_query_uid2pci_h->{$sample_uid};
}
    
open(OUT, '>' . $opts->{'o'}) or die $prog . ' could not open "' . $opts->{'o'} . '"' . "\n";
print OUT $o_s;
close(OUT);

exit (0);

#########################################################################################

# Usage display

sub usage 
# program_name_
{
    my $program_name_ = shift;
    my $version = '1.0';
    
    my $p = $program_name_ . ' ' . $version;
    print STDERR <<USAGE
    
$program_name_ (Version $version)

Usage: perl $program_name_ [options]

This program calculates the PCI, the \'probability of correct identification\'.

The program inputs:
    (1) a mandatory input filename for multiple alignment Fasta file for all samples, 
    (2) a mandatory output filename, and
    (3) an optional input filename, 
        its contents being barcode UIDs for \'new samples\', each on a separate line.

The barcode UIDs for the new samples must be distinct.
The barcode UIDs for new samples must be a subset 
    of the barcode UIDs for all samples in the multiple alignment Fasta file.
The default set of new samples is the set of all samples in the Fasta file.

The program checks the input for fatal errors in Fasta or barcode UID format, as below.
It then outputs a file with results from the PCI calculations, with lines as follows.
The first line has the PCI average over species, a tab, and a standard deviation (described below).
Each line thereafter has 
    the barcode UID for each new sample, a tab, and the sample PCI (described below).
The lines are output in the order of the optional set of barcode UIDs for new samples, 
    or in the order they appear within the multiple alignment Fasta file, 
    if the optional set is omitted.
    
The Fasta file must contain at least two records.
Each record is separated by at least one \"\\n\" (newline charater) from the record following it.
Each record has three parts, in order:
    (1) a single mandatory definition line, beginning with '>',
    (2) an optional set of comment lines beginning with ';', and 
    (3) a multiply aligned sequence.
All multiply aligned sequences must have the same length, after any white space is removed.
If the study uses several barcode loci, 
    the multiple alignment should contain concatenations of the corresponding sequences, 
    with sequences for missing loci filled with '-' (gap characters).

The Fasta definition line (defline) contains barcode sample UIDs corresponding to the sequence.
The defline starts with '>'.
Each '>' in the defline corresponds to a UID, 
    which is the first set of contiguous non-white space characters following the '>'.
Permitting multiple UIDs in a defline is a convenience to permit computational efficiency.
Internally in fact, the program groups UIDs corresponding to a single sequence, 
    so that you do not need to (although you may, if you wish).
White space and then arbitary characters (excluding '>') may follow each UID in the defline.
The UID consists of 3 parts, separated by '_' (underscore), e.g., Acacia_exuvialis_JLS2:
    (1) Genus of the sample
    (2) Species of the sample
    (3) The non-empty unique identifier of the sample (which may contain '_' characters)
The computer assesses species by exact case-sensitive match of both Genus and Species.
Thus, misspellings in Genus and Species create error (because they create novel species names).
Two samples may not share the same UID.
The program checks that each sample barcode UID is indeed unique.    

As its distance between pairs of sequences, the PCI calculation uses p-distance.
Sequences are converted internally to lower case, 
    so p-distance between pairs of sequences is the fraction of 
    multiple alignment columns containing two characters from {a,c,g,t}
    where the two characters are not the same.
The PCI is calculated only for each new sample 
    where the Fasta file contains another sample with the same species as the new sample.
The PCI for each new sample is the fraction of its nearest neighbors with the same species.
The species PCI for each species is the sample PCI, averaged over all new samples from the species.
The PCI average is the species PCI, averaged over all species represented in the new samples.
The standard deviation given for the PCI average is the square root of the expectation of
    the variance of PCI average, the variance of the PCI for each new sample being estimated as 
    p * (1 - p), where p is the estimated PCI of the new sample.

Program options are arguments with \'-\' followed by a letter.
An option requiring further input(s) appears with a colon.
The default for an option, if any, is indicated in parentheses.

-c Turns off defline compression (computationally inefficient, recommended only for debugging).
-i : the input multiple alignment barcode file
-o : the output file containing the PCI calculations
-s (all barcode UIDs in the input file) : the barcode UIDs for the new samples, separated by ':'
USAGE
}
#########################################################################################

1;
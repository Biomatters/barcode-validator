#!/usr/bin/perl

use strict;
use warnings;

package Jls::BarcodeValidator;

use Exporter;
our @ISA = qw/Exporter/;
our @EXPORT = 'string2fastas fas_def2barcodes fa_def2barcodes lc_unambiguous_nucleotides seqs_are_same_length barcode_uids_are_valid single_defline_barcode_uids_are_valid barcode_uids_are_distinct compress cfas2barcode_uids cfa_defs2barcode_uids cfa_def2barcode_uids samples_with_nonunique_species distance_statistics_h distance_statistics2p_distance barcode_uid2fasta_h get_species cfa_def2distance_statistics_h key2distance_h nearest_neighbors_in_key2distance_h fraction_of_same_species species2pci_h species_average_pci';

=pod

=cut

# Returns an array corresponding to split ("\n>") with canonical deflines and sequences.
# Dies on invalid Fasta format. Permits ';' comments.
# Each defline in the canonical Fasta object replaces by s/^>\s*(\S+)\s+(.*)/>$1/.

sub string2fastas 
# $s_ # string with barcode multiple alignment
{
    my $s_ = shift;

    use Jls::Barcode;
    use Jls::Fasta;
    
    my $fas = Jls::BarcodeValidator::fas_def2barcodes( # >barcode_uid repeated
              Jls::Fasta::canonical( # Sequences have no white space & are on a single line.
              Jls::Fasta::string2fastas($s_))); 

    return $fas;
}

# Abbreviates the deflines of an array of Fasta objects.

sub fas_def2barcodes
# \@fas_ # (possibly compressed) barcode Fasta objects
{
    my $fas_ = shift;

    foreach my $fa (@$fas_) 
    {
        $fa->{'def'} = fa_def2barcodes ($fa->{'def'});
    }
    
    return \@$fas_;
}

# Abbreviates the defline of a Fasta object.

sub fa_def2barcodes
# $s_ # valid barcode defline
{
    my $s_ = shift;

    my $s = '';
    
    while ($s_ =~ s/^>[^>\S]*([^>\s]+)[^>]*//) { $s .= '>' . $1; }
    
    return $s;
}

# Returns Fasta objects with lc sequences, done in place.

sub lc_unambiguous_nucleotides 
# \@fas_ # canonical compressed Barcode Fastas 
{
    my $fas_ = shift;

    foreach my $fa (@$fas_) {
        
        $fa->{'seq'} = CORE::lc ($fa->{'seq'}); 
        $fa->{'seq'} =~ s/[^acgt]/-/g;  
    }

    return $fas_;
}

# Checks for necessary condition of multiple sequence alignment.
# Returns 1 if the sequences are the same length.
# Returns 0 otherwise.

sub seqs_are_same_length 
# \@fas_ # canonical compressed Barcode Fastas 
{
    my $fas_ = shift;
    
    return Jls::Fasta::is_same_length ($fas_);
}
   
# Returns 1 if barcode UIDs have no white space and contain /^>([^_]+?)_([^_]+?)_(\S+)$/.
# Returns 0 otherwise.

sub barcode_uids_are_valid 
# \@fas_ # canonical compressed Barcode Fastas 
{
    my $fas_ = shift;
    
    foreach my $fa (@$fas_) {
        
        if (! single_defline_barcode_uids_are_valid($fa->{'def'})) { return 0; }
    }
    
    return 1;
}
   
# Returns 1 if barcode UIDs have no white space and contain /^>([^_]+?)_([^_]+?)_(\S+)$/.
# Returns 0 otherwise.

sub single_defline_barcode_uids_are_valid 
# $def_ # defline
{
    my $def_ = shift;
    
    my @barcode_uids = split('>', $def_);
    
    my $s = shift @barcode_uids;
    if ($s ne '') { return 0; } # Defline does not start with '>'.
    
    foreach my $barcode_uid (@barcode_uids) {
        
        if ($barcode_uid =~ /\s/) { return 0; } # Must not contain white space.
        if ($barcode_uid !~ /^([^_]+?)_([^_]+?)_(\S+)$/) { return 0; } # no double '_';   
    }    
    
    return 1;
}
   
# Returns 1 if barcode UIDs are unique.
# Returns 0 otherwise.

sub barcode_uids_are_distinct 
# \@fas_ # canonical compressed Barcode Fastas
{
    my $fas_ = shift;
    
    my $h = {};
    
    foreach my $fa (@$fas_) {
        
        my $barcode_uids = cfa_def2barcode_uids($fa->{'def'});
        
        foreach my $barcode_uid (@$barcode_uids) {
            
            if (defined $h->{$barcode_uid}) { return 0; } # duplicate uid
            $h->{$barcode_uid} = 1;   
        }
    }
    
    return 1;
}
   
# Returns Fasta objects sorted by defline then sequence, with Fasta deflines compressed.
# Within compressed deflines, original deflines are alphabetized.

sub compress 
# \@fas_ # canonical compressed Barcode Fastas 
{
    my $fas_ = shift;

    use Jls::Array;
    use Jls::Fasta;

    my $c_fastass = Jls::Array::unique( # Alphabetizes and Compresses barcode deflines.
                         $fas_, 
                         \&Jls::Fasta::cmp_seq_def, 
                         \&Jls::Fasta::new_concatenate_def, 
                         \&Jls::Fasta::eq_seq);
                         
    foreach my $fa (@$c_fastass) { # Alphabetizes barcode UIDs within compressed deflines.
        
        my $barcode_uids = cfa_def2barcode_uids($fa->{'def'});
        $fa->{'def'} = '>' . join('>', sort @$barcode_uids);
    }                         

    my @cfass = sort { Jls::Fasta::cmp_def_seq($a, $b) } @$c_fastass;
                             
    return \@cfass;
}

# Decompresses an array of compressed Fastas.
# Returns barcode_uids in array order within Fastas and then within deflines.

sub cfas2barcode_uids
# \@cfas_ 
{
    my $cfas_ = shift;
    
    my @barcode_uids = ();
    
    foreach my $cfa (@$cfas_) {
    
        my $uids = cfa_def2barcode_uids($cfa->{'def'});
        
        push(@barcode_uids, @$uids);
    }
    
    return \@barcode_uids;
}

# Decompresses an array of compressed Fastas deflines.
# Returns barcode_uids in array order within defline array and then within deflines.

sub cfa_defs2barcode_uids
# \@cfa_defs_ 
{
    my $cfa_defs_ = shift;
    
    my @barcode_uids = ();
    
    foreach my $cfa_def (@$cfa_defs_) {
    
        my $uids = cfa_def2barcode_uids($cfa_def);
        
        push(@barcode_uids, @$uids);
    }
    
    return \@barcode_uids;
}

# Decompresses Fasta defline into barcode_uid array reference.
# Returns barcode_uids in array order within defline array, if ! defined \@new_sample_uids_.

sub cfa_def2barcode_uids 
# $cfa_def_ # Fasta defline
# \@new_sample_uids_ # sample barcode UIDs
{
    my $cfa_def_ = shift;
    my $new_sample_uids_ = shift;
    
    use Jls::Set;
    
    my @barcode_uids = split('>', $cfa_def_);
    shift @barcode_uids; # Deletes leading empty string from initial '>'.

    my $uids = \@barcode_uids;
    if (defined $new_sample_uids_) { $uids = Jls::Set::intersection($uids, $new_sample_uids_); }
        
    return $uids;
}

# Returns in original order the subset sample UIDs with nonunique species.

sub samples_with_nonunique_species
# \@all_uids_ 
# \@subset_of_uids_ # (\@all_uids_)
{
    my $all_uids_ = shift;
    my $subset_of_uids_ = shift;
    
    if (! defined $subset_of_uids_) { $subset_of_uids_ = $all_uids_; }
    
    my $h = {};
    
    foreach my $uid (@$all_uids_) { $h->{get_species($uid)}++; }
    
    my @samples_with_nonunique_species = ();
    
    foreach my $uid (@$subset_of_uids_) {
    
        if ($h->{get_species($uid)} > 1) { push(@samples_with_nonunique_species, $uid); }
    }
    
    return \@samples_with_nonunique_species;
}

# Returns a hash with the unambiguous-pair count and unequal number 
#    for calculating p-distance between two Fasta objects.
# Dies if the two Fasta objects have sequences of unequal length.
# Bases p-distance on lc matches of {a,c,g,t}.

sub distance_statistics_h 
# $fa0_ # canonical Fasta 
# $fa1_ # canonical Fasta 
{
    my $fa0_ = shift;
    my $fa1_ = shift;
    
    die 'Jls::BarcodeValidator::distance_statistics_h : sequence lengths are not equal.' 
        unless length($fa0_->{'seq'}) == length($fa1_->{'seq'});

    my $NUCLEOTIDES = 'acgt';

    my $h = {
        
        'total' => 0,
        'unequal' => 0,
    };
        
    for (my $i = 0; $i != length($fa0_->{'seq'}); $i++) {
    
        my $c0 = substr($fa0_->{'seq'}, $i ,1);
        if ($NUCLEOTIDES !~ /\Q$c0\E/) { next; }
        my $c1 = substr($fa1_->{'seq'}, $i ,1);
        if ($NUCLEOTIDES !~ /\Q$c1\E/) { next; }
        
        $h->{'total'}++;
        if ($c0 ne $c1) { $h->{'unequal'}++; }
    }
    
    return $h;
}

# Returns the p-distance between two Fasta objects from the p_distance_hash above.

sub distance_statistics2p_distance 
# $h_ # hash from sub p_distance_hash
{
    my $h_ = shift;
    
    return $h_->{'total'} == 0 ? 1.0 : $h_->{'unequal'} / $h_->{'total'};
}

# Returns species.

sub get_species
# $s_ # barcode uid
{
    my $s_ = shift;
    
    $s_ =~ /([^_]+?_[^_]+?)_/;
    
    return $1;
}

# Returns a hash of compressed_defline2distance_statistics.

sub cfa_def2distance_statistics_h 
# $cfa_ # canonical compressed Barcode Fasta
# \@cfas_ # canonical compressed Barcode Fastas
{
    my $cfa_ = shift;
    my $cfas_ = shift;
    
    my $h = {};
    
    foreach my $cfa (@$cfas_) {
        
        $h->{$cfa->{'def'}} = distance_statistics_h($cfa_, $cfa);
    }
    
    return $h;
}

# Returns a hash of key2distance_h.

sub key2distance_h
# $key2distance_statistics_h_ # map barcode to distance_statistics
{
    my $key2distance_statistics_h_ = shift;
    
    my $h = {};
    
    while ((my $key, my $distance_statistics) 
                = each(%$key2distance_statistics_h_)) {
        
        $h->{$key} = 
            distance_statistics2p_distance($distance_statistics);
    }
    
    return $h;
}

# Returns nearest neighbors from key2distance_h.

sub nearest_neighbors_in_key2distance_h
# \%h_ # key2distance
# $exclude_ # (no key excluded) match for exclusion
{
    my $h_ = shift;
    my $exclude_ = shift;
    my $cmp_ = shift;
    
    my @keys = sort { $h_->{$a} <=> $h_->{$b} } keys %$h_; # distances in ascending order

    if (defined $exclude_) {
        
        for (my $i = 0; $i != @keys; $i++) {
            
             if ($keys[$i] eq $exclude_) {
             
                 splice (@keys, $i, 1);
                 last;
             }
        }
    }
    
    if (! @keys) { return []; }
    
    my $d = $h_->{$keys[0]}; # smallest distance

    my @nearest_neighbors = sort grep { $h_->{$_} == $d } @keys; # distance is smallest

    return \@nearest_neighbors;
}

# Calculates the fraction_of_same_species as $query_uid_ within \@barcode_uids_.

sub fraction_of_same_species
# $query_uid_ 
# \@barcode_uids_ 
{
    my $query_uid_ = shift;
    my $barcode_uids_ = shift;
    
    my $query_species_ = get_species($query_uid_);
    my $same_species = 0; # $query_species_ within \@barcode_uids_
    my $total = 0; # scalar @barcode_uids_, minus 1 if $query_uid_ is present. 
    
    foreach my $barcode_uid (@$barcode_uids_) {
    
        if ($query_uid_ eq $barcode_uid) { next; }
        
        $total++;
        if ($query_species_ eq get_species($barcode_uid)) { $same_species++ }
    } 
    
    return $same_species / $total;
}

# Calculates the statistics for the sample PCI.

sub pci_stats_h
# $barcode_uid2pci_h_ 
{
    my $barcode_uid2pci_h_ = shift;
    
    my $pci_stats_h = {};
    $pci_stats_h->{'species'} = {};
    my $s_h = $pci_stats_h->{'species'};
    
    while ((my $barcode_uid, my $pci) = each(%$barcode_uid2pci_h_)) {

        my $species = get_species($barcode_uid);
        
        if (! exists $s_h->{$species}) { 
        
            $pci_stats_h->{'number_of_species'}++;
            $s_h->{$species} = {}; 
        }
        
        $s_h->{$species}->{'pci'} += $pci;
        $s_h->{$species}->{'count'}++;
    }
    
    # wrap-up
    
    foreach my $h (values %$s_h) {

        $h->{'pci'} /= $h->{'count'}; # sample PCI within the species
    }

    my $number_of_species = $pci_stats_h->{'number_of_species'};
    
    while ((my $barcode_uid, my $pci) = each(%$barcode_uid2pci_h_)) {

        my $species = get_species($barcode_uid);
        my $count = $pci_stats_h->{'species'}->{$species}->{'count'};
        my $w = 1.0 / $number_of_species / $count;
                    
        $pci_stats_h->{'average_pci'} += $w * $pci;
        $pci_stats_h->{'total_weight'} += $w;
        $pci_stats_h->{'variance_pci'} += $w * $w * $pci * (1 - $pci);
    }

    $pci_stats_h->{'st_dev_pci'} = sqrt($pci_stats_h->{'variance_pci'});
    return $pci_stats_h;
}

1;

#!/usr/bin/perl

use strict;
use warnings;

package Jls::Barcode;

use Exporter;
our @ISA = qw/Exporter/;
our @EXPORT = 'abbreviate_defline_fastas abbreviate_array _is_genus_species_uid is_uncertain_species is_defline_good is_record decorate_deflines output_format_species string2statistics _is_pcr_failure _classify _array2arrays _abbreviate_defline';

=pod

=cut

$Jls::Barcode::_nucleotide = 'ACGTMRWSYKVHDBN\-'; 
$Jls::Barcode::_multi_nucleotide = 'ACGTMRWSYKVHDBN'; 
$Jls::Barcode::_nucleotide_regex = 'ACGTMRWSYKVHDBNacgtmrwsykvhdbn\-'; 

$Jls::Barcode::_is_not_multi = 0;

sub is_not_multi 
# $is_not_multi_ # ? Is > acceptable only at the beginning ?
{
    if (! @_) { return $Jls::Barcode::_is_not_multi; }
    
    return $Jls::Barcode::_is_not_multi = shift;
}

# Returns an array corresponding to split ("\n>").
# A final "\n" in the string $s_ is always acceptable.

# ? Is each $s_ a barcode fasta record ?

sub is_records
# $s_ # string in putative fasta file containing barcodes
# $is_not_multi_ # ? Is > only at the beginning and . acceptable ?
{ 
    use Jls::Fasta;
    
    my $s_ = shift;
    my $is_not_multi_ = shift;
        
    if (! defined ($is_not_multi_)) { $is_not_multi_ = 0; }
    
    is_not_multi ($is_not_multi_);

    my $is_record = \&Jls::Barcode::_is_record;

    return Jls::Fasta::is_records ($s_, $is_record);
}

# ? Is every element of \@is_records_ true ?

sub is_every_record
# \@is_records_ # string in putative fasta file containing barcodes
{ 
    use Jls::Fasta;
    
    my $is_records_ = shift;
        
    foreach my $record (@$is_records_) { if (! $record) { return 0; } }

    return 1;
}

# Converts a string in a loosely valid barcode FASTA file
#     into a hash containing 6 strings
# (1) ->{'genus'} : alphabetized genera, separated by "\n"
# (2) ->{'species'} : alphabetized species, separated by "\n"
# (3) ->{'pcr_failure'} : FASTA file of a single multi-defline of PCR failures
# (4) ->{'uncertain_species'} : alphabetized uncertain species uids, separated by "\n"
# (5) ->{'replicates'} : alphabetized replicates, separated by "\n"
# (6) ->{'valid'} : FASTA file of multi-defline sequences of PCR successes

sub string2strings
# $s_ # string in a valid fasta file (loose definition)
# $is_mfa_ # ? is multiple sequence alignment permitted ?
{
    my $s_ = shift;
    my $is_mfa_ = shift;
    
    if (! defined ($is_mfa_)) { $is_mfa_ = 0; }

    use Jls::Fasta;
    use Jls::MultiFasta;
    
    my $fas = Jls::Fasta::string2fastas ($s_); # array of Fasta objects
    my $fastas = Jls::Fasta::canonical ($fas);

    $fastas = _abbreviate_defline_fastas ($fastas); # Abbreviates the deflines.
     
    my $arrays = _array2arrays ($fastas, $is_mfa_); # Separates into 3 arrays.

    my $strings = {};
    
    $strings->{'uncertain_species'} = join ("\n", @{$arrays->{'uncertain_species'}});

    $strings->{'pcr_failure'} = 
            Jls::Fasta::fastas2string (
            Jls::MultiFasta::fastas2multi_fastas ($arrays->{'pcr_failure'}));
    $strings->{'pcr_failure'} = Jls::Validate::rtrim ($strings->{'pcr_failure'});
    
    my $multi = Jls::MultiFasta::fastas2multi_fastas ($arrays->{'valid'});
    $strings->{'valid'} = Jls::Fasta::fastas2string ($multi);
            
    $strings->{'genus'} = $arrays->{'genus'};
    $strings->{'species'} = $arrays->{'species'};
    $strings->{'replicates'} = $arrays->{'replicates'};
    $strings->{'sample'} = $arrays->{'sample'};

    return $strings;
}

sub decorate_deflines
# $s_ # string for a FASTA file
# $is_records_ # array corresponding to the records 1 = good, 0 = bad
{
    my $s_ = shift;
    my $is_records_ = shift;

    my $text = '';
    
    use Jls::Fasta;
    
    my $fastas = Jls::Fasta::string2fastas ($s_); # array of FASTA objects
    $fastas = Jls::Barcode::_abbreviate_defline_fastas ($fastas);
     
    # Abbreviates the deflines.
    
    if (scalar (@$is_records_) != scalar (@$fastas)) 
    {
        die 'Jls::Barcode::decorate_deflines : ' .
            'scalar (@$is_records) != scalar (@$fastas)';
    }
    
    my $init = 1;
    
    for (my $i = 0; $i != scalar (@$is_records_); $i++)
    {
        if ($is_records_->[$i]) { next; }
        
        if (! $init) { $text .= '<br />'; }
        $text .= $fastas->[$i]->{'def'};
        $init = 0;
    }
    
    return $text;
}

$Jls::Barcode::_CLASSIFY_VALID = 0;
$Jls::Barcode::_CLASSIFY_PCR_FAILURE = 1;
$Jls::Barcode::_CLASSIFY_UNCERTAIN_SPECIES = 2;
$Jls::Barcode::_CLASSIFY_NUMBER = 3;

# Classifies canonical Fasta barcode array into arrays:
#    uncertain species, pcr failure, or other.
# Standardizes the pcr failures with a blank sequence.

sub _array2arrays 
# $array_ # canonical Fasta barcode array
# $is_mfa_ # ? is multiple sequence alignment permitted ?
{
    my $array_ = shift;
    my $is_mfa_ = shift;
    
    if (! defined ($is_mfa_)) { $is_mfa_ = 0; }

    my $arrays = {};
    
    $arrays->{'uncertain_species'} = [];
    $arrays->{'pcr_failure'} = [];
    $arrays->{'valid'} = [];

    my $compute = [];
    
    foreach my $fasta (@$array_) 
    { 
        if (_classify ($fasta, $is_mfa_) == $Jls::Barcode::_CLASSIFY_UNCERTAIN_SPECIES)
        {
            push (@{$arrays->{'uncertain_species'}}, 
                    substr (_abbreviate_defline ($fasta->{'def'}), 1));
        } 
        elsif (_classify ($fasta, $is_mfa_) == $Jls::Barcode::_CLASSIFY_PCR_FAILURE)
        {
            $fasta->{'seq'} = '';
            push (@{$arrays->{'pcr_failure'}}, $fasta);
            push (@$compute, substr ($fasta->{'def'}, 1));
        } 
        elsif (_classify ($fasta, $is_mfa_) == $Jls::Barcode::_CLASSIFY_VALID)
        {
            $fasta->{'seq'} = uc ($fasta->{'seq'});
            push (@{$arrays->{'valid'}}, $fasta);
            push (@$compute, substr ($fasta->{'def'}, 1));
        }
        else { die 'Jls::Barcode::_classify_array : impossible alternative'; } 
    }
    
    my $list = _defline2genus_species_replicates ($compute);

    $arrays->{'genus'} = $list->{'genus'};
    $arrays->{'species'} = $list->{'species'};
    $arrays->{'replicates'} = $list->{'replicates'};
        
    $arrays->{'sample'} = join ("\n", sort { $a cmp $b } @$compute);

    return $arrays;
}

# Formats a Genus_species separated by "\n" for output.

sub string2statistics
# $s_ # string in a valid fasta file (loose definition)
# $is_mfa_ # ? is multiple sequence alignment permitted ?
{
    my $s_ = shift;
    my $is_mfa_ = shift;
    
    if (! defined ($is_mfa_)) { $is_mfa_ = 0; }
    
    my $h = Jls::Barcode::string2strings ($s_, $is_mfa_);

    my @a = split ("\n", $h->{'genus'});
    
    $h->{'genus_stats'} = [];
    
    foreach my $genus (@a)
    {
        my $stat = {};
        $stat->{'genus'} = $genus;

        my $count = 0;
        $count++ while ($h->{'species'} =~ m/$genus/gs); # species in genus
        $stat->{'genus_species_count'} = $count;

        $count = 0;
        $count++ while ($h->{'sample'} =~ m/$genus/gs); # species in genus
        $stat->{'genus_sample_count'} = $count;
        
        push (@{$h->{'genus_stats'}}, $stat);
    }

    @a = split ("\n", $h->{'species'});
    
    $h->{'species_stats'} = [];
    
    foreach my $species (@a)
    {
        my $stat = {};
        $stat->{'species'} = $species;

        my $count = 0;
        $count++ while ($h->{'sample'} =~ m/$species/gs); # species in genus
        $stat->{'species_sample_count'} = $count;
        
        push (@{$h->{'species_stats'}}, $stat);
    }

    return $h;
}

# Formats a Genus_species separated by "\n" for output.

sub output_format_species
# $species_ # Genus_species separated by "\n"
{
    my $species_ = shift;
    
    my @v = split ("\n", $species_);
    my @a = ();
    
    foreach my $s (@v) { push (@a, _output_format_one_species ($s)); }
    
    my @sorted = sort { $a cmp $b } @a;
    my $s = join ("\n", @sorted);
    
    return $s;
}

# Formats a single Genus_species for output.

sub _output_format_one_species
# $species_ # Genus_species
{
    my $species_ = shift;
    
    my @v = split ('_', $species_);
    my $s = $v [1] . ' [' . $v [0] . ']';
    
    return $s;
}

# Abbreviates the deflines of an array of Fasta objects.

sub _defline2genus_species_replicates
# \@compute_ # valid barcode deflines without leading '>'
{
    my $compute_ = shift;

    my @genus = ();
    my @species = ();
    my @replicates = ();
    
    foreach my $def (@$compute_) 
    {
        my @v = split ('_', $def);
        
        push (@genus, $v [0]);
        push (@species, $v [0] . '_' . $v [1]);
        push (@replicates, $def);
    }
    
    @genus = sort { $a cmp $b } @genus;
    @species = sort { $a cmp $b } @species;
    @replicates = sort { $a cmp $b } @replicates;
    
    my @genus0 = ();
    my @species0 = ();
    my @replicates0 = ();
        
    for (my $i = 0; $i != scalar (@genus); $i++)
    {
        if ($i == 0 || $genus [$i] ne $genus [$i - 1]) 
        { 
            push (@genus0, $genus [$i]);
        } 

        if ($i == 0 || $species [$i] ne $species [$i - 1]) 
        { 
            push (@species0, $species [$i]);
        } 

        # Permits one copy.
        
        if ($i != 0 && $replicates [$i] eq $replicates [$i - 1] &&
           ($i == 1 || $replicates [$i - 1] ne $replicates [$i - 2])) 
        { 
            push (@replicates0, $replicates [$i]);
        } 
    } 
    
    my $list = {};
    
    $list->{'genus'} = join ("\n", @genus0);
    $list->{'species'} = join ("\n", @species0);
    $list->{'replicates'} = join ("\n", @replicates0);

    return $list;
}

# Abbreviates the deflines of an array of Fasta objects.

sub abbreviate_defline_fastas
# \@array_ # valid array containing barcode Fasta objects
{
    my $array_ = shift;
    
    _abbreviate_defline_fastas($array_);
    
    return $array_;
}

# Abbreviates the deflines of an array of Fasta objects.

sub _abbreviate_defline_fastas
# \@array_ # valid array containing barcode Fasta objects
{
    my $array_ = shift;

    foreach my $fasta (@$array_) 
    {
        $fasta->{'def'} = _abbreviate_defline ($fasta->{'def'});
    }
    
    return \@$array_;
}

# Returns ($genus, $species, $uid)

sub _genus_species_uid
# $s_ # string without whitespace
{
    my $s_ = shift;
    
    $s_ =~ /(.*?)_(.*?)_(.*)/;
    
    my @a = ($1, $2, $3);
    
    return \@a;
}

sub _is_genus_species_uid
# $s_ # line
# $is_not_multi_ # ? Is this for a single sample ?
{
    use Jls::Validate;
    
    my $s_ = shift;
    my $is_not_multi_ = shift;
    
    if (! defined ($is_not_multi_)) { $is_not_multi_ = 0; }
    
    if (! $s_) { return 0; }    
    if ($s_ =~ /\s/s) { return 0; }
    
    my $v = _genus_species_uid ($s_);
    my $genus = $v->[0];
    my $species = $v->[1];

    if (! $genus || ! $species) { return 0; }

    if (! Jls::Validate::is_uc (substr ($genus, 0, 1))) { return 0; }

    if (substr ($genus, 1) !~ /^[a-z\.]+$/) { return 0; } 
    if (substr ($species, 0, 1) !~ /[a-z]/) { return 0; }
    if (length ($species) != 1 && 
        substr ($species, 1) !~ /^[\d\-_a-z\.]+$/) { return 0; }
    
    return 1;
}

# ? Is $s_ a fasta barcode defline ?

sub is_defline_good 
# $s_ # line
# $is_not_multi_ # ? Is > acceptable only at the beginning ?
{
    use Jls::Fasta;
    use Jls::Validate;
    
    my $s_ = shift;
    my $is_not_multi_ = shift;

    if (! defined ($is_not_multi_)) { $is_not_multi_ = 0; }
    
    if (! Jls::Fasta::is_defline_good ($s_, $is_not_multi_)) { return 0; }
    
    if ($is_not_multi_)
    {
        $s_ = Jls::Validate::ltrim (substr ($s_, 1)); # trim leading whitespace
        if ($s_ =~ />/) { return 0; } # multi-defline
        $s_ =~ s/\s(.*)$//; # trim after 1-st space

        if (! _is_genus_species_uid ($s_, $is_not_multi_)) { return 0; }    
    }
    else
    {
        if (substr ($s_, 0, 1) ne '>') { return 0; }
        
        my @barcodes = split ('>', substr ($s_, 1));
        
        if (! @barcodes) { return 0; }
        
        foreach my $barcode (@barcodes)
        {            
            if ($barcode =~ /\s/) { return 0; }
            
            if (! _is_genus_species_uid ($barcode, $is_not_multi_)) 
            { 
                return 0; 
            } 
        }
    }

    return 1;
}

# ? Is $s_ a FASTA barcode record ?

sub _is_record 
# $s_ # string
{ 
    use Jls::Fasta;
    
    my $s_ = shift;
    my $is_not_multi_ = shift;
    
    if (! defined ($is_not_multi_)) { $is_not_multi_ = is_not_multi (); }

    my $alphabet = $is_not_multi_ ? $Jls::Barcode::_nucleotide
                                  : $Jls::Barcode::_multi_nucleotide;
                                  
    if (! Jls::Fasta::is_record ($s_, $is_not_multi_, 0, ! $is_not_multi_, 
                                 $alphabet, 
                                 ! $is_not_multi_, ! $is_not_multi_, 1))
    { return 0; }

    if ($s_ =~ /(.*?)\n/) { $s_ = $1; }
    if (! is_defline_good ($s_, $is_not_multi_)) { return 0; }

    return 1;
}

# ? Is $s_ a sequence with PCR failure ?

sub _is_pcr_failure 
# $s_ # valid barcode sequence
# $is_mfa_ # ? is multiple sequence alignment permitted ?
{
    use Jls::Validate;
    
    my $s_ = shift;
    my $is_mfa_ = shift;
    
    if (! defined ($is_mfa_)) { $is_mfa_ = 0; }

    if (! $is_mfa_ && $s_ =~ /-/) { return 1; }
    if (Jls::Validate::is_chars_in_bag ($s_, 'nN', 1)) { return 1; }
    
    return 0;
}

# ? Is $s_ a canonical barcode defline with an uncertain species ?

sub is_uncertain_species
# $s_ # valid barcode defline
{
    my $s_ = shift;
    
    my $v = _genus_species_uid ($s_);
    my $genus = $v->[0];
    my $species = $v->[1];
    
    if ($genus =~ /\./) { return 1; }

    if ($species eq 'sp') { return 1; }
    if ($species eq 'aff') { return 1; }
    if ($species eq 'cf') { return 1; }
    if ($species =~ /\./) { return 1; }
    
    return 0;
}

# Classifies canonical Fasta barcode object into 
#    uncertain species, pcr failure, or other.

sub _classify 
# $fasta_ # valid Fasta barcode object
# $is_mfa_ # ? is multiple sequence alignment permitted ?
{
    my $fasta_ = shift;
    my $is_mfa_ = shift;
    
    if (! defined ($is_mfa_)) { $is_mfa_ = 0; }

    if (is_uncertain_species ($fasta_->{'def'})) 
    { 
        return $Jls::Barcode::_CLASSIFY_UNCERTAIN_SPECIES; 
    }
    elsif (_is_pcr_failure ($fasta_->{'seq'}, $is_mfa_)) 
    { 
        return $Jls::Barcode::_CLASSIFY_PCR_FAILURE; 
    }
    
    return $Jls::Barcode::_CLASSIFY_VALID;
}

# Abbreviates a valid barcode to the Genus_species_uid.

sub _abbreviate_defline
# $s_ # valid barcode defline
{
    my $s_ = shift;

    $s_ =~ s/>\s*?(\S+)\s*(.*)$/>$1/;
    
    return $s_;
}

1;

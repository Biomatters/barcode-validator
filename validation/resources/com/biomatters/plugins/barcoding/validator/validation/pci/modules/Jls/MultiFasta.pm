#!/usr/bin/perl

use strict;
use warnings;

package Jls::MultiFasta;

use Exporter;

our @ISA = qw/Exporter/;
our @EXPORT = 'new fastas2multi_fastas';

=pod

=cut

# Creates a multifasta object.
#
# A multifasta object is a canonical fasta object
#     (i.e., no comments or whitespace in sequence), 
#     a hash reference, from fasta records (loose definition) 
#         with a common sequence.
# The default orders the deflines of the common sequence alphabetically.
#
# The hash reference from a fasta record (loose definition)
# {'def'}
# {'seq'} 

sub new
# $class_ 
# \@array_ // Contains canonical fasta records with a common sequence.
# $is_not_lexicographic_ // ? Is not reordered alphabetically ?
{
    use Jls::Validate;
    
    my $proto_ = shift;
    my $class_ = ref ($proto_) || $proto_;

    my $array_ = shift;
    my $is_not_lexicographic_ = shift;
        
    my $array;
    my @a = ();
    
    if ($is_not_lexicographic_) { $array = $array_; }
    else 
    { 
        @a = sort { $a->{'def'} cmp $b->{'def'} } @$array_;
        $array = \@a;
    }
    
    my $fasta;
    my $def = '';
    
    foreach $fasta (@$array)
    {
        $def .= $fasta->{'def'};
    }
    
    my $seq = $array->[0]->{'seq'};

    my $self = {};    
    
    $self->{'def'} = $def;
    $self->{'seq'} = $seq;
    
    return bless $self, $class_;
}

# Returns an array reference to canonical fasta objects with multi-deflines.

sub fastas2multi_fastas 
# \@array_ // array reference to canonical fasta records
# $is_not_lexicographic_ 
# // ? Are the deflines within a multi-defline not reordered lexicographically ?
{
    my $array_ = shift;
    my $is_not_lexicographic_ = shift;

    my $fastas = Jls::Fasta::canonical ($array_);

    my @array = sort { $a->{'seq'} cmp $b->{'seq'} } @$fastas;

    my $i = 0;
    my $j = 0;
    my @multi_array = ();
    
    for (my $i = 0; $i != scalar (@array); $i = $j)
    {
        my @a = ();
        
        for ($j = $i; $j != scalar (@array) && 
                         $array [$j]->{'seq'} eq $array [$i]->{'seq'}; $j++)
        {
            push (@a, $array [$j]);
        }
        
        my $multi = new Jls::MultiFasta (\@a, $is_not_lexicographic_);
        push (@multi_array, $multi);
    }

    if (! $is_not_lexicographic_) 
    {
        @multi_array = sort { $a->{'def'} cmp $b->{'def'} } @multi_array;
    }

    return \@multi_array;
}

1;

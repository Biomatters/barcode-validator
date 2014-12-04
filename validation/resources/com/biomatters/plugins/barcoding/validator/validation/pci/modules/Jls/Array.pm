#!/usr/bin/perl

use strict;
use warnings;

package Jls::Array;

use Exporter;
our @ISA = 'Exporter';
our @EXPORT = 'reorder cmp unique are_equal vector matrix';

=pod

=cut

# Returns an array reference to \@subset_ reordered by the order in \@order_.
# Equal elements within subset are placed in the first position within \@order.

sub reorder
# \@subset_ 
# \@order_
{
    my $subset_ = shift;
    my $order_ = shift;
    
    my $h = {};
    
    foreach my $element (@$subset_) { $h->{$element}++; }
    
    my @subset_in_order = ();
    
    foreach my $element (@$order_) { 
        
        if (! exists $h->{$element}) { next; }
        for (my $i = 0; $i != $h->{$element}; $i++) { push(@subset_in_order, $element); }
    }
    
    return \@subset_in_order;
}

# Returns -1/0/+1 by comparing arrays on length and then \&cmp_.

sub cmp
# \@a_
# \@b_
# \&cmp_ // (cmp)
{
    my $a_ = shift;
    my $b_ = shift;
    my $cmp_ = shift;
    
    if (! defined $cmp_) { $cmp_ = sub { return shift cmp shift; }; }
    
    my $cmp = @$a_ <=> @$b_;
    if ($cmp) { return $cmp; }
    
    for (my $j = 0; $j != @$a_; $j++)
    {
        $cmp = &$cmp_ ($a_->[$j], $b_->[$j]);
        if ($cmp) { return $cmp; }
    }
    
    return 0;
}

# Sorts the array @a_ with &$cmp_.
# Iterates through the sorted array.
# Replaces each $e0 satisfying &$eq_($e0, $e1) with &$operate_($e0, $e1).

sub unique
# \@a_
# \&cmp_ // (cmp)
# \&operate_ // (do nothing)
# \&eq_ // (! &cmp_)
{
    my $a_ = shift;
    my $cmp_ = shift;
    my $operate_ = shift;
    my $eq_ = shift;
    
    if (! defined $cmp_) { $cmp_ = sub { return shift CORE::cmp shift; }; }
    if (! defined $operate_) { $operate_ = sub { return shift; }; }
    if (! defined $eq_) { $eq_ = sub { return ! &$cmp_(shift, shift); }; }
    
    my @as = sort { &$cmp_($a, $b) } @$a_;
    my @bs = ();
    
    my $j = 0;
    
    for (my $i = 0; $i != @$a_; $i = $j)
    {
        push (@bs, $as[$i]); # Pushes novel elements onto the output array.
         
        for ($j = $i + 1; 
             $j != @$a_ && &$eq_($as[$i], $as[$j]); 
             $j++) 
                 { $bs[$#bs] = &$operate_ ($bs[$#bs], $as[$j]); } #
    }
    
    return \@bs;
}

# Returns 1 if arrays are equal; 0, otherwise.

sub are_equal
# \@a_
# \@b_
{
    my $a_ = shift;
    my $b_ = shift;
    
    @$a_ == @$b_ or return 0;
    
    for (my $j = 0; $j != @$a_; $j++)
    {
        $a_->[$j] eq $b_->[$j] or return 0;
    }
    
    return 1;
}

# Returns an initialized m-vector reference.

sub vector
# m_
{
    my $m_ = shift;
    
    my @vector = ();

    for (my $j = 0; $j != $m_; $j++)
    {
        push (@vector, 0);
    }
    
    return \@vector;
}

# Returns an initialized m x n matrix reference.

sub matrix
# m_
# n_
{
    my $m_ = shift;
    my $n_ = shift;
    
    my @matrix = ();

    for (my $i = 0; $i != $m_; $i++)
    {
        my @a = ();

        for (my $j = 0; $j != $n_; $j++)
        {
            push (@a, 0);
        }
        
        push (@matrix, \@a);
    }
    
    return \@matrix;
}

1;

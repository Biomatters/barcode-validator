#!/usr/bin/perl

use strict;
use warnings;

package Jls::Set;

use Exporter;
our @ISA = qw/Exporter/;
our @EXPORT = 'intersection';

=pod

=cut

# Returns the (sorted) intersection of two sets.
# Elements are "equal" if cmp_ ($a, $b) == 0.
# When two elements are equal, $c = op_ ($a, $b) is pushed into the intersection.

sub intersection
# \@as_ // array
# \@bs_ // probability distribution
# $cmp_ // (_cmp) string comparison function
# $op_ // (_projection) operation function, to form intersection
{ 
	my $as_ = shift;
	my $bs_ = shift;
	my $cmp_ = shift;
	my $op_ = shift;

    if (! defined $cmp_) { $cmp_ = \&Jls::Set::_cmp; }
    if (! defined $op_) { $op_ = \&Jls::Set::_projection; }
    
	my @as = sort { &$cmp_ ($a, $b) } @$as_;
	my @bs = sort { &$cmp_ ($a, $b) } @$bs_;
	
    my @cs = ();
    
    my $ia = 0;
    my $ib = 0;

    while ($ia != @as && $ib != @bs) {
        
        my $a = $as [$ia];
        my $b = $bs [$ib];
        my $cmp = &$cmp_ ($a, $b);
        
        if ($cmp < 0) { $ia++; }
        elsif ($cmp > 0) { $ib++; }
        else {
        
            my $c = &$op_ ($a, $b);
            push (@cs, $c);
            $ia++;
        }
    }    

	
	return \@cs;
}

# String comparison.

sub _cmp 
# $a_ 
# $b_ 
{ 
	my $a_ = shift;
	my $b_ = shift;

    return $a_ cmp $b_;
}

# Returns the first argument.

sub _projection 
# $a_ 
# $b_ 
{ 
	my $a_ = shift;
	my $b_ = shift;

    return $a_;
}

1;
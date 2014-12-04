#!/usr/bin/perl

use strict;
use warnings;

package Jls::Check;

use Exporter;
our @ISA = qw/Exporter/;
our @EXPORT = 'check';

=pod

=cut

# Compares the subroutine result with the expected result.
# Special characters require double backslash, e.g., '\\t', '\\n', etc.

sub check
# module_ # name of the module
# $subroutine_ # name of the subroutine
# arguments_ # arguments for the subroutine, e.g., '"[arguments as written]"'
# expected_result_ # expected result
# output_ # ? print the result to $output_?
{ 
	my $module_ = shift;
	my $subroutine_ = shift;
	my $arguments_ = shift;
	my $expected_result_ = shift;
	my $output_ = shift;
    
    my $string = $module_ . '::' . $subroutine_ . '(' . $arguments_ . ')';

    my $result = eval ($string);

    if ($output_) 
    { 
        open FILE, ">>$output_";    
        print FILE ($result . "\n\n"); 
        close FILE; 
        
        return;
    }
    
    if ($result ne $expected_result_) 
    {
        my $warning = '';
        
        $warning .= "\n" . $string . ' = ' . $result . ' but should be ' . $expected_result_;
        print $warning;
        
        exit (1);
    }
}

# ? Are the strings equal ?

sub is_equal_string
# $s_ 
# $s0_ 
{
	my $s_ = shift;
	my $s0_ = shift;
    
    return $s_ eq $s0_;
}

# ? Are the numbers equal ?

sub is_equal_number
# $s_ 
# $s0_ 
{
	my $s_ = shift;
	my $s0_ = shift;
    
    return $s_ == $s0_;
}

# ? Are the arrays equal ?

sub is_equal_array
# \@array_ 
# \@array0_ 
# \&equality_ # \&Jls::Check::is_equal_numbers, i.e., ==
{ 
	my $array_ = shift;
	my $array0_ = shift;
	my $equality_ = shift;
    
    if (! $equality_) { $equality_ = \&Jls::Check::is_equal_number; }

    if (scalar (@$array_) != scalar (@$array0_)) { return 0; }
    
    for (my $i = 0; $i < scalar (@$array_) && $i < scalar (@$array0_); $i++)
    {
        if (! &$equality_ ($array_->[$i], $array0_->[$i])) { return 0; }
    }
    
    return 1;
}

1;
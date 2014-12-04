#!/usr/bin/perl

use strict;
use warnings;

package Jls::String;

use Exporter;
our @ISA = qw/Exporter/;
our @EXPORT = 'symbols_in_string bad_chars_not_in_bag count_symbols_in_string line_break';

=pod

=cut

# Returns a hash reference with (keys, values) = (characters, counts in $str_).
# Returns a null hash reference if $str_ eq ''.

sub count_symbols_in_string 
# $str_ # string
# $case_ # (0 = case-sensitive) : -1 = lower case, 1 = upper case
# $order_ # (1 = individual letters)
{
    my $str_ = shift;
    my $case_ = shift;
    my $order_ = shift;

    if (! defined ($order_)) { $order_ = 1; }
    if (! defined ($case_)) { $case_ = 0; }
    if ($str_ eq '') { return {}; }
    
    $case_ == -1 || $case_ == 0 || $case_ == 1 ||
        die 'Jls::String::symbols_in_string : $case_ = ' . "$case_";
        
    use Jls::Validate;
    
    Jls::Validate::is_integer ($order_) ||
        die 'Jls::String::symbols_in_string : $order_ = ' . "$order_";
    
    if ($case_ == -1) { $str_ = lc ($str_); }
    elsif ($case_ == 1) { $str_ = uc ($str_); }

    my @a = split ('', $str_);
    my $count = {}; 
    my $c = '';
    
    for (my $i = 0; $i != scalar (@a) - $order_ + 1; $i++) 
    {   
        $c = substr ($str_, $i, $order_);
        
        if (! defined ($count->{$c})) { $count->{$c} = 1; }
        else { $count->{$c}++; } 
    }
    
    return $count; # hash reference (characters, counts in $str_)
}

# Inserts line-breaks into a string.

sub line_break 
# $str_ # string without line-break
# $line_length_ # line-length
{
    if ($_ [0] eq '' || ! $_ [1]) { return $_ [0]; }
    
    my $str_ = shift;
    my $line_length_ = shift;
    
    use Jls::Validate;
    
    if (! Jls::Validate::is_integer ($line_length_) || $line_length_ < 0) 
    {
        die 'Jls::String::line_break : $line_length_ must be a non-negative integer.';
    }
    
    my $out = '';
    
    while ($line_length_ < length ($str_))
    {
        if ($out) { $out .= "\n"; }
        $out .= substr ($str_, 0, $line_length_);
        $str_ = substr ($str_, $line_length_);
    }

    if ($out) { $out .= "\n"; }
    $out .= $str_;
    
    return $out;
}

# Returns sorted string of symbols in $str_.

sub symbols_in_string 
# $str_ # string
# $case_ # (0 = case-sensitive) : -1 = lower case, 1 = upper case
{
    my $count = Jls::String::count_symbols_in_string (@_);

    my @a = keys %{$count}; 
    my @sort = sort { $a cmp $b } @a;
    
    return join ('', @sort); # sorted string of symbols in $str_
}

# Returns characters in $s_ not in the $bag_.
# Ignores newlines.

sub bad_chars_not_in_bag
# $s_ // string
# $bag_ // string bag of characters
{
    use Jls::Validate;
    
    my $s_ = shift;
    my $bag_ = shift;
    my $eok_ = shift;

    if (Jls::Validate::is_chars_in_bag ($s_, $bag_, 1)) { return ''; }
    
    my $s = Jls::String::symbols_in_string ($s_);
    my $bad = '';
    my $c = '';
    
    for (my $i = 0; $i != length ($s); $i++)
    {
        $c = substr ($s, $i, 1);
        if ($c eq "\n") { next; }
        elsif ($bag_ =~ /$c/) { next; }
        else { $bad .= $c; }
    }

    $bad ne '' or die '';
    
    return $bad;
}

1;
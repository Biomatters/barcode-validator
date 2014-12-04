#!/usr/bin/perl

use strict;
use warnings;

package Jls::Validate;

use Exporter;
our @ISA = qw/Exporter/;
our @EXPORT = 'is_whitespace is_letter is_digit is_letter_or_digit is_alphabetic is_alphanumeric is_integer strip_chars_in_bag strip_chars_not_in_bag strip_whitespace ltrim strip_initial_whitespace rtrim strip_terminal_whitespace trim strip_bounding_whitespace';

=pod

=cut

# ? Is $s_ empty or whitespace ?

sub is_whitespace 
# $s_ // string
{
    return ($_ [0] =~ /^\s*$/s) ? 1 : 0;
}

# ? Is $s_ a lowercase letter ?

sub is_lc 
# $s_ // string
{
    return ($_ [0] =~ /^[a-z]$/s) ? 1 : 0;
}

# ? Is $s_ an uppercase letter ?

sub is_uc 
# $s_ // string
{
    return ($_ [0] =~ /^[A-Z]$/s) ? 1 : 0;
}

# ? Is $s_ a letter ?

sub is_letter 
# $s_ // string
{
    return ($_ [0] =~ /^[a-zA-Z]$/s) ? 1 : 0;
}

sub is_digit 
# $s_ // string
{
    return ($_ [0] =~ /^[0-9]$/s) ? 1 : 0;
}

# ? Is $s_ a letter or digit ?

sub is_letter_or_digit 
# $s_ // string
{
    return ($_ [0] =~ /^[a-zA-Z0-9]$/s) ? 1 : 0;
}

# ? Is $s_ all lower case alphabetic ?

sub is_lc_alphabetic 
# $s_ // string
# $eok_ // ? Is empty OK ?
{
    if ($_ [1] && $_ [0] eq '') { return 1; }
    
    return ($_ [0] =~ /^[a-z]+$/s) ? 1 : 0;
}

# ? Is $s_ all upper case alphabetic ?

sub is_uc_alphabetic 
# $s_ // string
# $eok_ // ? Is empty OK ?
{
    if ($_ [1] && $_ [0] eq '') { return 1; }
    
    return ($_ [0] =~ /^[A-Z]+$/s) ? 1 : 0;
}

# ? Is $s_ alphabetic ?

sub is_alphabetic 
# $s_ // string
# $eok_ // ? Is empty OK ?
{
    if ($_ [1] && $_ [0] eq '') { return 1; }
    
    return ($_ [0] =~ /^[a-zA-Z]+$/s) ? 1 : 0;
}

# ? Is $s_ alphanumeric ?

sub is_alphanumeric
# $s_ // string
# $eok_ // ? Is empty OK ?
{
    if ($_ [1] && $_ [0] eq '') { return 1; }

    return ($_ [0] =~ /^[a-zA-Z0-9]+$/s) ? 1 : 0;
}

# ? Is $s_ an integer ?

sub is_integer
# $s_ // string
# $eok_ // ? Is empty OK ?
{
    my $s_ = shift;
    my $eok_ = shift;
    
    if ($eok_ && $s_ eq '') { return 1; }
    
    my $c = substr ($s_, 0, 1);
    
    if ($c =~ /[0-9]/) { }
    elsif ($c eq '+' && length ($s_) != 1) { }
    elsif ($c eq '-' && length ($s_) != 1) { }
    else { return 0; }
    
    if (substr ($s_, 1) =~ /[^0-9]/) { return 0; }

    return 1;
}

# ? Is $s_ entirely in the $bag_ ?
# Permits the presence of newlines.

sub is_chars_in_bag
# $s_ // string
# $bag_ // string bag of characters
# $eok_ // ? Is empty OK ?
{
    my $s_ = shift;
    my $bag_ = shift;
    my $eok_ = shift;
    
    if ($eok_ && $s_ eq '') { return 1; }
    if (! $bag_) { return 0; }

    my $regex = '^[' . $bag_ . ']+$';

    return ($s_ =~ /$regex/s) ? 1 : 0;
}

# Removes all characters in string bag from a string.

sub strip_chars_in_bag 
# $s_ // string
# $bag_ // string bag of characters
{
    my $s_ = shift;
    my $bag_ = shift;
    
    my $regex = '[' . $bag_ . ']';
    $s_ =~ s/$regex//gs;

    return $s_;
}

# Removes all characters in string bag from a string.

sub strip_chars_not_in_bag 
# $s_ // string
# $bag_ // string bag of characters
{
    my $s_ = shift;
    my $bag_ = shift;
    
    my $regex = '[^' . $bag_ . ']';
    $s_ =~ s/$regex//gs;

    return $s_;
}

# Removes all whitespace characters from a string.

sub strip_whitespace
# $s_ // string
{   
    my $s_ = $_ [0];
    $s_ =~ s/\s//gs;
    
    return $s_;
}

# Removes all initial whitespace characters from a strings.

sub ltrim
# $s_ // string
{   
    my $s_ = $_ [0];
    $s_ =~ s/^\s+//;
    
    return $s_;
}

# Removes all initial whitespace characters from a strings.

sub strip_initial_whitespace
# $s_ // string
{   
    return ltrim (shift);
}

# Removes all terminal whitespace characters from a string.

sub rtrim
# $s_ // string
{   
    my $s_ = $_ [0];
    $s_ =~ s/\s+$//s;
    
    return $s_;
}

# Removes all terminal whitespace characters from a string.

sub strip_terminal_whitespace
# $s_ // string
{   
    return rtrim (shift);
}

# Removes all bounding whitespace characters from a string.

sub trim
# $s_ // string
{   
    my $s_ = $_ [0];
    
    return strip_initial_whitespace (strip_terminal_whitespace ($s_));
}

# Removes all bounding whitespace characters from a string.

sub strip_bounding_whitespace
# $s_ // string
{
    return trim (shift);
}

1;

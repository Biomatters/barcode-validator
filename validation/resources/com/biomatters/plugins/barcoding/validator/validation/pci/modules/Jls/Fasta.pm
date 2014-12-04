#!/usr/bin/perl

use strict;
use warnings;

package Jls::Fasta;

use Exporter;
our @ISA = qw/Exporter/;
our @EXPORT = 'new copy fastas2string string2fastas cmp_seq_def cmp_def_seq eq_seq new_concatenate_def is_same_length bad_chars strip_chars canonical uc lc composition seq_lengths seq_lengths_detail is_defline_good is_record is_records match _is_comment _is_sequence';

=pod

=cut

# Returns a fasta record as a hash reference 
# {'def'} # Defline
# {'com'} # Comment lines
# {'seq'} # Sequence lines

sub new
# $class_
# $record_ # string containing the FASTA record (loose definition)
# $delete_comments_ # ? delete comments ?
{
    use Jls::Validate;
    
    my $proto_ = shift;
    my $class_ = ref ($proto_) || $proto_;
    my $record_ = shift;
    my $delete_comments_ = shift;

    if (! defined $delete_comments_) { $delete_comments_ = 0; }

    my $self = {}; 

    my $def;
    my $com;
    my $seq;
    
    my @lines = split ("\n", $record_);
    
    if (! @lines) { die 'Fasta::new : No record'; }
    
    $def = $lines [0];
    shift (@lines);
    if (substr ($def, 0, 1) ne '>') { die 'Fasta::new : Invalid defline'; }
    
    my $init;
    
    for ($init = 1; @lines &&
                   (_is_comment ($lines [0]) || 
                    Jls::Validate::is_whitespace ($lines [0])); shift (@lines)) 
    {
        if ($init) { $com = ''; }
        else { $com .= "\n"; }

        $com .= $lines [0]; 
        $init = 0;
    }

    for ($init = 1; @lines; shift (@lines)) 
    {
        if ($init) { $seq = ''; }
        else { $seq .= "\n"; }

        $seq .= $lines [0]; 
        $init = 0;
    }

    if ($record_ =~ m/(\n*)$/s) 
    { 
        if (defined ($seq)) { $seq .= $1; } 
        elsif (defined ($com)) { $com .= $1; } 
        else { $seq = ''; } # Record must be an empty canonical record.
    }

    if (defined ($def)) { $self->{'def'} = $def; }
    if (defined ($com) && ! $delete_comments_) { $self->{'com'} = $com; }
    if (defined ($seq)) { $self->{'seq'} = $seq; }
    
    return bless $self, $class_;
}

# Converts a valid string containing the FASTA records 
#     into an array of FASTA objects.
# Returns an empty array reference for an empty string.
# Otherwise, dies if the string does not start with '>'.

sub string2fastas
# $s_ # string containing the FASTA records (loose definition)
# $delete_comments_ # ? delete comments ?
{
    my $s_ = shift; 
    my $delete_comments_ = shift;

    if (! defined $delete_comments_) { $delete_comments_ = 0; }

    my @array = ();

    if ($s_ eq '') { return \@array; }

    $s_ = "\n" . $s_;
    
    my @records = split ("\n>", $s_);    
    
    while ($records[0] =~ /^\s*$/) { shift @records; }
    
    foreach my $record (@records) 
    {
        $record = '>' . $record;
        push (@array, new Jls::Fasta ($record, $delete_comments_));
    }
    
    return \@array;
}

# Converts an array of FASTA objects to a string.
# Undefined comments and sequences are not omitted completely, without newlines.

sub fastas2string 
# $fastas_ # array of FASTA objects
{
    my $fastas_ = shift;

    my $s = '';
    my $init = 1;
    
    foreach my $fasta (@$fastas_)
    {
        if (! $init) { $s .= "\n"; } 
        $s .= $fasta->{'def'};
        if (defined ($fasta->{'com'})) { $s .= "\n" . $fasta->{'com'}; }
        if (defined ($fasta->{'seq'})) { $s .= "\n" . $fasta->{'seq'}; }
        $init = 0;
    }
    
    return $s;
}

# ? Is every $fastas_->{'seq'} of the same length ?
# Does not strip whitespace, so call Jls::Fasta::canonical first!
# Checks length as a criterion for a multiple sequence alignment.
# Returns 1 if all lengths are equal, 0 otherwise.

sub is_same_length
# $fastas_ # array of FASTA objects
{
    my $fastas_ = shift;

    my $length = -1;
    
    foreach my $fasta (@$fastas_)
    {
        if ($length == -1) { $length = length ($fasta->{'seq'}); }
        elsif ($length != length ($fasta->{'seq'})) { return 0; }
    }    
    
    return 1;
}

# Checks sequences in Fastas for characters outside a good set.
# Returns an alphabetized concatenation of characters outside the good set.

sub bad_chars
# \@fastas_ # array of FASTA objects
# $good_char_s_ # bad characters
{
    my $fastas_ = shift;
    my $good_char_s_ = shift;

    use Jls::String;
    
    my $bad_char_h = {};

    foreach my $fasta (@$fastas_)
    {
        my $bad_char = Jls::String::bad_chars_not_in_bag 
                                ($fasta->{'seq'}, $good_char_s_);
        if ($bad_char) { 
            
            my @as = split('', $bad_char);
            foreach my $a (@as) { $bad_char_h->{$a}++; } 
        }
    }

    my $bad_char_s = join('', sort keys %$bad_char_h);

    return $bad_char_s;
}

# Compares (as strings) first sequences and then deflines.

sub cmp_seq_def
# $a_ # FASTA object
# $b_ # FASTA object
{
    my $a_ = shift; 
    my $b_ = shift; 

    my $cmp = $a_->{'seq'} cmp $b_->{'seq'};
    if ($cmp) { return $cmp; }

    $cmp = $a_->{'def'} cmp $b_->{'def'};
    if ($cmp) { return $cmp; }

    return 0;
}
 
# Compares (as strings) first sequences and then deflines.

sub cmp_def_seq
# $a_ # FASTA object
# $b_ # FASTA object
{
    my $a_ = shift; 
    my $b_ = shift; 

    my $cmp = $a_->{'def'} cmp $b_->{'def'};
    if ($cmp) { return $cmp; }

    $cmp = $a_->{'seq'} cmp $b_->{'seq'};
    if ($cmp) { return $cmp; }

    return 0;
}
 
# Returns boolean ? Equal (as strings) sequences ?

sub eq_seq
# $a_ # FASTA object
# $b_ # FASTA object
{
    my $a_ = shift; 
    my $b_ = shift; 
    
    my $cmp = $a_->{'seq'} cmp $b_->{'seq'};

    return ! $cmp;
}
 
# Returns a new Fasta object with the defline of the first argument 
#     concatenated with the defline of the second argument.
#     and the sequence of the first argument.

sub new_concatenate_def
# $a_ # FASTA object
# $b_ # FASTA object
{
    my $a_ = shift; 
    my $b_ = shift; 

    return new Jls::Fasta ($a_->{'def'} . $b_->{'def'} . "\n" . $a_->{'seq'});
}

# Deep copies an array of FASTA objects.

sub copy 
# $fastas_ # array of FASTA objects
{
    my $fastas_ = shift;
    
    my $str = Jls::Fasta::fastas2string ($fastas_);
    my $a = Jls::Fasta::string2fastas ($str);
    
    return $a;
}

# Converts an array of FASTA objects to a canonical set.
# No comments, and sequences stripped of white space.
# Empty sequences become empty strings (and newlines appear accordingly).

sub canonical 
# $fastas_ # array of FASTA objects
# $line_length_ # (no ws in FASTA sequences) line-lengths
{
    my $fastas_ = shift;
    my $line_length_ = shift;
    
    if (! defined $line_length_) { $line_length_ = 0; }
    
    my $a = Jls::Fasta::copy ($fastas_);
    
    foreach my $fasta (@$a)
    {
        delete $fasta->{'com'};
        if (defined ($fasta->{'seq'})) 
        {
            $fasta->{'seq'} = Jls::Validate::strip_whitespace ($fasta->{'seq'});
        }
        else { $fasta->{'seq'} = ''; }
        
        use Jls::String;
        
        if ($line_length_ != 0) { # Breaks lines if desired.
            
            $fasta->{'seq'} = Jls::String::line_break ($fasta->{'seq'}, $line_length_); 
        }
    }
    
    return $a;
}

# Strips sequences of specific characters (like '-' or '?').

sub strip_chars 
# $fastas_ # array of FASTA objects
# $chars_ # () characters for stripping, as a string
{
    my $fastas_ = shift;
    my $chars_ = shift;
    
    my $a = Jls::Fasta::copy ($fastas_);
    my @cs = split ('', $chars_);
    
    foreach my $fasta (@$a)
    {
        foreach my $c (@cs)
        {        
            $fasta->{'seq'} =~ s/$c//gs;
        }
    }
    
    return $a;
}

# Extracts an array of FASTA objects matching a regex.

sub match 
# $fastas_ # array of FASTA objects
# $regex_ # regular expression
# $is_case_insensitive_ # ? Is the match case-insensitive ?
# $complement_ # empty array reference, to hold the complementary FASTA objects
{   
    my $fastas_ = shift;
    my $regex_ = shift;
    my $is_case_insensitive_ = shift;
    my $complement_ = shift;

    if (defined ($complement_))
    { 
        unless (ref $complement_ eq 'ARRAY' && scalar (@$complement_) == 0) 
        {
            die 'Jls::Fasta::match : ' .
                $complement_ . ' must be an empty array reference.';
        }
    }
    
    if (! defined ($is_case_insensitive_)) { $is_case_insensitive_ = 0; }

    if (! $regex_) { die 'Jls::Fasta::regex : No regular expression given'; }
    elsif ($regex_ =~ /=/)
    {
        die "Jls::Fasta::regex rejects regular expressions containing '='." . 
            "\nReplace '=' in every defline with a different character." . 
            "\nDied"; 
    }
    
    my @fa = ();
    
    foreach my $fasta (@$fastas_)
    {
        my $push;
         
        eval
        {
            if ($is_case_insensitive_ && $fasta->{'def'} =~ m=$regex_=i) 
                { $push = 1; }
            elsif (! $is_case_insensitive_ && $fasta->{'def'} =~ m=$regex_=) 
                { $push = 1; }
            else { $push = 0; }
        };

        if ($@) 
        {
            my $str = '';
            
            $str .= 'Jls::Fasta::match : The regular expression ';
            $str .= '[' . $regex_ . '] ';
            $str .= 'with a case-';
            if ($is_case_insensitive_) { $str .= 'in'; }
            $str .= 'sensitive match';
            $str .= ' could not be applied to the defline' . "\n";
            $str .= $fasta->{'def'};
            
            die $str; 
        }

        if ($push) { push (@fa, $fasta); }
        elsif (defined ($complement_)) { push (@$complement_, $fasta); }
    }
    
    return \@fa;
}

# Collects composition statistics on an array of FASTA objects.

sub composition 
# $fastas_ # array of FASTA objects
# $case_insensitive_ # (case-sensitive) : 1 = upper case
# $order_ # (1 = individual letters)
{
    use Jls::String;
    use Jls::Validate;
    
    my $fastas_ = shift;
    my $case_insensitive_ = shift;
    my $order_ = shift;

    if (! defined ($case_insensitive_)) { $case_insensitive_ = 0; }
    if (! defined ($order_)) { $order_ = 1; }
    
    my $h = {};
    
    foreach my $fasta (@$fastas_)
    {
        if (! defined ($fasta->{'seq'})) { next; }
        
        my $str = $fasta->{'seq'};
        $str = Jls::Validate::strip_whitespace ($str);
        
        if ($case_insensitive_ == 1) { $str = uc ($str); }
        
        my $h0 = Jls::String::count_symbols_in_string 
                    ($str, $case_insensitive_, $order_);
        
        foreach my $key (keys (%$h0))
        {
            if (defined ($h->{$key})) { $h->{$key} += $h0->{$key}; }
            else { $h->{$key} = $h0->{$key}; }
        }
    }
    
    return $h;
}

# Collects length statistics on an array of FASTA objects.

sub seq_lengths 
# $fastas_ # array of FASTA objects
{
    use Jls::String;
    use Jls::Validate;
    
    my $fastas_ = shift;
    
    my $h = seq_lengths_detail ($fastas_);
    
    return $h->{'lengths'};
}

# Collects detailed length statistics on an array of FASTA objects.

sub seq_lengths_detail 
# $fastas_ # array of FASTA objects
{
    use Jls::String;
    use Jls::Validate;
    
    my $fastas_ = shift;
    
    my $h = {};
    foreach my $fasta (@$fastas_)
    {
        my $length = 0;

        if (defined ($fasta->{'seq'}))
        {
            my $str = $fasta->{'seq'};
            $str = Jls::Validate::strip_whitespace ($str);
            $length = length ($str);
        }
        else { $length = 0; }
        
        if (defined ($h->{'lengths'})) 
        { 
            ++$h->{'lengths'}->{$length}; 
        }
        else 
        { 
            $h->{'lengths'} = {};
            $h->{'lengths'}->{$length} = 1;
            my @a = (); 
            $h->{'fastas'} = {};
            $h->{'fastas'}->{$length} = \@a; 
        }
        
        push (@{$h->{'fastas'}->{$length}}, $fasta);
    }
    
    return $h;
}

# Converts an array of FASTA objects to upper case sequences.

sub uc 
# $fastas_ # array of FASTA objects
{
    my $fastas_ = shift;
    
    my $a = Jls::Fasta::copy ($fastas_);
    
    foreach my $fasta (@$a)
    {
        if (defined ($fasta->{'seq'}))
        {
            $fasta->{'seq'} = CORE::uc ($fasta->{'seq'});
        }
    }
    
    return $a;
}

# Converts an array of FASTA objects to lower case sequences.

sub lc 
# $fastas_ # array of FASTA objects
{
    my $fastas_ = shift;
       
    my $a = Jls::Fasta::copy ($fastas_);
    
    foreach my $fasta (@$a)
    {
        if (defined ($fasta->{'seq'}))
        {
            $fasta->{'seq'} = CORE::lc ($fasta->{'seq'});
        }
    }
    
    return $a;
}

# ? Is $s_ a fasta defline, with '>' at the beginning ?

sub is_defline_good 
# $s_ # line
# $is_not_multi_ # ? Is > acceptable only at the beginning ?
# $eok_ # (0) ? Is an empty defline, with only '>' acceptable ?
{
    my $s_ = shift;
    my $is_not_multi_ = shift;
    my $eok_ = shift;

    if (! $eok_) { $eok_ = 0; }
    if (! $is_not_multi_) { $is_not_multi_ = 0; }

    if ($s_ =~ /\n/) { return 0; }
    if ($s_ !~ /^>/) { return 0; }
    if ($is_not_multi_ && ($s_ =~ />.*>/)) { return 0; }
    if (! $eok_ && ($s_ =~ /^>\s*$/)) { return 0; }

    return 1;
}

# Returns an array corresponding to split ("\n>").
# A final "\n" in the string $s_ is always acceptable.

# ? Is each $s_ a fasta record ?

sub is_records 
# $s_ # string 
# $is_record_ # (&Jls::Fasta::is_record) ? Is a record ?
{ 
    use Jls::Validate;
    
    my $s_ = shift;
    my $is_record_ = shift;
    if (! defined ($is_record_)) { $is_record_ = \&Jls::Fasta::is_record; }
    
    my @classify = ();
    
    chomp ($s_);

    if (length ($s_) == 0) { return \@classify; }
    elsif (length ($s_) == 1) 
    {
        push (@classify, &$is_record_ ($s_));
        return \@classify;
    }
        
    my @records = split ("\n>", $s_);
        
    my $is_good = &$is_record_ ($records [0]);
    my $init = 1;

    foreach my $record (@records) 
    { 
        if (! $init) { $record = '>' . $record; }

        push (@classify, ($init && $is_good ? 1 : 0) || &$is_record_ ($record));
        
        $init = 0;
    }
       
    return \@classify;
}

# Breaks a $string_ into lines of length $line_length_.
# The $string_ retains its newlines, and no newline is inserted at the end.
# Lines beginning with characters in $no_break_string_ are not broken.

sub string_break
# $string_ # string to be broken into separate lines of length $line_length
# $line_length_ # [0] : maximum length in each break; 0 = no breaks introduced
# $no_break_string_ # [''] : lines beginning with characters in $no_break_string_ are not broken
{ 
    my $string_ = shift;
    my $line_length_ = shift;
    my $no_break_string_ = shift;
    
    if (! $line_length_) { $line_length_ = 80; }
    if (! $no_break_string_) { $no_break_string_ = ''; }
    
    my $s = '';
    
    my @array = split ("\n", $string_);
    
    ADD_LINE : foreach my $line (@array)
    {
        if ($s) { $s .= "\n"; }

        my $c = substr ($line, 0, 1);

        if ($no_break_string_ && $no_break_string_ =~ /$c/)
        {
            $s .= $line;
            next ADD_LINE;
        }
        
        for (my $i = 0; $i < length ($line); $i += $line_length_)
        {
            my $length = length ($line) - $i;
            if ($line_length_ <= $length) { $length = $line_length_; }
            
            if ($i != 0) { $s .= "\n"; }
            $s .= substr ($line, $i, $length);
        }
    }
    
    return $s;
}

# ? Is $s_ a (case-insensitive) (lower-case nucleotide) fasta record ?
# The module values are used in the determination.

sub is_record 
# $s_ # string (without \n at end)
{
    use Jls::Validate;
    
    my $s_ = shift;
    my $is_not_multi_ = shift;
    my $defline_eok_ = shift;
    my $is_no_comment_ = shift;
    my $alphabet_ = shift;
    my $is_case_sensitive_ = shift;
    my $no_ws_ = shift;
    my $eok_ = shift;

    if (! defined ($eok_)) { $eok_ = $Jls::Fasta::_eok; }
    if (! defined ($no_ws_)) { $no_ws_ = $Jls::Fasta::_no_ws; }
    
    if (! defined ($is_case_sensitive_)) 
    { 
        $is_case_sensitive_ = $Jls::Fasta::_is_case_sensitive; 
    }
    
    if (! defined ($alphabet_)) { $alphabet_ = $Jls::Fasta::_alphabet; }
    
    if (! defined ($is_no_comment_)) 
    { 
        $is_no_comment_ = $Jls::Fasta::_is_no_comment; 
    }
    
    if (! defined ($defline_eok_)) { $defline_eok_ = $Jls::Fasta::_defline_eok; }
    
    if (! defined ($is_not_multi_)) 
    { 
        $is_not_multi_ = $Jls::Fasta::_is_not_multi; 
    }
    
    my @lines = split ("\n", $s_);
    
    if (! $no_ws_ ) 
    { 
        while (@lines && Jls::Validate::is_whitespace ($lines [0])) 
        {
            shift (@lines); 
        }
        
        if (! @lines) { return 0; } 
    }
    
    if (is_defline_good ($lines [0], $is_not_multi_, $defline_eok_)) 
    { 
        shift (@lines); 
    }
    else { return 0; }
    
    if (! @lines) { return $eok_; }

    if ($is_no_comment_ && _is_comment ($lines [0])) { return 0; }
    
    while (@lines && _is_comment ($lines [0])) { shift (@lines); }
    
    my $seq = join ("\n", @lines);
    
    return _is_sequence ($seq, $alphabet_, 
                         $is_case_sensitive_, $no_ws_, $eok_);
}
 
$Jls::Fasta::_is_not_multi = 0; 

sub is_not_multi 
# $is_not_multi_ # ? Is > acceptable only at the beginning ?
{
    if (! @_) { return $Jls::Fasta::_is_not_multi; }
    
    return $Jls::Fasta::_is_not_multi = shift;
}

$Jls::Fasta::_defline_eok = 0; 

sub defline_eok 
# $defline_eok_ # ? Is an empty defline alright ?
{
    if (! @_) { return $Jls::Fasta::_defline_eok; }
    
    return $Jls::Fasta::_defline_eok = shift;
}

$Jls::Fasta::_is_no_comment = 0; 

sub is_no_comment 
# $is_no_comment_ # ? Is a comment line unacceptable ?
{
    if (! @_) { return $Jls::Fasta::_is_no_comment; }
    
    return $Jls::Fasta::_is_no_comment = shift;
}

$Jls::Fasta::_alphabet = $Jls::Fasta::_nucleotide; 

sub alphabet 
# $alphabet # the sequence alphabet
{
    if (! @_) { return $Jls::Fasta::_alphabet; }
    
    return $Jls::Fasta::_alphabet = shift;
}

$Jls::Fasta::_is_case_sensitive = 0; 

sub is_case_sensitive 
# $is_case_sensitive_ # ? Is the sequence case-sensitive ?
{
    if (! @_) { return $Jls::Fasta::_is_case_sensitive; }
    
    return $Jls::Fasta::_is_case_sensitive = shift;
}

$Jls::Fasta::_no_ws = 0; # ? Is no white space in the sequence enforced ?

sub no_ws 
# $no_ws_ # ? Is no white space in the sequence enforced ?
{
    if (! @_) { return $Jls::Fasta::_no_ws; }
    
    return $Jls::Fasta::_no_ws = shift;
}

$Jls::Fasta::_eok = 0; 

sub eok 
# $eok_ # ? Is an empty sequence acceptable ?
{
    if (! @_) { return $Jls::Fasta::_eok; }
    
    return $Jls::Fasta::_eok = shift;
}

# Adds $msg_ to $output_ for xhtml.

sub _add 
# $out_ # output
# $msg_ # message
{
    my $out_ = shift;
    my $msg_ = shift;
    
    my $out = $out_;
    
    if ($out) { $out .= "\n"; }
    $out .= $msg_;
    
    return $out; 
}

# Decorates $s_ with a span for xhtml.

sub _decorate_span
# $s_ # record
# $type_ # (1/0 = 'good'/'bad') class for a decorating span
{
    use Jls::Pm2html;
    
    my $s_ = shift;
    my $type_ = shift;
    
    if ($type_) { return ''; }
    
    my $s = $s_;
    
    $s = Jls::Pm2html::text2html ($s);
    $s =~ s|\n|\n<br />|sg;
    
    my $title = 'This section is valid.';
    if (! $type_) { $title =~ s/valid/invalid/; }
    
    my $str = 'class="good" ';
    if (! $type_) { $str =~ s/good/bad/; }

    my $msg = '';
    $msg .= '<div '; 
    
    $msg .= $str; 
    $msg .= 'title="' . $title . '" ';
    $msg .= '>'; 

    $msg .= "\n" . $s; 
    $msg .= "\n" . '</div>'; 

    return $msg;  
}

# ? Is $s_ a fasta comment line, with ';' at the beginning ?

sub _is_comment 
# $s_ # line
{
    my $s_ = shift;

    if ($s_ =~ /\n/) { return 0; }
    if ($s_ !~ /^;/) { return 0; }

    return 1;
}

$Jls::Fasta::_nucleotide = "ACGTMRWSYKVHDBNU"; 

# ? Is $s_ a (case-insensitive) (lower-case nucleotide) fasta sequence ?

# White space is stripped before the determination.

sub _is_sequence 
# $s_ # string
# $alphabet_ # ($Jls::Fasta::_nucleotide)
# $is_case_sensitive_ # (0)
# $no_ws_ # (0) ? Is whitespace interpolated in the sequence unacceptable ?
# $eok_ # (0) ? Is an empty sequence acceptable ?
{
    use Jls::Validate;
    
    my $s_ = shift;
    my $alphabet_ = shift;
    my $is_case_sensitive_ = shift;
    my $no_ws_ = shift;
    my $eok_ = shift;

    if (! $eok_) { $eok_ = 0; }
    if (! $no_ws_) { $no_ws_ = 0; }
    if (! $is_case_sensitive_) { $is_case_sensitive_ = 0; }
    if (! $alphabet_) { $alphabet_ = $Jls::Fasta::_nucleotide; }
    
    if (! $s_) { return $eok_; }
    
    my $s = $s_;
    
    if ($no_ws_) { if ($s =~ /\s/s) { return 0; } }
    else { $s = Jls::Validate::strip_chars_in_bag ($s, '\s'); }
    
    if (! $eok_ && ! $s) { return 0; }
    
    if (! $is_case_sensitive_) 
    { 
        $s = CORE::lc ($s); 
        $alphabet_ = CORE::lc ($alphabet_); 
    }
    
    return Jls::Validate::is_chars_in_bag ($s, $alphabet_);
}

1;

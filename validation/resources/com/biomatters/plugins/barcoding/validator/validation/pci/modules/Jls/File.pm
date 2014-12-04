#!/usr/bin/perl

use strict;
use warnings;

package Jls::File;

use Exporter;
our @ISA = qw/Exporter/;
our @EXPORT = 'write_download_file download_file file2string file_to_string is_blank_line file_handle_to_string logical_newline download clean append_slash unlink move_regex rename is_01 is_blank is_boolean is_integer is_float is_float_or_NaN dos perl2javascript';

=head 1

=cut

# Convert a Perl string to a string for javascript.
# Assumes the string represents the value of an input.

sub perl2javascript
# $string_ // the string to appear on the web page
# $quote_ // the quotes surrounding the string on the web page
{
    my $string_ = shift;
    my $quote_ = shift;

    $string_ =~ s/\n/\\n/gs; 
    $string_ =~ s/\r/\\r/gs; 
    $string_ =~ s/\t/\\t/gs; 
    
    if ($quote_)
    {
        $string_ =~ s|$quote_|\\$quote_|gs; # Takes care of quotation marks.
    }
    
    return $string_;
}

# Converts an input file to a string.

sub file2string 
# $file_ // input file
{
    my $file_ = shift;
    
    return Jls::File::file_to_string ($file_);
}

# Converts an input file to a string.

sub file_to_string 
# $file_ // input file
{
    my $file_ = $_ [0];
    
    my $msg = "Input file '" . $file_ . 
        "' failed to open.\n" . "Died";

    open INPUT, "<$file_" or die $msg;
    my $str = Jls::File::file_handle_to_string INPUT;
    close INPUT;

    return $str;
}

# Checks whether $line_ is blank.

sub is_blank_line
# line_
{
    my $line_ = shift;
    
    return $line_ =~ /^\s*$/ ? 1 : 0;
}

# Converts PC, UNIX, MAC newlines to logical newline.

sub logical_newline 
# $str_ // Input string
{
    my $str_ = shift;

    use Socket qw(:DEFAULT :crlf);

    $str_ =~ s/$CR$LF/$LF/g; # Converts any PC newline to a UNIX newline.
    $str_ =~ s/($LF|$CR)/\n/g; # Converts UNIX or MAC newline to a UNIX newline

    return $str_;
}

# Converts an file-handle from a file upload box to a string.

sub file_handle2string 
# $file_handle_ // Input file-handle
{
    return Jls::File::file_handle_to_string(shift);
}

# Converts an file-handle from a file upload box to a string.

sub file_handle_to_string 
# $file_handle_ // Input file-handle
{
    my $file_handle_ = shift;

    use Socket qw(:DEFAULT :crlf);

    local($/) = LF; 

    my $str = '';
    
    while (my $line = <$file_handle_>) # Reads entire file.
    {
        $str .= $line;
    }

    # Converts PC, UNIX, MAC newlines to logical newline
    
    $str = logical_newline ($str);

    return $str;
}

# Does directory clean-up of files older than 1 day.

sub clean 
# $abs_dir_ // Absolute directory path
# $regex_ // (.cgi$) Filename match regex for cleanup
# $age_ // (1) Age of file in days before clean-up
{
    my $abs_dir_ = shift;
    my $regex_ = shift;
    my $age_ = shift;
    
    if (! defined ($age_)) { $age_ = 1; } 
    if (! defined ($regex_)) { $regex_ = '\\.cgi$'; } 
    
    opendir (DIR, $abs_dir_);
    my @files = grep (/$regex_/, readdir (DIR));

    closedir (DIR);
    
    foreach my $file (@files)
    {
		my $abs_path = $abs_dir_ . $file;
        if (-M $abs_path > $age_) { unlink ($abs_path); }
    }
}

# Returns the tmp directory for CGI writing and download.

sub die_if_no_file_
# $abs_dir_ // absolute directory path to file for download
# $filename_ // filename of the file to be downloaded
# $age_ // age for cleaning a file out
# $error_msg_ // (die_if_no_file_ (), see below) error message 
{
    my $abs_dir_ = shift;
    my $filename_ = shift;
    my $age_ = shift;
    my $error_msg_ = shift;
    
    if (! defined ($age_)) { $age_ = 1; } 
    if (! defined ($error_msg_)) { $error_msg_ = ''; } 

    my $die = "Within the directory '" . $abs_dir_ . "'";
    $die .= " the file '" . $filename_ . "'";
    $die .= " does not exist.";
    $die .= "\nIf it was created more than ";
    $die .= $age_;
    $die .= " day ago, it was probably been deleted.";

    if ($error_msg_ ne '') { $die = $error_msg_; }    
    die $die unless (-e $abs_dir_ . $filename_);

    return 0;
}

# Writes a text file for later download.

sub write_download_file
# $abs_dir_ // absolute directory path to file for download
# $filename_ // filename of the file for downloaded
# $str_ // contents of the file as a string
{
    my $abs_dir_ = shift;
    my $filename_ = shift;
    my $str_ = shift;

    open FILE, ">" . $abs_dir_ . $filename_;
    print FILE $str_;   
    close FILE;

    return 0;
}

# Returns the string to download a file.

sub download_file
# $abs_dir_ // absolute directory path to file for download
# $filename_ // filename of the file for downloaded
# $user_filename_ // filename that the user sees
# $age_ // age for cleaning a file out
# $error_msg_ // (die_if_no_file_ (), see below) error message 
{
    my $abs_dir_ = shift;
    my $filename_ = shift;
    my $user_filename_ = shift;
    my $age_ = shift;
    my $error_msg_ = shift;
    
    if (! defined ($age_)) { $age_ = 1; } 

    Jls::File::die_if_no_file_ ($abs_dir_, $filename_, $age_, $error_msg_);
    
    my $str = '';
    
    $str .= 'Content-Type: ';
    $str .= 'text/plain'; 
    $str .= "\n";
    
    $str .= 'Content-Disposition: ';
    $str .= 'attachment; '; 
    
    $str .= 'filename=';
    $str .= $user_filename_;

    $str .= "\n\n";

    $str .= Jls::File::file_to_string ($abs_dir_ . $filename_);

    return $str;
}

# Appends a slash to a directory name, if it is not already present.

sub append_slash
# $dir_ // directory ('./')
{
    my $dir_ = shift;

    if (! defined ($dir_)) { $dir_ = '.\/'; }
    if ($dir_ =~ /\/$/) { return $dir_; }
    
    $dir_ = $dir_ . '/';

    return $dir_;    
}

# Deletes all files matching $regex_ from the directory $dir_

sub unlink
# $regex_ // regular expression
# $dir_ // directory ('./')
{
    my $regex_ = shift;
    my $dir_ = shift;

    if (! defined ($dir_)) { $dir_ = './'; }
    $dir_ = append_slash ($dir_);
    
    opendir (DIR, $dir_);
    my @files = grep (/$regex_/, readdir (DIR));
    closedir (DIR);

    foreach my $file (@files)
    {
        unlink ($dir_ . $file);
    }
}

# Moves all files matching $regex_ from the directory $dir_ to directory $dir2_.

sub move_regex
# $regex_ // regular expression
# $dir2_ // directory ('./')
# $dir_ // directory ('./')
{
    my $regex_ = shift;
    my $dir2_ = shift;
    my $dir_ = shift;
    
    if (! defined ($dir_)) { $dir_ = './'; }
    $dir_ = append_slash ($dir_);
    
    if (! defined ($dir2_)) { $dir_ = './'; }
    $dir2_ = append_slash ($dir2_);
    
    opendir (DIR, $dir_);
    my @files = grep (/$regex_/, readdir (DIR));
    closedir (DIR);

    use File::Copy;
    
    foreach my $file (@files)
    {
        $file =~ s|^.*/||;
        File::Copy::move ($dir_ . $file, $dir2_ . $file);
    }
}

# Substitutes $str for all files matching $regex_ in the directory $dir_.

sub rename
# $regex_ // regular expression
# $str_ // rename string
# $dir_ // directory
{
    my $regex_ = shift;
    my $str_ = shift;
    my $dir_ = shift;
    
    if (! defined ($dir_)) { $dir_ = './'; }
    $dir_ = append_slash ($dir_);
    
    opendir (DIR, $dir_);
    my @files = grep (/$regex_/, readdir (DIR));
    closedir (DIR);

    use File::Copy;

    foreach my $file (@files)
    {
        my $file2 = $file;
        $file2 =~ s/$regex_/$str_/;
        
        File::Copy::move ($dir_ . $file, $dir_ . $file2);
    }
}

# DEPRECATED: Downloads a text file.

sub download 
# $filename_ // Download filename visible to user
# $string_ // Text-string in the download file
# $abs_cgi_filename_ // Absolute directory path to cgi file triggering download
{
    my $filename_ = shift;
    my $string_ = shift;
    my $abs_cgi_filename_ = shift;

    use Socket qw(:DEFAULT :crlf);
    $string_ =~ s/\n/$CRLF/gs; # Prepares compatible newlines for the socket.
        
    my $s = '';
    
    $s .= "#! /usr/bin/perl\n";
    $s .= "\n";
    $s .= "use strict;\n";
    $s .= "use warnings;\n";
    $s .= "\n";
    $s .= "use CGI qw/:standard/;\n";
    $s .= "use CGI::Carp qw(fatalsToBrowser);\n";
    $s .= "\n";
    $s .= "# Starts a download file.\n";
    $s .= "\n";

    $s .= "print 'Content-Type: text/plain\n";
    $s .= "Content-Disposition: attachment; filename=$filename_;\n\n';\n\n";
    $s .= 'print<<FILE' . "\n";
    $s .= $string_ . "\n";
    $s .= 'FILE' . "\n";

    # Opens, prints, and closes the download *.CGI.
    
    open DOWN, ">$abs_cgi_filename_"; 
    print DOWN $s;
    close DOWN;

    chmod (0755, $abs_cgi_filename_); # Data file permissions
}

# Takes care of DOS returns & backslashes in a string with a directory.

sub dos
# $string_
# $quote_
{
    my $string_ = shift;
    my $quote_ = shift;

    use Socket qw(:DEFAULT :crlf);

    my $msg = $string_;
    $msg =~ s|\\|/|gs; # Takes care of DOS backslashes.
    $string_ =~ s/\n/$CRLF/gs; # Prepares compatible newlines for the socket.
    $msg =~ s|\r{0,1}\n|\\n|gs; # Takes care of DOS newlines.
    
    if ($quote_)
    {
        $msg =~ s|$quote_|\\$quote_|gs; # Takes care of quotation marks.
    }
    
    return $msg;
}

# Is the string blank?

sub is_blank
# $string_
{
    my $string_ = $_ [0];
    if ($string_ =~ m/^\s*$/) { return 1; }

    return 0;
}

# ? Is the $string_ 0 or 1 ?

sub is_01
# $string_
{
    my $string_ = $_ [0];

    if ($string_ =~ m/^0$/) { return 1; }
    if ($string_ =~ m/^1$/) { return 1; }

    return 0;
}

# ? Is the $string_ a boolean ?

sub is_boolean
# $string_
{
    my $string_ = $_ [0];

    if ($string_ =~ m/^false$/i) { return 1; }
    if ($string_ =~ m/^f$/i) { return 1; }
    if ($string_ =~ m/^0$/) { return 1; }

    if ($string_ =~ m/^true$/i) { return 1; }
    if ($string_ =~ m/^t$/i) { return 1; }
    if ($string_ =~ m/^1$/) { return 1; }

    return 0;
}

# ? Is the $string_ an integer ?

sub is_integer
# $string_
{
    my $string_ = $_ [0];

    return $string_ =~ m/^(\+|\-)?\d*$/ ;
}

# ? Is the $string_ a real number ?

sub is_float
# $string_
{
    my $string_ = $_ [0];

    if ($string_  =~ m/^\D+$/) { return ''; }

    return $string_ =~ m/^(\+|\-)?\d*\.?\d*(e(\+|\-)?\d+)?$/i ;
}

# ? Is the $string_ a real number or a 'NaN'?

sub is_float_or_NaN
# $string_
{
    my $string_ = $_ [0];

    if (is_float ($string_)) { return is_float ($string_); }

    return $string_ eq 'NaN' ? 'NaN' : '';
}

1;

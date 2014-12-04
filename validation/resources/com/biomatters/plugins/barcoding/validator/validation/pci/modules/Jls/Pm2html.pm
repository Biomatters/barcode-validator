#!/usr/bin/perl

use strict;
use warnings;

package Jls::Pm2html;

use Exporter;
our @ISA = 'Exporter';
our @EXPORT = 'doc';

use Jls::Pm2html;

=pod

=cut

sub doc 
# $file_ // file
{
my $str = '

pm2html.pl Version 1.0

The program pm2html.pl documents Perl modules in its present working directory.
It uses the HTML template \'template.html\' in its present working directory.

Usage> Perl pm2html.pl [Perl module . \'.pm\']

The output is the file [Perl module . \'.html\'"]
';

    my $pm;
    
    eval { $pm = shift; };
    
    if ($@ || ! defined ($pm))
    {
        print $str . "\n";
        exit (0);
    }
    
    my $template = 'template.html';
    
    my $module = Jls::File::file_to_string ($pm);
    my $html = Jls::File::file_to_string ($template);
    my $main = '';
    
    $module =~ s/(.*?)=cut//s;
    
    my @subroutines = split ("\n}\n", $module);
    delete $subroutines [$#subroutines];
    foreach my $subroutine (@subroutines) { $subroutine .= "\n}\n"; }
    
    my $index = 0; # subroutine number
    
    foreach my $remainder (@subroutines)
    {
        $remainder =~ /(.*?)\nsub (.*)/s;
        
        my $comment = $1;
        $remainder = $2;
        
        $remainder =~ /(.*?)\n(.*)/s;
        
        my $subroutine = $1;
        $remainder = $2;
    
        $remainder =~ /(.*?)\n({.*)/s;
        
        my $argument = $1 eq $subroutine ? '' : $1; 
        # empty match => no reset of $1
        
        my $body = $2;
        
        my @comments = ();
          
        my $edit = '';
               
        if ($index != 0) { $edit .= '<br />'; }
        $edit .= "\n" . '<span class="accent" style="display: none; " id="comment_' . $index . '" ><br />';
        push (@comments, $edit);
            
        my $start = 0;
        
        foreach my $line (split ("\n", $comment))
        {
            my $code = 0;
            if ($line !~ /^#/) { $code = 1; } 
            if ($line !~ /^#(\s*)$/) { $line =~ s/^# //; } # Removes the Perl comment octothorp.
            Jls::Validate::strip_bounding_whitespace ($line);
            if (! $line) { next; } # Skips blank lines.
            
            $edit = '';
            
            if ($start)
            {
                $edit .= "\n<br />";
            }

            $start = 1;
            
            if ($line =~ /^#(\s*)$/) { $line = ' '; }
            
            $edit .= $line;
            
            push (@comments, $edit);
        }
    
        push (@comments, "\n" . '<br /></span>' . "\n");
        
        $edit = Jls::Validate::strip_bounding_whitespace ($subroutine);
        
        $subroutine  = '';    
    
        $subroutine .= "\n<br />\n";
        $subroutine .= '<script type="text/javascript">' . "\n";
        $subroutine .= '/* <![CDATA[ */' . "\n";
        $subroutine .= 'document.write (icon (\'head\', ' . $index . '))' . "\n";
        $subroutine .= '/* ]]> */' . "\n";
        $subroutine .= '</script>' . "\n";
        $subroutine .= '<span class="alert" id="title_' . $index . '" " >';
        $subroutine .= $edit;
        $subroutine .= '</span>';
        
        my @arguments = ();
             
        $edit = "\n" . '<span id="argument_' . $index . '" style="display: none; " >';
           
        push (@arguments, $edit);
    
        foreach my $line (split ("\n", $argument))
        {
            $line =~ s/^# //; # Removes the Perl comment octothorp.
            Jls::Validate::strip_bounding_whitespace ($line);
            if (! $line) { next; } # Skips blank lines.
    
            $edit = "\n<br />" . $line;
    
            if ($edit !~ m|//|) { $edit .= '<span class="comment" >'; }
            else { $edit =~ s|//|<span class="comment" >//|; }
            $edit .= '</span>';
            
            push (@arguments, $edit);
        }
    
        $body = Jls::Validate::strip_bounding_whitespace ($body);
        
        $body = text2html ($body);
        $body =~ s|\n|\n<br />|g;
        
        $edit = "\n<br />";
        $edit .= '<script type="text/javascript">';
        $edit .= '/* <![CDATA[ */';
        $edit .= 'document.write (icon (\'body\', ' . $index . '))';
        $edit .= '/* ]]> */';
        $edit .= '</script>';
        $edit .= '</span>';
        
        $edit .= "\n" . '<div style="display: none; " id="body_';
        $edit .= $index . '" >';
    
        $body = "\n" . $edit . "\n" . $body;
    
        $edit = '';
        $edit .= $body;
        $edit .= '</div>';
        $edit .= "\n" . '<span style="display: none; " id="tail_';
        $edit .= $index . '" ><br /></span>';
        $edit .= "\n";
        
        $body = $edit;
    
        $main .= join ('', @comments);
        $main .= $subroutine;
        $main .= join ('', @arguments);
        $main .= $body;
    
        $index++;
    }    
    
    my @file = split ('/', $pm);
    my $out = $file [$#file];
    
    $html =~ s|Perl Documentation</title>|$out</title>|;
    $html =~ s|h3</h3>|$out</h3>|;
    $html =~ s|// main|$main|;

    return $html;    
}

sub text2html 
# $text_ // text before using html characters
{
    my $text_ = shift;

    my $html = $text_;
    
    $html =~ s|&|&amp;|gs;
    $html =~ s|<|&lt;|gs;
    $html =~ s|>|&gt;|gs;
    $html =~ s| |&nbsp;|gs;
    
    return $html;
}

1;
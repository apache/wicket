#!/usr/bin/perl -w
#  Licensed to the Apache Software Foundation (ASF) under one or more
#  contributor license agreements.  See the NOTICE file distributed with
#  this work for additional information regarding copyright ownership.
#  The ASF licenses this file to You under the Apache License, Version 2.0
#  (the "License"); you may not use this file except in compliance with
#  the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

use strict;

my $baseurl = "http://svn.apache.org/repos/asf/wicket/";
my $url = $baseurl . "releases/";
my $tag = $ARGV[0];

if ($#ARGV == -1) {
	print "Usage: ./logsincetag.pl DESIRED_RELEASE_TAG\n";
	print "Example: ./logsincetag.pl wicket-1.4.8\n\n";
	print "This will print all log messages since ${url}wicket-1.4.8 was tagged\n";
	print "I recommend piping this through your favorite pager.\n\n";
	exit 0;
}

print "Getting revision number for tag '$tag'" . "\n";

my $cmd = "svn log --stop-on-copy " . $url . $tag;
print "Using command: $cmd\n";


open DATA, "$cmd |"   or die "Error running cmd: $!";

my $last = '';
while ( defined( $_ = <DATA> )  ) {
	chomp();
	#print "line: $_\n";
	if ( /^r([0-9]+) \|.*/ ) {
		$last = $1;
	}
}
close DATA;
print "Last revision: " . $last . "\n\n";

open DATA, "svn info |"   or die "Error running cmd: $!";

my $thisVersionURL = '';
while ( defined( $_ = <DATA> )  ) {
	chomp();
	#print "line: $_\n";
	if ( /^URL: (http.*)/ ) {
		$thisVersionURL = $1;
	}
}
close DATA;
print "This version's URL: $thisVersionURL";

$cmd = "svn log " . $thisVersionURL . " -r " . $last . ":HEAD";
open DATA, "$cmd |"   or die "Error running cmd: $!";

while ( defined( $_ = <DATA> )  ) {
	print $_;
}
close DATA;

#!/bin/sh
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

function fail {
	echo "$1"
	exit 1
}

function getProjectVersionFromPom {
	cat << EOF | xmllint --noent --shell pom.xml | grep content | cut -f2 -d=
setns pom=http://maven.apache.org/POM/4.0.0
xpath /pom:project/pom:version/text()
EOF
}

if [ "$1" = "--help" ] ; then
	echo "
Usage: $0 [--help] [version]

Retrieves the release notes for the next release of Apache Wicket,
and merges this into the CHANGELOG file.

  version
      optional version number to retrieve the release notes for

  --help
      shows this help

"
	exit 0
fi

if [ ! -z "$1" ] ; then
	current_version="$1"
	major_version=$(expr $current_version : '\(.*\)\..*\..*\-.*')
	minor_version=$(expr $current_version : '.*\.\(.*\)\..*\-.*')
	bugfix_version=$(expr $current_version : '.*\..*\.\(.*\)-.*')
	milestone_version=$(expr $current_version : '.*\..*-\(.*\)')
	version="$major_version.$minor_version.0-$milestone_version"
	previous_version="$major_version.$(expr $minor_version - 1).0"
else
	current_version=$(getProjectVersionFromPom)
	major_version=$(expr $current_version : '\(.*\)\..*\..*\-.*')
	minor_version=$(expr $current_version : '.*\.\(.*\)\..*\-.*')
	bugfix_version=$(expr $current_version : '.*\..*\.\(.*\)-.*')
	version="$major_version.$minor_version.0"
	previous_version="$major_version.$(expr $minor_version - 1).0"
fi

echo "
Apache Wicket release notes generator
=====================================
This tool retrieves the release notes for the upcoming release from JIRA
and merges this with the existing CHANGELOG in a text format.

Version to retrieve the release notes from: $version

Press <enter> to continue \c"

read

git status --porcelain CHANGELOG-$major_version.x | grep -q "CHANGELOG-$major_version.x"
if [ $? -eq 0 ] ; then
	fail "You already have changes in the CHANGELOG-$major_version.x
"
fi

grep -q "$version\$" CHANGELOG-$major_version.x
if [ $? -eq 0 ] ; then
	fail "You already have added release notes for this version to the changelog.
"
fi


echo "
Extracting JIRA Release notes -- making web requests"
echo "  - determining JIRA version id for $version: \c"
jira_project_id=12310561
jira_version_id=$( \
	curl -s https://issues.apache.org/jira/secure/ReleaseNote.jspa?projectId=$jira_project_id \
	| xmllint --noout --noblanks --html --xpath "string(//select[@id=\"version_select\"]/option[translate(normalize-space(text()), ' ', '')=\"$version\"]/@value)" - 2>/dev/null \
	)

re='^[0-9]+$'
if ! [[ $jira_version_id =~ $re ]] ; then
	echo "ERROR"
	echo "
Unable to retrieve the version ID from JIRA: received '$jira_version_id'" >&2
	exit 1
fi

echo "$jira_version_id"

echo "  - retrieving text release notes: \c"
curl -s "https://issues.apache.org/jira/secure/ReleaseNote.jspa?projectId=$jira_project_id&version=$jira_version_id&styleName=Text&Create=Create" \
	|  xmllint --noout --noblanks --html --xpath "string(//textarea)" - 2>/dev/null | cat -s | awk '{ if ($0 ~ /^\*\* / ) {
    printf( "%s\n\n", $0);
} else {
    printf( "%s\n", $0 );
}
}' > /tmp/release-notes-$version.txt

echo "done"

echo "  - merging release notes into changelog: \c"

echo "This file contains all changes done in releases for Apache Wicket 7.x.

=======================================================================
$(cat /tmp/release-notes-$version.txt)

=======================================================================
$(tail -n +4 CHANGELOG-$major_version.x)
" > /tmp/changelog-$version.txt
cp /tmp/changelog-$version.txt CHANGELOG-$major_version.x

echo "done"

echo "
The CHANGELOG-$major_version.x file has been updated. Please check the contents
and commit the changes.

To see the status:

    git status
    git diff

To add and commit the CHANGELOG:

    git add CHANGELOG-$major_version.x
    git commit -m \"Added CHANGELOG for release $version

Have fun!
"

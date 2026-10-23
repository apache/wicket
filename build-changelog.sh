#!/bin/bash
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

jira_project=WICKET
jira_url=https://issues.apache.org/jira/rest/api/2
github_owner=apache
github_name=wicket
github_repository=$github_owner/$github_name

function fail {
	echo "$1" >&2
	exit 1
}

function getProjectVersionFromPom {
	cat << EOF | xmllint --noent --shell pom.xml | grep content | cut -f2 -d=
setns pom=http://maven.apache.org/POM/4.0.0
xpath /pom:project/pom:version/text()
EOF
}

# Prints one record per JIRA issue the query finds; a key in the query that JIRA does not know is
# skipped.
function getJiraIssues {
	local response
	response=$(curl -sf -G "$jira_url/search" \
		--data-urlencode "jql=$1" \
		--data-urlencode "validateQuery=warn" \
		--data-urlencode "fields=issuetype,summary" \
		--data-urlencode "maxResults=1000") \
		|| fail "Unable to retrieve the issues from JIRA for: $1"
	jq -e '.total == (.issues | length)' <<< "$response" > /dev/null \
		|| fail "JIRA returned only part of the issues for: $1"
	jq -r '.issues[]
		| [
			.fields.issuetype.name,
			"1",
			(.key | sub("^[A-Z]+-"; "")),
			.key,
			(.fields.summary | gsub("\\s+"; " ") | sub(" $"; ""))
		]
		| join("\t")' <<< "$response"
}

# Prints the JIRA keys in the subjects of the commits that git log selects with the given
# arguments.
function getJiraKeys {
	git log --format=%s "$@" | grep -oE "$jira_project-[0-9]+" | sort -u
}

# Prints one record per GitHub issue resolved by the given issue and pull request numbers: a pull
# request contributes the issues it closes.
function getGitHubIssues {
	local query="query { repository(owner: \"$github_owner\", name: \"$github_name\") {"
	for number in "$@" ; do
		query+=" n$number: issueOrPullRequest(number: $number) { ...issueOrPullRequest }"
	done
	query+=" } }
fragment issue on Issue { number title url state stateReason issueType { name } }
fragment issueOrPullRequest on IssueOrPullRequest {
	... on Issue { ...issue }
	... on PullRequest { closingIssuesReferences(first: 25) { nodes { ...issue } } }
}"

	# a number that is no issue or pull request fails gh, but leaves the others in the response
	local response
	response=$(gh api graphql -f query="$query" 2> /dev/null)
	jq -e '.data.repository' <<< "$response" > /dev/null \
		|| fail "Unable to retrieve the issues from GitHub: $response"

	jq -r --arg url "https://github.com/$github_repository/issues/" '
		.data.repository[]
		| select(. != null)
		| if has("closingIssuesReferences") then .closingIssuesReferences.nodes[] else . end
		| select(.url | startswith($url))
		| select(.stateReason != "NOT_PLANNED" and .stateReason != "DUPLICATE")
		| [
			(.issueType.name | if . == null then "Other" elif . == "Feature" then "New Feature" else . end),
			"2",
			(.number | tostring),
			"GH-\(.number)",
			(.title | gsub("\\s+"; " ") | sub(" $"; "")),
			.state
		]
		| join("\t")' <<< "$response"
}

# Prints one record per GitHub issue referenced from the commits that git log selects with the given
# arguments: by any reference in the subject, or in the body as "GitHub issue #N" or after fixes,
# closes or resolves.
function getReferencedGitHubIssues {
	local references
	references=$( \
		{
			git log --format=%s "$@" \
				| grep -oE "([[:alnum:]_.-]+/[[:alnum:]_.-]+)?#[0-9]+|GH-[0-9]+|github\.com/[[:alnum:]_.-]+/[[:alnum:]_.-]+/(issues|pull)/[0-9]+" \
				| grep -E "^(#|GH-|$github_repository#|github\.com/$github_repository/)"
			git log --format=%b "$@" \
				| grep -oiE "(github issues? |(fix(e[sd])?|close[sd]?|resolve[sd]?)( issue)? )#[0-9]+"
		} \
		| grep -oE '[0-9]+$' \
		| sort -n -u)
	echo $references | xargs -n 50 | while read -r numbers ; do
		[ -n "$numbers" ] || continue
		getGitHubIssues $numbers || exit 1
	done | sort -u
	return ${PIPESTATUS[2]}
}

if [ "$1" = "--help" ] ; then
	echo "
Usage: $0 [--help] [--since <ref>] [version]

Collects the issues resolved in the next release of Apache Wicket from
JIRA and GitHub, and merges them into the CHANGELOG file.

  version
      optional version number to collect the release notes for, defaults
      to the version in the pom without its bugfix and snapshot parts

  --since <ref>
      git reference of the previous release, defaults to the tag of the
      highest version before this one

  --help
      shows this help

The JIRA issues are the ones resolved as Fixed with the version as fix
version, and the ones whose key is in the subject of a commit since the
previous release but of no commit before it, unless resolved otherwise
than as Fixed: JIRA is read-only, so a fix that went in after that can
no longer be resolved or given a fix version there.

The GitHub issues are the ones referenced from the commits since the
previous release, either directly or through a pull request that closes
them. A reference counts anywhere in the subject, and in the body as
'GitHub issue #N' or after fixes, closes or resolves. A GitHub issue
closed as not planned or as a duplicate is left out. The issue type of a
GitHub issue decides its group, and an issue without one goes into
'Other'.

A GitHub issue with a JIRA key in its title is a copy of that JIRA issue:
when the JIRA issue is in the release notes, the copy is listed under the
JIRA key only. Before merging the notes, the tool reports the GitHub
issues in them that are still open, copies included.

Needs curl, jq, xmllint and an authenticated GitHub CLI (gh auth login).
"
	exit 0
fi

since_ref=""
version=""
while [ $# -gt 0 ] ; do
	case "$1" in
		--since)
			[ -n "$2" ] || fail "--since needs a git reference"
			since_ref="$2"
			shift
			;;
		-*)
			fail "Unknown option: $1, see $0 --help"
			;;
		*)
			version="$1"
			;;
	esac
	shift
done

for tool in curl jq gh xmllint ; do
	command -v $tool > /dev/null || fail "$tool is required to build the release notes"
done
gh auth status --hostname github.com > /dev/null 2>&1 \
	|| fail "The GitHub CLI is not logged in to github.com, run: gh auth login"

if [ -z "$version" ] ; then
	current_version=$(getProjectVersionFromPom)
	major_version=$(expr $current_version : '\(.*\)\..*\..*\-.*')
	minor_version=$(expr $current_version : '.*\.\(.*\)\..*\-.*')
	version="$major_version.$minor_version.0"
fi
major_version=${version%%.*}

if git rev-parse -q --verify "refs/tags/rel/wicket-$version" > /dev/null ; then
	until_ref="rel/wicket-$version"
else
	until_ref=HEAD
fi

if [ -z "$since_ref" ] ; then
	previous_version=$( \
		{ git tag -l 'rel/wicket-*' | sed -e 's|^rel/wicket-||' ; echo "$version" ; } \
		| sort -V -u \
		| awk -v version="$version" '$0 == version { print previous; exit } { previous = $0 }' \
		)
	[ -n "$previous_version" ] \
		|| fail "No release tag found before $version: fetch the tags (git fetch --tags) or use --since"
	since_ref="rel/wicket-$previous_version"
fi
git rev-parse -q --verify "$since_ref^{commit}" > /dev/null || fail "Unknown git reference: $since_ref"

printf "%s" "
Apache Wicket release notes generator
=====================================
This tool collects the issues resolved in the upcoming release from JIRA
and GitHub, and merges them with the existing CHANGELOG in a text format.

Version to collect the release notes for: $version
Commits searched for GitHub issues:       $since_ref..$until_ref

Press <enter> to continue "

read

git status --porcelain CHANGELOG-$major_version.x | grep -q "CHANGELOG-$major_version.x"
if [ $? -eq 0 ] ; then
	fail "You already have changes in the CHANGELOG-$major_version.x
"
fi

grep -q "$version\$" CHANGELOG-$major_version.x 2> /dev/null
if [ $? -eq 0 ] ; then
	fail "You already have added release notes for this version to the changelog.
"
fi

issues=/tmp/release-issues-$version.tsv
: > $issues

echo "
Collecting the resolved issues -- making web requests"
printf "  - retrieving JIRA issues fixed in %s: " "$version"
jira_versions=$(curl -sf "$jira_url/project/$jira_project/versions") \
	|| fail "Unable to retrieve the versions from JIRA"
if jq -e --arg version "$version" 'any(.[]; .name == $version)' <<< "$jira_versions" > /dev/null ; then
	jira_issues=$(getJiraIssues "project = $jira_project AND fixVersion = \"$version\" AND resolution = Fixed") \
		|| exit 1
	[ -n "$jira_issues" ] && echo "$jira_issues" >> $issues
	echo "$jira_issues" | grep -c .
else
	echo "none, JIRA has no version $version"
fi

printf "  - retrieving JIRA issues referenced since %s: " "$since_ref"
jira_keys=$(comm -23 <(getJiraKeys "$since_ref..$until_ref") <(getJiraKeys "$since_ref") | paste -s -d , -)
if [ -n "$jira_keys" ] ; then
	jira_issues=$(getJiraIssues "key in ($jira_keys) AND (resolution is EMPTY OR resolution = Fixed)") \
		|| exit 1
	[ -n "$jira_issues" ] && echo "$jira_issues" >> $issues
	echo "$jira_issues" | grep -c .
else
	echo 0
fi

printf "  - retrieving GitHub issues referenced since %s: " "$since_ref"
github_issues=$(getReferencedGitHubIssues "$since_ref..$until_ref") || exit 1
[ -n "$github_issues" ] && echo "$github_issues" >> $issues
echo "$github_issues" | grep -c .

printf "  - looking up GitHub copies of the JIRA issues: "
titles=$(gh issue list --repo $github_repository --state all --limit 1000 \
	--search "$jira_project in:title" --json number,title,state) \
	|| fail "Unable to retrieve the GitHub copies of the JIRA issues"
copies=$(jq -r --arg project "$jira_project" '
		.[]
		| . as $issue
		| .title
		| [scan($project + "-[0-9]+")]
		| unique[]
		| [., "GH-\($issue.number)", $issue.state, $issue.title]
		| join("\t")' <<< "$titles" \
	| grep -F -f <(awk -F '\t' '$2 == 1 { print $4 "\t" }' $issues))
echo "$copies" | grep -c .

# a copy is listed under the key of its JIRA issue
if [ -n "$copies" ] ; then
	grep -v -F -f <(awk -F '\t' '{ print "\t" $2 "\t" }' <<< "$copies") $issues > $issues.tmp
	mv $issues.tmp $issues
fi
open_copies=$(awk -F '\t' '$3 == "OPEN" { print $2 "\t" $4 }' <<< "$copies")
open_issues=$(awk -F '\t' '$2 == 2 && $6 == "OPEN" { print $4 "\t" $5 }' $issues)

[ -s $issues ] || fail "
No resolved issues found for $version in JIRA or GitHub"

awk -F '\t' '
	BEGIN {
		count = split("Sub-task,Bug,New Feature,Improvement,Test,Wish,Task", groups, ",")
		for (i = 1; i <= count; i++) order[groups[i]] = i
	}
	{ print ($1 in order ? order[$1] : ($1 == "Other" ? 99 : 50)) "\t" $0 }' $issues \
	| sort -t $'\t' -u -k1,1n -k2,2 -k3,3n -k4,4n \
	| awk -F '\t' -v version="$version" '
		BEGIN { print ""; print "Release Notes - Wicket - Version " version; print "" }
		$2 != group { if (group != "") print ""; print "** " $2; print ""; group = $2 }
		{ print "    * [" $5 "] - " $6 }' > /tmp/release-notes-$version.txt

untyped=$(grep -c $'^Other\t' $issues)
if [ -n "$open_copies" ] || [ -n "$open_issues" ] || [ "$untyped" -gt 0 ] ; then
	echo ""
	if [ "$untyped" -gt 0 ] ; then
		echo "GitHub issues without an issue type: $untyped. They are listed under 'Other'; set
their type (Bug, Feature or Task) on GitHub and run this tool again to group them."
		echo ""
	fi
	if [ -n "$open_issues" ] ; then
		echo "These GitHub issues are fixed in this release, but still open. Close them once every
branch that needs the fix has it:
"
		echo "$open_issues" | sed -e 's/^/    /'
		echo ""
	fi
	if [ -n "$open_copies" ] ; then
		echo "These GitHub issues copy a JIRA issue fixed in this release, and are listed under its
key. They are still open; close them once every branch that needs the fix has it:
"
		echo "$open_copies" | sed -e 's/^/    /'
		echo ""
	fi
	printf "Press <enter> to continue, or Ctrl-C to stop "
	read
fi

printf "  - merging release notes into changelog: "

echo "This file contains all changes done in releases for Apache Wicket $major_version.x.

=======================================================================
$(cat /tmp/release-notes-$version.txt)

=======================================================================
$(tail -n +4 CHANGELOG-$major_version.x 2> /dev/null)
" > /tmp/changelog-$version.txt
cp /tmp/changelog-$version.txt CHANGELOG-$major_version.x

echo "done
"
git add CHANGELOG-$major_version.x
git diff --cached

echo ""

git commit -m "Added CHANGELOG for release $version"

echo "
The CHANGELOG-$major_version.x file has been updated and committed.

To revert the changes to the CHANGELOG:

    git reset HEAD^ --hard

Have fun!
"

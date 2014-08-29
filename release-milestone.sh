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
	if [ -f $log ] ; then
		echo ""
		cat $log
	fi
	exit
}

function setup_gpg {

	if [ "$agentcount" -ne 1 ]; then
		echo "Found gpg-agent running, killing all agents"
		killall gpg-agent
	fi

	echo ""
	echo "You are asked twice for your passphrase, one for scripting purposes, and one "
	echo "for gpg-agent using pinentry such that gpg and git are able to sign things."
	echo ""
	echo "Enter your GPG passphrase (input will be hidden) \c"
	stty_orig=`stty -g` 
	stty -echo 
	read passphrase
	stty $stty_orig

	# test the GPGP passphrase to fail-fast:
	echo "$passphrase" | gpg --passphrase-fd 0 --armor --output pom.xml.asc --detach-sig pom.xml
	gpg --verify pom.xml.asc
	if [ $? -ne 0 ]; then
	        echo "It appears that you fat-fingered your GPG passphrase"
			rm pom.xml.asc
	        exit $?
	fi
	rm pom.xml.asc

	echo "Starting new gpg-agent"
	eval $(gpg-agent --daemon --pinentry-program $(which pinentry))
	if [ $? -ne 0 ] ; then
		fail "ERROR: Unable to start gpg-agent"
	fi
	gpg --armor --detach-sign --use-agent --sign pom.xml >& $log
	if [ $? -ne 0 ] ; then
		fail "ERROR: Unable to run gpg properly"
	fi

	gpg --verify pom.xml.asc >& $log
	if [ $? -ne 0 ]; then
		rm pom.xml.asc
	    fail "It appears that you fat-fingered your GPG passphrase"
	fi
	rm pom.xml.asc
}

function getVersion {
	cat << EOF | xmllint --noent --shell pom.xml | grep content | cut -f2 -d=
setns pom=http://maven.apache.org/POM/4.0.0
xpath /pom:project/pom:version/text()
EOF
}

# set -e

echo "
Apache Wicket Release script
----------------------------
Building a release for Apache Wicket.

This script assumes you are running on OS X, it hasn't been tested on any other
operating systems, and you can bet it won't work on Windows...

REQUIREMENTS:

 - a pure JDK 7 environment, JDK 8 or newer won't cut it
 - Maven 3.2.3
 - gpg, gpg-agent and pinentry for signing"

export JAVA_HOME=`/usr/libexec/java_home -v1.7`
echo "
Current Java version is: $(java -version 2>&1 | tail -n 2 | head -n 1)
"

agentcount=`ps aux|grep gpg-agent|wc -l`

current_version=$(getVersion)
major_version=$(expr $current_version : '\(.*\)\..*\..*\-SNAPSHOT')
minor_version=$(expr $current_version : '.*\.\(.*\)\..*\-SNAPSHOT')
bugfix_version=$(expr $current_version : '.*\..*\.\(.*\)-SNAPSHOT')

read -p "What is the version to be released? " version

current_milestone_version=$(expr $version : '.*-M(\d+)')

#previous_version="$major_version.$(expr $minor_version - 1).0"
previous_version="7.0.0-M$(expr $current_milestone_version - 1)"

log=/tmp/wicketrelease-$version.out

branch="build/wicket-$version"
tag="wicket-$version"

grep -q "$version\$" CHANGELOG-7.x
if [ $? -ne 0 ] ; then
	fail "
You have forgotten to add the closed tickets for Wicket $version to the CHANGELOG-7.x file

Go to https://issues.apache.org/jira/secure/ConfigureReleaseNote.jspa?projectId=12310561
and export the issues to the changelog.
"
fi

read -p "
This script will release version: Apache Wicket $version and continue 
development with 7.0.0-SNAPSHOT

Press enter to continue or CTRL-C to abort"

# setup_gpg

echo "Ensuring we are starting from wicket-7.x"
# otherwise we can't remove a previous release branch that failed
git checkout master

echo "Cleaning up any release artifacts that might linger"
mvn -q release:clean

echo "Removing previous release tag $tag (if exists)"
oldtag=`git tag -l |grep -e "$tag"|wc -l`
[ "$oldtag" -ne 0 ] && git tag -d $tag

echo "Removing previous build branch $branch (if exists)"
oldbranch=`git branch |grep -e "$branch"|wc -l`
[ "$oldbranch" -ne 0 ] && git branch -D $branch

git checkout -b $branch

echo "# Release configuration for Wicket-$version
scm.tag=${tag}
" > release.properties

./release-milestone.py $version 7.0.0-SNAPSHOT >> release.properties

cat ./release.properties

# Clear the current NOTICE.txt file
echo "Creating notice file."

NOTICE=NOTICE

echo "Apache Wicket
Copyright 2006-$(date +%Y) The Apache Software Foundation

This product includes software developed at
The Apache Software Foundation (http://www.apache.org/).

This is an aggregated NOTICE file for the Apache Wicket projects included
in this distribution.

NB: DO NOT ADD LICENSES/NOTICES/ATTRIBUTIONS TO THIS FILE, BUT IN THE
    NOTICE FILE OF THE CORRESPONDING PROJECT. THE RELEASE PROCEDURE WILL
    AUTOMATICALLY INCLUDE THE NOTICE IN THIS FILE.

" > $NOTICE

# next concatenate all NOTICE files from sub projects to the root file
for i in `find . -name "NOTICE" -not -regex ".*/target/.*" -not -regex "./NOTICE"`
do
	echo "---------------------------------------------------------------------------" >> $NOTICE
	echo "src/"$i | sed -e "s/\/src.*//g" >> $NOTICE
	echo "---------------------------------------------------------------------------" >> $NOTICE
	cat $i >> $NOTICE
	echo >> $NOTICE
done

echo "Fixing the quickstart to use the correct wicket version"
sed -e "s/\<wicket\.version\>.*\<\/wicket\.version\>/\<wicket.version\>$version\<\/wicket.version\>/g" -i "" archetypes/quickstart/src/main/resources/archetype-resources/pom.xml

echo "Committing changes"
git commit -am "Changes to notice files and quickstart archetype"

# clean all projects
echo "Clean all projects"
mvn -q clean -Pall

# package and assemble the release
echo "Prepare the release"
mvn --batch-mode release:prepare -l $log -DpreparationGoals="clean" -Dtag=$tag -Papache-release 
if [ $? -ne 0 ] ; then
	fail "ERROR: mvn release:prepare was not successful"
fi

# this needs to be done before signing the tag otherwise the snapshot version
# is tagged
#echo "Rollback the last commit of the release plugin"
#git reset HEAD^ --hard >> $log

#echo "Sign the tag"
# TODO the git tag --sign doesn't utilize the gpg-agent for some reason
#git tag --sign --force --message "Signed release tag for Apache Wicket $version" $tag >> $log

echo "Performing the release using Maven"
mvn -Dgpg.passphrase="$passphrase" -ff -l $log release:perform -DlocalCheckout=true -Dtag=$tag -Papache-release
if [ $? -ne 0 ] ; then
	fail "ERROR: mvn release:perform was not successful"
fi

stagingrepoid=$(mvn org.sonatype.plugins:nexus-staging-maven-plugin:LATEST:rc-list -DnexusUrl=https://repository.apache.org -DserverId=apache.releases.https | grep -v "CLOSED" | grep -Eo "(orgapachewicket-\d+)";)

echo "Closing staging repository with id $stagingrepoid"

mvn org.sonatype.plugins:nexus-staging-maven-plugin:LATEST:rc-close -DstagingRepositoryId=$stagingrepoid -DnexusUrl=https://repository.apache.org -DserverId=apache.releases.https -Ddescription="Release has been built, awaiting vote"


echo "Create and sign the source tarballs"

mkdir -p target/dist/binaries

git archive --format=tar.gz --prefix=apache-wicket-$version/ -o target/dist/apache-wicket-$version.tar.gz $tag
git archive --format=zip --prefix=apache-wicket-$version/ -o target/dist/apache-wicket-$version.zip $tag
gpg --armor --detach-sign --use-agent --sign target/dist/apache-wicket-$version.tar.gz
gpg --armor --detach-sign --use-agent --sign target/dist/apache-wicket-$version.zip
gpg --print-md SHA1 target/dist/apache-wicket-$version.tar.gz > target/dist/apache-wicket-$version.tar.gz.sha
gpg --print-md MD5  target/dist/apache-wicket-$version.tar.gz > target/dist/apache-wicket-$version.tar.gz.md5
gpg --print-md SHA1 target/dist/apache-wicket-$version.zip > target/dist/apache-wicket-$version.zip.sha
gpg --print-md MD5  target/dist/apache-wicket-$version.zip > target/dist/apache-wicket-$version.zip.md5

echo "Create and sign the binaries"
mkdir target/apache-wicket-$version-bin
pushd target/apache-wicket-$version-bin
find ../checkout ! \( -type d -name "WEB-INF" -prune \) -regex ".*wicket-.*.[jw]ar" ! -name "*-sources*" ! -name "*-javadoc*" ! -name "*wicket-archetype-quickstart*" ! -name "wicket-common-tests*"  -type f -exec cp {} . \;
find ../checkout ! \( -type d -name "WEB-INF" -prune \) -regex ".*wicket-.*.[jw]ar\.asc" ! -name "*-sources*" ! -name "*-javadoc*" ! -name "*wicket-archetype-quickstart*" ! -name "wicket-common-tests*"  -type f -exec cp {} . \;
cp ../../LICENSE .
cp ../../README .
cp ../../NOTICE .
cp ../../CHANGELOG* .
popd

pushd target

tar cfz dist/binaries/apache-wicket-$version-bin.tar.gz apache-wicket-$version-bin
zip -r dist/binaries/apache-wicket-$version-bin.zip apache-wicket-$version-bin
gpg --armor --detach-sign --use-agent --sign dist/binaries/apache-wicket-$version-bin.tar.gz
gpg --armor --detach-sign --use-agent --sign dist/binaries/apache-wicket-$version-bin.zip
gpg --print-md SHA1 dist/binaries/apache-wicket-$version-bin.tar.gz > dist/binaries/apache-wicket-$version-bin.tar.gz.sha
gpg --print-md MD5  dist/binaries/apache-wicket-$version-bin.tar.gz > dist/binaries/apache-wicket-$version-bin.tar.gz.md5
gpg --print-md SHA1 dist/binaries/apache-wicket-$version-bin.zip > dist/binaries/apache-wicket-$version-bin.zip.sha
gpg --print-md MD5  dist/binaries/apache-wicket-$version-bin.zip > dist/binaries/apache-wicket-$version-bin.zip.md5
popd

echo "Uploading release"
pushd target/dist
svn mkdir https://dist.apache.org/repos/dist/dev/wicket/$version -m "Create $version release staging area"
svn co --force --depth=empty https://dist.apache.org/repos/dist/dev/wicket/$version .
cp ../../CHANGELOG* .
svn add *
svn commit -m "Upload wicket-$version to staging area"
popd

echo "Generating Vote email"

echo "This is a vote to release Apache Wicket $version

Please download the source distributions found in our staging area
linked below.

I have included the signatures for both the source archives. This vote
lasts for 72 hours minimum.

[ ] Yes, release Apache Wicket $version
[ ] No, don't release Apache Wicket $version, because ...

Distributions, changelog, keys and signatures can be found at:

    https://dist.apache.org/repos/dist/dev/wicket/$version

Staging repository:

    https://repository.apache.org/content/repositories/$stagingrepoid/

The binaries are available in the above link, as are a staging
repository for Maven. Typically the vote is on the source, but should
you find a problem with one of the binaries, please let me know, I can
re-roll them some way or the other.

========================================================================

The signatures for the source release artefacts:

" > release-vote.txt

pushd target/dist > /dev/null
for i in apache-wicket*{zip,tar.gz}
do
	echo "Signature for $i:

$(cat $i.asc)
" >> ../../release-vote.txt
done
popd > /dev/null

echo "========================================================================

CHANGELOG for $version:
" >> release-vote.txt

awk "/Release Notes - Wicket - Version $version/{flag=1;next} /==================/{flag=0} flag { print }" CHANGELOG-7.x >> release-vote.txt


# do the same for the original snapshot version, as our maven release
# plugin friend doesn't do that for us in the dependency management section

mvn_version_to_replace="$major_version.$minor_version.1-SNAPSHOT"
mvn_version_to_replace2="$major_version.$minor_version.0-SNAPSHOT"
next_dev_version="7.0.0-SNAPSHOT"

echo "
The release has been created. It is up to you to check if the release is up
to par, and perform the following commands yourself when you start the vote
to enable future development during the vote and after.

A vote email has been generated in release-vote.txt, you can copy/paste it using:

    cat release-vote.txt | pbcopy

You can find the distribution in target/dist

    cd target/dist

To verify all signatures:

    find . -name \"*.asc\" -exec gpg --verify {} \; 

To push the release branch to ASF git servers

    git push origin $branch:refs/heads/$branch

To move the release from staging to the mirrors:

    svn mv https://dist.apache.org/repos/dist/dev/wicket/$version https://dist.apache.org/repos/dist/release/wicket -m \"Upload release to the mirrors\"

Remove previous version $previous_version from the mirrors

    svn rm https://dist.apache.org/repos/dist/release/wicket/$previous_version -m \"Remove previous version from mirrors\"

To sign the release tag issue the following three commands: 

    git checkout $tag
    git tag --sign --force --message \"Signed release tag for Apache Wicket $version\" $tag >> $log
    git checkout $branch

To release the Maven artefacts:

	mvn org.sonatype.plugins:nexus-staging-maven-plugin:LATEST:rc-release -DstagingRepositoryId=$stagingrepoid -DnexusUrl=https://repository.apache.org -DserverId=apache.releases.https -Ddescription=\"Release vote has passed\"

Or in case of a failed vote, to drop the staging repository:

	mvn org.sonatype.plugins:nexus-staging-maven-plugin:LATEST:rc-drop -DstagingRepositoryId=$stagingrepoid -DnexusUrl=https://repository.apache.org -DserverId=apache.releases.https -Ddescription=\"Release vote has failed\"
" > release.txt

cat release.txt


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

log=/tmp/wicketrelease.out

function fail {
	echo "$1"
	if [ -f $log ] ; then
		echo ""
		cat $log
	fi
	exit
}

function setup_gpg {
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

echo "Apache Wicket Release script"
echo "----------------------------"
echo "Building a release for Apache Wicket."
echo ""
echo "This script assumes you are running on OS X, it hasn't been tested on any other"
echo "operating systems, and you can bet it won't work on Windows..."
echo ""
echo "REQUIREMENTS:"
echo ""
echo " - a pure JDK 6 environment, JDK 7 or newer won't cut it"
echo " - Maven 3.0.4 (older releases are b0rked, just don't bother)"
echo " - gpg, gpg-agent and pinentry for signing"
echo ""

agentcount=`ps aux|grep gpg-agent|wc -l`

current_version=$(getVersion)
major_version=$(expr $current_version : '\(.*\)\..*\..*\-SNAPSHOT')
minor_version=$(expr $current_version : '.*\.\(.*\)\..*\-SNAPSHOT')
bugfix_version=$(expr $current_version : '.*\..*\.\(.*\)-SNAPSHOT')
version="$major_version.$minor_version.0"
echo "This script will release version: Apache Wicket $version"
echo ""
echo "Press enter to continue or CTRL-C to abort \c"
read 

branch="build/wicket-$version"
tag="wicket-$version"

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

setup_gpg

echo "Ensuring we are starting from master"
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

# Clear the current NOTICE.txt file
echo "Creating notice file."

NOTICE=NOTICE
> $NOTICE 
echo "Apache Wicket" >> $NOTICE
echo "Copyright 2006-$(date +%Y) The Apache Software Foundation" >> $NOTICE
echo "" >> $NOTICE
echo "This product includes software developed at" >> $NOTICE
echo "The Apache Software Foundation (http://www.apache.org/)." >> $NOTICE
echo "" >> $NOTICE
echo "This is an aggregated NOTICE file for the Apache Wicket projects included" >> $NOTICE
echo "in this distribution." >> $NOTICE
echo "" >> $NOTICE
echo "NB: DO NOT ADD LICENSES/NOTICES/ATTRIBUTIONS TO THIS FILE, BUT IN THE" >> $NOTICE
echo "    NOTICE FILE OF THE CORRESPONDING PROJECT. THE RELEASE PROCEDURE WILL" >> $NOTICE
echo "    AUTOMATICALLY INCLUDE THE NOTICE IN THIS FILE." >> $NOTICE
echo "" >> $NOTICE

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
mvn --batch-mode release:prepare -DpreparationGoals="clean" -Dtag=$tag
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
mvn -Dgpg.passphrase="$passphrase" -ff -l $log release:perform -DlocalCheckout=true -Dtag=$tag
if [ $? -ne 0 ] ; then
	fail "ERROR: mvn release:perform was not successful"
fi

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
svn export http://svn.apache.org/repos/asf/wicket/common/KEYS KEYS
cp ../../CHANGELOG* .
ssh people.apache.org mkdir -p public_html/wicket-$version
scp -r * people.apache.org:public_html/wicket-$version/
popd


echo ""
echo "The release has been created. It is up to you to check if the release is up"
echo "to par, and perform the following commands yourself when you start the vote"
echo "to enable future development during the vote and after."
echo ""
echo "You can find the distribution in target/dist"
echo ""
echo "    cd target/dist"
echo ""
echo "To verify all signatures:"
echo ""
echo "    find . -name \"*.asc\" -exec gpg --verify {} \; "
echo ""
echo "To push the release branch to ASF git servers"
echo ""
echo "    git push origin $branch:refs/heads/$branch"
echo ""

echo "To sign the release tag issue the following three commands: "
echo ""
echo "    git checkout $tag"
echo "    git tag --sign --force --message \"Signed release tag for Apache Wicket $version\" $tag >> $log"
echo "    git checkout $branch"
echo ""

mvn_version_to_replace="$major_version.$minor_version.1-SNAPSHOT"
next_dev_version="$major_version.$(expr $minor_version + 1).0-SNAPSHOT"

echo "To renumber the next development iteration $next_dev_version:"
echo ""
echo "    git checkout master"
echo "    mvn release:update-versions --batch-mode"
echo "    find . ! \\( -type d -name \"target\" -prune \\) -name pom.xml -exec sed -i \"\" -E \"s/$mvn_version_to_replace/$next_dev_version/g\" {} \\;"
# do the same for the original snapshot version, as our maven release
# plugin friend doesn't do that for us in the dependency management section
mvn_version_to_replace="$major_version.$minor_version.0-SNAPSHOT"
echo "    find . ! \\( -type d -name \"target\" -prune \\) -name pom.xml -exec sed -i \"\" -E \"s/$mvn_version_to_replace/$next_dev_version/g\" {} \\;"
echo "    git add \`find . ! \\( -type d -name \"target\" -prune \\) -name pom.xml\`"
echo "    git commit -m \"Start next development version\""
echo "    git push"
echo ""


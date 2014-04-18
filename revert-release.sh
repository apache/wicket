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

function getVersion {
	cat << EOF | xmllint --noent --shell pom.xml | grep content | cut -f2 -d=
setns pom=http://maven.apache.org/POM/4.0.0
xpath /pom:project/pom:version/text()
EOF
}

# current_version=$(getVersion)
# major_version=$(expr $current_version : '\(.*\)\..*\..*\-SNAPSHOT')
# minor_version=$(expr $current_version : '.*\.\(.*\)\..*\-SNAPSHOT')
# bugfix_version=$(expr $current_version : '.*\..*\.\(.*\)-SNAPSHOT')
# version="$major_version.$minor_version.0"

read -p "Revert which version? " version

echo ""
echo "Revert the current in-progress release for apache-wicket-$version"

echo ""
echo "Press enter to continue or CTRL-C to abort \c"
read 

branch="build/wicket-$version"
tag="wicket-$version"

git checkout master
git branch -D $branch
git tag -d $tag

svn rm https://dist.apache.org/repos/dist/dev/wicket/$version -m "Reverting release $version"

find . -name "*.releaseBackup" -exec rm {} \;
rm release.properties release.txt > /dev/null

echo ""
echo "Cleaned up the release"
echo ""
echo "Don't forget to drop the Maven staging repository"
echo ""

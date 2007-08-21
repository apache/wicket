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
echo "Apache Wicket Release script"
echo "----------------------------"
echo "Building a release for Apache Wicket. We will need the passphrase for"
echo "GPG to sign the release."
echo "This program assumes you have a 'mvn4' script that starts Maven 2 using"
echo "a Java 1.4 development kit to compile the projects that are JDK-1.4 based."
echo ""

echo "Enter your GPG passphrase (input will be hidden)"
stty_orig=`stty -g` 
stty -echo 
read passphrase
stty $stty_orig

# Clear the current NOTICE.txt file
NOTICE=NOTICE
> $NOTICE 
echo "Apache Wicket" >> $NOTICE
echo "Copyright 2007 The Apache Software Foundation" >> $NOTICE
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
for i in `find jdk* -name "NOTICE" -not -regex ".*/target/.*"`
do
	echo "---------------------------------------------------------------------------" >> $NOTICE
	echo "src/"$i | sed -e "s/\/src.*//g" >> $NOTICE
	echo "---------------------------------------------------------------------------" >> $NOTICE
	cat $i >> $NOTICE
	echo >> $NOTICE
done

# clean all projects
mvn clean -Pall

# compile the JDK-1.4 projects
mvn4 -ff -Pjdk-1.4 test $1

# package and assemble the release
mvn -ff -Dgpg.passphrase=$passphrase -Prelease deploy assembly:attached $1

filename=`ls target/dist/apache-wicket*gz`
gpg --print-md MD5 $filename > $filename.md5
gpg --print-md SHA1 $filename > $filename.sha
echo $passphrase | gpg --passphrase-fd 0 --armor --output $filename.asc --detach-sig $filename

filename=`ls target/dist/apache-wicket*zip`
gpg --print-md MD5 $filename > $filename.md5
gpg --print-md SHA1 $filename > $filename.sha
echo $passphrase | gpg --passphrase-fd 0 --armor --output $filename.asc --detach-sig $filename

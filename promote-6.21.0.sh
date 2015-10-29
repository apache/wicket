#!/bin/sh
echo -n "Promoting release 6.21.0

Actions about to be performed:
------------------------------

$(cat $0 | tail -n +14)

------------------------------------------
Press enter to continue or CTRL-C to abort"

read

# push the build branch to ASF git repo

git push origin build/wicket-6.21.0:refs/heads/build/wicket-6.21.0

# push the release tag to ASF git repo

git push origin wicket-6.21.0

# promote the source distribution by moving it from the staging area to the release area

svn mv https://dist.apache.org/repos/dist/dev/wicket/6.21.0 https://dist.apache.org/repos/dist/release/wicket -m "Upload release to the mirrors"

mvn org.sonatype.plugins:nexus-staging-maven-plugin:LATEST:rc-release -DstagingRepositoryId=orgapachewicket-1054 -DnexusUrl=https://repository.apache.org -DserverId=apache.releases.https -Ddescription="Release vote has passed"

# Renumber the next development iteration 6.22.0-SNAPSHOT:

git checkout wicket-6.x
mvn release:update-versions --batch-mode
find . ! ( -type d -name "target" -prune ) -name pom.xml -exec sed -i "" -E "s/6.21.1-SNAPSHOT/6.22.0-SNAPSHOT/g" {} ;
find . ! ( -type d -name "target" -prune ) -name pom.xml -exec sed -i "" -E "s/6.21.1-SNAPSHOT/6.22.0-SNAPSHOT/g" {} ;
git add ` find . ! ( -type d -name "target" -prune ) -name pom.xml `
git commit -m "Start next development version"
git push

echo "Remove the previous version of Wicket using this command:

  svn rm https://dist.apache.org/repos/dist/release/wicket/6.20.0 -m "Remove previous version from mirrors"

"

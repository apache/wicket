#!/usr/bin/python
#
# prints a release.properties file for instructing the Maven Release Plugin
# to generate the proper release artefacts without having to manually version
# everything.
#
# Usage:
#
#     build-versions.py <release-version> <dev-version>
#
# This will generate a release.properties file that will release the
# release-version, and will continue development on dev-version.
#
# Example:
#
#    build-milestone.py 7.0.0-M1 7.0.0-SNAPSHOT
#

import sys
import xml.etree.ElementTree as ET
import re

relVersions = list()
devVersions = list()

NS = {"maven":"http://maven.apache.org/POM/4.0.0"}

def get(pom, tag):
    return list(pom.iterfind('maven:' + tag, NS))[0].text

def addVersions(groupId, artifactId, releaseVersion, developVersion):
    relVersions.append("project.rel." + groupId + "\\:" + artifactId + "=" + releaseVersion)
    devVersions.append("project.dev." + groupId + "\\:" + artifactId + "=" + developVersion)

def determineNextWicketVersion():
    # Regular expression for matching a semver version identifier
    SV = re.compile(
        "^(?P<major>[0-9]+)\."
        "(?P<minor>[0-9]+)\."
        "(?P<patch>[0-9]+)"
        "(?:-(?P<prerel>[0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?"
        "(?:\+(?P<build>[0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?$")

    pom = ET.parse("pom.xml")

    if get(pom, 'groupId') == 'org.apache.wicket' and get(pom, 'artifactId') == 'wicket-parent':
        projectVersion = list(pom.iterfind('maven:version', NS))[0].text
        versionInfo = SV.match(projectVersion).groupdict();
        releaseVersion = "" + versionInfo["major"] + "." + versionInfo["minor"] + "." + versionInfo["patch"]
        developVersion = "" + versionInfo["major"] + "." + str(int(versionInfo["minor"]) + 1) + "." + versionInfo["patch"] + "-SNAPSHOT"
        
        return releaseVersion, developVersion
    else:
        print >> sys.stderr, "This script can only be run in the folder of the wicket-parent pom"
        sys.exit(1)

def determineNextExperimentalVersion(module):
    # Regular expression for matching experimental version identifier that don't
    # conform to the semver standard of x.y.z versioning.
    EV = re.compile(
        "^0\."
        "(?P<minor>[0-9]+)"
        "-SNAPSHOT$")

    pom = ET.parse("wicket-experimental/" + module + "/pom.xml")

    if get(pom, 'artifactId') == module:
        projectVersion = list(pom.iterfind('maven:version', NS))[0].text
        versionInfo = EV.match(projectVersion).groupdict();
        releaseVersion = "0." + versionInfo["minor"]
        developVersion = "0." + str(int(versionInfo["minor"]) + 1) + "-SNAPSHOT"
        
        return releaseVersion, developVersion
    else:
        print >> sys.stderr, "This function only works for experimental wicket 6 modules"
        sys.exit(1)

def getModulesFromParent(parentPomFile):
    pom = ET.parse(parentPomFile)

    res = list()
    modules = pom.findall('maven:modules/maven:module', NS)
    for module in modules :
        # rebuild the module name for quickstart and the testing/ projects
        # because in the generated properties file, they have the same groupId
        # as wicket-core and wicket-parent (are not under a sub-groupId)
        res.append(module.text.replace("testing/", "").replace("archetypes/quickstart", "wicket-archetype-quickstart"))
    return res

#
# All wicket core projects have the same groupId and version, and they are only
# specified in the parent POM. Therefore we need to generate for each normal
# module lines that upgrade their versions.
#
wicketReleaseVersion, wicketDevelopVersion = determineNextWicketVersion()
    
print "# " + wicketReleaseVersion + "-SNAPSHOT -> " + wicketReleaseVersion + " -> " + wicketDevelopVersion

modules = getModulesFromParent("pom.xml")
modules.insert(0, 'wicket-parent')

for module in modules:
    addVersions('org.apache.wicket', module, wicketReleaseVersion, wicketDevelopVersion)

#
# Experimental modules are versioned independently, so we need to grab the
# version from each POM and update that specifically. This will fail when we
# get a multi-module experimental project, but until then, this suffices.
#

modules = getModulesFromParent("wicket-experimental/pom.xml")
for module in modules:
    releaseVersion, developVersion = determineNextExperimentalVersion(module)
    addVersions('org.apache.wicket.experimental.wicket6', module, releaseVersion, developVersion)

for version in relVersions:
    print version

print

for version in devVersions:
    print version

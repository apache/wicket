#!/usr/bin/env python3
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
#    build-versions.py 7.0.0-M1 7.0.0-SNAPSHOT
#

import os
import sys
import xml.etree.ElementTree as ET

NS = {"pom": "http://maven.apache.org/POM/4.0.0"}

if len(sys.argv) != 3:
    print("Usage: %s <release-version> <dev-version>" % sys.argv[0])
    sys.exit(1)

relVersion = sys.argv[1]
devVersion = sys.argv[2]

relVersions = []
devVersions = []

def addVersions(groupId, artifactId):
    relVersions.append("project.rel." + groupId + "\\:" + artifactId + "=" + relVersion)
    devVersions.append("project.dev." + groupId + "\\:" + artifactId + "=" + devVersion)

def addModule(directory):
    pom = ET.parse(os.path.join(directory, "pom.xml")).getroot()

    groupId = pom.findtext("pom:groupId", namespaces=NS) or pom.findtext("pom:parent/pom:groupId", namespaces=NS)
    addVersions(groupId, pom.findtext("pom:artifactId", namespaces=NS))

    for module in pom.iterfind(".//pom:modules/pom:module", NS):
        addModule(os.path.join(directory, module.text))

addModule(".")

for version in relVersions:
    print(version)

print()

for version in devVersions:
    print(version)

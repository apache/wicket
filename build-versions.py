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
# release-version, and will continue development on dev-version. A module
# with a version of its own, such as the experimental modules, is released
# at that version without -SNAPSHOT and continues on the next one.
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

def ownVersions(version):
    release = version[:-len("-SNAPSHOT")] if version.endswith("-SNAPSHOT") else version
    parts = release.split(".")
    parts[-1] = str(int(parts[-1]) + 1)
    return release, ".".join(parts) + "-SNAPSHOT"

def addVersions(groupId, artifactId, version):
    rel, dev = (relVersion, devVersion) if version == projectVersion else ownVersions(version)
    relVersions.append("project.rel." + groupId + "\\:" + artifactId + "=" + rel)
    devVersions.append("project.dev." + groupId + "\\:" + artifactId + "=" + dev)

def addModule(directory):
    pom = ET.parse(os.path.join(directory, "pom.xml")).getroot()

    groupId = pom.findtext("pom:groupId", namespaces=NS) or pom.findtext("pom:parent/pom:groupId", namespaces=NS)
    version = pom.findtext("pom:version", namespaces=NS) or pom.findtext("pom:parent/pom:version", namespaces=NS)
    addVersions(groupId, pom.findtext("pom:artifactId", namespaces=NS), version)

    for module in pom.iterfind(".//pom:modules/pom:module", NS):
        addModule(os.path.join(directory, module.text))

projectVersion = ET.parse("pom.xml").getroot().findtext("pom:version", namespaces=NS)

addModule(".")

for version in relVersions:
    print(version)

print()

for version in devVersions:
    print(version)

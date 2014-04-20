#!/usr/bin/python
#
# prints a release.properties file for instructing the Maven Release Plugin
# to generate the proper release artefacts without having to manually version
# everything.
#
# Usage:
#
#     release-milestone.py <release-version> <dev-version>
#
# This will generate a release.properties file that will release the 
# release-version, and will continue development on dev-version.
#
# Example:
#
#    release-milestone.py 7.0.0-M1 7.0.0-SNAPSHOT
#

import sys
from xml.dom.minidom import parse

groupId = "org.apache.wicket"

if len(sys.argv) != 3:
    print "Usage: %s <release-version> <dev-version>" % sys.argv[0]
    sys.exit(1)

relVersion = sys.argv[1]
devVersion = sys.argv[2]

relVersions = []
devVersions = []

def addVersions(groupId, module):
    relVersions.append("project.rel." + groupId + "\\:" + module + "=" + relVersion)
    devVersions.append("project.dev." + groupId + "\\:" + module + "=" + devVersion)
    
def getModulesFromParent(parentPomFile):
    pom = parse(parentPomFile)

    for moduleTag in pom.getElementsByTagName('module'):
        module = moduleTag.childNodes[0].nodeValue.replace("testing/", "").replace("archetypes/quickstart", "wicket-archetype-quickstart")
        addVersions(groupId, module)

addVersions(groupId, "wicket-parent")
addVersions("org.apache.wicket.experimental.wicket7", "wicket-experimental")

getModulesFromParent("pom.xml")
getModulesFromParent("wicket-native-websocket/pom.xml")

for version in relVersions:
    print version

print

for version in devVersions:
    print version

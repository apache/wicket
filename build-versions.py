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

'''import sys
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
addVersions("org.apache.wicket.experimental.wicket8", "wicket-experimental")

getModulesFromParent("pom.xml")
getModulesFromParent("wicket-native-websocket/pom.xml")

for version in relVersions:
    print version

print

for version in devVersions:
    print version'''
import argparse
from xml.dom.minidom import parse

GROUP_ID = "org.apache.wicket"

def add_versions(group_id, module, rel_version, dev_version):
    return [
        f"project.rel.{group_id}\\:{m}={rel_version}"
        for m in module
    ], [
        f"project.dev.{group_id}\\:{m}={dev_version}"
        for m in module
    ]

def get_modules_from_parent(parent_pom_file):
    pom = parse(parent_pom_file)

    modules = [
        module_tag.childNodes[0].nodeValue.replace("testing/", "").replace("archetypes/quickstart", "wicket-archetype-quickstart")
        for module_tag in pom.getElementsByTagName('module')
    ]

    return modules

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Add versions to a POM file')
    parser.add_argument('release_version', type=str, help='the release version')
    parser.add_argument('dev_version', type=str, help='the development version')
    args = parser.parse_args()

    release_version = args.release_version
    dev_version = args.dev_version

    rel_versions, dev_versions = add_versions(
        GROUP_ID,
        ["wicket-parent", "wicket-experimental"] + get_modules_from_parent("pom.xml") + get_modules_from_parent("wicket-native-websocket/pom.xml"),
        release_version,
        dev_version
    )

    print("\n".join(rel_versions))
    print()
    print("\n".join(dev_versions))

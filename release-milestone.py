#!/usr/bin/python

import sys
from xml.dom.minidom import parse

pom = parse("pom.xml")

groupId = "org.apache.wicket"

relVersions = ["project.rel.org.apache.wicket\\:wicket-parent=" + sys.argv[1]]
devVersions = ["project.dev.org.apache.wicket\\:wicket-parent=7.0.0-SNAPSHOT"]

for moduleTag in pom.getElementsByTagName('module'):
    module = moduleTag.childNodes[0].nodeValue.replace("testing/", "").replace("archetypes/quickstart", "wicket-archetype-quickstart")
    relVersions.append("project.rel." + groupId + "\\:" + module + "=" + sys.argv[1])
    devVersions.append("project.dev." + groupId + "\\:" + module + "=7.0.0-SNAPSHOT")

# experimentalPom = parse("wicket-experimental/pom.xml")
# 
# for moduleTag in experimentalPom.getElementsByTagName('module'):
#     experimentalModuleName = moduleTag.childNodes[0].nodeValue    
# 
#     print "Parsing " + experimentalModuleName
# 
#     experimentalModulePom = parse("wicket-experimental/" + experimentalModuleName + "/pom.xml")
#     experimentalModuleProject = experimentalModulePom.getElementsByTagName('project')[0]
#     experimentalModuleParent = experimentalModulePom.getElementsByTagName('parent')[0]
#     experimentalModuleGroupId = experimentalModuleParent.getElementsByTagName('groupId')[0].childNodes[0].nodeValue
# 
#     experimentalModuleArtifactId = experimentalModuleProject.getElementsByTagName('artifactId')[1].childNodes[0].nodeValue
#     experimentalModuleVersion = experimentalModuleProject.getElementsByTagName('version')[0].childNodes[0].nodeValue
# 
#     newVersion = experimentalModuleVersion.replace("-SNAPSHOT", "")
#     
#     print experimentalModuleGroupId + ":" + experimentalModuleArtifactId + ":" + experimentalModuleVersion

for version in relVersions:
    print version

print

for version in devVersions:
    print version


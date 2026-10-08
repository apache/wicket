#!/usr/bin/env python3
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
"""Sanity-check the aggregated JaCoCo report before it is published.

Most Wicket tests live in a module other than the code they exercise, so the
report depends on the dependency scopes declared in wicket-coverage/pom.xml:
compile contributes classes, test contributes execution data only. If someone
overrides maven-surefire-plugin's <argLine> without keeping the
@{jacoco.argLine} placeholder, or changes those scopes, coverage silently drops
to zero instead of failing the build. These assertions are that tripwire.

This deliberately checks structure, not a coverage percentage. It is not a
quality gate: it only fails when the measurement itself is broken.
"""
import sys
import xml.etree.ElementTree as ET

REPORT = 'wicket-coverage/target/site/jacoco-aggregate/jacoco.xml'

# Every module listed at compile scope in wicket-coverage/pom.xml.
EXPECTED_MODULES = {
    'wicket-auth-roles', 'wicket-bean-validation', 'wicket-cdi', 'wicket-core',
    'wicket-devutils', 'wicket-extensions', 'wicket-extensions-tester',
    'wicket-guice', 'wicket-ioc', 'wicket-jmx', 'wicket-native-websocket-core',
    'wicket-native-websocket-javax', 'wicket-native-websocket-tester',
    'wicket-request', 'wicket-spring', 'wicket-tester', 'wicket-util',
    'wicket-velocity',
}

# Modules whose tests live elsewhere. Zero here means cross-module attribution
# has broken, which is the failure this script exists to catch.
MUST_BE_COVERED = ('wicket-core', 'wicket-tester', 'wicket-cdi')


def instructions(group):
    for counter in group.findall('counter'):
        if counter.get('type') == 'INSTRUCTION':
            return int(counter.get('missed')), int(counter.get('covered'))
    return 0, 0


def main():
    try:
        root = ET.parse(REPORT).getroot()
    except (OSError, ET.ParseError) as e:
        sys.exit('cannot read %s: %s' % (REPORT, e))

    groups = {g.get('name'): g for g in root.findall('group')}
    failures = []

    for name in sorted(groups):
        missed, covered = instructions(groups[name])
        total = missed + covered
        pct = (100.0 * covered / total) if total else 0.0
        print('%-34s %8d / %8d instructions  (%5.1f%%)' % (name, covered, total, pct))
    print()

    missing = EXPECTED_MODULES - set(groups)
    extra = set(groups) - EXPECTED_MODULES
    if missing:
        failures.append('missing from the report: %s' % ', '.join(sorted(missing)))
    if extra:
        failures.append('unexpectedly present: %s -- update EXPECTED_MODULES here and '
                        'the dependency list in wicket-coverage/pom.xml together'
                        % ', '.join(sorted(extra)))

    for name in MUST_BE_COVERED:
        if name in groups and instructions(groups[name])[1] == 0:
            failures.append('%s has zero coverage: its tests live in another module, so '
                            'this means the JaCoCo agent did not attach or a dependency '
                            'scope in wicket-coverage/pom.xml is wrong' % name)

    if failures:
        for f in failures:
            print('FAIL: %s' % f, file=sys.stderr)
        return 1
    print('OK: %d modules reported, cross-module attribution intact' % len(groups))
    return 0


if __name__ == '__main__':
    sys.exit(main())

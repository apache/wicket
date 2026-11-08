<!--
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-->
# Migration to Wicket 10

The main topic in Wicket 10 is the migration from Javax APIs to Jakarta APIs.

As a result Wicket upgraded its dependency of Spring Framework to 6.x which also led to requirement to use Java 17 for building and running Wicket applications.

## Automatic migration

#### Add migration recipes to Wicket 10 [WICKET-7029](https://issues.apache.org/jira/browse/WICKET-7029)

Apache Wicket 10 provides automatic migration based on [OpenRewrite](https://docs.openrewrite.org/).

Usage:

*Maven command*

```text
mvn -U org.openrewrite.maven:rewrite-maven-plugin:run -Drewrite.recipeArtifactCoordinates=org.apache.wicket:wicket-migration:LATEST -DactiveRecipes=org.apache.wicket.BestPractices
```

## **TODO**

#### Use stable version of Commons FileUpload [FILEUPLOAD-309](https://issues.apache.org/jira/browse/FILEUPLOAD-309)

#### Re-enable cdi-unit tests once there is a new release [cdi-unit#195](https://github.com/cdi-unit/cdi-unit/issues/195)

## **Changes**

#### WicketTester and related classes have been extracted to wicket-tester Maven module [WICKET-7072](https://issues.apache.org/jira/browse/WICKET-7072)

TL;DR: Add *org.apache.wicket:wicket-tester* as a test dependency to your project and fix the imports in your project.

Until now WicketTester class was a member of the wicket-core module. But it depends on JUnit to do its job and this caused problems in JPMS (Java 9+ modules) because JPMS does not support optional dependencies like Maven/Gradle do. For this reason WicketTester and the other \*\*Tester classes have been extracted to a new Maven/JPMS module - ***wicket-tester***. Same is valid for WebSocketTester - it has been moved to ***wicket-web-socket-tester*** module,

#### wicket-http2 module have been merged to wicket-core module [WICKET-6887](https://issues.apache.org/jira/browse/WICKET-6887)

Since Wicket 10.0 requires Servlet 5.0+ the HTTP2 PushBuilder abstraction has been moved from wicket-experimental/wicket-http2/wicket-http2-servlet4 and wicket-http2-core to wicket-core module.

PushHeaderItem class has been moved to package *org.apache.wicket.markup.head.http2*.

#### Render MarkupContainer class name as an attribute [WICKET-6890](https://issues.apache.org/jira/browse/WICKET-6890)

When org.apache.wicket.settings.DebugSettings#isOutputMarkupContainerClassName() returns *true* then the MarkupContainer class name should be rendered as an attribute of the component tag instead of as an HTML comment because an HTML comment may break a CSS rule, e.g. by appearing between two sibling HTML elements.

For example:

With CSS rule like *p + p {color: red;}* then the text color of any paragraph that follows another paragraph will be red. But if an HTML comment appears between them then the rule won't apply.

#### Make ApplicationContextMock smarter by delegating to DefaultListableBeanFactory [WICKET-6893](https://issues.apache.org/jira/browse/WICKET-6893)

ApplicationContextMock now uses internally an instance of DefaultListableBeanFactory and this way it implements almost all methods of Spring's ApplicationContext. In addition ApplicationContextMock now extends from Spring's AbstractApplicationContext and thus could be used fully in AnnotProxyFieldValueFactory and SpringBeanLocator.

#### Replace CGLib with ByteBuddy [WICKET-6913](https://issues.apache.org/jira/browse/WICKET-6913)

Wicket used CGLib for creating serializable proxies for classes. CGLib is not well maintained since several years and it does not work with JDK 17 where illegal access is no more allowed. For this reason it usage has been replaced with ByteBuddy library.

This is a change in implementation in wicket-ioc module, without API changes for the end-user applications!

## **Dependencies**

#### Wicket 10 requires Java 17

#### Wicket 10 requires Servlet 5+

I.e. Jakarta APIs

#### Spring Framework to 6.x [WICKET-6940](https://issues.apache.org/jira/browse/WICKET-6940)

#### Guice to 7.x

Upgrade Guice to 7.0.0 [WICKET-7053](https://issues.apache.org/jira/browse/WICKET-7053)

#### Commons Fileupload to 2.x [WICKET-6915](https://issues.apache.org/jira/browse/WICKET-6915)

#### Jackson to 2.13.x

Upgrade Jackson to 2.13.4 [WICKET-7009](https://issues.apache.org/jira/browse/WICKET-7009)

## **Changes in Wicket 10 releases after 10.0.0**

#### 10.13.0: `PageParameters.hashCode()` ignores the order of named parameters [#1866](https://github.com/apache/wicket/issues/1866)

`PageParameters.hashCode()` now agrees with `equals()`, which ignores the order of the named parameters ([#1866](https://github.com/apache/wicket/issues/1866)). The fix is backported from Wicket 12. Before, two `PageParameters` that differed only in that order were equal but had different hashes, so a `HashMap` or `HashSet` kept them as two keys.

This shows in rendered pages. When a page renders a CSS or JavaScript reference header item twice for the same resource, with the same page parameters added in a different order, it now gets one `<link>` or `<script>` tag, with the URL of the first item. Before, it got two. An application that keeps `PageParameters` in a hash-based collection of its own now finds reordered parameters under the same entry.

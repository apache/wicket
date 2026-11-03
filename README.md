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
<p align="center">
  <a href="https://wicket.apache.org">
    <img src="https://wicket.apache.org/img/logo-apachewicket-mark.svg" width="112" height="112" alt="Apache Wicket">
  </a>
</p>

Apache Wicket 12
================

This branch, `master`, is where Wicket 12 is developed; it is not released yet. The current
release line is maintained on `wicket-11.x` and the long-term support line on `wicket-10.x`.
Pull requests go here: fixes land on `master` first and are cherry-picked to the release lines
they apply to. `SECURITY.md` says which lines receive security fixes.

What is Apache Wicket?
----------------------
 
Apache Wicket is an open source, java, component based, web application
framework. With proper mark-up/logic separation, a POJO data model, and a
refreshing lack of XML, Apache Wicket makes developing web-apps simple and
enjoyable again. Swap the boilerplate, complex debugging and brittle code for
powerful, reusable components written with plain Java and HTML.

Apache Wicket can be found at https://wicket.apache.org and is licensed under
the Apache Software Foundation license, version 2.0.

Getting started
---------------

The Wicket project has several resources and projects where you can learn 
from, and get started quickly:

 - The Wicket user guide - https://wicket.apache.org/docs/:

    tutorials, how-to guides and explanations of the framework, with
    many real-world examples. The guide for this line is also available as
    a single page: https://nightlies.apache.org/wicket/guide/12.x/single.html

 - The Wicket JavaDoc:

    the API docs of this line are published at
    https://nightlies.apache.org/wicket/apidocs/12.x/

 - Wicket Examples:

    shows all components in short usage examples. A deployment of the
    Wicket 10 examples runs at https://examples10x.wicket.apache.org/index.html

 - Wicket Quickstart - https://wicket.apache.org/start/quickstart.html:

    provides a skeleton project for use in NetBeans, Eclipse, IntelliJ IDEA
    and other major IDE's, without having to configure anything yourself. Just
    copy'n'paste the generated command line and Maven will do the job.


What does Wicket's download package contain?
-----------------------

You can download Wicket's source package here: https://wicket.apache.org/versions/wicket-11.x/ . 
It contains the source code and the jars of the core projects of Wicket. 
If you are just starting out, you probably only need to include wicket-util-x.jar, wicket-request-x.jar and
wicket-core-x.jar, where x stands for the version. As a rule, use just the jars
you need.

You will find the source code here:

	|-- apidocs
	|   |-- org
	|   `-- resources
	|-- lib
	|-- licenses
	`-- src
	    |-- archetypes
	    |-- testing
	    |-- wicket
	    |-- wicket-auth-roles
	    |-- wicket-bean-validation
	    |-- wicket-benchmarks
	    |-- wicket-cdi
	    |-- wicket-cdi-tests
	    |-- wicket-core
	    |-- wicket-tester
	    |-- wicket-core-tests
	    |-- wicket-coverage
	    |-- wicket-devutils
	    |-- wicket-eclipse-settings
	    |-- wicket-examples
	    |-- wicket-experimental
	    |   `-- wicket-metrics
	    |-- wicket-extensions
	    |-- wicket-extensions-tester
	    |-- wicket-guice
	    |-- wicket-ioc
	    |-- wicket-jmx
	    |-- wicket-migration
	    |-- wicket-native-websocket
	    |-- wicket-objectsizeof-agent
	    |-- wicket-request
	    |-- wicket-spring
	    |-- wicket-util
	    |-- wicket-user-guide
	    `-- wicket-velocity
	    

Here is a list of projects in the distribution and what they do.

 - wicket-core: the core project, includes the framework and basic components;
 - wicket-tester: contains common classes for unit testing
 - wicket-core-tests: contains test cases for wicket-core module
 - wicket-extensions: contains utilities and more specialized components;
 - wicket-extensions-tester: contains classes for unit testing wicket-extensions
   components;
 - wicket-auth-roles: a basic authorization package based on roles;
 - wicket-jmx: registers JMX beans for managing things like your Wicket 
   configuration and markup cache;
 - wicket-objectsizeof-agent: utility for making better estimates of object 
   sizes in the JVM - most people probably never need this;
 - wicket-ioc: base project for IoC (aka DI) implementations such as 
   Spring and Guice;
 - wicket-spring: support project for using Spring with Wicket and including 
   Spring managed dependencies through using @SpringBean annotations;
 - wicket-guice: support project for using Google Guice with Wicket;
 - wicket-velocity: contains special components for rendering Velocity
   templates using Wicket components - most people probably don't need this,
   but it can be neat when you want to do CMS-like things;
 - wicket-examples: contains a basic component reference and many examples of 
   how to use Wicket and Wicket components, including examples for subprojects
   such as wicket-spring, wicket-velocity and wicket-auth-roles.
 - wicket-devutils: provides small utilities which can help in development
   phase and during debugging
 - wicket-bean-validation: validates beans with annotations based on
   Jakarta Validation;
 - wicket-cdi: Jakarta Contexts and Dependency Injection (CDI) integration for wicket;
 - wicket-experimental: experimental implementations for wicket;
 - wicket-native-websocket: wicket's native web sockets integration 
   for several servers;
 - wicket-request: lightweight project which contains all classes dealing with request
   handlers and so on;
 - wicket-util: the util project for wicket;
 - wicket-eclipse-settings: specifies Eclipse settings for a uniform development environment.
   Most notably the formatting rules;
 - wicket-user-guide: the user guide of wicket;
 - wicket-metrics: collects data of a running wicket application;
 - wicket-migration: OpenRewrite recipes that migrate an application to a newer
   Wicket version;
 - wicket-benchmarks: performance benchmarks, not published;
 - wicket-coverage: aggregates the code coverage report, not published.

Dependencies
------------

The easiest way of getting the dependencies of your Wicket based projects
right is to use Apache Maven (https://maven.apache.org) with your projects and
include the wicket dependencies you want as outlined in the wicket-quickstart.
Maven will then take care of including the appropriate dependencies.

If you do not want to use Maven, here is a break-down of the dependencies you
need. The versions are not repeated here: for the complete and precise reference
see the wicket-parent pom.xml in the root folder.

 - wicket and wicket-extensions:

    You only need to include the Jakarta Servlet API (just for compiling), SLF4J
    API and the SLF4J logging implementation you want. You cannot use Wicket
    without adding a SLF4J logging implementation to your classpath. 
    Please see the SLF4J site (https://www.slf4j.org/) for more information.

    As the following projects all depend on Wicket, they inherit these
    dependencies.

 - wicket-velocity:

    Apache Velocity Engine (https://velocity.apache.org/) and its dependencies

 - wicket-ioc:

    byte-buddy (https://bytebuddy.net/) and
    asm-util (https://asm.ow2.io/)

 - wicket-spring:

    wicket-ioc and Spring (https://spring.io/projects/spring-framework/) and its
    dependencies

 - wicket-guice:

    Google Guice (https://github.com/google/guice)

 - wicket-cdi:

    Jakarta Contexts and Dependency Injection
    (https://jakarta.ee/specifications/cdi/)

 - wicket-examples:

    All of the above.

Building Wicket from source
---------------------------

Wicket's source distribution (download package mentioned above) contains 
also the binaries (jar files) for each of its modules (subprojects). 
You can use these directly in your applications. The Wicket project uploads 
the source and JavaDoc jars to the Maven repository used by the Maven build 
tool as well. So there is actually no specific need to build Wicket yourself 
from the distribution.

When building using Maven 3, execute one of the following in the root folder:

 - mvn package

    creates wicket-(subproject)-x.y.z.jar(s) in according target subdirectories.

 - mvn install

    creates wicket-(subproject)-x.y.z.jar(s) in according target subdirectories and 
    installs the jar files into your local Maven repository for use in other projects.

Code coverage
-------------

Coverage is measured with JaCoCo and aggregated into a single report, because most
Wicket tests live in a module other than the code they exercise (the tests for
wicket-core are in wicket-core-tests, for example). To produce it:

 - mvn clean verify -Pcoverage

    writes the aggregated report to
    wicket-coverage/target/site/jacoco-aggregate/index.html

To build only the modules that feed the report, which is considerably faster:

 - mvn clean verify -Pcoverage -pl wicket-coverage -am

Note that a build without "clean" merges the previous run's data into the new report,
because the JaCoCo agent appends by default.

Every push and pull request also uploads this report to
https://app.codecov.io/gh/apache/wicket. Coverage is reported there, never enforced:
no coverage check can fail a build or block a merge.

Migrating from 11.x
-------------------

The OpenRewrite recipe org.apache.wicket.MigrateToWicket12 in wicket-migration
handles the mechanical part of the migration. The migration guide is published
with the 12.0.0 release. To move from 10.x, follow migration-to-11.md in the root
folder first.

Getting help
------------

 - Read the online documentation available on our website
   (https://wicket.apache.org)

 - Read the migration guide above

 - Read the mailing list archives at
   https://lists.apache.org/list.html?users@wicket.apache.org

 - Send a complete message containing your problem, stacktrace and problem
   you're trying to solve to our user list (users@wicket.apache.org)

 - Ask a question in GitHub Discussions (https://github.com/apache/wicket/discussions)
   or on IRC at Libera.Chat, channel #wicket

License
-------

Wicket is distributed under the terms of the Apache Software Foundation
license, version 2.0. The text is included in the file LICENSE in the root
of the project.

Java/Application server requirements
------------------------------------

Wicket 12 requires at least Java 21. The application server for running your web
application should adhere to the Jakarta Servlet 6.1 specification or newer.

Cryptographic Software Notice
-----------------------------

This distribution includes cryptographic software. The country in which you
currently reside may have restrictions on the import, possession, use, and/or
re-export to another country, of encryption software. BEFORE using any
encryption software, please check your country's laws, regulations and
policies concerning the import, possession, or use, and re-export of
encryption software, to see if this is permitted. See http://www.wassenaar.org
for more information.

The U.S. Government Department of Commerce, Bureau of Industry and Security
(BIS), has classified this software as Export Commodity Control Number (ECCN)
5D002.C.1, which includes information security software using or performing
cryptographic functions with asymmetric algorithms. The form and manner of
this Apache Software Foundation distribution makes it eligible for export
under the License Exception ENC Technology Software Unrestricted (TSU)
exception (see the BIS Export Administration Regulations, Section 740.13) for
both object code and source code.

The following provides more details on the included cryptographic software:

For encoding HTTP URL data (see org.apache.wicket.core.request.mapper.CryptoMapper)
Wicket requires the Java Cryptography extensions
(http://java.sun.com/javase/technologies/security/). Wicket does not include
these libraries itself, but is designed to use them.


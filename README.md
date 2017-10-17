<p align="center">
  <a href="http://wicket.apache.org">
    <img height="200" width="200" src="https://wicket.apache.org/img/logo.svg">
  </a>
</p>

What is Apache Wicket?
=================

Apache Wicket is an open source, java, component based, web application
framework. With proper mark-up/logic separation, a POJO data model, and a
refreshing lack of XML, Apache Wicket makes developing web-apps simple and
enjoyable again. Swap the boilerplate, complex debugging and brittle code for
powerful, reusable components written with plain Java and HTML.

Apache Wicket can be found at: http://wicket.apache.org and is licensed under
the Apache Software Foundation license, version 2.0.

Getting started
---------------

The Wicket project has several resources and projects where you can learn 
from, and get started quickly:

 - The Wicket user guide - https://wicket.apache.org/learn/#guide: 

    learn Wicket from scratch reading its userguide which gradually 
    introduces you to the various features of the framework with 
    many real-world examples.

 - The Wicket JavaDoc:
    
   the API Docs are available on the main site of the project:
   http://wicket.apache.org/learn/#javadoc
    
 - Wicket Examples:

    shows all components in short usage examples, also available live on:
    http://examples8x.wicket.apache.org

 - Wicket Quickstart - http://wicket.apache.org/start/quickstart.html:

    provides a skeleton project for use in NetBeans, Eclipse, IntelliJ IDEA
    and other major IDE's, without having to configure anything yourself. Just
    copy'n'paste the generated command line and Maven will do the job.


What is in this package
-----------------------

The archive you just downloaded and unpacked contains the source code and the
jars of the core projects of Wicket. If you are just starting out, you probably
only need to include wicket-util-x.jar, wicket-request-x.jar and
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
	    |-- wicket-cdi
	    |-- wicket-cdi-1.1
	    |-- wicket-core
	    |-- wicket-datetime
	    |-- wicket-devutils
	    |-- wicket-eclipse-settings
	    |-- wicket-examples
	    |-- wicket-experimental
	    |   |-- wicket-metrics
	    |   |-- wicket-http2
	    |-- wicket-extensions
	    |-- wicket-guice
	    |-- wicket-ioc
	    |-- wicket-jmx
	    |-- wicket-native-websocket
	    |-- wicket-objectssizeof-agent
	    |-- wicket-request
	    |-- wicket-spring
	    |-- wicket-util
	    |-- wicket-user-guide
	    `-- wicket-velocity
	    

Here is a list of projects in this distribution and what they do.

 - wicket: the core project, includes the framework and basic components;
 - wicket-extensions: contains utilities and more specialized components;
 - wicket-auth-roles: a basic authorization package based on roles;
 - wicket-datetime: contains date/ time specific components such as a date
   picker;
 - wicket-jmx: registers JMX beans for managing things like your Wicket 
   configuration and markup cache;
 - wicket-objectssizeof-agent: utility for making better estimates of object 
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
   how to use Wicket and Wicket components, including examples for sub 
   projects such as wicket-spring, wicket-velocity and wicket-auth-roles.
 - wicket-devutils: provides small utilities which can help in development
   phase
 - wicket-bean-validation: validates beans with annotation based on 
   javax.validation;
 - wicket-cdi: the context and dependency injection of the jee standard for wicket;
 - wicket-cdi-1.1: the context and dependency injection of the jee standard 1.1
   for wicket;
 - wicket-devutils: some utils to help debugging wicket;
 - wicket-experimental: experimental implementations for wicket;
 - wicket-native-websocket: wicket's native web sockets integration 
   for several servers;
 - wicket-request: lightweight project which contains all classes dealing with request
   handlers and so on;
 - wicket-util: the util project for wicket;
 - wicket-eclipse-settings: specifies Eclipse settings for a uniform development environment.
   Most notably the formatting rules;
 - wicket-user-guide: the user guide of wicket
 - wicket-metrics: collects data of a running wicket application
 - wicket-http2: http/2 push support

Dependencies
------------

The easiest way of getting the dependencies of your Wicket based projects
right is to use Apache Maven (http://maven.apache.org) with your projects and
include the wicket dependencies you want is outlined in the wicket-quickstart.
Maven will then take care of including the appropriate dependencies.

If you do not want to use maven, here is a break down of the dependencies you
need. For the complete and precise reference see the wicket-parent pom.xml in
the src/ folder.

 - wicket and wicket-extensions:

    You only need to include the Servlet API (2.5, just for compiling), SLF4J
    API and the SLF4J logging implementation you want. You cannot use Wicket
    without adding a SLF4J logging implementation to your classpath. Most
    people use log4j. If you do, just include slf4j-log4j12.jar on your
    classpath to get Wicket to use log4j too. If you want to use
    commons-logging or JDK14 logging or something else, please see the SLF4J
    site (http://www.slf4j.org/) for more information.

	As the following projects all depend on wicket, they inherit these
    dependencies.

 - wicket-velocity:

    Apache Velocity 1.7 (http://velocity.apache.org/) and it's dependencies
    (it ships a velocity-deps jar for convenience)

 - wicket-ioc:

    cglib 3.1 (http://cglib.sourceforge.net/) and 
    asm-util 5.0.3 (http://asm.objectweb.org/)

 - wicket-spring:

    wicket-ioc and Spring (http://www.springframework.org/) and it's
    dependencies

 - wicket-guice:

    Google Guice (http://code.google.com/p/google-guice/)

 - wicket-cdi / wicket-cdi-1.1:
	Component Dependency Injection 
	(http://www.oracle.com/technetwork/articles/java/cdi-javaee-bien-225152.html 
	and 
	http://download.oracle.com/otndocs/jcp/cdi-1_1-pr-spec/index.html)

 - wicket-examples:

    All of the above.

Building Wicket from source
---------------------------

The Wicket distribution contains the final Wicket jar. You can use this
directly in your applications. The Wicket project also uploads the source 
and JavaDoc jars as well as the final jar to the Maven repository used by
the Maven build tool. So there is actually no specific need to build Wicket
yourself from the distribution.

Building using Maven 2 or 3, change the working directory to src and either
do:

 - mvn package

    creates wicket-x.y.z.jar in target/ subdirectory.

 - mvn install

    creates wicket-x.y.z.jar in target/ subdirectory and installs the file
    into your local Maven repository for use in other projects.

Migrating from 7.x
------------------

This file is a copy of the migration guide from available on our Wiki:

    https://cwiki.apache.org/confluence/display/WICKET/Migration+to+Wicket+8.0
    
Getting help
------------

 - Read the online documentation available on our website
   (http://wicket.apache.org)

 - Read the migration guide (migration-to-wicket-60.html)

 - Read the mailing archives available on Nabble, GMane and Apache

 - Send a complete message containing your problem, stacktrace and problem
   you're trying to solve to the user list (users@wicket.apache.org)

 - Ask a question on IRC at freenode.net, channel ##wicket

License
-------

Wicket is distributed under the terms of the Apache Software Foundation
license, version 2.0. The text is included in the file LICENSE in the root
of the project.

Java/Application server requirements
------------------------------------

Wicket requires at least Java 1.8. The application server for running your web
application should adhere to the servlet specification version 3.1 or newer.

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


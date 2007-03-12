Apache Wicket 1.3
=================

This is the readme file for the Wicket core project. 

Apache Wicket is an open source, java, component based, web application framework.
With proper mark-up/logic separation, a POJO data model, and a refreshing lack of XML, 
Apache Wicket makes developing web-apps simple and enjoyable again. Swap the boilerplate, 
complex debugging and brittle code for powerful, reusable components written with plain Java 
and HTML.

Contents
--------
 - Incubation
 - License
 - Java/Application server requirements
 - Getting started
 - Building Wicket from source
 - Migrating from 1.2
 - Getting help

Incubation
----------

The Wicket project is an effort undergoing incubation at the Apache Software
Foundation (ASF), sponsored by the Incubator PMC. Incubation is required of all 
newly accepted projects until a further review indicates that the 
infrastructure, communications, and decision making process have stabilized in 
a manner consistent with other successful ASF projects. While incubation 
status is not necessarily a reflection of the completeness or stability of the 
code, it does indicate that the project has yet to be fully endorsed by the 
ASF.

For more information about the incubation status of the Wicket project you
can go to the following page:

http://incubator.apache.org/projects/wicket.html

License
-------

Wicket is distributed under the terms of the Apache Software Foundation license,
version 2.0. The text is included in the file LICENSE.txt in the root of the 
project.

Java/Application server requirements
------------------------------------

Wicket requires at least Java 1.4. The application server for running your web
application should adhere to the servlet specification version 2.3 or newer. All
necessary dependencies are located in the /lib directory of this package.

Getting started
---------------

The Wicket project has several projects where you can learn from, and get started
quickly:
 - wicket-examples : shows all components in short usage examples, also available
     live on: http://www.wicket-library.com/wicket-examples

 - wicket-quickstart : provides a skeleton project for use in NetBeans, Eclipse, 
     IntelliJ IDEA and other major IDE's, without having to configure anything 
     yourself. You can copy'n'paste the examples from the website into your pages
     and see them running on your own box.

Building Wicket from source
---------------------------

The Wicket distribution contains the final Wicket jar. You can use this directly
in your applications. The Wicket project also uploads the source-jars together with
the final jar to the Ibiblio repository used by the Maven build tool. So there is
actually no specific need to build Wicket yourself from the distribution.

Now if you do with to do so, you can build Wicket using Ant or Maven 2. Support for
Maven 1 is limited to downloading the artifacts from the Ibiblio repository and the
conversion of the pom.xml file is done automatically by the Maven project.

Building using maven 2:
 - mvn package
     creates wicket-x.y.z.jar in target/ subdirectory.
 - mvn install
     creates wicket-x.y.z.jar in target/ subdirectory and installs the file into your
     local repository for use in other projects.
     
Migrating from 1.2
------------------
There is a migration guide available on our Wiki:
    http://cwiki.apache.org/WICKET/migrate-13.html
    
Getting help
------------

 - Read the online documentation available on our website (http://incubator.apache.org/wicket)
 - Read the migration guide (http://cwiki.apache.org/WICKET/migrate-13.html)
 - Read the mailing archives available on nabble, gmane and sourceforge
 - Send a complete message containing your problem, stacktrace and problem you're trying
   to solve to the user list (wicket-user@lists.sourceforge.net)
 - Ask a question on IRC at freenode.net, channel ##wicket
 

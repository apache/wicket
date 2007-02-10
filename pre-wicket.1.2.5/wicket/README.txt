Wicket 1.2
==========

This is the README file for the Wicket core project. 

Wicket is a component oriented, open source Java web application framework.
With proper mark-up/logic separation, a POJO data model, and a refreshing lack 
of XML, Wicket makes developing Java web-apps simple and enjoyable again. Swap 
the boilerplate, complex debugging and brittle code for powerful, reusable 
components written with plain Java and HTML.

-------------------------------------------------------------------------------
DISCLAIMER

This release of the Wicket project is not endorsed or approved by the Apache 
Software Foundation.

Although Wicket has recently entered the ASF Incubator, this interim release 
is provided outside of the ASF, solely as a service to existing Wicket users 
to resolve existing bugs in the Wicket product.

The Apache Software Foundation is in no way affiliated with this release.

-------------------------------------------------------------------------------

Contents
--------
 - License
 - Java/Application server requirements
 - Getting started
 - Building Wicket from source
 - Migrating from 1.1
 - Getting help

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

Building using ant:
 - ant jar
     creates wicket.jar in target/ subdirectory.

Building using maven (2):
 - mvn package
     creates wicket-x.y.z.jar in target/ subdirectory.
 - mvn install
     creates wicket-x.y.z.jar in target/ subdirectory and installs the file into your
     local repository for use in other projects.
     
Migrating from 1.1
------------------

There is a migration guide available on our Wiki:
    http://cwiki.apache.org/WICKET/migrate-12.html
    
Getting help
------------

 - Read the supplied documentation in the docs/ directory
 - Read the online documentation available on our Wiki (http://cwiki.apache.org/WICKET)
 - Read the migration guide (http://cwiki.apache.org/WICKET/migrate-12.html)
 - Read the mailing archives available on nabble, gmane and sourceforge
 - Send a complete message containing your problem, stacktrace and problem you're trying
   to solve to the user list (wicket-user@lists.sourceforge.net)
 - Ask a question on IRC at freenode.net, channel ##wicket
 

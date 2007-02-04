                             ---------------------
                             Wicket Parent project
                             ---------------------
                                Martijn Dashorst
                             ---------------------

Introduction

 This is the Wicket parent project, creating default settings for all Wicket projects.
 The defaults entail plug-in settings, versioning of the Wicket projects, managing the
 various dependencies and more.
	
 For more information on the project structure, please read the 'Better Builds With Maven'
 book which has a detailed description on how to structure a maintainable, enterprise ready
 build. The book is available for free from the Mergere website.

 The artifacts in this project are there only for having a consistent build. This project
 doesn't have sources for itself.

Building

 The build entails two parts: a Java 1.4 part for the projects that are Java 1.4 based and
 a Java 5 part, specific for the projects that are Java 5 based.

 Here's a list of the Java 1.4 based projects:

---------------------------------------------------------------------------
 * wicket
 * wicket-extensions
 * wicket-examples
 * wicket-quickstart
 * wicket-spring
 * wicket-spring-examples
 * wicket-portlet-examples
---------------------------------------------------------------------------
	
 Here's a list of the Java 5 based projects:
	
---------------------------------------------------------------------------
 * wicket-auth-roles
 * wicket-auth-roles-examples
 * wicket-spring-annot
 * wicket-spring-annot-examples
---------------------------------------------------------------------------

 To build the projects, make sure you have downloaded and installed all projects in one
 directory structure, each project a subdirectory next to one another. If you want to get it
 all in one sweep, then use subversion to checkout the whole project structure in one go.
	
---------------------------------------------------------------------------
svn co https://svn.sourceforge.net/svnroot/wicket/releases/<VERSION>
---------------------------------------------------------------------------
	
 substituting the <VERSION> part with "wicket-X.Y.Z", where X, Y and Z are the major, minor and
 bugfix version. So for Wicket-1.2.1 you would check out:
	
---------------------------------------------------------------------------
svn co https://svn.sourceforge.net/svnroot/wicket/releases/wicket-1.2.1
---------------------------------------------------------------------------

The development version of wicket 1.x can be found in branch wicket-1.x, and
the latest and greatest development version (currently Wicket 2.x) can be found in trunk.
The branches 1.0.x, 1.1.x and 1.2.x are for versions that are no longer actively
developed, but which may in exception cases receive bug fixes.


Stage one:
---------------------------------------------------------------------------
mvn -Pjdk1.4 install
---------------------------------------------------------------------------

Stage two (if you're running JDK 5):
---------------------------------------------------------------------------
mvn -Pjdk1.5 install
---------------------------------------------------------------------------

You may need to run with -Dmaven.test.skip=true if you have failing unit tests (this obviously
shouldn't be the case for a release build).

The following commands are useful:
---------------------------------------------------------------------------
mvn compile             <- compiles the projects
mvn package             <- creates the jar files in the target/ subdirectories
mvn install             <- installs the jar files in your local repository
---------------------------------------------------------------------------

 You can just run the install target, as Maven will build the previous stages automatically.

---------------------------------------------------------------------------

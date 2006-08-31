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
	
 substituting the <VERSION> part with "wicket_X_Y_Z", where X, Y and Z are the major, minor and
 bugfix version. So for Wicket-1.2.1 you would check out:
	
---------------------------------------------------------------------------
svn co https://svn.sourceforge.net/svnroot/wicket/releases/wicket-1.2.1
---------------------------------------------------------------------------

 In the root of the wicket-parent project do the following to build all Wicket projects:

---------------------------------------------------------------------------
mvn compile             <- compiles the projects
mvn package             <- creates the jar files in the target/ subdirectories
mvn install             <- installs the jar files in your local repository
---------------------------------------------------------------------------

 You can just run the install target, as Maven will build the previous stages automatically.

 If you run using Java 5, you may need to edit the wicket-parent pom, and add the modules from
 the Java 1.4 profile to the Java 5 profile, otherwise you will only build the Java 5 specific
 projects. See the following section for more information:

---------------------------------------------------------------------------
<profiles> 
	<profile> 
		<id>jdk1.4</id> 
		<activation> 
			<jdk>1.4</jdk> 
		</activation> 
		<modules> 
			<module>../wicket</module>
			<module>../wicket-extensions</module>
			<module>../wicket-examples</module>
			<module>../wicket-spring</module>
			<module>../wicket-spring-examples</module>
			<module>../wicket-quickstart</module>
		</modules> 
	</profile> 
	<profile> 
		<id>jdk1.5</id> 
		<activation> 
			<jdk>1.5</jdk> 
		</activation> 
		<modules> 
			<module>../wicket-spring-annot</module>
			<module>../wicket-spring-annot-examples</module>
			<module>../wicket-auth-roles</module> 
			<module>../wicket-auth-roles-examples</module> 
		</modules> 
	</profile> 
</profiles> 
---------------------------------------------------------------------------

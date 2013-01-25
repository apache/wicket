Hello World
=========================================
.. toctree::
   :maxdepth: 3


This HelloWorld application demonstrates the basic structure of a web application in Apache Wicket. A `Label <http://ci.apache.org/projects/wicket/apidocs/6.0.x/org/apache/wicket/markup/html/basic/Label.html>`_ component is used to display a message on the home page for the application.

In all the Wicket examples, you have to put all files in the same package directory. This means putting the markup files and the java files next to one another. It is possible to alter this behavior, but that is beyond the scope of this example. The only exception is the obligatory web.xml file which should reside in the WEB-INF/ directory of your web application root folder.

If you wish to start building this example, you may want to take a look at the `Wicket Quickstart project <http://wicket.apache.org/start/quickstart.html>`_, which provides a quick way of getting up and running without having to figure things out yourself. The Quickstart project contains the necessary build files for `Apache Maven <http://maven.apache.org>`_, libraries, minimal set of Java and markup files and an embedded Jetty server to run your application without having to go through the whole build-deploy cycle.

HelloWorldApplication.java
--------------------------

Each Wicket application is defined by an `Application <http://ci.apache.org/projects/wicket/apidocs/6.0.x/org/apache/wicket/Application.html>`_ object. This object defines what the home page is, and allows for some configuration.

.. includecode:: ../../../helloworld/src/main/java/org/apache/wicket/reference/helloworld/HelloWorldApplication.java

Here you can see that we define org.apache.wicket.examples.helloworld.HelloWorld to be our home page. When the base URL (the context root) of our application is requested, the markup rendered by the HelloWorld page is returned.

HelloWorld.java
---------------

.. includecode:: ../../../helloworld/src/main/java/org/apache/wicket/reference/helloworld/HelloWorld.java

The Label is constructed using two parameters:

#. “message”

#. “Hello World!”

The first parameter is the component identifier, which Wicket uses to identify the Label component in your HTML markup. The second parameter is the message which the Label should render.

HelloWorld.html
---------------

The HTML file that defines our Hello World functionality is as follows:

.. includecode:: ../../../helloworld/src/main/java/org/apache/wicket/reference/helloworld/HelloWorld.html

In this file, you see two elements that need some attention:

#. the component declaration *<span wicket:id="message">*

#. the text *The real message goes here*

The component declaration consists of the Wicket identifier wicket:id. The component identifier should be the same as the name of the component you defined in your WebPage. The text between the <span> tags is removed when the component renders its message. The final content of the component is determined by your Java code.

web.xml
-------

In order to deploy our HelloWorld application, we need to make our application known to the application server by means of the web.xml file.

.. includecode:: ../../../helloworld/src/main/webapp/WEB-INF/web.xml

In this definition you see the Wicket filter defined, which handles all requests. In order to let Wicket know which application is available, only the applicationClassName filter parameter is needed.

Also, notice the url-mapping to /\*. The Wicket filter will only process requests that are Wicket requests. If a request is not Wicket related, the filter will pass the request on to the chain. This ensures that (static) resources outside the realm of the Wicket application, such as style sheets, JavaScript files, images and so forth will be served by the container.

Ready to deploy

That’s it. No more configuration necessary! All you need to do now is to deploy the web application into your favorite application server. Point your browser to the url: http://<servername>/<warfilename>/, substituting servername and warfilename to the appropriate values, such as http://localhost:8080/helloworld/.

As you can see: no superfluous XML configuration files are needed to enable a Wicket application. Only the markup (HTML) files, the Java class files and the required web.xml were needed to create this application.

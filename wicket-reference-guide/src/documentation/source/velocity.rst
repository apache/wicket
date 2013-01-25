Apache Velocity
===============

wicket-velocity integration module provides a specialized panel and some related utilities that enable users to use `Apache Velocity <http://velocity.apache.org/>`_ in `Apache Wicket <http://wicket.apache.org/>`_ applications. Particularly useful for simple CMS like applications.

Contents

#. Introduction_
#. Example_
#. Installing_
   
.. _Introduction:

Introduction
------------

Velocity brings a templating language to your users. You can let them create conditional markup, use loops and do all other things made possible by Velocity.

Velocity templates look like the following::

    #set ($foo = "deoxyribonucleic acid")
    #set ($bar = "ribonucleic acid")

    #if ($foo == $bar)
        In this case it's clear they aren't equivalent. So...
    #else
        They are not equivalent and this will be the output.
    #end
    
Read `more <http://velocity.apache.org/engine/releases/velocity-1.4/user-guide.html>`_ about the Velocity template language.

This project allows you to use Velocity templates as a component within your Wicket pages, and let them live next to Wicket components. A typical usecase would be to enable your users to embed Velocity templates in your application and using that as a type of portlet.

The main component for the Veloticy/Wicket integration is the VelocityPanel.

.. _Exammple:

Example
--------

Showing Hello, World using Velocity in a Wicket application, embedded in a Wicket page.::

    <h2>This is a Velocity template</h2>

    <p>The secret message is: $message</p>
    
In this template we want to replace the string $message with the text “Hello, World!”. $message is Velocity markup denoting a variable that is taken from the context that is provided to the Velocity rendering engine.

To use Velocity in your Wicket pages we provide a VelocityPanel which enables you to generate parts of your page using Velocity markup. Adding the panel to your Wicket page is shown in the following example:::

    public VelocityPage() {
        HashMap<String, String> values = new HashMap<String, String>();
        values.put("message", "Hello, World!");
        Model<HashMap<String, String>> context = Model.of(values);

        UrlResourceStream template = new UrlResourceStream(getClass().getResource("test.html"));
        add(VelocityPanel.forTemplateResource("velocityPanel", context, template));
    }
    
VelocityPanel.forTemplateResource creates a VelocityPanel and sets up the engine such that the context is merged with the template with each render.

The markup of the page is quite simple: adding a VelocityPanel is as simple as using a div and attaching a wicket:identifier to it. The following example shows this.
::

    <!DOCTYPE html>
    <h1>This is a test page for Velocity</h1>
    <div wicket:id="velocityPanel"></div>

.. _Installing:

Installing
----------

Installing Wicket Velocity can be done through adding a dependency in your project’s Maven pom, or by putting the wicket-velocity.jar and the required dependencies in your projects classpath.

Using Maven

Add the following dependency to your pom:::

    <dependency>
         <groupId>org.apache.wicket</groupId>
         <artifactId>wicket-velocity</artifactId>
         <version>${wicket.version}</version>
    </dependency>
    
Required dependencies

If you use dependency management tool like Apache Maven, Apache Ivy, Gradle then just adding the dependency above will download all transitive dependencies. Otherwise you will need at least:

- wicket-util.jar
- wicket-request.jar
- wicket-core.jar
- wicket-velocity.jar
- velocity.jar

Check the Apache Velocity project to find out which other dependencies you may need additionally.

Also see `Wicket Mustache <https://github.com/l0rdn1kk0n/wicket-mustache>`_ - a third party module that provides similar integration with `Mustache <https://github.com/mustache>`_ templating language.

Stateless pages
===============


Being stateful
--------------

By nature a page is stateless, i.e. a new instance is created for each request and discarded at the end. A page becomes stateful as soon as a stateful behavior or component is added in the tree. Stateful pages are stored in a storage for later use, i.e. in following requests the same page instance is reused instead of creating a new instance. Technically, the page may be deserialized so it is not always the same JVM instance but the important thing is that any state/data in the page will be preserved.

A `Component <http://ci.apache.org/projects/wicket/apidocs/6.0.x/org/apache/wicket/Component.html>`_ declares that it needs to be stateful by returning *false* in #getStatelessHint() method::

    protected boolean getStatelessHint()
    {
        return false;
    }

and `Behavior <http://ci.apache.org/projects/wicket/apidocs/6.0.x/org/apache/wicket/Behavior.html>`_ by overriding::

    public boolean getStatelessHint(Component component)
    {
        return false;
    } 


Example
-------

.. includecode:: ../../../stateless/src/main/java/org/apache/wicket/reference/stateless/StatefulPage.java#docu


Click on an `Link <http://ci.apache.org/projects/wicket/apidocs/6.0.x/org/apache/wicket/markup/html/link/Link.html>`_ will lead to a request that will try to find the Page object that contains this link, then find the link itself in that page and finally execute its `#onClick()` method. The value of *counter* field will increment for each click on the link.

If `StatelessLink <http://ci.apache.org/projects/wicket/apidocs/6.0.x/org/apache/wicket/markup/html/link/StatelessLink.html>`_ is used instead:

.. includecode:: ../../../stateless/src/main/java/org/apache/wicket/reference/stateless/StatelessPage.java#docu

then the value of *counter* field will always be *0* because a new instance of StatelessPage is created for each click on the link.

Keeping it stateless
--------------------

To make sure that a page is stateless and there is no stateful component/behavior added to it by accident `StatelessChecker <http://ci.apache.org/projects/wicket/apidocs/6.0.x/org/apache/wicket/devutils/stateless/StatelessChecker.html>`_ can be used. If there is a stateful component or behavior then this checker will throw an exception which message explains where is the problem.

1. Add dependency to `wicket-devutils` 

::

    <dependency>
        <groupId>org.apache.wicket</groupId>
        <artifactId>wicket-devutils</artifactId>
        <version>${wicket.version}<version>
    </dependency>

2. Register StatelessChecker

Register StatelessChecker as a listener that is invoked before rendering the page:

**MyApplication.java**

.. includecode:: ../../../stateless/src/main/java/org/apache/wicket/reference/stateless/StatelessApplication.java#check-stateless

3. Annotate the pages

Use `StatelessComponent <http://ci.apache.org/projects/wicket/apidocs/6.0.x/org/apache/wicket/devutils/stateless/StatelessComponent.html>`_ annotation to require the page to be stateless.

.. includecode:: ../../../stateless/src/main/java/org/apache/wicket/reference/stateless/CheckedPage.java#check-stateless







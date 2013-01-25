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




For example, click on an `Link <http://ci.apache.org/projects/wicket/apidocs/6.0.x/org/apache/wicket/markup/html/link/Link.html>`_ will lead to a request that will try to find the Page object that contains this link, then find the link itself in that page and finally execute its `#onClick()` method.

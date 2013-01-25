Stateless pages
===============

By nature a page is stateless, i.e. a new instance is created for each request and discarded at the end. A stateful page is stored in a some storage for later use. For example, clicking on an Link [#]_ request needs to 

A page becomes stateful as soon as a stateful behavior or component is added in the tree. 

A Component [#]_ declares that it needs to be stateful by returning *false* in #getStatelessHint() method::

    protected boolean getStatelessHint()
    {
        return false;
    }

and Behavior [#]_ by overriding::

    public boolean getStatelessHint(Component component)
    {
        return false;
    } 

and org.apache.wicket.Behavior can be marked as stateful by overriding their

.. [#] org.apache.wicket.markup.html.link.Link
.. [#] org.apache.wicket.Component
.. [#] org.apache.wicket.Behavior

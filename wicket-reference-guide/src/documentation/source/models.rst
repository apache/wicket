What are Wicket Models?
============================
.. toctree::
   :maxdepth: 3

In Wicket, a model holds a value for a component to display and/or edit. How exactly this value is held is determined by a given model's implementation of the :ref:`IModel <models--imodel-label>` interface. The ``IModel`` interface decouples a component from the *model object* which forms its value. This in turn decouples the whole Wicket framework from any and all details of model storage, such as the details of a given persistence technology. As far as Wicket itself is concerned, a model is anything that implements the ``IModel`` interface, no matter how it might do that. Although there are some refinements described below, conceptually, ``IModel`` looks like this:

.. _models--imodel-label:

.. literalinclude:: ../../../../wicket-core/src/main/java/org/apache/wicket/model/IModel.java
	:start-after: */
	:lines: 40-

The ``IModel`` interface defines a simple contract for getting and setting a value. The nature of the Object retrieved or set will depend on the component referencing the model. For a ``Label`` component, the value must be something which can be converted to a ``String`` (see :doc:`converter`) which will be displayed when the label is rendered. For a ``ListView``, it must be a ``java.util.List`` containing the values to be displayed as a list.

Different frameworks implement the model concept differently. Swing has a number of component-specific model interfaces. Struts requires that the model be a Java Bean and there is no explicit model interface. The ``IModel`` interface in Wicket allows models to be generic (as in Struts) but it can do things that would not be possible if components accessed their model directly (as in Swing). For example, Wicket applications can use or provide ``IModel`` implementations that read a model value from a resource file or retrieve a model value from a database only when needed.

The use of a single model interface (as compared to having multiple interfaces, or having no model interface at all) has a number of advantages:

* Wicket provides ``IModel`` implementations you can use with any component. These models can do things such as retrieve the value from a resource file, or read and write the value from a Java Bean property.
* Wicket also provides ``IModel`` implementations that defer retrieving the value until it is actually needed, and remove it from the servlet Session when the request is complete. This reduces session memory consumption and is particularly useful with large values such as lists.
* Unlike Swing, you do not have to implement an extra interface or helper class for each different component. Especially for the most often used components such as ``Labels`` and ``TextFields`` you can easily bind to a bean property.
* In many cases you can provide the required value directly to the component and it will wrap a default model implementation around it for you.
* And while you do not have to use beans as your models as you must with Struts, you may still easily use beans if you wish. Wicket provides the appropriate model implementations.


Simple Models
-------------

The HelloWorld example program demonstrates the simplest model type in Wicket:

.. includecode:: ../../../helloworld/src/main/java/org/apache/wicket/reference/helloworld/HelloWorld.java#docu
	:tabsize: 2

The constructor for this page constructs a ``Label`` component. The first parameter to the ``Label`` component's constructor is the Wicket id, which associates the ``Label`` with a tag in the HelloWorld.html markup file:

.. includecode:: ../../../helloworld/src/main/java/org/apache/wicket/reference/helloworld/HelloWorld.html
	
The second parameter to the ``Label`` component's constructor is the model data for the Label, providing content that replaces any text inside the ``<span>`` tag to which the ``Label`` is associated. The model data passed to the ``Label`` constructor above is apparently a String. Internally ``Label`` creates a Model for the String. :ref:`Model<models--model-label>` is a simple default implementation of IModel.


.. todo:: replace with real code

.. _models--model-label:

Thus instead we could have created our label this way::

	add(new Label("message", new Model<String>("Hello World!")));
	
or::

	add(new Label("message", Model.of("Hello World!")));


The ``Label`` constructor that takes a ``String`` is simply a convenience.



Dynamic Models
--------------

The data we gave to the model in the previous example, the string "Hello World", is constant. No matter how many times Wicket asks for the model data, it will get the same thing. Now consider a slightly more complex example::

	Label name = new Label ("name", Model.of(person.getName()));
	
The model data is still a String, the value of ``person.getName()`` is set at the time the model is created. Recall that Java strings are immutable: this string will never change. Even if ``person.getName()`` would later return a different value, the model data is unchanged. So the page will still display the old value to the user even if it is reloaded. Models like this, whose values never change, are known as *static* models.

In many cases the underlying data can change, and you want the user to see those changes. For example, the user might use a form to change a person's name. Models which can automatically reflect change are known as *dynamic* models. While the :ref:`Model<models--model-label>` class is static, most of the other core Wicket model classes are dynamic.

It's instructive to see how to make a dynamic model by subclassing Model.

.. includecode:: ../../../models/src/main/java/org/apache/wicket/reference/models/dynamic/CustomModelFormPage.java#customModel

It would be inconvenient to have to do this for every component that needs a dynamic model. Instead, you can use the :ref:`PropertyModel<models--propertymodel-label>` class or one of the other classes described below.

.. _models--propertymodel-label:

Property Models
---------------

The PropertyModel class allows you to create a model that accesses a particular property of its associated model object at runtime. This property is accessed using a simple expression language with a dot notation (e.g. ``'name'`` means property ``'name'``, and ``'person.name'`` means property name of object person). The PropertyModel constructor looks like:

.. literalinclude:: ../../../../wicket-core/src/main/java/org/apache/wicket/model/PropertyModel.java
	:start-after: */
	:end-before: {
	:lines: 90-
		
which takes a model object and a property expression. When the property model is asked for its value by the framework, it will use the property expression to access the model object's property. For example, if we have a Java Bean or "POJO" (Plain Old Java Object) like this:

.. includecode:: ../../../models/src/main/java/org/apache/wicket/reference/models/dynamic/Person.java#classOnly

then the property expression "name" can be used to access the "name" property of any Person object via the ``getName()`` getter method.

.. todo:: replace with real code

::

	personForm.add(new RequiredTextField("personName", new PropertyModel(person, "name")));

Nested property expressions are possible as well. You can access sub-properties via reflection using a dotted path notation, which means the property expression ``'person.name'`` is equivalent to calling ``getPerson().getName()`` on the given model object.

.. warning::

	If the Field is accesible and has the same name, the ``PropertyModel`` would try to access the field first.

There are three principal reasons why you might use PropertyModel instead of Model:

* PropertyModel instances are dynamic
* the property expression language is more compact than the analogous Java code
* it's much simpler to create a property model than to subclass Model












Compound Property Models
------------------------

Compound models allow containers to share models with their children. This saves memory, but more importantly, it makes replication of models much cheaper in a clustered environment. The basic idea is that the contained components usually want model data that can be easily derived at runtime from the model of their container. So, give the contained components no explicit model, and when a model is needed, Wicket will search up the containment hierarchy for a compound model. The compound model can retrieve model data for any of its contained components.

``CompoundPropertyModel`` is the most commonly used compound model. An instance of this class uses the name of the contained component as a property expression to retrieve data from its own model data.

To use a ``CompoundPropertyModel``, simply set one as the model for a container, such as a Form or a Page. Create the contained components with no model of their own. Insure that the component identifier names match the appropriate property names.

Here's a simple example using a ``CompoundPropertyModel``. Suppose we have a Person class, with two properties: Name and Age. We want a simple form for the user to edit a Person.

.. includecode:: ../../../models/src/main/java/org/apache/wicket/reference/models/compound/CompoundModelPanel.java#form

.. note::

	A complete working example would require a save button and so forth but the use of a compound model doesn't change those.

The component name can in fact be a more complicated property expression. Suppose for example that the Person class also has an address property, of class Address, and that class in turn has a city property. To define this field in the form we can do this:

.. includecode:: ../../../models/src/main/java/org/apache/wicket/reference/models/compound/CompoundModelPanel.java#addressCity

The corresponding input field in the html must have a wicket id of ``'address.city'``. This works, but it does expose the internal structure of the model data in the html. ``CompoundPropertyModel`` has a method that can be used to rectify this.

The model associates a different property expression with the component being bound.

.. todo:: replace with real code

::

	public <S> IModel<S> bind(String property)
	
With this association in place the child component can have whatever name we like, rather than having the match the property expression.

To use ``CompoundPropertyModel.bind`` for the city field discussed above we might do something like this:

.. includecode:: ../../../models/src/main/java/org/apache/wicket/reference/models/compound/CompoundModelBindPanel.java#bind
	
Also, note that if you are using a component that you do not want to reference the compound property model, but is a child of the form, that you define a model for that component. For example:

.. todo:: replace with real code

::

	// throws exception
	personForm.add(new Label("non-compound-model-reference"));
	// does not throw an exception
	personForm.add(new Label("non-compound-model-reference", new Model<String>()));




















Ignore Following Stuff
----------------------





Models are a important part of any wicket application. Despite it's simple interface its a complex topic. But let's start with some easy examples.

A very simple Model
-------------------

There is a simple model implementation, which can hold any data, which is serializable (see :ref:`models--detach-label`). This implementation implements two methods from the IModel interface for interacting with the model value.

.. includecode:: ../../../models/src/main/java/org/apache/wicket/reference/models/SerializableModelPage.java#docu

This examples shows an easy way to create a model instance for a value and how the value can be changed afterwards. The ``Label`` component accepts any serializable model value (not only strings, see :doc:`converter`).

TODO
-------------------

.. todo:: custom detach
.. todo:: cascading models

.. _models--detach-label:

Model and detach (TODO)
-----------------------

As any page contains mainly components and models. Most data is stored in models, it is important to know, that models are detached after the page is rendered (see :doc:`requestcycle`).  to remove anything from the page which is not needed anymore. 



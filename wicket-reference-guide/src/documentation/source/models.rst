Model
============================
.. toctree::
   :maxdepth: 3

Models are a important part of any wicket application. Despite it's simple interface its a complex topic. But let's start with some easy examples.

A very simple Model
-------------------

There is a simple model implementation, which can hold any data, which is serializable (see :ref:`models--detach-label`). This implementation implements two methods from the IModel interface for interacting with the model value.

.. includecode:: ../../../models/src/main/java/org/apache/wicket/reference/models/SerializableModelPage.java#docu

This examples shows an easy way to create a model instance for a value and how the value can be changed afterwards. The Label component accepts any model value (not only strings, see :doc:`converter`).

TODO
-------------------

.. todo:: custom detach
.. todo:: cascading models

.. _models--detach-label:

Model and detach (TODO)
-------------------

As any page contains mainly components and models. Most data is stored in models, it is important to know, that models are detached after the page is rendered (see :doc:`requestcycle`).  to remove anything from the page which is not needed anymore. 



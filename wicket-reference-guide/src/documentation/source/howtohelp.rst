How to contribute to this documentation
=======================================

If you want to help to improve this documentation here is how to do it:

Get the sources
---------------

The sources are hosted in the same Git repository as Apache Wicket's code::

    $ git clone http://git-wip-us.apache.org/repos/asf/wicket.git
    
At the moment they are in branch with name `reference-guide`::

    $ git checkout reference-guide
    
Edit the sources
----------------

Maven module `wicket-reference-guide` contains few sample applications and the documentation itself::

    $ cd wicket-reference-guide/
    $ ls
       helloworld/
       src/documentation/
       stateless/

The documentation is build with Sphinx documentation tool (http://sphinx-doc.org/). It uses `reStructured Text` as markup. 
More about the syntax can be found at `Quick Ref <http://docutils.sourceforge.net/docs/user/rst/quickref.html>`_ and `Full documentation <http://docutils.sourceforge.net/rst.html>`_.

Improve the documentation in `src/documentation/source/*.rst` files and send us the patch when ready::

    $ git diff > updated-documentation.patch


Check the changes
-----------------

This step is optional. Do it only if you want to see how your changes will look like.

1. Install `Python <http://www.python.org/>`_. This will provide `easy_install` command.
2. Install `Sphinx <http://sphinx-doc.org/>`_::

    $ easy_install -U Sphinx

3. Build the documentation::

    $ cd wicket-reference-guide/src/documentation/
    $ make html

4. See your changes in a browser::

    $ google-chrome build/html/index.html


Follow the documentation process
--------------------------------

To follow how this documentation evolves you can subcribe with your feed reader to https://github.com/apache/wicket/commits/reference-guide.atom


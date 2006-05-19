/*
 * Closure.java
 * 
 * Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License Version 1.0
 * (the "License"). You may not use this file except in compliance with the
 * License. A copy of the License is available at http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original Code is
 * Sun Microsystems, Inc. Portions Copyright 2000-2001 Sun Microsystems, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): Thomas Ball
 * 
 * Version: $Revision$
 */

package wicket.util.lang;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.ClassName;
import org.netbeans.modules.classfile.ConstantPool;

import wicket.WicketRuntimeException;
import wicket.util.collections.ArrayListStack;

/**
 * Closure: report all classes which a given class references in one way or
 * another. Note: this utility won't find classes which are dynamically loaded.
 * 
 * @author Thomas Ball
 * @author Jonathan Locke
 */
// TODO Post 1.2: Add this to wicket.util.lang
public abstract class AbstractClassClosure
{
	/** Closure of classes referenced by the class passed to the constructor */
	final Set<String> closure = new HashSet<String>();

	/**
	 * Construct.
	 * 
	 * @param classes
	 *            The root classes to find the closure of
	 * @param includeJDK
	 *            True to include JDK classes
	 */
	public AbstractClassClosure(final List/* <Class> */classes, final boolean includeJDK)
	{
		final Set<String> visited = new HashSet<String>();
		final ArrayListStack stack = new ArrayListStack();
		ClassLoader classloader = null;
		for (final Iterator iterator = classes.iterator(); iterator.hasNext();)
		{
			final Class c = (Class)iterator.next();
			if(classloader == null) classloader = c.getClassLoader(); 
			final ClassName classname = ClassName.getClassName(c.getName().replace('.', '/'));
			stack.push(classname);
			visited.add(classname.getExternalName());
		}

		if(classloader == null)
		{
			classloader = getClass().getClassLoader();
			if(classloader == null) classloader = ClassLoader.getSystemClassLoader();
		}
		
		while (!stack.empty())
		{
			// Add class to closure.
			final ClassName classname = (ClassName)stack.pop();
			final InputStream in = inputStreamForClassName(classloader, classname.getType());
			try
			{
				final ClassFile classfile = new ClassFile(in);
				closure.add(classfile.getName().getExternalName());

				final ConstantPool pool = classfile.getConstantPool();
				final Iterator references = pool.getAllClassNames().iterator();
				while (references.hasNext())
				{
					final ClassName classnameReference = (ClassName)references.next();
					final String name = classnameReference.getExternalName();
					if (name.indexOf('[') != -1)
					{
						// skip arrays
					}
					else if (!includeJDK
							&& (name.startsWith("java.") || name.startsWith("javax.")
									|| name.startsWith("sun.") || name.startsWith("com.sun.corba")
									|| name.startsWith("com.sun.image")
									|| name.startsWith("com.sun.java.swing")
									|| name.startsWith("com.sun.naming") || name
									.startsWith("com.sun.security")))
					{
						// if directed, skip JDK references
					}
					else
					{
						final boolean isNew = visited.add(name);
						if (isNew)
						{
							stack.push(classnameReference);
						}
					}
				}

				// Get the input stream a second time to add to JAR
				final InputStream in2 = inputStreamForClassName(classloader,classname.getType());
				try
				{
					addClass(classfile.getName().getInternalName(), in2);
				}
				finally
				{
					in2.close();
				}
			}
			catch (IOException e)
			{
				throw new WicketRuntimeException(e);
			}
			finally
			{
				try
				{
					in.close();
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		}
	}

	/**
	 * @return Iterator over all class names in the closure
	 */
	public Iterator/* <String> */dependencies()
	{
		return closure.iterator();
	}

	/**
	 * Called for each class added to the closure
	 * 
	 * @param name
	 *            The class name
	 * @param is
	 *            The input stream to the class
	 */
	protected void addClass(String name, InputStream is)
	{
	}

	/**
	 * @param classname
	 *            The class name
	 * @return Class contents as a stream
	 */
	protected InputStream inputStreamForClassName(ClassLoader loader, String classname)
	{
		InputStream in = loader.getResourceAsStream(
				classname + ".class");
		if (in == null)
		{
			throw new IllegalStateException("Unable to find class " + classname);
		}
		return in;
	}
}

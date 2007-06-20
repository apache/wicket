/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.guice;

import java.lang.reflect.Field;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.proxy.LazyInitProxyFactory;

import com.google.inject.Guice;
import com.google.inject.ImplementedBy;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Injects fields/members of components using Guice.
 * <p>
 * Add this to your application in its {@link Application#init()} method like
 * so:
 * <pre>
 * addComponentInstantiationListener(new GuiceComponentInjector(this));
 * </pre>
 * <p>
 * There are different constructors for this object depending on how you want to
 * wire things. See the javadoc for the constructors for more information.
 * 
 * @author Alastair Maw
 */
public class GuiceComponentInjector implements IComponentInstantiationListener
{
	/**
	 * Creates a new Wicket GuiceComponentInjector instance.
	 * <p>
	 * Internally this will create a new Guice {@link Injector} instance, with
	 * no {@link Module} instances. This is only useful if your beans have
	 * appropriate {@link ImplementedBy} annotations on them so that they can be
	 * automatically picked up with no extra configuration code.
	 * 
	 * @param app
	 */
	public GuiceComponentInjector(Application app)
	{
		this(app, new Module[0]);
	}
	
	/**
	 * Creates a new Wicket GuiceComponentInjector instance, using the supplied
	 * Guice {@link Module} instances to create a new Guice {@link Injector}
	 * instance internally.
	 * 
	 * @param app
	 * @param modules
	 */
	public GuiceComponentInjector(Application app, Module ... modules)
	{
		this(app, Guice.createInjector(modules));
	}

	/**
	 * Creates a new Wicket GuiceComponentInjector instance, using the provided
	 * Guice {@link Injector} instance.
	 * 
	 * @param app
	 * @param modules
	 */
	public GuiceComponentInjector(Application app, Injector injector)
	{
		app.setMetaData(GuiceInjectorHolder.INJECTOR_KEY, new GuiceInjectorHolder(injector));
	}
	
	public void onInstantiation(Component component)
	{
		Class<?> current = component.getClass();
		do
		{
			Field[] currentFields = current.getDeclaredFields();
			for (final Field field : currentFields)
			{
				if (field.getAnnotation(Inject.class) != null)
				{
					Object proxy = LazyInitProxyFactory.createProxy(field.getType(), new GuiceProxyTargetLocator(field.getType()));
					try {
						if (!field.isAccessible())
						{
							field.setAccessible(true);
						}
						field.set(component, proxy);
					}
					catch (IllegalAccessException e) {
						throw new WicketRuntimeException("Error Guice-injecting field " + field.getName() + " in " + component, e);
					}
				}
			}
			current = current.getSuperclass();
		}
		// use null check just in case Component is in a different classloader.
		while (current != null && current != Component.class);
	}
}

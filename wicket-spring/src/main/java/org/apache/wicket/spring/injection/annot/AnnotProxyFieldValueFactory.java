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
package org.apache.wicket.spring.injection.annot;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.wicket.injection.IFieldValueFactory;
import org.apache.wicket.proxy.LazyInitProxyFactory;
import org.apache.wicket.spring.ISpringContextLocator;
import org.apache.wicket.spring.SpringBeanLocator;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Generics;
import org.apache.wicket.util.string.Strings;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * {@link IFieldValueFactory} that uses {@link LazyInitProxyFactory} to create proxies for Spring
 * dependencies based on the {@link SpringBean} annotation applied to a field. This class is usually
 * used by the {@link SpringComponentInjector} to inject objects with lazy init proxies. However,
 * this class can be used on its own to create proxies for any field decorated with a
 * {@link SpringBean} annotation.
 * <p>
 * Example:
 * 
 * <pre>
 * IFieldValueFactory factory = new AnnotProxyFieldValueFactory(contextLocator);
 * field = obj.getClass().getDeclaredField(&quot;dependency&quot;);
 * IDependency dependency = (IDependency)factory.getFieldValue(field, obj);
 * </pre>
 * 
 * In the example above the
 * 
 * <code>dependency</code> object returned is a lazy init proxy that will look up the actual
 * IDependency bean from spring context upon first access to one of the methods.
 * <p>
 * This class will also cache any produced proxies so that the same proxy is always returned for the
 * same spring dependency. This helps cut down on session size beacause proxies for the same
 * dependency will not be serialized twice.
 * 
 * @see LazyInitProxyFactory
 * @see SpringBean
 * @see SpringBeanLocator
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @author Istvan Devai
 */
public class AnnotProxyFieldValueFactory implements IFieldValueFactory
{
	private final ISpringContextLocator contextLocator;

	private final ConcurrentMap<SpringBeanLocator, Object> cache = Generics.newConcurrentHashMap();

	private final ConcurrentMap<Class<?>, String> beanNameCache = Generics.newConcurrentHashMap();

	private final boolean wrapInProxies;

	/**
	 * @param contextLocator
	 *            spring context locator
	 */
	public AnnotProxyFieldValueFactory(final ISpringContextLocator contextLocator)
	{
		this(contextLocator, true);
	}

	/**
	 * @param contextLocator
	 *            spring context locator
	 * @param wrapInProxies
	 *            whether or not wicket should wrap dependencies with specialized proxies that can
	 *            be safely serialized. in most cases this should be set to true.
	 */
	public AnnotProxyFieldValueFactory(final ISpringContextLocator contextLocator,
		final boolean wrapInProxies)
	{
		this.contextLocator = Args.notNull(contextLocator, "contextLocator");
		this.wrapInProxies = wrapInProxies;
	}

	@Override
	public Object getFieldValue(final Field field, final Object fieldOwner)
	{
		if (supportsField(field))
		{
			SpringBean annot = field.getAnnotation(SpringBean.class);

			String name;
			boolean required;
			if (annot != null)
			{
				name = annot.name();
				required = annot.required();
			}
			else
			{
				Named named = field.getAnnotation(Named.class);
				name = named != null ? named.value() : "";
				required = false;
			}

			String beanName = getBeanName(field, name, required);

			if (beanName == null)
			{
				return null;
			}

			SpringBeanLocator locator = new SpringBeanLocator(beanName, field.getType(),
				contextLocator);

			// only check the cache if the bean is a singleton
			Object cachedValue = cache.get(locator);
			if (cachedValue != null)
			{
				return cachedValue;
			}

			Object target;
			try
			{
				// check whether there is a bean with the provided properties
				target = locator.locateProxyTarget();
			}
			catch (IllegalStateException isx)
			{
				if (required)
				{
					throw isx;
				}
				else
				{
					return null;
				}
			}

			if (wrapInProxies)
			{
				target = LazyInitProxyFactory.createProxy(field.getType(), locator);
			}

			// only put the proxy into the cache if the bean is a singleton
			if (locator.isSingletonBean())
			{
				Object tmpTarget = cache.putIfAbsent(locator, target);
				if (tmpTarget != null)
				{
					target = tmpTarget;
				}
			}
			return target;
		}
		return null;
	}

	/**
	 * 
	 * @param field
	 * @return bean name
	 */
	private String getBeanName(final Field field, String name, boolean required)
	{

		if (Strings.isEmpty(name))
		{
			Class<?> fieldType = field.getType();
			name = beanNameCache.get(fieldType);
			if (name == null)
			{
				name = getBeanNameOfClass(contextLocator.getSpringContext(), fieldType, required);

				if (name != null)
				{
					String tmpName = beanNameCache.putIfAbsent(fieldType, name);
					if (tmpName != null)
					{
						name = tmpName;
					}
				}
			}
		}

		return name;
	}

	/**
	 * Returns the name of the Bean as registered to Spring. Throws IllegalState exception if none
	 * or more than one beans are found.
	 * 
	 * @param ctx
	 *            spring application context
	 * @param clazz
	 *            bean class
	 * @param required
	 *            true if the value is required
	 * @throws IllegalStateException
	 * @return spring name of the bean
	 */
	private String getBeanNameOfClass(final ApplicationContext ctx, final Class<?> clazz,
		final boolean required)
	{
		// get the list of all possible matching beans
		List<String> names = new ArrayList<String>(
			Arrays.asList(BeanFactoryUtils.beanNamesForTypeIncludingAncestors(ctx, clazz)));

		// filter out beans that are not candidates for autowiring
		if (ctx instanceof AbstractApplicationContext)
		{
			Iterator<String> it = names.iterator();
			while (it.hasNext())
			{
				final String possibility = it.next();
				BeanDefinition beanDef = getBeanDefinition(
					((AbstractApplicationContext)ctx).getBeanFactory(), possibility);
				if (BeanFactoryUtils.isFactoryDereference(possibility) ||
					possibility.startsWith("scopedTarget.") ||
					(beanDef != null && !beanDef.isAutowireCandidate()))
				{
					it.remove();
				}
			}
		}

		if (names.isEmpty())
		{
			if (required)
			{
				throw new IllegalStateException("bean of type [" + clazz.getName() + "] not found");
			}
			return null;
		}
		else if (names.size() > 1)
		{
			if (ctx instanceof AbstractApplicationContext)
			{
				List<String> primaries = new ArrayList<>();
				for (String name : names)
				{
					BeanDefinition beanDef = getBeanDefinition(
						((AbstractApplicationContext)ctx).getBeanFactory(), name);
					if (beanDef instanceof AbstractBeanDefinition)
					{
						if (beanDef.isPrimary())
						{
							primaries.add(name);
						}
					}
				}
				if (primaries.size() == 1)
				{
					return primaries.get(0);
				}
			}
			StringBuilder msg = new StringBuilder();
			msg.append("More than one bean of type [");
			msg.append(clazz.getName());
			msg.append("] found, you have to specify the name of the bean ");
			msg.append("(@SpringBean(name=\"foo\")) or (@Named(\"foo\") if using @javax.inject classes) in order to resolve this conflict. ");
			msg.append("Matched beans: ");
			msg.append(Strings.join(",", names.toArray(new String[names.size()])));
			throw new IllegalStateException(msg.toString());
		}
		else
		{
			return names.get(0);
		}
	}

	private BeanDefinition getBeanDefinition(final ConfigurableListableBeanFactory beanFactory,
		final String name)
	{
		if (beanFactory.containsBeanDefinition(name))
		{
			return beanFactory.getBeanDefinition(name);
		}
		else
		{
			BeanFactory parent = beanFactory.getParentBeanFactory();
			if ((parent != null) && (parent instanceof ConfigurableListableBeanFactory))
			{
				return getBeanDefinition((ConfigurableListableBeanFactory)parent, name);
			}
			else
			{
				return null;
			}
		}
	}

	/**
	 * @see org.apache.wicket.injection.IFieldValueFactory#supportsField(java.lang.reflect.Field)
	 */
	@Override
	public boolean supportsField(final Field field)
	{
		return field.isAnnotationPresent(SpringBean.class) || field.isAnnotationPresent(Inject.class);
	}
}

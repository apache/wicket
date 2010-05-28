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
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.injection.IFieldValueFactory;
import org.apache.wicket.proxy.LazyInitProxyFactory;
import org.apache.wicket.spring.ISpringContextLocator;
import org.apache.wicket.spring.SpringBeanLocator;
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
 * used by the {@link AnnotSpringInjector} to inject objects with lazy init proxies. However, this
 * class can be used on its own to create proxies for any field decorated with a {@link SpringBean}
 * annotation.
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
 * 
 */
public class AnnotProxyFieldValueFactory implements IFieldValueFactory
{
	private final ISpringContextLocator contextLocator;

	private final ConcurrentHashMap<SpringBeanLocator, Object> cache = Generics
			.newConcurrentHashMap();

	private final ConcurrentHashMap<Class< ? >, String> beanNameCache = Generics
			.newConcurrentHashMap();

	private final boolean wrapInProxies;

	/**
	 * @param contextLocator
	 *            spring context locator
	 */
	public AnnotProxyFieldValueFactory(ISpringContextLocator contextLocator)
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
	public AnnotProxyFieldValueFactory(ISpringContextLocator contextLocator, boolean wrapInProxies)
	{
		if (contextLocator == null)
		{
			throw new IllegalArgumentException("[contextLocator] argument cannot be null");
		}
		this.contextLocator = contextLocator;
		this.wrapInProxies = wrapInProxies;
	}

	/**
	 * @see org.apache.wicket.injection.IFieldValueFactory#getFieldValue(java.lang.reflect.Field,
	 *      java.lang.Object)
	 */
	public Object getFieldValue(Field field, Object fieldOwner)
	{
		if (supportsField(field))
		{
			SpringBeanLocator locator = new SpringBeanLocator(getBeanName(field), field.getType(),
					contextLocator);

			// only check the cache if the bean is a singleton
			Object cachedValue = cache.get(locator);
			if (cachedValue != null)
			{
				return cachedValue;
			}

			final Object target;
			if (wrapInProxies)
			{
				target = LazyInitProxyFactory.createProxy(field.getType(), locator);
			}
			else
			{
				target = locator.locateProxyTarget();
			}

			// only put the proxy into the cache if the bean is a singleton
			if (locator.isSingletonBean())
			{
				cache.put(locator, target);
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
	private String getBeanName(final Field field)
	{
		SpringBean annot = field.getAnnotation(SpringBean.class);

		String name = annot.name();
		if (Strings.isEmpty(name))
		{
			name = beanNameCache.get(field.getType());
			if (name == null)
			{
				name = getBeanNameOfClass(contextLocator.getSpringContext(), field.getType());
				beanNameCache.put(field.getType(), name);
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
	 * @throws IllegalStateException
	 * @return spring name of the bean
	 */
	private final String getBeanNameOfClass(ApplicationContext ctx, Class< ? > clazz)
	{
		// get the list of all possible matching beans
		List<String> names = new ArrayList<String>(Arrays.asList(BeanFactoryUtils
				.beanNamesForTypeIncludingAncestors(ctx, clazz)));

		// filter out beans that are not candidates for autowiring
		Iterator<String> it = names.iterator();
		while (it.hasNext())
		{
			final String possibility = it.next();
			if (ctx instanceof AbstractApplicationContext)
			{
				BeanDefinition beanDef = getBeanDefinition(((AbstractApplicationContext)ctx)
						.getBeanFactory(), possibility);
				if (BeanFactoryUtils.isFactoryDereference(possibility) ||
						possibility.startsWith("scopedTarget.") || !beanDef.isAutowireCandidate())
				{
					it.remove();
				}
			}
		}

		if (names.isEmpty())
		{
			throw new IllegalStateException("bean of type [" + clazz.getName() + "] not found");
		}
		else if (names.size() > 1)
		{
			if (ctx instanceof AbstractApplicationContext)
			{
				List<String> primaries = new ArrayList<String>();
				for (String name : names)
				{
					BeanDefinition beanDef = getBeanDefinition(((AbstractApplicationContext)ctx)
							.getBeanFactory(), name);
					if (beanDef instanceof AbstractBeanDefinition)
					{
						if (((AbstractBeanDefinition)beanDef).isPrimary())
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
			msg.append("(@SpringBean(name=\"foo\")) in order to resolve this conflict. ");
			msg.append("Matched beans: ");
			msg.append(Strings.join(",", names.toArray(new String[0])));
			throw new IllegalStateException(msg.toString());
		}
		else
		{
			return names.get(0);
		}
	}

	private BeanDefinition getBeanDefinition(ConfigurableListableBeanFactory beanFactory,
			String name)
	{
		if (beanFactory.containsBeanDefinition(name))
		{
			return beanFactory.getBeanDefinition(name);
		}
		else
		{
			BeanFactory parent = beanFactory.getParentBeanFactory();
			if (parent != null && parent instanceof ConfigurableListableBeanFactory)
			{
				return getBeanDefinition(beanFactory, name);
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
	public boolean supportsField(Field field)
	{
		return field.isAnnotationPresent(SpringBean.class);
	}
}

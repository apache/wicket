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
package org.apache.wicket.spring;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.proxy.IProxyTargetLocator;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.lang.WicketObjects;
import org.apache.wicket.util.string.Strings;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * Implementation of {@link IProxyTargetLocator} that can locate beans within a spring application
 * context. Beans are looked up by the combination of name and type, if name is omitted only type is
 * used.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @author Istvan Devai
 */
public class SpringBeanLocator implements IProxyTargetLocator
{
	private static final long serialVersionUID = 1L;

	// Weak reference so we don't hold up WebApp classloader garbage collection.
	private transient WeakReference<Class< ? >> beanTypeCache;

	private final String beanTypeName;

	private String beanName;

	private ISpringContextLocator springContextLocator;

	private Boolean singletonCache = null;

	/**
	 * Constructor
	 * 
	 * @param beanType
	 *            bean class
	 * @param locator
	 *            spring context locator
	 */
	public SpringBeanLocator(final Class< ? > beanType, final ISpringContextLocator locator)
	{
		this(null, beanType, locator);
	}

	/**
	 * Constructor
	 * 
	 * @param beanName
	 *            bean name
	 * @param beanType
	 *            bean class
	 * @param locator
	 *            spring context locator
	 */
	public SpringBeanLocator(final String beanName, final Class< ? > beanType,
			final ISpringContextLocator locator)
	{
		if (locator == null)
		{
			throw new IllegalArgumentException("[locator] argument cannot be null");
		}
		if (beanType == null)
		{
			throw new IllegalArgumentException("[beanType] argument cannot be null");
		}

		beanTypeCache = new WeakReference<Class< ? >>(beanType);
		beanTypeName = beanType.getName();
		springContextLocator = locator;
		this.beanName = beanName;
		springContextLocator = locator;
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
	private final String getBeanNameOfClass(final ApplicationContext ctx, final Class< ? > clazz)
	{
		// get the list of all possible matching beans
		List<String> names = new ArrayList<String>(Arrays.asList(BeanFactoryUtils
				.beanNamesForTypeIncludingAncestors(ctx, clazz)));
		Iterator<String> it = names.iterator();

		// filter out beans that are not candidates for autowiring
		while (it.hasNext())
		{
			final String possibility = it.next();
			BeanDefinition beanDef = ((AbstractApplicationContext)ctx).getBeanFactory()
					.getBeanDefinition(possibility);
			if (BeanFactoryUtils.isFactoryDereference(possibility) ||
					possibility.startsWith("scopedTarget.") || !beanDef.isAutowireCandidate())
			{
				it.remove();
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
				for (String name : names)
				{
					BeanDefinition beanDef = ((AbstractApplicationContext)ctx).getBeanFactory()
							.getBeanDefinition(name);
					if (beanDef instanceof AbstractBeanDefinition)
					{
						if (((AbstractBeanDefinition)beanDef).isPrimary())
						{
							return name;
						}
					}
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

	/**
	 * @return returns whether the bean (the locator is supposed to istantiate) is a singleton or
	 *         not
	 */
	public boolean isSingletonBean()
	{
		if (singletonCache == null)
		{
			singletonCache = Boolean.valueOf(getSpringContext().isSingleton(getBeanName()));
		}
		return singletonCache.booleanValue();
	}

	/**
	 * @return bean class this locator is configured with
	 */
	public Class< ? > getBeanType()
	{
		Class< ? > clazz = beanTypeCache == null ? null : (Class< ? >)beanTypeCache.get();
		if (clazz == null)
		{
			beanTypeCache = new WeakReference<Class< ? >>(clazz = WicketObjects
					.resolveClass(beanTypeName));
			if (clazz == null)
			{
				throw new RuntimeException("SpringBeanLocator could not find class [" +
						beanTypeName + "] needed to locate the [" +
						((beanName != null) ? (beanName) : ("bean name not specified")) + "] bean");
			}
		}
		return clazz;
	}

	/**
	 * @see org.apache.wicket.proxy.IProxyTargetLocator#locateProxyTarget()
	 */
	public Object locateProxyTarget()
	{
		final ApplicationContext context = getSpringContext();

		if (beanName != null && beanName.length() > 0)
		{
			return lookupSpringBean(context, beanName, getBeanType());
		}
		else
		{
			return lookupSpringBean(context, getBeanType());
		}
	}

	private ApplicationContext getSpringContext()
	{
		final ApplicationContext context = springContextLocator.getSpringContext();

		if (context == null)
		{
			throw new IllegalStateException("spring application context locator returned null");
		}
		return context;
	}

	/**
	 * @return bean name this locator is configured with
	 */
	public final String getBeanName()
	{
		if (beanName == null || "".equals(beanName))
		{
			beanName = getBeanNameOfClass(getSpringContext(), getBeanType());
		}
		return beanName;
	}

	/**
	 * @return context locator this locator is configured with
	 */
	public final ISpringContextLocator getSpringContextLocator()
	{
		return springContextLocator;
	}

	/**
	 * Looks up a bean by its class. Throws IllegalState exception if none or more than one beans
	 * are found.
	 * 
	 * @param ctx
	 *            spring application context
	 * 
	 * @param clazz
	 *            bean class
	 * @throws IllegalStateException
	 * @return found bean
	 */
	private final Object lookupSpringBean(ApplicationContext ctx, Class< ? > clazz)
	{
		return lookupSpringBean(ctx, getBeanNameOfClass(ctx, clazz), clazz);
	}

	/**
	 * Looks up a bean by its name and class. Throws IllegalState exception if bean not found.
	 * 
	 * @param ctx
	 *            spring application context
	 * 
	 * @param name
	 *            bean name
	 * @param clazz
	 *            bean class
	 * @throws IllegalStateException
	 * @return found bean
	 */
	private static Object lookupSpringBean(ApplicationContext ctx, String name, Class< ? > clazz)
	{
		try
		{
			return ctx.getBean(name, clazz);
		}
		catch (NoSuchBeanDefinitionException e)
		{
			throw new IllegalStateException("bean with name [" + name + "] and class [" +
					clazz.getName() + "] not found");
		}
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj)
	{
		if (obj instanceof SpringBeanLocator)
		{
			SpringBeanLocator other = (SpringBeanLocator)obj;
			return beanTypeName.equals(other.beanTypeName) &&
					Objects.equal(beanName, other.beanName);
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		int hashcode = beanTypeName.hashCode();
		if (getBeanName() != null)
		{
			hashcode = hashcode + (127 * beanName.hashCode());
		}
		return hashcode;
	}
}

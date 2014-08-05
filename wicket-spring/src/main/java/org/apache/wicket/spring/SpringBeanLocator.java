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

import org.apache.wicket.proxy.IProxyTargetLocator;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

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
	private transient WeakReference<Class<?>> beanTypeCache;

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
	public SpringBeanLocator(final Class<?> beanType, final ISpringContextLocator locator)
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
	public SpringBeanLocator(final String beanName, final Class<?> beanType,
		final ISpringContextLocator locator)
	{
		Args.notNull(locator, "locator");
		Args.notNull(beanType, "beanType");

		beanTypeCache = new WeakReference<Class<?>>(beanType);
		beanTypeName = beanType.getName();
		springContextLocator = locator;
		this.beanName = beanName;
		springContextLocator = locator;
	}

	/**
	 * @return returns whether the bean (the locator is supposed to istantiate) is a singleton or
	 *         not
	 */
	public boolean isSingletonBean()
	{
		if (singletonCache == null)
		{
			singletonCache = getSpringContext().isSingleton(getBeanName());
		}
		return singletonCache;
	}

	/**
	 * @return bean class this locator is configured with
	 */
	public Class<?> getBeanType()
	{
		Class<?> clazz = beanTypeCache == null ? null : beanTypeCache.get();
		if (clazz == null)
		{
			beanTypeCache = new WeakReference<Class<?>>(
				clazz = WicketObjects.resolveClass(beanTypeName));
			if (clazz == null)
			{
				throw new RuntimeException("SpringBeanLocator could not find class [" +
					beanTypeName + "] needed to locate the [" +
					((beanName != null) ? (beanName) : ("bean name not specified")) + "] bean");
			}
		}
		return clazz;
	}

	@Override
	public Object locateProxyTarget()
	{
		final ApplicationContext context = getSpringContext();

		return lookupSpringBean(context, beanName, getBeanType());
	}

	/**
	 * 
	 * @return ApplicationContext
	 */
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
	 * Looks up a bean by its name and class. Throws IllegalState exception if bean not found.
	 * 
	 * @param ctx
	 *            spring application context
	 * 
	 * @param name
	 *            bean name
	 * @param clazz
	 *            bean class
	 * @throws java.lang.IllegalStateException
	 * @return found bean
	 */
	private Object lookupSpringBean(ApplicationContext ctx, String name, Class<?> clazz)
	{
		try
		{
			if (name == null)
			{
				return ctx.getBean(clazz);
			}
			else
			{
				return ctx.getBean(name, clazz);
			}
		}
		catch (NoSuchBeanDefinitionException e)
		{
			throw new IllegalStateException("bean with name [" + name + "] and class [" +
				clazz.getName() + "] not found", e);
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

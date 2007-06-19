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
import org.apache.wicket.util.lang.Objects;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;


/**
 * Implementation of {@link IProxyTargetLocator} that can locate beans within a
 * spring application context. Beans are looked up by the combination of name
 * and type, if name is omitted only type is used.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @author Istvan Devai
 */
public class SpringBeanLocator implements IProxyTargetLocator
{
	// Weak reference so we don't hold up WebApp classloader garbage collection.
	private transient WeakReference/*<Class>*/ beanTypeCache;

	private String beanTypeName;

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
	public SpringBeanLocator(Class beanType, ISpringContextLocator locator)
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
	public SpringBeanLocator(String beanName, Class beanType,
			ISpringContextLocator locator)
	{
		if (locator == null)
		{
			throw new IllegalArgumentException("[locator] argument cannot be null");
		}
		if (beanType == null)
		{
			throw new IllegalArgumentException("[beanType] argument cannot be null");
		}

		this.beanTypeCache = new WeakReference(beanType);
		this.beanTypeName = beanType.getName();
		this.springContextLocator = locator;
		this.beanName = beanName;
		this.springContextLocator = locator;
	}

	/**
	 * Returns the name of the Bean as registered to Spring. Throws IllegalState
	 * exception if none or more then one beans are found.
	 * 
	 * @param ctx
	 *            spring application context
	 * @param clazz
	 *            bean class
	 * @throws IllegalStateException
	 * @return spring name of the bean
	 */
	private final String getBeanNameOfClass(ApplicationContext ctx, Class clazz)
	{
		String[] names = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(ctx, clazz);
		if (names.length == 0)
		{
			throw new IllegalStateException("bean of type ["
					+ clazz.getName() + "] not found");
		}
		if (names.length > 1)
		{
			throw new IllegalStateException(
					"more then one bean of type ["
							+ clazz.getName()
							+ "] found, you have to specify the name of the bean (@SpringBean(name=\"foo\")) in order to resolve this conflict");
		}
		return names[0];
	}

	/**
	 * @return returns whether the bean (the locator is supposed to istantiate)
	 *         is a singleton or not
	 */
	public boolean isSingletonBean()
	{
		if (singletonCache == null)
		{
			singletonCache = Boolean.valueOf(getSpringContext()
					.isSingleton(getBeanName()));
		}
		return singletonCache.booleanValue();
	}

	/**
	 * @return bean class this locator is configured with
	 */
	public Class getBeanType()
	{
		Class clazz = beanTypeCache == null ? null : (Class)beanTypeCache.get();
		if (clazz == null)
		{
			try
			{
				/* 
				 * Need to make this scoped, rather than sticking it straight into the WeakReference,
				 * otherwise it might get garbage collected before we've even returned it!
				 */
				clazz = Class.forName(beanTypeName, true, Thread.currentThread()
						.getContextClassLoader());
				beanTypeCache = new WeakReference(clazz);
			}
			catch (ClassNotFoundException e)
			{
				throw new RuntimeException("SpringBeanLocator could not find class ["
						+ beanTypeName + "] needed to locate the ["
						+ ((beanName != null) ? (beanName) : ("bean name not specified"))
						+ "] bean", e);
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
			throw new IllegalStateException(
					"spring application context locator returned null");
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
	 * Looks up a bean by its class. Throws IllegalState exception if none or
	 * more then one beans are found.
	 * 
	 * @param ctx
	 *            spring application context
	 * 
	 * @param clazz
	 *            bean class
	 * @throws IllegalStateException
	 * @return found bean
	 */
	private final Object lookupSpringBean(ApplicationContext ctx, Class clazz)
	{
		return lookupSpringBean(ctx, getBeanNameOfClass(ctx, clazz), clazz);
	}

	/**
	 * Looks up a bean by its name and class. Throws IllegalState exception if
	 * bean not found.
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
	private static Object lookupSpringBean(ApplicationContext ctx, String name,
			Class clazz)
	{
		try
		{
			return ctx.getBean(name, clazz);
		}
		catch (NoSuchBeanDefinitionException e)
		{
			throw new IllegalStateException("bean with name ["
					+ name + "] and class [" + clazz.getName() + "] not found");
		}
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if (obj instanceof SpringBeanLocator)
		{
			SpringBeanLocator other = (SpringBeanLocator) obj;
			return beanTypeName.equals(other.beanTypeName)
					&& Objects.equal(beanName, other.beanName);
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		int hashcode = beanTypeName.hashCode();
		if (beanName != null)
		{
			hashcode = hashcode + (127 * beanName.hashCode());
		}
		return hashcode;
	}
}

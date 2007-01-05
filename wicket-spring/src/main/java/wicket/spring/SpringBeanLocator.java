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
package wicket.spring;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import wicket.proxy.IProxyTargetLocator;
import wicket.util.lang.Objects;

/**
 * Implementation of {@link IProxyTargetLocator} that can locate beans within a
 * spring application context. Beans are looked up by the combination of name
 * and type, if name is omitted only type is used.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class SpringBeanLocator implements IProxyTargetLocator
{
	private transient Class beanTypeCache;

	private String beanTypeName;

	private String beanName;

	private ISpringContextLocator springContextLocator;

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

		this.beanTypeCache = beanType;
		this.beanTypeName = beanType.getName();
		this.beanName = beanName;
		this.springContextLocator = locator;
	}

	/**
	 * @return bean class this locator is configured with
	 */
	public Class getBeanType()
	{
		if (beanTypeCache == null)
		{
			try
			{
				beanTypeCache = Class.forName(beanTypeName, true, Thread.currentThread()
						.getContextClassLoader());
			}
			catch (ClassNotFoundException e)
			{
				throw new RuntimeException("SpringBeanLocator could not find class ["
						+ beanTypeName + "] needed to locate the ["
						+ ((beanName != null) ? (beanName) : ("bean name not specified"))
						+ "] bean", e);
			}
		}
		return beanTypeCache;
	}

	/**
	 * @see wicket.proxy.IProxyTargetLocator#locateProxyTarget()
	 */
	public Object locateProxyTarget()
	{
		final ApplicationContext context = springContextLocator.getSpringContext();

		if (context == null)
		{
			throw new IllegalStateException(
					"spring application context locator returned null");
		}

		if (beanName != null && beanName.length() > 0)
		{
			return lookupSpringBean(context, beanName, getBeanType());
		}
		else
		{
			return lookupSpringBean(context, getBeanType());
		}
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
		Map beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(ctx, clazz);
		if (beans.size() == 0)
		{
			throw new IllegalStateException("bean of type ["
					+ clazz.getName() + "] not found");
		}
		if (beans.size() > 1)
		{
			// there are more then one bean of this class found, try to default
			// to the one with matching class name

			final String defaultName = clazz.getSimpleName();
			final Iterator entries = beans.entrySet().iterator();
			while (entries.hasNext())
			{
				Map.Entry beanDef = (Entry) entries.next();
				if (defaultName.equalsIgnoreCase((String) beanDef.getKey()))
				{
					return beanDef.getValue();
				}
			}

			// no default could be found, error out

			String msg = "more then one bean of type [["
					+ clazz.getName()
					+ "]] found, you have to specify the name of the bean (@SpringBean(name=\"foo\")) in order to resolve this conflict. Beans that match type [[";

			Iterator beanNames = beans.keySet().iterator();
			while (beanNames.hasNext())
			{
				String beanName = (String) beanNames.next();
				msg += beanName;
				if (beanNames.hasNext())
				{
					msg += ", ";
				}
			}

			msg += "]]";
			throw new IllegalStateException(msg);
		}
		return beans.values().iterator().next();
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

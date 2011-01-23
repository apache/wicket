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
package org.apache.wicket.spring.test;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.io.Resource;

/**
 * Mock application context object. This mock context allows easy creation of unit tests by allowing
 * the user to put bean instances into the context.
 * 
 * Only {@link #getBean(String)}, {@link #getBean(String, Class)}, and
 * {@link #getBeansOfType(Class)
 * } are implemented so far. Any other method throws
 * {@link UnsupportedOperationException}.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
@SuppressWarnings("unchecked")
public class ApplicationContextMock implements ApplicationContext, Serializable
{
	private static final long serialVersionUID = 1L;

	private final Map<String, Object> beans = new HashMap<String, Object>();

	/**
	 * puts bean with the given name into the context
	 * 
	 * @param name
	 * @param bean
	 */
	public void putBean(final String name, final Object bean)
	{
		if (beans.containsKey(name))
		{
			throw new IllegalArgumentException("a bean with name [" + name +
				"] has already been added to the context");
		}
		beans.put(name, bean);
	}

	/**
	 * puts bean with into the context. bean object's class name will be used as the bean name.
	 * 
	 * @param bean
	 */
	public void putBean(final Object bean)
	{
		putBean(bean.getClass().getName(), bean);
	}

	/**
	 * @see org.springframework.beans.factory.BeanFactory#getBean(java.lang.String)
	 */
	public Object getBean(final String name) throws BeansException
	{
		Object bean = beans.get(name);
		if (bean == null)
		{
			throw new NoSuchBeanDefinitionException(name);
		}
		return bean;
	}

	/**
	 * @see org.springframework.beans.factory.BeanFactory#getBean(java.lang.String, java.lang.Class)
	 */
	public Object getBean(final String name, final Class requiredType) throws BeansException
	{
		Object bean = getBean(name);
		if (!(requiredType.isAssignableFrom(bean.getClass())))
		{
			throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
		}
		return bean;
	}

	/**
	 * @see org.springframework.beans.factory.ListableBeanFactory#getBeansOfType(java.lang.Class)
	 */
	public Map getBeansOfType(final Class type) throws BeansException
	{
		Map found = new HashMap();

		for (Entry entry : beans.entrySet())
		{
			if (type.isAssignableFrom(entry.getValue().getClass()))
			{
				found.put(entry.getKey(), entry.getValue());
			}
		}

		return found;
	}

	/**
	 * @see org.springframework.context.ApplicationContext#getParent()
	 */
	public ApplicationContext getParent()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.context.ApplicationContext#getDisplayName()
	 */
	public String getDisplayName()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.context.ApplicationContext#getStartupDate()
	 */
	public long getStartupDate()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.context.ApplicationContext#publishEvent(org.
	 *      springframework.context.ApplicationEvent)
	 */
	public void publishEvent(final ApplicationEvent event)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.ListableBeanFactory#
	 *      containsBeanDefinition(java.lang.String)
	 */
	public boolean containsBeanDefinition(final String beanName)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.ListableBeanFactory# getBeanDefinitionCount()
	 */
	public int getBeanDefinitionCount()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.ListableBeanFactory# getBeanDefinitionNames()
	 */
	public String[] getBeanDefinitionNames()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.ListableBeanFactory#
	 *      getBeanNamesForType(java.lang.Class)
	 */
	public String[] getBeanNamesForType(final Class type)
	{
		ArrayList names = new ArrayList();
		for (Entry<String, Object> entry : beans.entrySet())
		{
			Object bean = entry.getValue();

			if (type.isAssignableFrom(bean.getClass()))
			{
				names.add(entry.getKey());
			}
		}
		return (String[])names.toArray(new String[names.size()]);
	}

	/**
	 * @see org.springframework.beans.factory.ListableBeanFactory#
	 *      getBeanNamesForType(java.lang.Class, boolean, boolean)
	 */
	public String[] getBeanNamesForType(final Class type, final boolean includePrototypes,
		final boolean includeFactoryBeans)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.ListableBeanFactory#getBeansOfType(java.lang.Class,
	 *      boolean, boolean)
	 */
	public Map getBeansOfType(final Class type, final boolean includePrototypes,
		final boolean includeFactoryBeans) throws BeansException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.BeanFactory#containsBean(java.lang. String)
	 */
	public boolean containsBean(final String name)
	{
		return beans.containsKey(name);
	}

	/**
	 * @see org.springframework.beans.factory.BeanFactory#isSingleton(java.lang. String)
	 */
	public boolean isSingleton(final String name) throws NoSuchBeanDefinitionException
	{
		return true;
	}

	/**
	 * @see org.springframework.beans.factory.BeanFactory#getType(java.lang.String)
	 */
	public Class getType(final String name) throws NoSuchBeanDefinitionException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.BeanFactory#getAliases(java.lang. String)
	 */
	public String[] getAliases(final String name) throws NoSuchBeanDefinitionException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.HierarchicalBeanFactory# getParentBeanFactory()
	 */
	public BeanFactory getParentBeanFactory()
	{
		return null;
	}

	/**
	 * @see org.springframework.context.MessageSource#getMessage(java.lang.String,
	 *      java.lang.Object[], java.lang.String, java.util.Locale)
	 */
	public String getMessage(final String code, final Object[] args, final String defaultMessage,
		final Locale locale)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.context.MessageSource#getMessage(java.lang.String,
	 *      java.lang.Object[], java.util.Locale)
	 */
	public String getMessage(final String code, final Object[] args, final Locale locale)
		throws NoSuchMessageException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.context.MessageSource#getMessage(org.springframework
	 *      .context.MessageSourceResolvable, java.util.Locale)
	 */
	public String getMessage(final MessageSourceResolvable resolvable, final Locale locale)
		throws NoSuchMessageException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.core.io.support.ResourcePatternResolver#getResources
	 *      (java.lang.String)
	 */
	public Resource[] getResources(final String locationPattern) throws IOException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.core.io.ResourceLoader#getResource(java.lang.String)
	 */
	public Resource getResource(final String location)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.context.ApplicationContext# getAutowireCapableBeanFactory()
	 */
	public AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.HierarchicalBeanFactory#
	 *      containsLocalBean(java.lang.String)
	 */
	public boolean containsLocalBean(final String arg0)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.core.io.ResourceLoader#getClassLoader()
	 */
	public ClassLoader getClassLoader()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.context.ApplicationContext#getId()
	 */
	public String getId()
	{
		throw new UnsupportedOperationException();

	}

	/**
	 * @see org.springframework.beans.factory.BeanFactory#getBean(java.lang.String,
	 *      java.lang.Object[])
	 */
	public Object getBean(final String name, final Object[] args) throws BeansException
	{
		throw new UnsupportedOperationException();

	}

	/**
	 * @see org.springframework.beans.factory.BeanFactory#isPrototype(java.lang.String)
	 */
	public boolean isPrototype(final String name) throws NoSuchBeanDefinitionException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.BeanFactory#isTypeMatch(java.lang.String,
	 *      java.lang.Class)
	 */
	public boolean isTypeMatch(final String name, final Class targetType)
		throws NoSuchBeanDefinitionException
	{
		throw new UnsupportedOperationException();
	}

}

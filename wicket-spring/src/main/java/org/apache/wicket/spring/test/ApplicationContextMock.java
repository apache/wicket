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
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
	@Override
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
	@Override
	@SuppressWarnings({ "unchecked" })
	public <T> T getBean(String name, Class<T> requiredType) throws BeansException
	{
		Object bean = getBean(name);
		if (!(requiredType.isAssignableFrom(bean.getClass())))
		{
			throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
		}
		return (T)bean;
	}

	/**
	 * @see org.springframework.beans.factory.ListableBeanFactory#getBeansOfType(java.lang.Class)
	 */
	@Override
	@SuppressWarnings({ "unchecked" })
	public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException
	{
		final Map<String, T> found = new HashMap<String, T>();

		for (Entry<String, Object> entry : beans.entrySet())
		{
			if (type.isAssignableFrom(entry.getValue().getClass()))
			{
				found.put(entry.getKey(), (T)entry.getValue());
			}
		}

		return found;
	}

	@Override
	public <T> T getBean(Class<T> requiredType) throws BeansException
	{
		Iterator<T> beans = getBeansOfType(requiredType).values().iterator();

		if (beans.hasNext() == false)
		{
			throw new NoSuchBeanDefinitionException("bean of required type " + requiredType +
				" not found");
		}
		final T bean = beans.next();

		if (beans.hasNext() != false)
		{
			throw new NoSuchBeanDefinitionException("more than one bean of required type " +
				requiredType + " found");
		}
		return bean;
	}

	@Override
	public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType)
		throws BeansException
	{
		final Map<String, Object> found = new HashMap<String, Object>();

		for (Entry<String, Object> entry : beans.entrySet())
		{
			if (entry.getValue().getClass().isAnnotationPresent(annotationType))
			{
				found.put(entry.getKey(), entry.getValue());
			}
		}
		return found;
	}

	@Override
	public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType)
	{
		return findAnnotationOnClass(getBean(beanName).getClass(), annotationType);
	}

	private <A extends Annotation> A findAnnotationOnClass(Class<?> cls, Class<A> annotationType)
	{
		// lookup annotation type on class
		A annotation = cls.getAnnotation(annotationType);

		// lookup annotation type on superclass
		if (annotation == null && cls.getSuperclass() != null)
		{
			annotation = findAnnotationOnClass(cls.getSuperclass(), annotationType);
		}

		// lookup annotation type on interfaces
		if (annotation == null)
		{
			for (Class<?> intfClass : cls.getInterfaces())
			{
				annotation = findAnnotationOnClass(intfClass, annotationType);

				if (annotation != null)
				{
					break;
				}
			}
		}

		return annotation;
	}

	/**
	 * @see org.springframework.context.ApplicationContext#getParent()
	 */
	@Override
	public ApplicationContext getParent()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.context.ApplicationContext#getDisplayName()
	 */
	@Override
	public String getDisplayName()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.context.ApplicationContext#getStartupDate()
	 */
	@Override
	public long getStartupDate()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.context.ApplicationContext#publishEvent(org.springframework.context.ApplicationEvent)
	 */
	@Override
	public void publishEvent(final ApplicationEvent event)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.ListableBeanFactory#
	 *      containsBeanDefinition(java.lang.String)
	 */
	@Override
	public boolean containsBeanDefinition(final String beanName)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.ListableBeanFactory# getBeanDefinitionCount()
	 */
	@Override
	public int getBeanDefinitionCount()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.ListableBeanFactory# getBeanDefinitionNames()
	 */
	@Override
	public String[] getBeanDefinitionNames()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.ListableBeanFactory#
	 *      getBeanNamesForType(java.lang.Class)
	 */
	@Override
	@SuppressWarnings({ "unchecked" })
	public String[] getBeanNamesForType(final Class type)
	{
		ArrayList<String> names = new ArrayList<String>();
		for (Entry<String, Object> entry : beans.entrySet())
		{
			Object bean = entry.getValue();

			if (type.isAssignableFrom(bean.getClass()))
			{
				names.add(entry.getKey());
			}
		}
		return names.toArray(new String[names.size()]);
	}

	/**
	 * @see org.springframework.beans.factory.ListableBeanFactory#
	 *      getBeanNamesForType(java.lang.Class, boolean, boolean)
	 */
	@Override
	@SuppressWarnings({ "unchecked" })
	public String[] getBeanNamesForType(Class type, boolean includeNonSingletons,
		boolean allowEagerInit)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.ListableBeanFactory#getBeansOfType(java.lang.Class,
	 *      boolean, boolean)
	 */
	@Override
	public <T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons,
		boolean allowEagerInit) throws BeansException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.BeanFactory#containsBean(java.lang.String)
	 */
	@Override
	public boolean containsBean(final String name)
	{
		return beans.containsKey(name);
	}

	/**
	 * @see org.springframework.beans.factory.BeanFactory#isSingleton(java.lang.String)
	 */
	@Override
	public boolean isSingleton(final String name) throws NoSuchBeanDefinitionException
	{
		return true;
	}

	/**
	 * @see org.springframework.beans.factory.BeanFactory#getType(java.lang.String)
	 */
	@Override
	public Class<?> getType(final String name) throws NoSuchBeanDefinitionException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.BeanFactory#getAliases(java.lang.String)
	 */
	@Override
	public String[] getAliases(final String name) throws NoSuchBeanDefinitionException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.HierarchicalBeanFactory# getParentBeanFactory()
	 */
	@Override
	public BeanFactory getParentBeanFactory()
	{
		return null;
	}

	/**
	 * @see org.springframework.context.MessageSource#getMessage(java.lang.String,
	 *      java.lang.Object[], java.lang.String, java.util.Locale)
	 */
	@Override
	public String getMessage(final String code, final Object[] args, final String defaultMessage,
		final Locale locale)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.context.MessageSource#getMessage(java.lang.String,
	 *      java.lang.Object[], java.util.Locale)
	 */
	@Override
	public String getMessage(final String code, final Object[] args, final Locale locale)
		throws NoSuchMessageException
	{
		throw new UnsupportedOperationException();
	}


	/**
	 * @see org.springframework.context.MessageSource#getMessage(org.springframework.context.MessageSourceResolvable, java.util.Locale)
	 */
	@Override
	public String getMessage(final MessageSourceResolvable resolvable, final Locale locale)
		throws NoSuchMessageException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.core.io.support.ResourcePatternResolver#getResources
	 *      (java.lang.String)
	 */
	@Override
	public Resource[] getResources(final String locationPattern) throws IOException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.core.io.ResourceLoader#getResource(java.lang.String)
	 */
	@Override
	public Resource getResource(final String location)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.context.ApplicationContext# getAutowireCapableBeanFactory()
	 */
	@Override
	public AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.HierarchicalBeanFactory#
	 *      containsLocalBean(java.lang.String)
	 */
	@Override
	public boolean containsLocalBean(final String arg0)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.core.io.ResourceLoader#getClassLoader()
	 */
	@Override
	public ClassLoader getClassLoader()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.context.ApplicationContext#getId()
	 */
	@Override
	public String getId()
	{
		throw new UnsupportedOperationException();

	}

	/**
	 * @see org.springframework.beans.factory.BeanFactory#getBean(java.lang.String,
	 *      java.lang.Object[])
	 */
	@Override
	public Object getBean(final String name, final Object... args) throws BeansException
	{
		throw new UnsupportedOperationException();

	}

	/**
	 * @see org.springframework.beans.factory.BeanFactory#isPrototype(java.lang.String)
	 */
	@Override
	public boolean isPrototype(final String name) throws NoSuchBeanDefinitionException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.beans.factory.BeanFactory#isTypeMatch(java.lang.String,
	 *      java.lang.Class)
	 */
	@Override
	@SuppressWarnings({ "unchecked" })
	public boolean isTypeMatch(final String name, final Class targetType)
		throws NoSuchBeanDefinitionException
	{
		throw new UnsupportedOperationException();
	}

}

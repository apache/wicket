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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.proxy.IProxyTargetLocator;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Objects;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.GenericCollectionTypeResolver;
import org.springframework.core.ResolvableType;

/**
 * Implementation of {@link IProxyTargetLocator} that can locate beans within a spring application
 * context. Beans are looked up by the combination of name and type, if name is omitted only type is
 * used.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @author Istvan Devai
 * @author Tobias Soloschenko
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
	 * Resolvable type for field to inject
	 */
	private ResolvableType fieldResolvableType;
	
	/**
	 * If the field to inject is a list this is the resolvable type of its elements
	 */
	private ResolvableType fieldCollectionResolvableType;
	
	private String fieldName;
	
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
		this(null, beanType, null, locator);
	}

	public SpringBeanLocator(final String beanName, final Class<?> beanType,
		final ISpringContextLocator locator)
	{
		this(beanName, beanType, null, locator);
	}

	/**
	 * Constructor
	 * 
	 * @param beanType
	 *            bean class
	 * @param locator
	 *            spring context locator
	 */
	public SpringBeanLocator(final Class<?> beanType, Field beanField,
		final ISpringContextLocator locator)
	{
		this(null, beanType, beanField, locator);
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
	public SpringBeanLocator(final String beanName, final Class<?> beanType, Field beanField,
		final ISpringContextLocator locator)
	{
		Args.notNull(locator, "locator");
		Args.notNull(beanType, "beanType");

		this.beanName = beanName;
		beanTypeCache = new WeakReference<Class<?>>(beanType);
		beanTypeName = beanType.getName();
		springContextLocator = locator;
		
		if (beanField != null)
		{
			fieldName = beanField.getName();
			fieldResolvableType = ResolvableType.forField(beanField);

			Class<?> collectionFieldType = GenericCollectionTypeResolver.getCollectionFieldType(beanField);
			fieldCollectionResolvableType = collectionFieldType != null ? 
				ResolvableType.forClass(collectionFieldType) : null;
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
			// If the name is set the lookup is clear
			if (name != null)
			{
				return ctx.getBean(name, clazz);
			}

			// If the beanField information is null the clazz is going to be used
			if (fieldResolvableType == null)
			{
				return ctx.getBean(clazz);
			}

			// If the given class is a list try to get the generic of the list
			Class<?> lookupClass = clazz == List.class ? 
				fieldResolvableType.getGeneric(0).resolve() : clazz;

			// Else the lookup is done via Generic
			List<String> names = loadBeanNames(ctx, lookupClass);

			List<Object> beansAsList = getBeansByName(ctx, names);

			if(beansAsList.size() == 1)
			{
				return beansAsList.get(0);
			}

			if (!beansAsList.isEmpty())
			{
				return beansAsList;
			}

			throw new IllegalStateException(
				"Concrete bean could not be received from the application context for class: " +
					clazz.getName() + ".");
		}
		catch (NoSuchBeanDefinitionException e)
		{
			throw new IllegalStateException("bean with name [" + name + "] and class [" +
				clazz.getName() + "] not found", e);
		}
	}

	/**
	 * Returns a list of candidate names for the given class.
	 * 
	 * @param ctx
	 * 			spring application context
	 * @param lookupClass
	 * 			the class to lookup
	 * @return a list of candidate names
	 */
	private List<String> loadBeanNames(ApplicationContext ctx, Class<?> lookupClass)
	{		
		List<String> beanNames = new ArrayList<>();
		String[] beanNamesArr = BeanFactoryUtils
			.beanNamesForTypeIncludingAncestors(ctx, lookupClass);

		//add field name if defined
		if (ctx.containsBean(fieldName))
		{
			beanNames.add(fieldName);
		}

		beanNames.addAll(Arrays.asList(beanNamesArr));

		return beanNames;
	}

	/**
	 * Retrieves a list of beans for the given list of names and assignable to the
	 * current field to inject.
	 * 
	 * @param ctx
	 * 				spring application context.
	 * @param names
	 * 				the list of candidate names
	 * @return a list of matching beans.
	 */
	private List<Object> getBeansByName(ApplicationContext ctx, List<String> names)
	{
		List<Object> beansAsList = new ArrayList<>();

		for (String beanName : names)
		{
			RootBeanDefinition beanDef = getBeanDefinition(ctx, beanName);

			if (beanDef == null)
			{
				continue;
			}

			ResolvableType candidateResolvableType = null;

			//check if we have the class of the bean or the factory method.
			//Usually if use XML as config file we have the class while we 
			//have the factory method if we use Java-based configuration.
			if (beanDef.hasBeanClass())
			{
				candidateResolvableType = ResolvableType.forClass(beanDef.getBeanClass());
			}
			else if (beanDef.getResolvedFactoryMethod() != null)
			{
				candidateResolvableType = ResolvableType.forMethodReturnType(
					beanDef.getResolvedFactoryMethod());
			}

			if (candidateResolvableType == null)
			{
				continue;
			}

			boolean exactMatch = fieldResolvableType.isAssignableFrom(candidateResolvableType);
			boolean elementMatch = fieldCollectionResolvableType != null ? 
				fieldCollectionResolvableType.isAssignableFrom(candidateResolvableType) : false;

			if (exactMatch || elementMatch)
			{
				beansAsList.add(ctx.getBean(beanName));
			}

			if (exactMatch)
			{
				this.beanName = beanName;
				return beansAsList;
			}
		}
		return beansAsList;
	}

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

	/**
	 * Gets the root bean definition for the given name.
	 * 
	 * @param ctx
	 * 				spring application context.
	 * @param name
	 * 				bean name
	 * @return bean definition for the current name, null if such a definition is not found.
	 */
	public RootBeanDefinition getBeanDefinition(final ApplicationContext ctx, final String name)
	{
		ConfigurableListableBeanFactory beanFactory = ((AbstractApplicationContext)ctx).getBeanFactory();

		BeanDefinition beanDef = beanFactory.getMergedBeanDefinition(name);

		if (beanDef instanceof RootBeanDefinition) {
			return (RootBeanDefinition)beanDef;
		}

		return null;
	}
}

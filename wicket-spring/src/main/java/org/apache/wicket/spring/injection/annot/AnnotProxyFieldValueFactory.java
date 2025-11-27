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
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

import jakarta.inject.Inject;
import jakarta.inject.Named;

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
import org.springframework.core.ResolvableType;

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
 * same spring dependency. This helps cut down on session size because proxies for the same
 * dependency will not be serialized twice.
 * 
 * @see LazyInitProxyFactory
 * @see SpringBean
 * @see SpringBeanLocator
 * @see jakarta.inject.Inject
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @author Istvan Devai
 * @author Tobias Soloschenko
 */
public class AnnotProxyFieldValueFactory implements IFieldValueFactory
{
	private final ISpringContextLocator contextLocator;

	private final ConcurrentMap<SpringBeanLocator, Object> cache = Generics.newConcurrentHashMap();

	private final ConcurrentMap<SimpleEntry<Class<?>, ResolvableType>,
								String> beanNameCache = Generics.newConcurrentHashMap();

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
				required = true;
			}

			ResolvableType resolvableType = ResolvableType.forField(field);
			String beanName = getBeanName(field, name, resolvableType);

			SpringBeanLocator locator = new SpringBeanLocator(beanName, field.getType(), field, contextLocator);

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
	private String getBeanName(final Field field, String name, final ResolvableType resolvableType)
	{
		if (Strings.isEmpty(name))
		{
			Class<?> fieldType = field.getType();
			
			SimpleEntry<Class<?>, ResolvableType> key = new SimpleEntry<>(fieldType, resolvableType);
			name = beanNameCache.get(key);
			if (name == null)
			{
				name = getBeanNameOfClass(contextLocator.getSpringContext(), fieldType, resolvableType, field.getName());
				if (name != null)
				{
					String tmpName = beanNameCache.putIfAbsent(key, name);
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
	 * @param fieldName 
	 * @throws IllegalStateException
	 * @return spring name of the bean
	 */
	private String getBeanNameOfClass(final ApplicationContext ctx, final Class<?> clazz,
		final ResolvableType resolvableType, String fieldName)
	{
		// get the list of all possible matching beans
		List<String> names = new ArrayList<>(
			Arrays.asList(BeanFactoryUtils.beanNamesForTypeIncludingAncestors(ctx, resolvableType)));

		// filter out beans that are not candidates for autowiring
		if (ctx instanceof AbstractApplicationContext abstractApplicationContext)
		{
			final Iterator<String> it = names.iterator();
			while (it.hasNext())
			{
				final String possibility = it.next();
				final ConfigurableListableBeanFactory beanFactory = abstractApplicationContext.getBeanFactory();
				final BeanDefinition beanDef = getBeanDefinition(beanFactory, possibility);
				if (BeanFactoryUtils.isFactoryDereference(possibility) ||
					possibility.startsWith("scopedTarget.") ||
					(beanDef != null && !beanDef.isAutowireCandidate()))
				{
					it.remove();
				}
			}
		}

		if (names.size() > 1)
		{
			// Check, if we can reduce the set of beannames to exactly one beanname probing the following criterias:
			// 1. Is there exactly one bean marked as primary?
			// 2. Is there a bean with the same name as the field?
			// 3. Is there exactly one bean marked as default candidate?
			final String exactMatchBeanName = Optional.ofNullable(detectPrimaryBeanName(ctx, names))
			  .or(() -> Optional.ofNullable(detectBeanNameByFieldName(fieldName, names)))
			  .orElseGet(() -> detectDefaultCandidateBeanName(ctx, names));

			// If so: take that beanname
			if (exactMatchBeanName != null) {
				return exactMatchBeanName;
			}

			// Hmm, dont know which one to take....
			final StringBuilder msg = new StringBuilder();
			msg.append("More than one bean of type [");
			msg.append(clazz.getName());
			msg.append("] found, you have to specify the name of the bean ");
			msg.append("(@SpringBean(name=\"foo\")) or (@Named(\"foo\") if using @jakarta.inject classes) in order to resolve this conflict. ");
			msg.append("Matched beans: ");
			msg.append(Strings.join(",", names));
			throw new IllegalStateException(msg.toString());
		}
		else if(!names.isEmpty())
		{
			return names.get(0);
		}
		
		return null;
	}

	private String detectPrimaryBeanName(final ApplicationContext ctx, final List<String> beanNames) {
		return detectBeanName(ctx, beanNames, AbstractBeanDefinition::isPrimary);
	}

	private String detectDefaultCandidateBeanName(final ApplicationContext ctx, final List<String> beanNames) {
		return detectBeanName(ctx, beanNames, AbstractBeanDefinition::isDefaultCandidate);
	}

	private String detectBeanName(final ApplicationContext ctx, final List<String> beanNames, final Predicate<AbstractBeanDefinition> predicate) {
		final List<String> found = new ArrayList<>();
		if (ctx instanceof AbstractApplicationContext abstractApplicationContext)
		{
			final ConfigurableListableBeanFactory beanFactory = abstractApplicationContext.getBeanFactory();
			for (final String beanName : beanNames)
			{
				final BeanDefinition beanDefinition = getBeanDefinition(beanFactory, beanName);
				if (beanDefinition instanceof AbstractBeanDefinition abstractBeanDefinition && predicate.test(abstractBeanDefinition))
				{
					found.add(beanName);
				}
			}
		}
		return found.size() == 1 ? found.get(0) : null;
	}

	private String detectBeanNameByFieldName(final String fieldName, final List<String> beanNames) 
	{
		return fieldName != null && beanNames.contains(fieldName) ? fieldName : null;
	}


	public BeanDefinition getBeanDefinition(final ConfigurableListableBeanFactory beanFactory,
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

	@Override
	public boolean supportsField(final Field field)
	{
		return field.isAnnotationPresent(SpringBean.class) || field.isAnnotationPresent(Inject.class);
	}
}

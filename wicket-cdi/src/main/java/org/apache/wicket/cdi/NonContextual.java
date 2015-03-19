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
package org.apache.wicket.cdi;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;

import org.apache.wicket.util.collections.ClassMetaCache;

/**
 * Manages lifecycle of non-contextual (non-CDI-managed) objects
 * 
 * @author igor
 * 
 * @param <T>
 */
public class NonContextual<T>
{
	private static final Object lock = new Object();
	private static volatile Map<BeanManager, ClassMetaCache<NonContextual<?>>> cache = Collections.emptyMap();

	final InjectionTarget<T> it;
	final BeanManager manager;

	/**
	 * Undeploys specified bean manager from cache
	 * 
	 * @param beanManager
	 */
	public static void undeploy(BeanManager beanManager)
	{
		if (cache.containsKey(beanManager))
		{
			synchronized (lock)
			{
				// copy-on-write the cache
				Map<BeanManager, ClassMetaCache<NonContextual<?>>> newCache = new WeakHashMap<>(
					cache);
				newCache.remove(beanManager);
				cache = Collections.unmodifiableMap(newCache);
			}
		}
	}

	/**
	 * Factory method for creating noncontextual instances
	 * 
	 * @param <T>
	 * @param clazz
	 * @param manager
	 * @return
	 */
	public static <T> NonContextual<T> of(Class<? extends T> clazz, BeanManager manager)
	{
		ClassMetaCache<NonContextual<?>> meta = getCache(manager);

		@SuppressWarnings("unchecked")
		NonContextual<T> nc = (NonContextual<T>)meta.get(clazz);

		if (nc == null)
		{
			nc = new NonContextual<>(manager, clazz);
			meta.put(clazz, nc);
		}
		return nc;
	}

	private static ClassMetaCache<NonContextual<?>> getCache(BeanManager manager)
	{
		ClassMetaCache<NonContextual<?>> meta = cache.get(manager);
		if (meta == null)
		{
			synchronized (lock)
			{
				meta = cache.get(manager);
				if (meta == null)
				{
					meta = new ClassMetaCache<>();

					// copy-on-write the cache
					Map<BeanManager, ClassMetaCache<NonContextual<?>>> newCache = new WeakHashMap<>(
						cache);
					newCache.put(manager, meta);
					cache = Collections.unmodifiableMap(newCache);
				}
			}
		}
		return meta;
	}

	@SuppressWarnings("unchecked")
	private NonContextual(BeanManager manager, Class<? extends T> clazz)
	{
		this.manager = manager;
		AnnotatedType<? extends T> type = manager.createAnnotatedType(clazz);
		this.it = (InjectionTarget<T>)manager.createInjectionTarget(type);
	}

	/**
	 * Injects the instance and calls any {@link PostConstruct} methods
	 * 
	 * @param instance
	 */
	public void postConstruct(T instance)
	{
		CreationalContext<T> cc = manager.createCreationalContext(null);
		it.inject(instance, cc);
		it.postConstruct(instance);
	}
	
	/**
	 * Injects the instance
	 * 
	 * @param instance
	 */
	public void inject(T instance)
	{
		CreationalContext<T> cc = manager.createCreationalContext(null);
		it.inject(instance, cc);
	}

	/**
	 * Calls any {@link PreDestroy} methods and destroys any injected dependencies that need to be
	 * destroyed.
	 * 
	 * @param instance
	 */
	public void preDestroy(T instance)
	{
		it.preDestroy(instance);
	}
}
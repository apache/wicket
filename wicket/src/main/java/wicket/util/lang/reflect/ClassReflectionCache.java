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
package wicket.util.lang.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * Provides a threadsafe cache of classes reflection information.
 * 
 * The slowest part of reflection is the discovery of methods and fields, this
 * class caches that information and provides utility methods for accessing it.
 * 
 * The cache has the following indecies that make for efficient lookups:
 * <ul>
 * <li>annotation to list of methods that are annotated with it</li>
 * </ul>
 * 
 * @author ivaynberg
 */
public class ClassReflectionCache
{
	private final Map<Class<? extends Annotation>, List<Method>> annotToMethods;

	/**
	 * Construct.
	 * 
	 * @param clazz
	 *            class whose reflection information is cached
	 * @param order
	 *            order in which fields and methods are sorted
	 * @param methodFilter
	 *            filter used to determine whether a method should be stored in
	 *            the cache
	 */
	public ClassReflectionCache(Class clazz, ClassOrder order, IMethodFilter methodFilter)
	{
		// build the cache

		Map<Class<? extends Annotation>, ArrayList<Method>> map;
		map = new HashMap<Class<? extends Annotation>, ArrayList<Method>>();

		ClassHieararchyIterator classes = new ClassHieararchyIterator(clazz, order);
		while (classes.hasNext())
		{
			Method[] methods = classes.next().getDeclaredMethods();
			for (Method method : methods)
			{
				Annotation[] annots = method.getDeclaredAnnotations();
				for (Annotation annot : annots)
				{
					ArrayList<Method> annotatedMethods = map.get(annot.annotationType());
					if (annotatedMethods == null)
					{
						annotatedMethods = new ArrayList<Method>();
						map.put(annot.annotationType(), annotatedMethods);
					}
					if (methodFilter.accept(method, annotatedMethods))
					{
						annotatedMethods.add(method);
					}

				}
			}
		}

		annotToMethods = new HashMap<Class<? extends Annotation>, List<Method>>();
		for (Entry<Class<? extends Annotation>, ArrayList<Method>> mapping : map.entrySet())
		{
			ArrayList<Method> methods = mapping.getValue();
			methods.trimToSize();
			annotToMethods.put(mapping.getKey(), Collections.unmodifiableList(methods));
		}
	}

	/**
	 * Returns all methods annotated with the specified annotaiton
	 * 
	 * @param annot
	 *            annotation
	 * @return list of methods
	 */
	public List<Method> methodsForAnnot(Class<? extends Annotation> annot)
	{
		List<Method> methods = annotToMethods.get(annot);
		if (methods == null)
		{
			return Collections.emptyList();
		}
		else
		{
			return methods;
		}

	}
}
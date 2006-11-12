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
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import wicket.util.lang.Objects;
import wicket.util.lang.Packages;


/**
 * Reflection utilities
 * 
 * @author ivaynberg
 */
@SuppressWarnings("unchecked")
public class ReflectionUtils
{
	/**
	 * Constructor
	 */
	private ReflectionUtils()
	{

	}

	/**
	 * array that contains class reflection caches
	 * 
	 * index[0] contains a reflection cache of all declared methods
	 * 
	 * index[1] and index[2] contain reflection caches of methods with overrides
	 * removed
	 * 
	 * index[1] is sorted sub to super
	 * 
	 * index[2] is sorted super to sub
	 */
	private static final Map<Class, ClassReflectionCache> classToMethods[] = new Map[] {
			new ConcurrentHashMap<Class, ClassReflectionCache>(),
			new ConcurrentHashMap<Class, ClassReflectionCache>(),
			new ConcurrentHashMap<Class, ClassReflectionCache>() };


	/**
	 * Returns a list of methods that are annotated with the specified
	 * annotation
	 * 
	 * @param clazz
	 * @param annot
	 * @return list of methods
	 */
	public static List<Method> methodsWithAnnotation(Class clazz, Class<? extends Annotation> annot)
	{
		ClassReflectionCache classCache = classToMethods[0].get(clazz);
		if (classCache == null)
		{
			classCache = new ClassReflectionCache(clazz, ClassOrder.SUPER_TO_SUB, IMethodFilter.ANY);
			classToMethods[0].put(clazz, classCache);
		}
		return classCache.methodsForAnnot(annot);
	}

	/**
	 * Returns a list of methods that are annotated with the specified
	 * annotation and with multiple representatives of the same override chain
	 * filtered
	 * 
	 * @param clazz
	 * @param annot
	 * @param order
	 * @return list of methods
	 */
	public static List<Method> invocationChainForAnnotation(Class clazz,
			Class<? extends Annotation> annot, ClassOrder order)
	{
		int index = 0;
		switch (order)
		{
			case SUB_TO_SUPER :
				index = 1;
				break;
			case SUPER_TO_SUB :
				index = 2;
				break;
		}

		ClassReflectionCache classCache = classToMethods[index].get(clazz);
		if (classCache == null)
		{
			classCache = new ClassReflectionCache(clazz, order, IMethodFilter.IGNORE_OVERRIDES);
			classToMethods[index].put(clazz, classCache);
		}
		return classCache.methodsForAnnot(annot);

	}

	/**
	 * Checks if either of the two methods is an override of the other
	 * 
	 * @param a
	 *            method a
	 * @param b
	 *            method b
	 * @return true if a overrides b or b overrides a
	 */
	public static final boolean overrides(Method a, Method b)
	{
		if (a.getName().equals(b.getName()))
		{
			// have same names
			if (Arrays.equals(a.getParameterTypes(), b.getParameterTypes()))
			{
				// have same parameter types

				final int amods = a.getModifiers();
				final int bmods = b.getModifiers();

				if (Modifier.isPublic(amods) || Modifier.isProtected(amods))
				{
					if (Modifier.isPublic(bmods) || Modifier.isProtected(bmods))
					{
						// are public or protected - so must be overrides
						return true;
					}
				}

				final String apack = Packages.extractPackageName(a.getDeclaringClass());
				final String bpack = Packages.extractPackageName(b.getDeclaringClass());
				if (Objects.equal(apack, bpack))
				{
					// are in the same package
					if (!Modifier.isPrivate(amods) && !Modifier.isPrivate(bmods))
					{
						// both are not private and not public or protected - so
						// must be package private and are overrides
						return true;
					}
				}
			}
		}

		return false;
	}

}

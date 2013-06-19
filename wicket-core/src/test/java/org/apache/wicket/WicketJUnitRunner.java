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
package org.apache.wicket;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

/**
 * An JUnit 4 runner that also executes all JUnit3 test methods
 */
public class WicketJUnitRunner extends BlockJUnit4ClassRunner
{

	/**
	 * Construct.
	 * 
	 * @param klass
	 * @throws InitializationError
	 */
	public WicketJUnitRunner(Class<?> klass) throws InitializationError
	{
		super(klass);
	}

	@Override
	protected List<FrameworkMethod> computeTestMethods()
	{
		List<FrameworkMethod> methods = new ArrayList<>();
		methods.addAll(super.computeTestMethods());

		for (Method javaMethod : getTestClass().getJavaClass().getMethods())
		{
			if (isJUnitMethod(javaMethod))
			{
				FrameworkMethod junitMethod = new FrameworkMethod(javaMethod);
				methods.add(junitMethod);
			}
		}

		return methods;
	}

	@Override
	protected Statement withBefores(FrameworkMethod method, Object target, Statement statement)
	{
		List<FrameworkMethod> befores = new ArrayList<>();
		befores.addAll(getTestClass().getAnnotatedMethods(Before.class));
		findMethod(getTestClass(), befores, "setUp");

		return befores.isEmpty() ? statement : new RunBefores(statement, befores, target);
	}

	@Override
	protected Statement withAfters(FrameworkMethod method, Object target, Statement statement)
	{
		List<FrameworkMethod> afters = new ArrayList<>();
		afters.addAll(getTestClass().getAnnotatedMethods(After.class));
		findMethod(getTestClass(), afters, "tearDown");

		return afters.isEmpty() ? statement : new RunAfters(statement, afters, target);
	}

	@Override
	protected void validateZeroArgConstructor(List<Throwable> errors)
	{
		int ctorsNumber = getTestClass().getOnlyConstructor().getParameterTypes().length;
		if ((ctorsNumber == 0 || ctorsNumber == 1) == false)
		{
			String gripe = "Test class should have exactly one public zero-argument constructor";
			errors.add(new Exception(gripe));
		}
	}

	@Override
	protected Object createTest() throws Exception
	{
		Object testObject;
		Constructor<?> constructor = getTestClass().getOnlyConstructor();
		if (constructor.getParameterTypes().length == 0)
		{
			testObject = constructor.newInstance();
		}
		else
		{
			testObject = constructor.newInstance("junit3 test");
		}
		return testObject;
	}

	/**
	 * Finds a method in the hierarchy of the tested class by his name
	 * 
	 * @param testClass
	 *            the junit class representation
	 * @param junitMethods
	 *            the collection where to add the found method
	 * @param methodName
	 *            the name of the method to find
	 * @param parameterTypes
	 *            the types of the parameters of the method to find
	 */
	private void findMethod(TestClass testClass, List<FrameworkMethod> junitMethods,
		String methodName, Class<?>... parameterTypes)
	{
		try
		{
			Class<?> clazz = getTestClass().getJavaClass();
			while (clazz != null && Object.class.equals(clazz) == false)
			{
				try
				{
					Method javaMethod = clazz.getDeclaredMethod(methodName, parameterTypes);
					if (javaMethod != null &&
						Modifier.isProtected(javaMethod.getModifiers()) &&
						(javaMethod.getReturnType().equals(Void.TYPE) || javaMethod.getReturnType()
							.equals(Void.class)))
					{
						javaMethod.setAccessible(true);
						junitMethods.add(new FrameworkMethod(javaMethod));
						break;
					}
				}
				catch (NoSuchMethodException nsmx)
				{
				}
				clazz = clazz.getSuperclass();
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Checks whether the passes {@code javaMethod} is JUnit 3 test method
	 * 
	 * @param javaMethod
	 *            the method to check
	 * @return {@code true} if the method passes all conditions to be JUnit3 method, otherwise
	 *         {@code false}
	 */
	private boolean isJUnitMethod(final Method javaMethod)
	{
		return Modifier.isPublic(javaMethod.getModifiers()) &&
			// is not JUnit 4 test method
			javaMethod.getAnnotation(Test.class) == null &&
			(Void.TYPE.equals(javaMethod.getReturnType()) || Void.class.equals(javaMethod.getReturnType())) &&
			javaMethod.getName().startsWith("test");
	}


}

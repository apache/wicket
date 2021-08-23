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
package org.apache.wicket.proxy.cglib;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.wicket.Application;
import org.apache.wicket.proxy.ILazyInitProxy;
import org.apache.wicket.proxy.IProxyFactory;
import org.apache.wicket.proxy.IProxyTargetLocator;
import org.apache.wicket.proxy.LazyInitProxyFactory.CGLibInterceptor;
import org.apache.wicket.proxy.LazyInitProxyFactory.IWriteReplace;
import org.apache.wicket.proxy.objenesis.IInstantiator;

import net.sf.cglib.core.DefaultNamingPolicy;
import net.sf.cglib.core.Predicate;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.NoOp;

/**
 * A factory class that creates cglib proxies.
 */
public class CglibProxyFactory implements IProxyFactory
{
	private static final int CGLIB_CALLBACK_NO_OVERRIDE = 0;
	private static final int CGLIB_CALLBACK_HANDLER = 1;
	
	private static IInstantiator instantiator = IInstantiator.getInstantiator();

	/**
	 * Create a lazy init proxy for the specified type. The target object will be located using the
	 * provided locator upon first method invocation.
	 * 
	 * @param type
	 *            type that proxy will represent
	 * 
	 * @param locator
	 *            object locator that will locate the object the proxy represents
	 * 
	 * @return lazily initializable proxy
	 */
	@Override
	public Object createProxy(final Class<?> type, final IProxyTargetLocator locator)
	{
		CGLibInterceptor handler = new CGLibInterceptor(type, locator);

		Callback[] callbacks = new Callback[2];
		callbacks[CGLIB_CALLBACK_NO_OVERRIDE] = SerializableNoOpCallback.INSTANCE;
		callbacks[CGLIB_CALLBACK_HANDLER] = handler;

		Enhancer e = new Enhancer();
		e.setClassLoader(resolveClassLoader());
		e.setInterfaces(new Class[] { Serializable.class, ILazyInitProxy.class,
				IWriteReplace.class });
		e.setSuperclass(type);
		e.setCallbackFilter(NoOpForProtectedMethodsCGLibCallbackFilter.INSTANCE);
		e.setNamingPolicy(WicketNamingPolicy.INSTANCE);

		if (!hasNoArgConstructor(type))
		{
			e.setCallbackTypes(new Class[] {callbacks[0].getClass(), callbacks[1].getClass()});

			Object instance = instantiator.newInstance(e.createClass());

			// set callbacks directly (WICKET-6607) 
			((Factory) instance).setCallbacks(callbacks);
			
			return instance;
		}
		else
		{
			e.setCallbacks(callbacks);
			
			return e.create();
		}
	}

	private static ClassLoader resolveClassLoader()
	{
		ClassLoader classLoader = null;
		if (Application.exists())
		{
			classLoader = Application.get().getApplicationSettings()
					.getClassResolver().getClassLoader();
		}

		if (classLoader == null) {
			classLoader = Thread.currentThread().getContextClassLoader();
		}

		return classLoader;
	}
	
	/**
	 * CGLib callback filter which does not intercept protected methods.
	 * 
	 * Protected methods need to be called with invokeSuper() instead of invoke().
	 * When invoke() is called on a protected method, it throws an "IllegalArgumentException:
	 * Protected method" exception.
	 * That being said, we do not need to intercept the protected methods so this callback filter
	 * is designed to use a NoOp callback for protected methods.
	 * 
	 * @see <a href="http://comments.gmane.org/gmane.comp.java.cglib.devel/720">Discussion about
	 * this very issue in Spring AOP</a>
	 * @see <a href="https://github.com/wicketstuff/core/wiki/SpringReference">The WicketStuff
	 * SpringReference project which worked around this issue</a>
	 */
	private static class NoOpForProtectedMethodsCGLibCallbackFilter implements CallbackFilter
	{
		private static final CallbackFilter INSTANCE = new NoOpForProtectedMethodsCGLibCallbackFilter();

		@Override
		public int accept(Method method) {
			if (Modifier.isProtected(method.getModifiers()))
			{
				return CGLIB_CALLBACK_NO_OVERRIDE;
			}
			else
			{
				return CGLIB_CALLBACK_HANDLER;
			}
		}
	}

	/**
	 * Serializable implementation of the NoOp callback.
	 */
	private static class SerializableNoOpCallback implements NoOp, Serializable
	{
		private static final long serialVersionUID = 1L;

		public static final NoOp INSTANCE = new SerializableNoOpCallback();
	}
	
	private static class WicketNamingPolicy extends DefaultNamingPolicy
	{
		public static final WicketNamingPolicy INSTANCE = new WicketNamingPolicy();

		private WicketNamingPolicy()
		{
			super();
		}

		@Override
		public String getClassName(final String prefix, final String source, final Object key,
				final Predicate names)
		{
			int lastIdxOfDot = prefix.lastIndexOf('.');
			String packageName = prefix.substring(0, lastIdxOfDot);
			String className = prefix.substring(lastIdxOfDot + 1);
			String newPrefix = packageName + ".Wicket_Proxy_" + className;
			return super.getClassName(newPrefix, source, key, names);
		}
	}

	private static boolean hasNoArgConstructor(Class<?> type)
	{
		for (Constructor<?> constructor : type.getDeclaredConstructors())
		{
			if (constructor.getParameterTypes().length == 0)
				return true;
		}

		return false;
	}
}

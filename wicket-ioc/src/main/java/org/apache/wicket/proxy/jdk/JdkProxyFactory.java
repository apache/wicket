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
package org.apache.wicket.proxy.jdk;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.wicket.Application;
import org.apache.wicket.proxy.ILazyInitProxy;
import org.apache.wicket.proxy.IProxyTargetLocator;
import org.apache.wicket.proxy.LazyInitProxyFactory;
import org.apache.wicket.proxy.LazyInitProxyFactory.IWriteReplace;
import org.apache.wicket.proxy.LazyInitProxyFactory.ProxyReplacement;

/**
 * A factory class that creates jdk dynamic proxies.
 */
public class JdkProxyFactory
{
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
	public Object createProxy(final Class<?> type, final IProxyTargetLocator locator)
	{
		JdkHandler handler = new JdkHandler(type, locator);

		try
		{
			return Proxy.newProxyInstance(resolveClassLoader(),
				new Class[] { type, Serializable.class, ILazyInitProxy.class,
						IWriteReplace.class }, handler);
		}
		catch (IllegalArgumentException e)
		{
			/*
			 * STW: In some clustering environments it appears the context classloader fails to
			 * load the proxied interface (currently seen in BEA WLS 9.x clusters). If this
			 * happens, we can try and fall back to the classloader (current) that actually
			 * loaded this class.
			 */
			return Proxy.newProxyInstance(LazyInitProxyFactory.class.getClassLoader(),
				new Class[] { type, Serializable.class, ILazyInitProxy.class,
						IWriteReplace.class }, handler);
		}
	}
	
	private ClassLoader resolveClassLoader()
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
	 * Invocation handler for proxies representing interface based object. For interface backed
	 * objects dynamic jdk proxies are used.
	 * 
	 * @author Igor Vaynberg (ivaynberg)
	 * 
	 */
	private static class JdkHandler
		implements
			InvocationHandler,
			ILazyInitProxy,
			Serializable,
			IWriteReplace
	{
		private static final long serialVersionUID = 1L;

		private final IProxyTargetLocator locator;

		private final String typeName;

		private transient Object target;

		/**
		 * Constructor
		 * 
		 * @param type
		 *            class of object this handler will represent
		 * 
		 * @param locator
		 *            object locator used to locate the object this proxy represents
		 */
		public JdkHandler(final Class<?> type, final IProxyTargetLocator locator)
		{
			super();
			this.locator = locator;
			typeName = type.getName();
		}

		/**
		 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
		 *      java.lang.reflect.Method, java.lang.Object[])
		 */
		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args)
			throws Throwable
		{
			if (LazyInitProxyFactory.isFinalizeMethod(method))
			{
				// swallow finalize call
				return null;
			}
			else if (LazyInitProxyFactory.isEqualsMethod(method))
			{
				return (equals(args[0])) ? Boolean.TRUE : Boolean.FALSE;
			}
			else if (LazyInitProxyFactory.isHashCodeMethod(method))
			{
				return hashCode();
			}
			else if (LazyInitProxyFactory.isToStringMethod(method))
			{
				return toString();
			}
			else if (method.getDeclaringClass().equals(ILazyInitProxy.class))
			{
				return getObjectLocator();
			}
			else if (LazyInitProxyFactory.isWriteReplaceMethod(method))
			{
				return writeReplace();
			}

			if (target == null)
			{

				target = locator.locateProxyTarget();
			}
			try
			{
				method.setAccessible(true);
				return method.invoke(target, args);
			}
			catch (InvocationTargetException e)
			{
				throw e.getTargetException();
			}
		}

		/**
		 * @see org.apache.wicket.proxy.ILazyInitProxy#getObjectLocator()
		 */
		@Override
		public IProxyTargetLocator getObjectLocator()
		{
			return locator;
		}

		/**
		 * @see org.apache.wicket.proxy.LazyInitProxyFactory.IWriteReplace#writeReplace()
		 */
		@Override
		public Object writeReplace() throws ObjectStreamException
		{
			return new ProxyReplacement(typeName, locator);
		}
	}
}
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
package org.apache.wicket.proxy.bytebuddy;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.proxy.ILazyInitProxy;
import org.apache.wicket.proxy.IProxyFactory;
import org.apache.wicket.proxy.IProxyTargetLocator;
import org.apache.wicket.proxy.LazyInitProxyFactory;
import org.apache.wicket.proxy.LazyInitProxyFactory.IWriteReplace;
import org.apache.wicket.proxy.LazyInitProxyFactory.ProxyReplacement;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.TypeCache;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.Pipe;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * A factory class that creates bytebuddy proxies.
 */
public class ByteBuddyProxyFactory implements IProxyFactory
{
	/**
	 * A cache used to store the dynamically generated classes by ByteBuddy.
	 * Without this cache a new class will be generated for each proxy creation
	 * and this will fill up the metaspace
	 */
	private static final TypeCache<TypeCache.SimpleKey> DYNAMIC_CLASS_CACHE = new TypeCache.WithInlineExpunction<>(TypeCache.Sort.SOFT);

	private static final ByteBuddy BYTE_BUDDY = new ByteBuddy().with(WicketNamingStrategy.INSTANCE);

	private static final boolean IS_OBJENESIS_AVAILABLE = isObjenesisAvailable();

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
		if (IS_OBJENESIS_AVAILABLE && !hasNoArgConstructor(type))
		{
			return ObjenesisProxyFactory.createProxy(type, locator);
		}
		else
		{
			Class<?> proxyClass = createOrGetProxyClass(type);

			try
			{
				Object instance = proxyClass.getDeclaredConstructor().newInstance();
				ByteBuddyInterceptor interceptor = new ByteBuddyInterceptor(type, locator);
				((InterceptorMutator) instance).setInterceptor(interceptor);
				return instance;
			}
			catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
			{
				throw new WicketRuntimeException(e);
			}
		}
	}

	public static Class<?> createOrGetProxyClass(Class<?> type)
	{
		ClassLoader classLoader = resolveClassLoader();
		return DYNAMIC_CLASS_CACHE.findOrInsert(classLoader,
				new TypeCache.SimpleKey(type),
				() -> BYTE_BUDDY
						.subclass(type)
						.method(ElementMatchers.isPublic())
							.intercept(
								MethodDelegation
									.withDefaultConfiguration()
									.withBinders(Pipe.Binder.install(Function.class))
									.toField("interceptor"))
						.defineField("interceptor", ByteBuddyInterceptor.class, Visibility.PRIVATE)
						.implement(InterceptorMutator.class).intercept(FieldAccessor.ofBeanProperty())
						.implement(Serializable.class, IWriteReplace.class, ILazyInitProxy.class).intercept(MethodDelegation.toField("interceptor"))
						.make()
						.load(classLoader, ClassLoadingStrategy.Default.INJECTION)
						.getLoaded());
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
	 * An interface used to set the Byte Buddy interceptor after creating an
	 * instance of the dynamically created proxy class.
	 * We need to set the interceptor as a field in the proxy class so that
	 * we could use different interceptors for proxied classes with generics.
	 * For example: a {@link org.apache.wicket.Component} may need to inject
	 * two beans with the same raw type but different generic type(s) (<em>
	 * ArrayList&lt;String&gt;</em> and <em>ArrayList&lt;Integer&gt;</em>).
	 * Since the generic types are erased at runtime, and we use caching for the
	 * dynamic proxy classes we need to be able to set different interceptors
	 * after instantiating the proxy class.
	 */
	public interface InterceptorMutator
	{
		void setInterceptor(ByteBuddyInterceptor interceptor);
	}

	/**
	 * Method interceptor for proxies representing concrete object not backed by an interface.
	 * These proxies are represented by ByteBuddy proxies.
	 * 
	 * @author Igor Vaynberg (ivaynberg)
	 */
	public static class ByteBuddyInterceptor
		implements
			ILazyInitProxy,
			Serializable,
			IWriteReplace
	{
		private static final long serialVersionUID = 1L;

		protected final IProxyTargetLocator locator;

		protected final String typeName;

		private transient Object target;

		/**
		 * Constructor
		 * 
		 * @param type
		 *            class of the object this proxy was created for
		 * 
		 * @param locator
		 *            object locator used to locate the object this proxy represents
		 */
		public ByteBuddyInterceptor(final Class<?> type, final IProxyTargetLocator locator)
		{
			super();
			
			this.typeName = type.getName();
			this.locator = locator;
		}

		@RuntimeType
		public Object intercept(@Origin Method method, @AllArguments Object[] args, @Pipe Function pipe) throws Exception
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
			
			if (target == null)
			{
				target = locator.locateProxyTarget();
			}
			
			return pipe.apply(target);
		}

		@Override
		public IProxyTargetLocator getObjectLocator()
		{
			return locator;
		}

		@Override
		public Object writeReplace() throws ObjectStreamException
		{
			return new ProxyReplacement(typeName, locator);
		}
	}

	/**
	 * A strategy that decides what should be the fully qualified name of the generated
	 * classes. Since it is not possible to create new classes in the <em>java.**</em>
	 * package we modify the package name by prefixing it with <em>bytebuddy_generated_wicket_proxy.</em>.
	 * For classes in any other packages we modify just the class name by prefixing
	 * it with <em>WicketProxy_</em>. This way the generated proxy class could still
	 * access package-private members of sibling classes.
	 */
	public static final class WicketNamingStrategy extends NamingStrategy.AbstractBase
	{
		public static final WicketNamingStrategy INSTANCE = new WicketNamingStrategy();

		private WicketNamingStrategy()
		{
			super();
		}

		@Override
		protected String name(TypeDescription superClass) {
			String prefix = superClass.getName();
			int lastIdxOfDot = prefix.lastIndexOf('.');
			String packageName = prefix.substring(0, lastIdxOfDot);
			String className = prefix.substring(lastIdxOfDot + 1);
			String name = packageName + ".";
			if (prefix.startsWith("java."))
			{
				name = "bytebuddy_generated_wicket_proxy." + name + className;
			}
			else
			{
				name += "WicketProxy_" + className;
			}
			return name;
		}

		@Override
		public String redefine(TypeDescription typeDescription) {
			return typeDescription.getName();
		}

		@Override
		public String rebase(TypeDescription typeDescription) {
			return typeDescription.getName();
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

	private static boolean isObjenesisAvailable()
	{
		try {
			Class.forName("org.objenesis.ObjenesisStd");
			return true;
		} catch (Exception ignored) {
			return false;
		}
	}
}

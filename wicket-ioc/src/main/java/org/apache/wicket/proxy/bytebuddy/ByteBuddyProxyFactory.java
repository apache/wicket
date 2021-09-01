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

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.proxy.ILazyInitProxy;
import org.apache.wicket.proxy.IProxyFactory;
import org.apache.wicket.proxy.IProxyTargetLocator;
import org.apache.wicket.proxy.LazyInitProxyFactory.IWriteReplace;
import org.apache.wicket.proxy.objenesis.IInstantiator;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.TypeCache;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Pipe;
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
	private static final String INTERCEPTOR_FIELD_NAME = "interceptor";

	private static final IInstantiator INSTANTIATOR = IInstantiator.getInstantiator();

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
	public <T> T createProxy(final Class<T> type, final IProxyTargetLocator locator)
	{
		Class<T> proxyClass = createOrGetProxyClass(type);

		T instance;
		
		if (!hasNoArgConstructor(type))
		{
			instance = INSTANTIATOR.newInstance(proxyClass);
		}
		else
		{
			try
			{
				instance = proxyClass.getDeclaredConstructor().newInstance();
			}
			catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
			{
				throw new WicketRuntimeException(e);
			}
		}
		
		ByteBuddyInterceptor interceptor = new ByteBuddyInterceptor(type, locator);
		((InterceptorMutator) instance).setInterceptor(interceptor);
		
		return instance;
	}

	@SuppressWarnings("unchecked")
	public static <T> Class<T> createOrGetProxyClass(Class<T> type)
	{
		ClassLoader classLoader = resolveClassLoader();
		return (Class<T>) DYNAMIC_CLASS_CACHE.findOrInsert(classLoader,
				new TypeCache.SimpleKey(type),
				() -> BYTE_BUDDY
						.subclass(type)
						.method(ElementMatchers.isPublic())
							.intercept(
								MethodDelegation
									.withDefaultConfiguration()
									.withBinders(Pipe.Binder.install(Function.class))
									.toField(INTERCEPTOR_FIELD_NAME))
						.defineField(INTERCEPTOR_FIELD_NAME, ByteBuddyInterceptor.class, Visibility.PRIVATE)
						.implement(InterceptorMutator.class).intercept(FieldAccessor.ofBeanProperty())
						.implement(Serializable.class, IWriteReplace.class, ILazyInitProxy.class).intercept(MethodDelegation.toField(INTERCEPTOR_FIELD_NAME))
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
	 * A strategy that decides what should be the fully qualified name of the generated
	 * classes. Since it is not possible to create new classes in the <em>java.**</em>
	 * package we modify the package name by prefixing it with <em>bytebuddy_generated_wicket_proxy.</em>.
	 * For classes in any other packages we modify just the class name by prefixing
	 * it with <em>WicketProxy_</em>. This way the generated proxy class could still
	 * access package-private members of sibling classes.
	 */
	private static final class WicketNamingStrategy extends NamingStrategy.AbstractBase
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
}

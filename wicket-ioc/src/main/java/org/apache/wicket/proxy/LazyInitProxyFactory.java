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
package org.apache.wicket.proxy;

import java.io.ObjectStreamException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.model.IModel;
import org.apache.wicket.proxy.bytebuddy.ByteBuddyProxyFactory;
import org.apache.wicket.proxy.jdk.JdkProxyFactory;
import org.apache.wicket.util.io.IClusterable;

/**
 * A factory class that creates lazy init proxies given a type and a {@link IProxyTargetLocator}
 * used to retrieve the object the proxy will represent.
 * <p>
 * A lazy init proxy waits until the first method invocation before it uses the
 * {@link IProxyTargetLocator} to retrieve the object to which the method invocation will be
 * forwarded.
 * <p>	
 * This factory creates two kinds of proxies: A standard dynamic proxy when the specified type is an
 * interface, and a ByteBuddy proxy when the specified type is a concrete class.
 * <p>
 * The general use case for such a proxy is to represent a dependency that should not be serialized
 * with a wicket page or {@link IModel}. The solution is to serialize the proxy and the
 * {@link IProxyTargetLocator} instead of the dependency, and be able to look up the target object
 * again when the proxy is deserialized and accessed. A good strategy for achieving this is to have
 * a static lookup in the {@link IProxyTargetLocator}, this keeps its size small and makes it safe
 * to serialize.
 * <p>
 * Example:
 * 
 * <pre>
 * class UserServiceLocator implements IProxyTargetLocator
 * {
 * 	public static final IProxyTargetLocator INSTANCE = new UserServiceLocator();
 * 
 * 	Object locateProxyObject()
 * 	{
 * 		MyApplication app = (MyApplication)Application.get();
 * 		return app.getUserService();
 * 	}
 * }
 * 
 * class UserDetachableModel extends LoadableDetachableModel
 * {
 * 	private UserService svc;
 * 
 * 	private long userId;
 * 
 * 	public UserDetachableModel(long userId, UserService svc)
 * 	{
 * 		this.userId = userId;
 * 		this.svc = svc;
 * 	}
 * 
 * 	public Object load()
 * 	{
 * 		return svc.loadUser(userId);
 * 	}
 * }
 * 
 * UserService service = LazyInitProxyFactory.createProxy(UserService.class,
 * 	UserServiceLocator.INSTANCE);
 * 
 * UserDetachableModel model = new UserDetachableModel(10, service);
 * 
 * </pre>
 * 
 * The detachable model in the example above follows to good citizen pattern and is easy to unit
 * test. These are the advantages gained through the use of the lazy init proxies.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class LazyInitProxyFactory
{
	/**
	 * Primitive java types and their object wrappers
	 */
	private static final List<Class<?>> PRIMITIVES = Arrays.asList(String.class, byte.class, Byte.class,
		short.class, Short.class, int.class, Integer.class, long.class, Long.class, float.class,
		Float.class, double.class, Double.class, char.class, Character.class, boolean.class,
		Boolean.class);
	
	private static final IProxyFactory JDK_PROXY_FACTORY = new JdkProxyFactory();
	private static final IProxyFactory CLASS_PROXY_FACTORY = new ByteBuddyProxyFactory();

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
	public static <T> T createProxy(final Class<T> type, final IProxyTargetLocator locator)
	{
		if (PRIMITIVES.contains(type) || Enum.class.isAssignableFrom(type))
		{
			// We special-case primitives as sometimes people use these as
			// SpringBeans (WICKET-603, WICKET-906). Go figure.
			return (T) locator.locateProxyTarget();
		}
		else if (type.isInterface())
		{
			return JDK_PROXY_FACTORY.createProxy(type, locator);
		}
		else
		{
			return CLASS_PROXY_FACTORY.createProxy(type, locator);
		}
	}
	
	/**
	 * This interface is used to make the proxy forward writeReplace() call to the handler instead
	 * of invoking it on itself. This allows us to serialize the replacement object instead of the
	 * proxy itself in case the proxy subclass is deserialized on a VM that does not have it
	 * created.
	 * 
	 * @see LazyInitProxyFactory.ProxyReplacement
	 * 
	 * @author Igor Vaynberg (ivaynberg)
	 * 
	 */
	public interface IWriteReplace
	{
		/**
		 * write replace method as defined by Serializable
		 * 
		 * @return object that will replace this object in serialized state
		 * @throws ObjectStreamException
		 */
		Object writeReplace() throws ObjectStreamException;
	}
	
	/**
	 * Object that replaces the proxy when it is serialized. Upon deserialization this object will
	 * create a new proxy with the same locator.
	 * 
	 * @author Igor Vaynberg (ivaynberg)
	 * 
	 */
	public static final class ProxyReplacement implements IClusterable
	{
		private static final long serialVersionUID = 1L;

		private final IProxyTargetLocator locator;

		private final String type;

		/**
		 * Constructor
		 * 
		 * @param type
		 * @param locator
		 */
		public ProxyReplacement(final String type, final IProxyTargetLocator locator)
		{
			this.type = type;
			this.locator = locator;
		}

		private Object readResolve() throws ObjectStreamException
		{
			Class<?> clazz = WicketObjects.resolveClass(type);
			if (clazz == null)
			{
				try
				{
					clazz = Class.forName(type, false, Thread.currentThread().getContextClassLoader());
				}
				catch (ClassNotFoundException ignored1)
				{
					try
					{
						clazz = Class.forName(type, false, LazyInitProxyFactory.class.getClassLoader());
					}
					catch (ClassNotFoundException ignored2)
					{
						ClassNotFoundException cause = new ClassNotFoundException(
								"Could not resolve type [" + type +
										"] with the currently configured org.apache.wicket.application.IClassResolver");
						throw new WicketRuntimeException(cause);
					}
				}
			}
			return LazyInitProxyFactory.createProxy(clazz, locator);
		}
	}

	/**
	 * Checks if the method is derived from Object.equals()
	 * 
	 * @param method
	 *            method being tested
	 * @return true if the method is derived from Object.equals(), false otherwise
	 */
	public static boolean isEqualsMethod(final Method method)
	{
		return (method.getReturnType() == boolean.class) &&
			(method.getParameterTypes().length == 1) &&
			(method.getParameterTypes()[0] == Object.class) && method.getName().equals("equals");
	}

	/**
	 * Checks if the method is derived from Object.hashCode()
	 * 
	 * @param method
	 *            method being tested
	 * @return true if the method is defined from Object.hashCode(), false otherwise
	 */
	public static boolean isHashCodeMethod(final Method method)
	{
		return (method.getReturnType() == int.class) && (method.getParameterTypes().length == 0) &&
			method.getName().equals("hashCode");
	}

	/**
	 * Checks if the method is derived from Object.toString()
	 * 
	 * @param method
	 *            method being tested
	 * @return true if the method is defined from Object.toString(), false otherwise
	 */
	public static boolean isToStringMethod(final Method method)
	{
		return (method.getReturnType() == String.class) &&
			(method.getParameterTypes().length == 0) && method.getName().equals("toString");
	}

	/**
	 * Checks if the method is derived from Object.finalize()
	 * 
	 * @param method
	 *            method being tested
	 * @return true if the method is defined from Object.finalize(), false otherwise
	 */
	public static boolean isFinalizeMethod(final Method method)
	{
		return (method.getReturnType() == void.class) && (method.getParameterTypes().length == 0) &&
			method.getName().equals("finalize");
	}

	/**
	 * Checks if the method is the writeReplace method
	 * 
	 * @param method
	 *            method being tested
	 * @return true if the method is the writeReplace method, false otherwise
	 */
	public static boolean isWriteReplaceMethod(final Method method)
	{
		return (method.getReturnType() == Object.class) &&
			(method.getParameterTypes().length == 0) && method.getName().equals("writeReplace");
	}	
}

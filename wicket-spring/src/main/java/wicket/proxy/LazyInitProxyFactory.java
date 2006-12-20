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
package wicket.proxy;

import java.io.InvalidClassException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import wicket.model.IModel;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * A factory class that creates lazy init proxies given a type and a
 * {@link IProxyTargetLocator} used to retrieve the object the proxy will
 * represent.
 * <p>
 * A lazy init proxy waits until the first method invocation before it uses the
 * {@link IProxyTargetLocator} to retrieve the object to which the method
 * invocation will be forwarded.
 * <p>
 * This factory creates two kinds of proxies: A standard dynamic proxy when the
 * specified type is an interface, and a CGLib proxy when the specified type is
 * a concrete class.
 * <p>
 * The general use case for such a proxy is to represent a dependency that
 * should not be serialized with a wicket page or {@link IModel}. The solution
 * is to serialize the proxy and the {@link IProxyTargetLocator} instead of the
 * dependency, and be able to look up the target object again when the proxy is
 * deserialized and accessed. A good strategy for achieving this is to have a
 * static lookup in the {@link IProxyTargetLocator}, this keeps its size small
 * and makes it safe to serialize.
 * <p>
 * Example:
 * 
 * <pre>
 * class UserServiceLocator implements IProxyTargetLocator
 * {
 * 
 * 	public static final IProxyTargetLocator INSTANCE = new UserServiceLocator();
 * 
 * 	Object locateProxyObject()
 * 	{
 * 		MyApplication app = (MyApplication) Application.get();
 * 		return app.getUserService();
 * 	}
 * }
 * 
 * class UserDetachableModel extends LoadableModel
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
 * 		UserServiceLocator.INSTANCE);
 * 
 * UserDetachableModel model = new UserDetachableModel(10, service);
 * 
 * </pre>
 * 
 * The detachable model in the example above follows to good citizen pattern and
 * is easy to unit test. These are the advantages gained through the use of the
 * lazy init proxies.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class LazyInitProxyFactory
{
	/**
	 * Create a lazy init proxy for the specified type. The target object will
	 * be located using the provided locator upon first method invocation.
	 * 
	 * @param type
	 *            type that proxy will represent
	 * 
	 * @param locator
	 *            object locator that will locate the object the proxy
	 *            represents
	 * 
	 * @return lazily initializable proxy
	 */
	public static Object createProxy(Class type, IProxyTargetLocator locator)
	{
		if (type.isInterface())
		{
			JdkHandler handler = new JdkHandler(type, locator);

			return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
					new Class[] {type, Serializable.class, ILazyInitProxy.class,
							IWriteReplace.class}, handler);

		}
		else
		{
			CGLibInterceptor handler = new CGLibInterceptor(type, locator);

			Enhancer e = new Enhancer();
			e.setInterfaces(new Class[] {Serializable.class, ILazyInitProxy.class,
					IWriteReplace.class});
			e.setSuperclass(type);
			e.setCallback(handler);

			return e.create();

		}

	}

	/**
	 * This interface is used to make the proxy forward writeReplace() call to
	 * the handler instead of invoking it on itself. This allows us to serialize
	 * the replacement objet instead of the proxy itself in case the proxy
	 * subclass is deserialized on a VM that does not have it created.
	 * 
	 * @see ProxyReplacement
	 * 
	 * @author Igor Vaynberg (ivaynberg)
	 * 
	 */
	protected static interface IWriteReplace
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
	 * Object that replaces the proxy when it is serialized. Upon
	 * deserialization this object will create a new proxy with the same
	 * locator.
	 * 
	 * @author Igor Vaynberg (ivaynberg)
	 * 
	 */
	static class ProxyReplacement implements Serializable
	{
		private IProxyTargetLocator locator;

		private String type;

		/**
		 * Constructor
		 * 
		 * @param type
		 * @param locator
		 */
		public ProxyReplacement(String type, IProxyTargetLocator locator)
		{
			this.type = type;
			this.locator = locator;
		}

		private Object readResolve() throws ObjectStreamException
		{
			Class clazz;
			try
			{
				clazz = Class.forName(type);
			}
			catch (ClassNotFoundException e)
			{
				throw new InvalidClassException(type, "could not resolve class ["
						+ type + "] when deserializing proxy");
			}

			return LazyInitProxyFactory.createProxy(clazz, locator);
		}
	}

	/**
	 * Method interceptor for proxies representing concrete object not backed by
	 * an interface. These proxies are representing by cglib proxies.
	 * 
	 * @author Igor Vaynberg (ivaynberg)
	 * 
	 */
	private static class CGLibInterceptor implements MethodInterceptor, ILazyInitProxy,
			Serializable, IWriteReplace
	{

		private IProxyTargetLocator locator;

		private String typeName;

		private transient Object target;

		/**
		 * Constructor
		 * 
		 * @param type
		 *            class of the object this proxy was created for
		 * 
		 * @param locator
		 *            object locator used to locate the object this proxy
		 *            represents
		 */
		public CGLibInterceptor(Class type, IProxyTargetLocator locator)
		{
			super();
			this.typeName = type.getName();
			this.locator = locator;
		}

		/**
		 * @see net.sf.cglib.proxy.MethodInterceptor#intercept(java.lang.Object,
		 *      java.lang.reflect.Method, java.lang.Object[],
		 *      net.sf.cglib.proxy.MethodProxy)
		 */
		public Object intercept(Object object, Method method, Object[] args,
				MethodProxy proxy) throws Throwable
		{
			if (isFinalizeMethod(method))
			{
				// swallow finalize call
				return null;
			}
			else if (isEqualsMethod(method))
			{
				return (equals(args[0])) ? Boolean.TRUE : Boolean.FALSE;
			}
			else if (isHashCodeMethod(method))
			{
				return new Integer(this.hashCode());
			}
			else if (isToStringMethod(method))
			{
				return toString();
			}
			else if (isWriteReplaceMethod(method))
			{
				return writeReplace();
			}
			else if (method.getDeclaringClass().equals(ILazyInitProxy.class))
			{
				return getObjectLocator();
			}

			if (target == null)
			{
				target = locator.locateProxyTarget();
			}
			return proxy.invoke(target, args);
		}

		/**
		 * @see wicket.proxy.ILazyInitProxy#getObjectLocator()
		 */
		public IProxyTargetLocator getObjectLocator()
		{
			return locator;
		}

		/**
		 * @see wicket.proxy.LazyInitProxyFactory.IWriteReplace#writeReplace()
		 */
		public Object writeReplace() throws ObjectStreamException
		{
			return new ProxyReplacement(typeName, locator);
		}

	}

	/**
	 * Invocation handler for proxies representing interface based object. For
	 * interface backed objects dynamic jdk proxies are used.
	 * 
	 * @author Igor Vaynberg (ivaynberg)
	 * 
	 */
	private static class JdkHandler implements InvocationHandler, ILazyInitProxy,
			Serializable, IWriteReplace
	{

		private IProxyTargetLocator locator;

		private String typeName;

		private transient Object target;

		/**
		 * Constructor
		 * 
		 * @param type
		 *            class of object this handler will represent
		 * 
		 * @param locator
		 *            object locator used to locate the object this proxy
		 *            represents
		 */
		public JdkHandler(Class type, IProxyTargetLocator locator)
		{
			super();
			this.locator = locator;
			this.typeName = type.getName();

		}

		/**
		 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
		 *      java.lang.reflect.Method, java.lang.Object[])
		 */
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
		{
			if (isFinalizeMethod(method))
			{
				// swallow finalize call
				return null;
			}
			else if (isEqualsMethod(method))
			{
				return (equals(args[0])) ? Boolean.TRUE : Boolean.FALSE;
			}
			else if (isHashCodeMethod(method))
			{
				return new Integer(this.hashCode());
			}
			else if (isToStringMethod(method))
			{
				return toString();
			}
			else if (method.getDeclaringClass().equals(ILazyInitProxy.class))
			{
				return getObjectLocator();
			}
			else if (isWriteReplaceMethod(method))
			{
				return writeReplace();
			}

			if (target == null)
			{

				target = locator.locateProxyTarget();
			}
			try
			{
				return method.invoke(target, args);
			}
			catch (InvocationTargetException e)
			{
				throw e.getTargetException();
			}
		}

		/**
		 * @see wicket.proxy.ILazyInitProxy#getObjectLocator()
		 */
		public IProxyTargetLocator getObjectLocator()
		{
			return locator;
		}

		/**
		 * @see wicket.proxy.LazyInitProxyFactory.IWriteReplace#writeReplace()
		 */
		public Object writeReplace() throws ObjectStreamException
		{
			return new ProxyReplacement(typeName, locator);
		}

	}

	/**
	 * Checks if the method is derived from Object.equals()
	 * 
	 * @param method
	 *            method being tested
	 * @return true if the method is derived from Object.equals(), false
	 *         otherwise
	 */
	protected static boolean isEqualsMethod(Method method)
	{
		return method.getReturnType() == boolean.class
				&& method.getParameterTypes().length == 1
				&& method.getParameterTypes()[0] == Object.class
				&& method.getName().equals("equals");
	}

	/**
	 * Checks if the method is derived from Object.hashCode()
	 * 
	 * @param method
	 *            method being tested
	 * @return true if the method is defined from Object.hashCode(), false
	 *         otherwise
	 */
	protected static boolean isHashCodeMethod(Method method)
	{
		return method.getReturnType() == int.class
				&& method.getParameterTypes().length == 0
				&& method.getName().equals("hashCode");
	}

	/**
	 * Checks if the method is derived from Object.toString()
	 * 
	 * @param method
	 *            method being tested
	 * @return true if the method is defined from Object.toString(), false
	 *         otherwise
	 */
	protected static boolean isToStringMethod(Method method)
	{
		return method.getReturnType() == String.class
				&& method.getParameterTypes().length == 0
				&& method.getName().equals("toString");
	}

	/**
	 * Checks if the method is derived from Object.finalize()
	 * 
	 * @param method
	 *            method being tested
	 * @return true if the method is defined from Object.finalize(), false
	 *         otherwise
	 */
	protected static boolean isFinalizeMethod(Method method)
	{
		return method.getReturnType() == void.class
				&& method.getParameterTypes().length == 0
				&& method.getName().equals("finalize");
	}

	/**
	 * Checks if the method is the writeReplace method
	 * 
	 * @param method
	 *            method being tested
	 * @return true if the method is the writeReplace method, false otherwise
	 */
	protected static boolean isWriteReplaceMethod(Method method)
	{
		return method.getReturnType() == Object.class
				&& method.getParameterTypes().length == 0
				&& method.getName().equals("writeReplace");
	}

}

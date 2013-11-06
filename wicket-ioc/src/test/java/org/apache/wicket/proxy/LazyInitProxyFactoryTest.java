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

import static org.hamcrest.CoreMatchers.instanceOf;

import java.lang.reflect.Proxy;

import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.proxy.LazyInitProxyFactory.ProxyReplacement;
import org.apache.wicket.proxy.util.ConcreteObject;
import org.apache.wicket.proxy.util.IInterface;
import org.apache.wicket.proxy.util.IObjectMethodTester;
import org.apache.wicket.proxy.util.InterfaceObject;
import org.apache.wicket.proxy.util.ObjectMethodTester;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests lazy init proxy factory
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class LazyInitProxyFactoryTest extends Assert
{
	private static InterfaceObject interfaceObject = new InterfaceObject("interface");

	private static ConcreteObject concreteObject = new ConcreteObject("concrete");

	private static IProxyTargetLocator interfaceObjectLocator = new IProxyTargetLocator()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public Object locateProxyTarget()
		{
			return LazyInitProxyFactoryTest.interfaceObject;
		}
	};

	private static IProxyTargetLocator concreteObjectLocator = new IProxyTargetLocator()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public Object locateProxyTarget()
		{
			return LazyInitProxyFactoryTest.concreteObject;
		}
	};

	private static IProxyTargetLocator stringObjectLocator = new IProxyTargetLocator()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public Object locateProxyTarget()
		{
			return "StringLiteral";
		}
	};

	/**
	 * Tests lazy init proxy to represent interfaces
	 */
	@Test
	public void testInterfaceProxy()
	{
		// test proxy creation for an interface class
		IInterface proxy = (IInterface)LazyInitProxyFactory.createProxy(IInterface.class,
			interfaceObjectLocator);

		// test we have a jdk dynamic proxy
		assertTrue(Proxy.isProxyClass(proxy.getClass()));

		// test proxy implements ILazyInitProxy
		assertThat(proxy, instanceOf(ILazyInitProxy.class));
		assertTrue(((ILazyInitProxy)proxy).getObjectLocator() == interfaceObjectLocator);

		// test method invocation
		assertEquals(proxy.getMessage(), "interface");

		// test serialization
		IInterface proxy2 = (IInterface)WicketObjects.cloneObject(proxy);
		assertTrue(proxy != proxy2);
		assertEquals(proxy2.getMessage(), "interface");

		// test equals/hashcode method interception
		final IObjectMethodTester tester = new ObjectMethodTester();
		assertTrue(tester.isValid());

		IProxyTargetLocator testerLocator = new IProxyTargetLocator()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Object locateProxyTarget()
			{
				return tester;
			}
		};

		IObjectMethodTester testerProxy = (IObjectMethodTester)LazyInitProxyFactory.createProxy(
			IObjectMethodTester.class, testerLocator);
		testerProxy.equals(this);
		testerProxy.hashCode();
		testerProxy.toString();
		assertTrue(tester.isValid());
	}

	/**
	 * Tests lazy init proxy to represent concrete objects
	 */
	@Test
	public void testConcreteProxy()
	{
		ConcreteObject proxy = (ConcreteObject)LazyInitProxyFactory.createProxy(
			ConcreteObject.class, concreteObjectLocator);

		// test proxy implements ILazyInitProxy
		assertThat(proxy, instanceOf(ILazyInitProxy.class));
		assertTrue(((ILazyInitProxy)proxy).getObjectLocator() == concreteObjectLocator);

		// test we do not have a jdk dynamic proxy
		assertFalse(Proxy.isProxyClass(proxy.getClass()));

		// test method invocation
		assertEquals(proxy.getMessage(), "concrete");

		// test serialization
		ConcreteObject proxy2 = (ConcreteObject)WicketObjects.cloneObject(proxy);
		assertTrue(proxy != proxy2);
		assertEquals(proxy2.getMessage(), "concrete");

		// test equals/hashcode method interception
		final IObjectMethodTester tester = new ObjectMethodTester();
		assertTrue(tester.isValid());

		IProxyTargetLocator testerLocator = new IProxyTargetLocator()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Object locateProxyTarget()
			{
				return tester;
			}
		};

		ObjectMethodTester testerProxy = (ObjectMethodTester)LazyInitProxyFactory.createProxy(
			ObjectMethodTester.class, testerLocator);
		testerProxy.equals(this);
		testerProxy.hashCode();
		testerProxy.toString();
		assertTrue(tester.isValid());
	}

	/**
	 * Tests lazy init concrete replacement replacement
	 */
	@Test
	public void testCGLibInterceptorReplacement()
	{
		ProxyReplacement ser = new ProxyReplacement(ConcreteObject.class.getName(),
			concreteObjectLocator);

		ConcreteObject proxy2 = (ConcreteObject)WicketObjects.cloneObject(ser);
		assertEquals(proxy2.getMessage(), "concrete");
	}

	/**
	 * Tests String beans.
	 */
	@Test
	public void testStringProxy()
	{
		// We special-case String objects to avoid proxying them, as they're
		// final.
		// See WICKET-603.
		String proxy = (String)LazyInitProxyFactory.createProxy(String.class, stringObjectLocator);
		assertEquals("StringLiteral", proxy);
	}
}

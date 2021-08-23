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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ObjectStreamException;
import java.lang.reflect.Proxy;

import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.proxy.LazyInitProxyFactory.ProxyReplacement;
import org.apache.wicket.proxy.util.ConcreteObject;
import org.apache.wicket.proxy.util.IInterface;
import org.apache.wicket.proxy.util.IObjectMethodTester;
import org.apache.wicket.proxy.util.InterfaceObject;
import org.apache.wicket.proxy.util.ObjectMethodTester;
import org.junit.jupiter.api.Test;

/**
 * Tests lazy init proxy factory
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
class LazyInitProxyFactoryTest
{
	private static InterfaceObject interfaceObject = new InterfaceObject("interface");

	private static final ConcreteObject concreteObject = new ConcreteObject("concrete");

	private static IProxyTargetLocator interfaceObjectLocator = new IProxyTargetLocator()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public Object locateProxyTarget()
		{
			return LazyInitProxyFactoryTest.interfaceObject;
		}
	};

	private static final IProxyTargetLocator concreteObjectLocator = new IProxyTargetLocator()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public Object locateProxyTarget()
		{
			return LazyInitProxyFactoryTest.concreteObject;
		}

		// This method is needed to prevent (de)serialization of this locator instance in #testByteBuddyInterceptorReplacement()
		private Object readResolve() throws ObjectStreamException {
			return concreteObjectLocator;
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
	void testInterfaceProxy()
	{
		// test proxy creation for an interface class
		IInterface proxy = (IInterface)LazyInitProxyFactory.createProxy(IInterface.class,
			interfaceObjectLocator);

		// test we have a jdk dynamic proxy
		assertTrue(Proxy.isProxyClass(proxy.getClass()));

		// test proxy implements ILazyInitProxy
		assertTrue(proxy instanceof ILazyInitProxy);
		assertSame(((ILazyInitProxy) proxy).getObjectLocator(), interfaceObjectLocator);

		// test method invocation
		assertEquals("interface", proxy.getMessage());

		// test serialization
		IInterface proxy2 = WicketObjects.cloneObject(proxy);
		assertNotSame(proxy, proxy2);
		assertEquals("interface", proxy2.getMessage());

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
	void testConcreteProxy()
	{
		ConcreteObject proxy = (ConcreteObject)LazyInitProxyFactory.createProxy(
			ConcreteObject.class, concreteObjectLocator);

		// test proxy implements ILazyInitProxy
		assertTrue(proxy instanceof ILazyInitProxy);
		final IProxyTargetLocator objectLocator = ((ILazyInitProxy) proxy).getObjectLocator();
		assertSame(objectLocator, concreteObjectLocator);

		// test we do not have a jdk dynamic proxy
		assertFalse(Proxy.isProxyClass(proxy.getClass()));

		// test method invocation
		assertEquals("concrete", proxy.getMessage());

		// test serialization
		ConcreteObject proxy2 = WicketObjects.cloneObject(proxy);
		assertNotSame(proxy, proxy2);
		assertEquals("concrete", proxy2.getMessage());

		// test equals/hashcode method interception
		final IObjectMethodTester tester = new ObjectMethodTester();
		assertTrue(tester.isValid());

		// test only a single class is generated,
		// otherwise meta space will fill up with each proxy
		assertSame(proxy.getClass(), LazyInitProxyFactory.createProxy(
			ConcreteObject.class, concreteObjectLocator).getClass());

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
	 * Tests lazy init concrete replacement
	 */
	@Test
	void testByteBuddyInterceptorReplacement()
	{
		ProxyReplacement ser = new ProxyReplacement(ConcreteObject.class.getName(),
			concreteObjectLocator);

		Object proxy2 = WicketObjects.cloneObject(ser);
		assertEquals("concrete", ((ConcreteObject)proxy2).getMessage());
	}

	/**
	 * Tests String beans.
	 */
	@Test
	void testStringProxy()
	{
		// We special-case String objects to avoid proxying them, as they're
		// final.
		// See WICKET-603.
		String proxy = (String)LazyInitProxyFactory.createProxy(String.class, stringObjectLocator);
		assertEquals("StringLiteral", proxy);
	}
}

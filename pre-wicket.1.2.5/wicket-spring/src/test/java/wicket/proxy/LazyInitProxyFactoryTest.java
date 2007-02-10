/*
 * $Id$
 * $Revision$
 * $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.proxy;

import java.lang.reflect.Proxy;

import junit.framework.TestCase;

import wicket.proxy.LazyInitProxyFactory.ProxyReplacement;
import wicket.proxy.util.ConcreteObject;
import wicket.proxy.util.IInterface;
import wicket.proxy.util.IObjectMethodTester;
import wicket.proxy.util.InterfaceObject;
import wicket.proxy.util.ObjectMethodTester;
import wicket.util.lang.Objects;
import wicket.util.tester.WicketTester;

/**
 * Tests lazy init proxy factory
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class LazyInitProxyFactoryTest extends TestCase
{


	protected void setUp() throws Exception
	{
		new WicketTester(null);
	}

	private static InterfaceObject interfaceObject = new InterfaceObject("interface");

	private static ConcreteObject concreteObject = new ConcreteObject("concrete");

	private static IProxyTargetLocator interfaceObjectLocator = new IProxyTargetLocator()
	{
		public Object locateProxyTarget()
		{
			return LazyInitProxyFactoryTest.interfaceObject;
		}
	};

	private static IProxyTargetLocator concreteObjectLocator = new IProxyTargetLocator()
	{
		public Object locateProxyTarget()
		{
			return LazyInitProxyFactoryTest.concreteObject;
		}
	};

	/**
	 * Tests lazy init proxy to represent interfaces
	 */
	public void testInterfaceProxy()
	{
		// test proxy creation for an interface class
		IInterface proxy = (IInterface) LazyInitProxyFactory.createProxy(
				IInterface.class, interfaceObjectLocator);

		// test we have a jdk dynamic proxy
		assertTrue(Proxy.isProxyClass(proxy.getClass()));

		// test proxy implements ILazyInitProxy
		assertTrue(proxy instanceof ILazyInitProxy);
		assertTrue(((ILazyInitProxy) proxy).getObjectLocator() == interfaceObjectLocator);

		// test method invocation
		assertEquals(proxy.getMessage(), "interface");

		// test serialization
		IInterface proxy2 = (IInterface) Objects.cloneObject(proxy);
		assertTrue(proxy != proxy2);
		assertEquals(proxy2.getMessage(), "interface");

		// test equals/hashcode method interception
		final IObjectMethodTester tester = new ObjectMethodTester();
		assertTrue(tester.isValid());

		IProxyTargetLocator testerLocator = new IProxyTargetLocator()
		{
			public Object locateProxyTarget()
			{
				return tester;
			}
		};

		IObjectMethodTester testerProxy = (IObjectMethodTester) LazyInitProxyFactory
		.createProxy(IObjectMethodTester.class, testerLocator);
		testerProxy.equals(this);
		testerProxy.hashCode();
		testerProxy.toString();
		assertTrue(tester.isValid());
	}

	/**
	 * Tests lazy init proxy to represent concrete objects
	 */
	public void testConcreteProxy()
	{
		ConcreteObject proxy = (ConcreteObject) LazyInitProxyFactory.createProxy(
				ConcreteObject.class, concreteObjectLocator);

		// test proxy implements ILazyInitProxy
		assertTrue(proxy instanceof ILazyInitProxy);
		assertTrue(((ILazyInitProxy) proxy).getObjectLocator() == concreteObjectLocator);

		// test we do not have a jdk dynamic proxy
		assertFalse(Proxy.isProxyClass(proxy.getClass()));

		// test method invocation
		assertEquals(proxy.getMessage(), "concrete");

		// test serialization
		ConcreteObject proxy2 = (ConcreteObject) Objects.cloneObject(proxy);
		assertTrue(proxy != proxy2);
		assertEquals(proxy2.getMessage(), "concrete");

		// test equals/hashcode method interception
		final IObjectMethodTester tester = new ObjectMethodTester();
		assertTrue(tester.isValid());

		IProxyTargetLocator testerLocator = new IProxyTargetLocator()
		{
			public Object locateProxyTarget()
			{
				return tester;
			}
		};

		ObjectMethodTester testerProxy = (ObjectMethodTester) LazyInitProxyFactory
		.createProxy(ObjectMethodTester.class, testerLocator);
		testerProxy.equals(this);
		testerProxy.hashCode();
		testerProxy.toString();
		assertTrue(tester.isValid());
	}

	/**
	 * Tests lazy init concrete replacement replacement
	 */
	public void testCGLibInterceptorReplacement()
	{
		ProxyReplacement ser = new ProxyReplacement(
				ConcreteObject.class.getName(), concreteObjectLocator);

		ConcreteObject proxy2 = (ConcreteObject) Objects.cloneObject(ser);
		assertEquals(proxy2.getMessage(), "concrete");
	}

}

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
package org.apache.wicket.proxy.packageprivate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;

import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.proxy.ILazyInitProxy;
import org.apache.wicket.proxy.IProxyTargetLocator;
import org.apache.wicket.proxy.LazyInitProxyFactory;
import org.apache.wicket.proxy.util.IObjectMethodTester;
import org.apache.wicket.proxy.util.ObjectMethodTester;
import org.junit.jupiter.api.Test;

/**
 * Tests lazy init proxy factory
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
class PackagePrivateTest
{
	private static final PackagePrivateConcreteObject PACKAGE_PRIVATE_CONCRETE_OBJECT = new PackagePrivateConcreteObject("package-private-concrete");

	private final static IProxyTargetLocator PACKAGE_PRIVATE_CONCRETE_OBJECT_LOCATOR = new IProxyTargetLocator()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public Object locateProxyTarget()
		{
			return PackagePrivateTest.PACKAGE_PRIVATE_CONCRETE_OBJECT;
		}
	};

	/**
	 * Tests lazy init proxy to represent package private concrete objects
	 *
	 * https://issues.apache.org/jira/browse/WICKET-4324
	 */
	@Test
	void testPackagePrivateConcreteProxy()
	{
		PackagePrivateConcreteObject proxy = (PackagePrivateConcreteObject)LazyInitProxyFactory.createProxy(
				PackagePrivateConcreteObject.class, PACKAGE_PRIVATE_CONCRETE_OBJECT_LOCATOR);

		// test proxy implements ILazyInitProxy
		assertTrue(proxy instanceof ILazyInitProxy);
		assertSame(((ILazyInitProxy)proxy).getObjectLocator(), PACKAGE_PRIVATE_CONCRETE_OBJECT_LOCATOR);

		// test we do not have a jdk dynamic proxy
		assertFalse(Proxy.isProxyClass(proxy.getClass()));

		// test method invocation
		assertEquals("package-private-concrete", proxy.getMessage());

		// test serialization
		PackagePrivateConcreteObject proxy2 = WicketObjects.cloneObject(proxy);
		assertNotSame(proxy, proxy2);
		assertEquals("package-private-concrete", proxy2.getMessage());

		// test equals/hashcode method interception
		final IObjectMethodTester tester = new ObjectMethodTester();
		assertTrue(tester.isValid());

		// test only a single class is generated,
		// otherwise permgen space will fill up with each proxy
		assertSame(proxy.getClass(), LazyInitProxyFactory.createProxy(
				PackagePrivateConcreteObject.class, PACKAGE_PRIVATE_CONCRETE_OBJECT_LOCATOR).getClass());

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
}

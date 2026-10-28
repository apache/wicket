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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Timestamp;

import org.apache.wicket.proxy.util.ConcreteObject;
import org.apache.wicket.proxy.util.MockObjectLocator;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link ByteBuddyProxyFactory}
 */
class ByteBuddyProxyFactoryTest
{
	/**
	 * A thread with another context class loader gets the proxy class that is already defined.
	 */
	@Test
	void proxyClassIsReusedAcrossContextClassLoaders() throws Exception
	{
		Thread thread = Thread.currentThread();
		ClassLoader original = thread.getContextClassLoader();

		try (URLClassLoader other = new URLClassLoader(new URL[0], original))
		{
			Class<ConcreteObject> first = ByteBuddyProxyFactory
				.createOrGetProxyClass(ConcreteObject.class);

			thread.setContextClassLoader(other);
			Class<ConcreteObject> second = ByteBuddyProxyFactory
				.createOrGetProxyClass(ConcreteObject.class);

			assertSame(first, second);
		}
		finally
		{
			thread.setContextClassLoader(original);
		}
	}

	/**
	 * A type in a <em>java.**</em> package of the platform class loader, which cannot see the
	 * interfaces the proxy implements.
	 */
	@Test
	void proxiesTypeInJavaPackageOfPlatformClassLoader()
	{
		Timestamp proxy = new ByteBuddyProxyFactory().createProxy(Timestamp.class,
			new MockObjectLocator(new Timestamp(42)));

		assertEquals(42, proxy.getTime());
	}
}

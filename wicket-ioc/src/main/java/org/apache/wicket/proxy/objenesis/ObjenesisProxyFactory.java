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
package org.apache.wicket.proxy.objenesis;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.core.NamingPolicy;
import org.apache.wicket.proxy.ILazyInitProxy;
import org.apache.wicket.proxy.IProxyTargetLocator;
import org.apache.wicket.proxy.LazyInitProxyFactory.IWriteReplace;
import org.objenesis.ObjenesisStd;

import java.io.Serializable;

public class ObjenesisProxyFactory
{
	private static final ObjenesisStd OBJENESIS = new ObjenesisStd(false);

	public static Object createProxy(final Class<?> type, final IProxyTargetLocator locator, NamingPolicy namingPolicy)
	{
		ObjenesisCGLibInterceptor handler = new ObjenesisCGLibInterceptor(type, locator);

		Enhancer e = new Enhancer();
		e.setInterfaces(new Class[]{Serializable.class, ILazyInitProxy.class, IWriteReplace.class});
		e.setSuperclass(type);
		e.setCallbackType(handler.getClass());
		e.setNamingPolicy(namingPolicy);
		e.setUseCache(false);

		Class<?> proxyClass = e.createClass();
		Enhancer.registerCallbacks(proxyClass, new Callback[]{handler});
		return OBJENESIS.newInstance(proxyClass);
	}
}

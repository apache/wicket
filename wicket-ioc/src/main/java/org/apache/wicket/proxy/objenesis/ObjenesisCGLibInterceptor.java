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

import java.io.ObjectStreamException;

import org.apache.wicket.proxy.IProxyTargetLocator;
import org.apache.wicket.proxy.LazyInitProxyFactory;

/**
 * Method interceptor for proxies representing concrete object not backed by an interface. These
 * proxies are representing by cglib proxies.
 */
public class ObjenesisCGLibInterceptor extends LazyInitProxyFactory.AbstractCGLibInterceptor
{
	public ObjenesisCGLibInterceptor(Class<?> type, IProxyTargetLocator locator) {
		super(type, locator);
	}

	@Override
	public Object writeReplace() throws ObjectStreamException
	{
		return new ObjenesisProxyReplacement(typeName, locator);
	}
}

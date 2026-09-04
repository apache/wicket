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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.function.Function;

import org.apache.wicket.proxy.ILazyInitProxy;
import org.apache.wicket.proxy.IProxyTargetLocator;
import org.apache.wicket.proxy.LazyInitProxyFactory;
import org.apache.wicket.proxy.LazyInitProxyFactory.IWriteReplace;
import org.apache.wicket.proxy.LazyInitProxyFactory.ProxyReplacement;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.Pipe;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

/**
 * Method interceptor for proxies representing concrete object not backed by an interface.
 * These proxies are represented by ByteBuddy proxies.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class ByteBuddyInterceptor
	implements
		ILazyInitProxy,
		Serializable,
		IWriteReplace
{
	private static final long serialVersionUID = 1L;

	protected final IProxyTargetLocator locator;

	protected final String typeName;

	private transient Object target;

	/**
	 * Constructor
	 * 
	 * @param type
	 *            class of the object this proxy was created for
	 * 
	 * @param locator
	 *            object locator used to locate the object this proxy represents
	 */
	public ByteBuddyInterceptor(final Class<?> type, final IProxyTargetLocator locator)
	{
		super();
		
		this.typeName = type.getName();
		this.locator = locator;
	}

	@RuntimeType
	public Object intercept(@Origin Method method, @AllArguments Object[] args, @Pipe Function pipe) throws Exception
	{
		if (LazyInitProxyFactory.isFinalizeMethod(method))
		{
			// swallow finalize call
			return null;
		}
		else if (LazyInitProxyFactory.isEqualsMethod(method))
		{
			return (equals(args[0])) ? Boolean.TRUE : Boolean.FALSE;
		}
		else if (LazyInitProxyFactory.isHashCodeMethod(method))
		{
			return hashCode();
		}
		else if (LazyInitProxyFactory.isToStringMethod(method))
		{
			return toString();
		}
		
		if (target == null)
		{
			target = locator.locateProxyTarget();
		}
		
		return pipe.apply(target);
	}

	@Override
	public IProxyTargetLocator getObjectLocator()
	{
		return locator;
	}

	@Override
	public Object writeReplace() throws ObjectStreamException
	{
		return new ProxyReplacement(typeName, locator);
	}
}
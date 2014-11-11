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
package org.apache.wicket.util.crypt;


import org.apache.wicket.util.lang.Args;

/**
 * {@link ICryptFactory} decorator that caches the call to {@link ICryptFactory#newCrypt()}
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class CryptFactoryCachingDecorator implements ICryptFactory
{
	private final ICryptFactory delegate;
	private ICrypt cache;

	/**
	 * Construct.
	 * 
	 * @param delegate
	 *            the crypt factory whose {@link ICryptFactory#newCrypt()} call will be cached
	 */
	public CryptFactoryCachingDecorator(final ICryptFactory delegate)
	{
		Args.notNull(delegate, "delegate");
		this.delegate = delegate;
	}

	@Override
	public final ICrypt newCrypt()
	{
		if (cache == null)
		{
			cache = delegate.newCrypt();
		}
		return cache;
	}
}

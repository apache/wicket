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
package org.apache.wicket.authentication.strategy;

import org.apache.wicket.authentication.IAuthenticationStrategy;

/**
 * A no-op implementation. No username or password will be persisted or retrieved.
 * 
 * @author Juergen Donnerstag
 * @deprecated no replacement; this implementation goes with the deprecated
 *             {@link IAuthenticationStrategy} it implements, and is removed in Wicket 11.
 */
@Deprecated(since = "8.19.0, 9.24.0, 10.11.0", forRemoval = true)
@SuppressWarnings("removal")
public class NoOpAuthenticationStrategy implements IAuthenticationStrategy
{
	@Override
	public String[] load()
	{
		return null;
	}

	@Override
	public void save(final String credential, final String... extraCredentials)
	{
	}

	@Override
	public void remove()
	{
	}
}

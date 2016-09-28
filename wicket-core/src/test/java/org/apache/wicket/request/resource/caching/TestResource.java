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
package org.apache.wicket.request.resource.caching;

import java.io.Serializable;

import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;

/**
 * Resource used by the tests for {@link IResourceCachingStrategy} implementations
 */
class TestResource implements IStaticCacheableResource
{
	@Override
	public boolean isCachingEnabled()
	{
		return true;
	}

	@Override
	public Serializable getCacheKey()
	{
		return "key";
	}

	@Override
	public IResourceStream getResourceStream()
	{
		return new StringResourceStream("content");
	}

	@Override
	public void respond(Attributes attributes)
	{
	}
}

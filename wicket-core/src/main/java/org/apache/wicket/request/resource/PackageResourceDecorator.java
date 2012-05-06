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
package org.apache.wicket.request.resource;

import java.io.Serializable;

import org.apache.wicket.util.resource.IResourceStream;

/**
 * wraps an existing {@link org.apache.wicket.request.resource.IPackageResource} and 
 * enables overriding of its public methods.
 * 
 * @autor Peter Ertl
 */
public class PackageResourceDecorator implements IPackageResource
{
	private final IPackageResource resource;

	public PackageResourceDecorator(final IPackageResource resource)
	{
		this.resource = resource;
	}

	@Override
	public Class<?> getScope()
	{
		return resource.getScope();
	}

	@Override
	public String getStyle()
	{
		return resource.getStyle();
	}

	@Override
	public IResourceStream getResourceStream()
	{
		return resource.getResourceStream();
	}

	@Override
	public boolean isCompress()
	{
		return resource.isCompress();
	}

	@Override
	public void setCompress(final boolean compress)
	{
		resource.setCompress(compress);
	}

	@Override
	public Serializable getCacheKey()
	{
		return resource.getCacheKey();
	}

	@Override
	public IResourceStream getCacheableResourceStream()
	{
		return resource.getCacheableResourceStream();
	}

	@Override
	public void respond(final Attributes attributes)
	{
		resource.respond(attributes);
	}
}

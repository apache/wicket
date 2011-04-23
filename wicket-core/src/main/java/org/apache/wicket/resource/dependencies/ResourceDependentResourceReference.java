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
package org.apache.wicket.resource.dependencies;

import java.util.Locale;

import org.apache.wicket.request.resource.IResource;

/**
 * A basic implementation of {@link AbstractResourceDependentResourceReference} that contains an
 * array of {@link AbstractResourceDependentResourceReference} dependencies that must be passed in
 * at construction time.
 * 
 * @author Jeremy Thomerson
 */
public class ResourceDependentResourceReference extends AbstractResourceDependentResourceReference
{

	private static final long serialVersionUID = 1L;

	private final AbstractResourceDependentResourceReference[] dependencies;

	/**
	 * Construct.
	 * 
	 * @param scope
	 * @param name
	 * @param locale
	 * @param style
	 * @param variation
	 * @param dependencies
	 */
	public ResourceDependentResourceReference(Class<?> scope, String name, Locale locale,
		String style, String variation, AbstractResourceDependentResourceReference[] dependencies)
	{
		super(scope, name, locale, style, variation);
		this.dependencies = dependencies;
	}

	/**
	 * Construct.
	 * 
	 * @param scope
	 * @param name
	 * @param dependencies
	 */
	public ResourceDependentResourceReference(Class<?> scope, String name,
		AbstractResourceDependentResourceReference[] dependencies)
	{
		super(scope, name);
		this.dependencies = dependencies;
	}

	/**
	 * Construct.
	 * 
	 * @param name
	 * @param dependencies
	 */
	public ResourceDependentResourceReference(String name,
		AbstractResourceDependentResourceReference[] dependencies)
	{
		super(name);
		this.dependencies = dependencies;
	}

	@Override
	public final AbstractResourceDependentResourceReference[] getDependentResourceReferences()
	{
		return dependencies;
	}

	@Override
	public IResource getResource()
	{
		return null;
	}

}

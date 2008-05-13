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
package org.apache.wicket.markup.html.resources;

import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.Resource;
import org.apache.wicket.markup.html.CompressedPackageResource;
import org.apache.wicket.markup.html.PackageResource;
import org.apache.wicket.markup.html.PackageResourceReference;


/**
 * 
 * A static resource reference which can be transferred to the browser using the gzip compression.
 * Reduces the download size of for example javascript resources.
 * 
 * see {@link PackageResourceReference} and {@link CompressedPackageResource}
 * 
 * @author Janne Hietam&auml;ki
 * 
 * @deprecated Will be renamed to CompressedResourceReference in Wicket 2.0
 */
@Deprecated
public class CompressedPackageResourceReference extends PackageResourceReference
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see PackageResourceReference#PackageResourceReference(Application, Class, String)
	 */
	public CompressedPackageResourceReference(Application application, Class<?> scope, String name)
	{
		super(application, scope, name);
	}

	/**
	 * @see PackageResourceReference#PackageResourceReference(Application, Class, String, Locale,
	 * 	String)
	 */
	public CompressedPackageResourceReference(Application application, Class<?> scope, String name,
		Locale locale, String style)
	{
		super(application, scope, name, locale, style);
	}

	/**
	 * @see PackageResourceReference#PackageResourceReference(Application, String)
	 */
	public CompressedPackageResourceReference(Application application, String name)
	{
		super(application, name);
	}

	/**
	 * @see PackageResourceReference#PackageResourceReference(Class, String)
	 */
	public CompressedPackageResourceReference(Class<?> scope, String name)
	{
		super(scope, name);
	}

	/**
	 * @see org.apache.wicket.markup.html.PackageResourceReference#newResource()
	 */
	@Override
	protected Resource newResource()
	{
		PackageResource packageResource = CompressedPackageResource.get(getScope(), getName(),
			getLocale(), getStyle());
		if (packageResource != null)
		{
			locale = packageResource.getLocale();
		}
		else
		{
			throw new IllegalArgumentException("package resource [scope=" + getScope() + ",name=" +
				getName() + ",locale=" + getLocale() + "style=" + getStyle() + "] not found");
		}
		return packageResource;

	}
}

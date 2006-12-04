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
package wicket.markup.html.resources;

import wicket.Resource;
import wicket.ResourceReference;
import wicket.markup.html.CompressedPackageResource;
import wicket.markup.html.PackageResource;

/**
 * 
 * A static resource reference which can be transferred to the browser using the
 * gzip compression. Reduces the download size of for example javascript
 * resources.
 * 
 * see {@link ResourceReference} and {@link CompressedPackageResource}
 * 
 * @author Janne Hietam&auml;ki
 */
public class CompressedResourceReference extends ResourceReference
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param scope
	 * @param name
	 */
	public CompressedResourceReference(Class scope, String name)
	{
		super(scope, name);
	}

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
			throw new IllegalArgumentException("package resource [scope=" + getScope() + ",name="
					+ getName() + ",locale=" + getLocale() + "style=" + getStyle() + "] not found");
		}
		return packageResource;

	}
}

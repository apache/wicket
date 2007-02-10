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

import java.util.Locale;

import wicket.Resource;
import wicket.ResourceReference;
import wicket.markup.html.JavascriptPackageResource;
import wicket.markup.html.PackageResource;

/**
 * Static resource reference for javacript resources. The resources are filtered
 * (stripped comments and whitespace) and gzipped.
 * 
 * @author Matej Knopp
 */
public class JavascriptResourceReference extends ResourceReference
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new javascript resource reference.
	 * @param scope
	 * @param name
	 * @param locale
	 * @param style
	 */
	public JavascriptResourceReference(Class scope, String name, Locale locale, String style)
	{
		super(scope, name, locale, style);
	}

	/**
	 * Creates a new javascript resource reference.
	 * @param scope
	 * @param name
	 */
	public JavascriptResourceReference(Class scope, String name)
	{
		super(scope, name);
	}

	/**
	 * Creates a new javascript resource reference.
	 * @param name
	 */
	public JavascriptResourceReference(String name)
	{
		super(name);
	}
	
	@Override
	protected Resource newResource()
	{
		PackageResource packageResource = JavascriptPackageResource.get(getScope(), getName(),
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

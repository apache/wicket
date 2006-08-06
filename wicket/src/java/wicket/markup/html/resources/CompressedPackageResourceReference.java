/*
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.resources;

import java.util.Locale;

import wicket.Application;
import wicket.Resource;
import wicket.markup.html.CompressedPackageResource;
import wicket.markup.html.PackageResource;
import wicket.markup.html.PackageResourceReference;

/**
 * 
 * A static resource reference which can be transferred to the browser
 * using the gzip compression. Reduces the download size of for example 
 * javascript resources.
 * 
 * see {@link PackageResourceReference} and {@link CompressedPackageResource}
 * 
 * @author Janne Hietam&auml;ki
 */
public class CompressedPackageResourceReference extends PackageResourceReference
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * @param application
	 * @param scope
	 * @param name
	 * @param locale
	 * @param style
	 */
	public CompressedPackageResourceReference(Application application, Class scope, String name, Locale locale, String style)
	{
		super(application, scope, name, locale, style);
	}

	/**
	 * Construct.
	 * @param application
	 * @param scope
	 * @param name
	 */
	public CompressedPackageResourceReference(Application application, Class scope, String name)
	{
		super(application, scope, name);
	}

	/**
	 * Construct.
	 * @param application
	 * @param name
	 */
	public CompressedPackageResourceReference(Application application, String name)
	{
		super(application, name);
	}

	/**
	 * Construct.
	 * @param scope
	 * @param name
	 */
	public CompressedPackageResourceReference(Class scope, String name)
	{
		super(scope, name);
	}

	@Override
	protected Resource newResource()
	{
		PackageResource packageResource = CompressedPackageResource.get(getScope(), getName(), getLocale(),
				getStyle());
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

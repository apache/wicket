/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.resource;

import java.util.Locale;

import wicket.Application;
import wicket.ApplicationPages;
import wicket.ApplicationSettings;
import wicket.ISessionFactory;
import wicket.resource.ApplicationStringResourceLoader;
import wicket.resource.IStringResourceLoader;
import junit.framework.Assert;

/**
 * Tests for the <code>ApplicationStringResourceLoader</code> class.
 * @author Chris Turner
 */
public class ApplicationStringResourceLoaderTest extends StringResourceLoaderTestBase
{

	/**
	 * Create the test case.
	 * @param message The test name
	 */
	public ApplicationStringResourceLoaderTest(String message)
	{
		super(message);
	}

	/**
	 * Return the loader instance
	 * @return The loader instance to test
	 */
	protected IStringResourceLoader createLoader()
	{
		return new ApplicationStringResourceLoader(application);
	}

	/**
	 * @see wicket.resource.StringResourceLoaderTestBase#testLoaderUnknownResources()
	 */
	public void testLoaderUnknownResources()
	{
		Application app = new Application()
		{
			public String getName()
			{
				return "MissingResourceApp";
			}

			public ApplicationSettings getSettings()
			{
				return settings;
			}
            
            public ApplicationPages getPages()
            {
                return pages;
            }
            
			public ISessionFactory getSessionFactory()
			{
				return null;
			}

            private ApplicationPages pages = new ApplicationPages();
			private ApplicationSettings settings = new ApplicationSettings(this);
		};

		IStringResourceLoader loader = new ApplicationStringResourceLoader(app);
		Assert.assertNull("Unknown resource should return null", loader.loadStringResource(component,
				"test.string", Locale.getDefault(), null));
	}

	/**
	 * 
	 */
	public void testNullApplication()
	{
		try
		{
			new ApplicationStringResourceLoader(null);
			Assert.fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException e)
		{
			// Extected result
		}
	}

}

// 

/*
 * $Id: StringResourceLoaderTestBase.java 5844 2006-05-24 20:53:56 +0000 (Wed,
 * 24 May 2006) joco01 $ $Revision$ $Date: 2006-05-24 20:53:56 +0000
 * (Wed, 24 May 2006) $
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.resource;

import java.util.Locale;

import junit.framework.Assert;
import junit.framework.TestCase;
import wicket.Application;
import wicket.Component;
import wicket.MockPageWithOneComponent;
import wicket.Page;
import wicket.resource.loader.IStringResourceLoader;
import wicket.util.tester.WicketTester;

/**
 * Abstract base class providing common test functionality to ensure that all
 * loader implementations comply with the contract of the loader interface.
 * 
 * @author Chris Turner
 */
public abstract class StringResourceLoaderTestBase extends TestCase
{
	// The loader to test
	protected IStringResourceLoader loader;

	// The dummy tester
	protected Application application;

	// The dummy component
	protected Component component;

	/**
	 * Create the test case.
	 * 
	 * @param message
	 *            The name of the test
	 */
	protected StringResourceLoaderTestBase(String message)
	{
		super(message);
	}

	/**
	 * Abstract method to create the loader instance to be tested.
	 * 
	 * @return The loader instance to test
	 */
	protected abstract IStringResourceLoader createLoader();

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		
		this.application = new WicketTester(new DummyApplication()).getApplication();
		Page page = new MockPageWithOneComponent();
		this.component = new DummyComponent(page, "component", this.application);
		this.loader = createLoader();
	}

	/**
	 * 
	 */
	public void testLoaderValidKeyNoStyleDefaultLocale()
	{
		String s = loader.loadStringResource(component.getClass(), "test.string", Locale
				.getDefault(), null);
		Assert.assertEquals("Resource should be loaded", "This is a test", s);

		// And do it again to ensure caching path is exercised
		s = loader.loadStringResource(component.getClass(), "test.string", Locale.getDefault(),
				null);
		Assert.assertEquals("Resource should be loaded", "This is a test", s);
	}

	/**
	 * 
	 */
	public void testLoaderInvalidKeyNoStyleDefaultLocale()
	{
		Assert.assertNull("Missing key should return null", loader.loadStringResource(component
				.getClass(), "unknown.string", Locale.getDefault(), null));
	}

	/**
	 * 
	 */
	public void testLoaderValidKeyNoStyleAlternativeLocale()
	{
		String s = loader.loadStringResource(component.getClass(), "test.string", new Locale("zz"),
				null);
		Assert.assertEquals("Resource should be loaded", "Flib flob", s);
	}

	/**
	 * 
	 */
	public void testLoaderInvalidKeyNoStyleAlternativeLocale()
	{
		Assert.assertNull("Missing key should return null", loader.loadStringResource(component
				.getClass(), "unknown.string", new Locale("zz"), null));
	}

	/**
	 * 
	 */
	public void testLoaderValidKeyStyleNoLocale()
	{
		String s = loader.loadStringResource(component.getClass(), "test.string", null, "alt");
		Assert.assertEquals("Resource should be loaded", "Alt test string", s);
	}

	/**
	 * 
	 */
	public abstract void testLoaderUnknownResources();

}

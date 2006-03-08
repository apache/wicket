/*
 * $Id: WicketTagComponentResolver.java,v 1.4 2005/01/18 08:04:29 jonathanlocke
 * Exp $ $Revision$ $Date$
 * 
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
package wicket.markup.html;

import java.util.regex.Pattern;

import junit.framework.TestCase;
import wicket.Application;
import wicket.SharedResources;
import wicket.protocol.http.MockWebApplication;

/**
 * Tests for package resources.
 * 
 * @author Eelco Hillenius
 */
public class PackageResourceTest extends TestCase
{
	/** mock application object */
	public MockWebApplication application;

	/**
	 * Construct.
	 */
	public PackageResourceTest()
	{
		super();
	}

	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public PackageResourceTest(String name)
	{
		super(name);
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		application = new MockWebApplication(null);
	}

	/**
	 * Tests binding a single absolute package resource.
	 * 
	 * @throws Exception
	 */
	public void testBindAbsolutePackageResource() throws Exception
	{
		final SharedResources sharedResources = Application.get().getSharedResources();
		PackageResource.bind(application, PackageResourceTest.class, "packaged1.txt");
		assertNotNull("resource packaged1.txt should be available as a packaged resource",
				sharedResources.get(PackageResourceTest.class, "packaged1.txt", null, null, true));
		assertNull("resource packaged2.txt should NOT be available as a packaged resource",
				sharedResources.get(PackageResourceTest.class, "packaged2.txt", null, null, true));
	}

	/**
	 * Tests binding a bunch of resources in one go using a regexp.
	 * 
	 * @throws Exception
	 */
	public void testBindGlobPackageResource() throws Exception
	{
		final SharedResources sharedResources = Application.get().getSharedResources();
		PackageResource.bind(application, PackageResourceTest.class, Pattern.compile(".*\\.js"));
		assertNotNull("resource packaged3.js should be available as a packaged resource",
				sharedResources.get(PackageResourceTest.class, "packaged3.js", null, null, true));
		assertNotNull("resource packaged4.js should be available as a packaged resource",
				sharedResources.get(PackageResourceTest.class, "packaged4.js", null, null, true));
		assertNull("resource packaged2.txt should NOT be available as a packaged resource",
				sharedResources.get(PackageResourceTest.class, "packaged2.txt", null, null, true));
	}
}

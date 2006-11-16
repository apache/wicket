/*
 * $Id: NiceUrlApplication.java 5398 2006-04-17 00:26:51 -0700 (Mon, 17 Apr
 * 2006) jdonnerstag $ $Revision$ $Date: 2006-04-17 00:26:51 -0700
 * (Mon, 17 Apr 2006) $
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
package wicket.examples.niceurl;

import wicket.examples.WicketExampleApplication;
import wicket.examples.niceurl.mounted.Page3;
import wicket.protocol.http.request.WebRequestCodingStrategy;
import wicket.request.IRequestCycleProcessor;
import wicket.request.compound.CompoundRequestCycleProcessor;
import wicket.request.target.coding.QueryStringUrlCodingStrategy;
import wicket.util.lang.PackageName;

/**
 * Application class for this example.
 * 
 * @author Eelco Hillenius
 */
public class NiceUrlApplication extends WicketExampleApplication
{
	/**
	 * Construct.
	 */
	public NiceUrlApplication()
	{
		super();
	}

	/**
	 * @see wicket.Application#getHomePage()
	 */
	public Class getHomePage()
	{
		return Home.class;
	}

	/**
	 * @see wicket.examples.WicketExampleApplication#init()
	 */
	protected void init()
	{
		// Disable creation of javascript which jWebUnit (test only)
		// doesn't handle properly
		getPageSettings().setAutomaticMultiWindowSupport(false);

		// mount single bookmarkable pages
		mountBookmarkablePage("/the/homepage/path", Home.class);
		mountBookmarkablePage("/a/nice/path/to/the/first/page", Page1.class);
		mountBookmarkablePage("/path/to/page2", Page2.class);

		mountBookmarkablePageWithUrlCoding("/path/to/page2qpencoded/", Page2QP.class);

		// mount a whole package at once (all bookmarkable pages,
		// the relative class name will be part of the url

		// maybe not the neatest sight, but for package mounting it makes
		// sense to use one of the (important) classes in your package, so
		// that any refactoring (like a package rename) will automatically
		// be applied here.
		mount("/my/mounted/package", PackageName.forClass(Page3.class));
	}

	private void mountBookmarkablePageWithUrlCoding(String path, Class pageClass)
	{
		mount(path, new QueryStringUrlCodingStrategy(path, pageClass));
	}

	/**
	 * Sets up a request coding strategy that uses case-insensitive mounts
	 * 
	 * @see wicket.protocol.http.WebApplication#newRequestCycleProcessor()
	 */
	protected IRequestCycleProcessor newRequestCycleProcessor()
	{
		WebRequestCodingStrategy.Settings stratSettings = new WebRequestCodingStrategy.Settings();
		stratSettings.setMountsCaseSensitive(false);

		WebRequestCodingStrategy strat = new WebRequestCodingStrategy(stratSettings);
		return new CompoundRequestCycleProcessor(strat);
	}
}

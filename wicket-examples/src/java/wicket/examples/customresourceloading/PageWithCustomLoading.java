/*
 * $Id: HelloWorld.java 4961 2006-03-15 13:37:17 -0800 (Wed, 15 Mar 2006)
 * jdonnerstag $ $Revision: 4961 $ $Date: 2006-03-15 13:37:17 -0800 (Wed, 15 Mar
 * 2006) $
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
package wicket.examples.customresourceloading;

import java.net.URL;

import wicket.examples.WicketExamplePage;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.UrlResourceStream;

/**
 * The markup for this page is loaded by the Page component itself.
 * 
 * @author Eelco Hillenius
 */
public class PageWithCustomLoading extends WicketExamplePage
{
	/**
	 * Constructor
	 */
	public PageWithCustomLoading()
	{
	}

	/**
	 * This implementation loads from a custom name/ location. While not
	 * advisable as the default way of loading resources, overriding this method
	 * can provide a component specific break out so that you e.g. can load a
	 * template from a database without any other component or the application
	 * having to know about it.
	 * 
	 * @see wicket.MarkupContainer#newMarkupResourceStream(Class)
	 * 
	 * @param containerClass
	 *            The container the markup should be associated with
	 * @return A IResourceStream if the resource was found
	 */
	public IResourceStream newMarkupResourceStream(final Class containerClass)
	{
		// load a template with a totally different name from this package using
		// this component's class loader
		final URL url = PageWithCustomLoading.class.getResource("CustomLoadedTemplate.html");
		if (url != null)
		{
			return new UrlResourceStream(url);
		}

		// not resource was not found
		return null;
	}
}
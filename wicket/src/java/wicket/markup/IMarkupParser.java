/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.markup;

import java.io.IOException;
import java.text.ParseException;

import wicket.ApplicationSettings;
import wicket.Page;
import wicket.util.resource.Resource;
import wicket.util.resource.ResourceNotFoundException;

/**
 * Interface to a markup parser which parses wicket markup according to various
 * settings.
 * 
 * @author Jonathan Locke
 */
public interface IMarkupParser
{
	/**
	 * Settings to use when configuring this markup parser
	 * 
	 * @param settings
	 *            Application settings
	 */
	public void configure(final ApplicationSettings settings);

	/**
	 * Return the encoding used while reading the markup file.
	 * 
	 * @return if null, than JVM default
	 */
	public String getEncoding();

	/**
	 * Reads and parses markup from a file. Autolinks are resolved relative to
	 * the autolinkBasePage passed in The page provided will serve as the
	 * reference for autolinks on the current markup.
	 * 
	 * @param resource
	 *            The file
	 * @param autolinkBasePage
	 *            Autolink reference page
	 * @return The markup
	 * @throws ParseException
	 * @throws IOException
	 * @throws ResourceNotFoundException
	 */
	public Markup read(final Resource resource, final Page autolinkBasePage) throws ParseException,
			IOException, ResourceNotFoundException;
}
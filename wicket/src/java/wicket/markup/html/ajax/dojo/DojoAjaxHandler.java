/*
 * $Id$
 * $Revision$
 * $Date$
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
package wicket.markup.html.ajax.dojo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.IInitializer;
import wicket.markup.html.HtmlHeaderContainer;
import wicket.markup.html.PackageResource;
import wicket.markup.html.PackageResourceReference;
import wicket.markup.html.ajax.AbstractAjaxHandler;

/**
 * Handles event requests using Dojo.
 * <p>
 * This class is mainly here to automatically add the javascript files you need. As
 * header contributions are done once per class, you can have multiple instances/
 * subclasses without having duplicate header contributions.
 * </p>
 * @see <a href="http://dojotoolkit.org/">Dojo</a>
 * @author Eelco Hillenius
 */
public abstract class DojoAjaxHandler
	extends AbstractAjaxHandler implements IInitializer
{
	/** log. */
	private static Log log = LogFactory.getLog(DojoAjaxHandler.class);

	/**
	 * Construct.
	 */
	public DojoAjaxHandler()
	{
	}

	/**
	 * Register packaged javascript files.
	 * @param application The application
	 */
	public void init(Application application)
	{
		PackageResource.bind(application, DojoAjaxHandler.class, "dojo.js");
	}

	/**
	 * Let this handler print out the needed header contributions.
	 * @param container
	 */
	public final void printHeadInitContribution(HtmlHeaderContainer container)
	{
		// add our basic javascript needs to the header
		addJsReference(container, new PackageResourceReference(
				Application.get(), DojoAjaxHandler.class, "dojo.js"));
	}
}

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
package wicket.markup.html.ajax.scriptaculous;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.AjaxHandler;
import wicket.Application;
import wicket.IInitializer;
import wicket.markup.html.HtmlHeaderContainer;
import wicket.markup.html.PackageResource;
import wicket.markup.html.PackageResourceReference;

/**
 * Handles event requests using 'script.aculo.us'.
 * <p>
 * This class is mainly here to automatically add the javascript files you need. As header
 * contributions are done once per class, you can have multiple instances/ subclasses
 * without having duplicate header contributions.
 * </p>
 * @see <a href="http://script.aculo.us/">script.aculo.us</a>
 * @author Eelco Hillenius
 */
public abstract class ScriptaculousAjaxHandler
	extends AjaxHandler implements IInitializer
{
	/** log. */
	private static Log log = LogFactory.getLog(ScriptaculousAjaxHandler.class);

	/**
	 * Construct.
	 */
	public ScriptaculousAjaxHandler()
	{
	}

	/**
	 * Register packaged javascript files.
	 * @param application The application
	 */
	public void init(Application application)
	{
		PackageResource.bind(application, ScriptaculousAjaxHandler.class, "prototype.js");
		PackageResource.bind(application, ScriptaculousAjaxHandler.class, "controls.js");
		PackageResource.bind(application, ScriptaculousAjaxHandler.class, "dragdrop.js");
		PackageResource.bind(application, ScriptaculousAjaxHandler.class, "effects.js");
		PackageResource.bind(application, ScriptaculousAjaxHandler.class, "scriptaculous.js");
		PackageResource.bind(application, ScriptaculousAjaxHandler.class, "util.js");
	}

	/**
	 * Let this handler print out the needed header contributions.
	 * @param container
	 */
	protected final void renderHeadInitContribution(HtmlHeaderContainer container)
	{
		// add our basic javascript needs to the header
		Application application = Application.get();
		addJsReference(container, new PackageResourceReference(application,
				ScriptaculousAjaxHandler.class, "prototype.js"));
		addJsReference(container, new PackageResourceReference(application,
				ScriptaculousAjaxHandler.class, "controls.js"));
		addJsReference(container, new PackageResourceReference(application,
				ScriptaculousAjaxHandler.class, "dragdrop.js"));
		addJsReference(container, new PackageResourceReference(application,
				ScriptaculousAjaxHandler.class, "effects.js"));
		addJsReference(container, new PackageResourceReference(application,
				ScriptaculousAjaxHandler.class, "scriptaculous.js"));
		addJsReference(container, new PackageResourceReference(application,
				ScriptaculousAjaxHandler.class, "util.js"));
	}
}

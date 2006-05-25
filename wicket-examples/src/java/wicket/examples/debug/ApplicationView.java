/*
 * $Id: ApplicationView.java 3650 2006-01-04 23:26:43Z jonathanlocke $
 * $Revision: 3650 $ $Date: 2006-01-05 00:26:43 +0100 (do, 05 jan 2006) $
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
package wicket.examples.debug;

import wicket.Application;
import wicket.MarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.panel.Panel;

/**
 * A Wicket panel that shows interesting information about a given Wicket
 * session.
 * 
 * @author Jonathan Locke
 */
public final class ApplicationView extends Panel
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            Component id
	 * @param application
	 *            The application to view
	 */
	public ApplicationView(MarkupContainer parent, final String id, final Application application)
	{
		super(parent, id);

		// Basic attributes
		add(new Label(this, "name", application.getName()));
		add(new Label(this, "componentUseCheck", ""
				+ application.getDebugSettings().getComponentUseCheck()));
		add(new Label(this, "compressWhitespace", ""
				+ application.getMarkupSettings().getCompressWhitespace()));
		add(new Label(this, "defaultLocale", ""
				+ application.getApplicationSettings().getDefaultLocale()));
		add(new Label(this, "maxPageVersions", ""
				+ application.getPageSettings().getMaxPageVersions()));
		add(new Label(this, "stripComments", ""
				+ application.getMarkupSettings().getStripComments()));
		add(new Label(this, "stripWicketTags", ""
				+ application.getMarkupSettings().getStripWicketTags()));
		add(new Label(this, "bufferResponse", ""
				+ application.getRequestCycleSettings().getBufferResponse()));
		add(new Label(this, "resourcePollFrequency", ""
				+ application.getResourceSettings().getResourcePollFrequency()));
		add(new Label(this, "versionPages", ""
				+ application.getPageSettings().getVersionPagesByDefault()));
		add(new Label(this, "pageMapEvictionStrategy", ""
				+ application.getSessionSettings().getPageMapEvictionStrategy()));
	}
}

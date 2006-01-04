/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.markup.html.debug;

import wicket.Application;
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
	 * @param app
	 *            The application to view
	 */
	public ApplicationView(final String id, final Application app)
	{
		super(id);

		// Basic attributes
		add(new Label("name", app.getName()));

		add(new Label("componentUseCheck", "" + app.getDebugSettings().getComponentUseCheck()));
		add(new Label("compressWhitespace", "" + app.getMarkupSettings().getCompressWhitespace()));
		add(new Label("defaultLocale", "" + app.getResourceSettings().getDefaultLocale()));
		add(new Label("maxPageVersions", "" + app.getPageSettings().getMaxPageVersions()));
		add(new Label("stripComments", "" + app.getMarkupSettings().getStripComments()));
		add(new Label("stripWicketTags", "" + app.getMarkupSettings().getStripWicketTags()));
		add(new Label("bufferResponse", "" + app.getRequestCycleSettings().getBufferResponse()));
		add(new Label("resourcePollFrequency", ""
				+ app.getResourceSettings().getResourcePollFrequency()));
		add(new Label("versionPages", "" + app.getPageSettings().getVersionPagesByDefault()));
		add(new Label("pageMapEvictionStrategy", ""
				+ app.getSessionSettings().getPageMapEvictionStrategy()));
	}
}

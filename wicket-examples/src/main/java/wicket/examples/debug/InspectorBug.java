/*
 * $Id: InspectorBug.java 3676 2006-01-08 23:23:54Z jonathanlocke $ $Revision:
 * 3676 $ $Date: 2006-01-09 00:23:54 +0100 (ma, 09 jan 2006) $
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

import wicket.MarkupContainer;
import wicket.PageParameters;
import wicket.markup.html.WebPage;
import wicket.markup.html.image.Image;
import wicket.markup.html.link.BookmarkablePageLink;
import wicket.markup.html.link.Link;
import wicket.markup.html.panel.Panel;

/**
 * A page that shows interesting attributes of the Wicket environment, including
 * the current session and the component tree for the current page.
 * 
 * @author Jonathan Locke
 */
public final class InspectorBug extends Panel
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 *            Component id
	 * @param page
	 *            Page to inspect
	 */
	public InspectorBug(MarkupContainer parent, final String id, final WebPage page)
	{
		super(parent, id);
		PageParameters parameters = new PageParameters();
		parameters.put("pageId", page.getId());
		Link link = new BookmarkablePageLink(this, "link", InspectorPage.class, parameters);
		new Image(link, "bug");
	}
}

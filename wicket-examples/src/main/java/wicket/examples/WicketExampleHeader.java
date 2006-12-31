/*
 * $Id: WicketExampleHeader.java 5838 2006-05-24 20:44:49 +0000 (Wed, 24 May
 * 2006) joco01 $ $Revision$ $Date: 2006-05-24 20:44:49 +0000 (Wed, 24
 * May 2006) $
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
package wicket.examples;

import wicket.MarkupContainer;
import wicket.PageMap;
import wicket.examples.debug.InspectorBug;
import wicket.examples.source.SourcesPage;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.Link;
import wicket.markup.html.link.PopupSettings;
import wicket.markup.html.panel.Panel;

/**
 * Navigation panel for the examples project.
 * 
 * @author Eelco Hillenius
 */
public final class WicketExampleHeader extends Panel
{
	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 *            id of the component
	 * @param exampleTitle
	 *            title of the example
	 */
	public WicketExampleHeader(final MarkupContainer parent, final String id, final String exampleTitle)
	{
		super(parent, id);
		WebPage page = (WebPage)parent.getPage();
		new InspectorBug(this, "inspector", page);
		new Label(this, "exampleTitle", exampleTitle);
		Link link = new Link(this, "sources")
		{
			@Override
			public void onClick()
			{
				setResponsePage(new SourcesPage(getPage().getClass()));
			}
		};

		PopupSettings settings = new PopupSettings(PageMap.forName("sources"));
		settings.setWidth(800);
		settings.setHeight(600);
		settings.setWindowName("sources");
		link.setPopupSettings(settings);
	}
}

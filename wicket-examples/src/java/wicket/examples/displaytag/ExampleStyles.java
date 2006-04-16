/*
 * $Id$ $Revision:
 * 5214 $ $Date$
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
package wicket.examples.displaytag;

import java.util.List;

import wicket.AttributeModifier;
import wicket.PageParameters;
import wicket.examples.displaytag.utils.SimpleListView;
import wicket.examples.displaytag.utils.TestList;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.link.BookmarkablePageLink;
import wicket.model.Model;

/**
 * A single table with different styles
 * 
 * @author Juergen Donnerstag
 */
public class ExampleStyles extends Displaytag
{
	/**
	 * Constructor.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public ExampleStyles(final PageParameters parameters)
	{
		// my model object
		List data = new TestList(10, false);

		// Add Links to handle the different styles
		addStyleLink("isis");
		addStyleLink("its");
		addStyleLink("mars");
		addStyleLink("simple");
		addStyleLink("report");
		addStyleLink("mark");

		// Apply the style to the <table> tag
		WebMarkupContainer htmlTable = new WebMarkupContainer("htmlTable");
		add(htmlTable);
		htmlTable.add(new AttributeModifier("class", new Model(parameters.getString("class"))));

		// Add the rows to the list
		htmlTable.add(new SimpleListView("rows", data));
	}

	/**
	 * 
	 * @param id
	 */
	public void addStyleLink(final String id)
	{
		add(new BookmarkablePageLink(id, this.getClass()).setParameter("class", id).setAutoEnable(
				false));
	}
}
/*
 * $Id: IncrementalTableNavigationPage.java 5844 2006-05-24 20:53:56 +0000 (Wed,
 * 24 May 2006) joco01 $ $Revision$ $Date: 2006-05-24 20:53:56 +0000
 * (Wed, 24 May 2006) $
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
package wicket.markup.html.list;

import java.util.ArrayList;
import java.util.List;

import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.navigation.paging.PagingNavigationIncrementLink;


/**
 * Dummy page used for resource testing.
 */
public class IncrementalTableNavigationPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct. page parameters.
	 */
	public IncrementalTableNavigationPage()
	{
		super();
		List<String> list = new ArrayList<String>();
		list.add("one");
		list.add("two");
		list.add("three");
		list.add("four");
		list.add("five");
		list.add("six");
		list.add("seven");
		list.add("eight");

		PageableListView table = new PageableListView<String>(this, "table", list, 2)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem listItem)
			{
				String txt = (String)listItem.getModelObject();
				new Label(listItem, "txt", txt);
			}
		};

		new PagingNavigationIncrementLink(this, "prev", table, -1);
		new PagingNavigationIncrementLink(this,	"nextNext", table, +2);
	}
}

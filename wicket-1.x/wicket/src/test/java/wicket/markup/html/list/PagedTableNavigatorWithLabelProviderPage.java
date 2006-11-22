/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.markup.html.list;

import java.util.ArrayList;
import java.util.List;

import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.navigation.paging.IPagingLabelProvider;
import wicket.markup.html.navigation.paging.PagingNavigator;


/**
 * Dummy page used for resource testing.
 */
public class PagedTableNavigatorWithLabelProviderPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * 
	 *            page parameters.
	 */
	public PagedTableNavigatorWithLabelProviderPage()
	{
		super();
		List list = new ArrayList();
		list.add("one");
		list.add("two");
		list.add("three");
		list.add("four");
		list.add("five");
		list.add("six");
		list.add("seven");
		list.add("eight");
		list.add("nine");
		list.add("ten");
		list.add("eleven");
		list.add("twelve");
		list.add("thirteen");
		list.add("fourteen");
		
		final int pageSize = 2;
		final PageableListView listview = new PageableListView("table", list, pageSize)
		{
			private static final long serialVersionUID = 1L;
			
			protected void populateItem(ListItem listItem)
			{
				String txt = (String)listItem.getModelObject();
				listItem.add(new Label("txt", txt));
			}
		};

		IPagingLabelProvider labelProvider = new IPagingLabelProvider()
		{
		
			public String getPageLabel(int page)
			{
				int size = listview.getList().size();
				 int current = page*pageSize;
				 int end = current+pageSize;
				 if (end > size) 
				 {
					 end = size;
				 }
				 current++; // page start at 0.
				 return current + "-" + end;
			}
		};


		add(listview);
		add(new PagingNavigator("navigator", listview,labelProvider));
	}
	/**
	 * @see wicket.Component#isVersioned()
	 */
	public boolean isVersioned()
	{
		// for testing we set versioning off, because it gets too difficult to
		// maintain otherwise
		return false;
	}
}

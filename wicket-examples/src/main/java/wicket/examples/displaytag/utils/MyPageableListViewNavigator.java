/*
 * $Id: MyPageableListViewNavigator.java 5838 2006-05-24 20:44:49 +0000 (Wed, 24
 * May 2006) joco01 $ $Revision$ $Date: 2006-05-24 20:44:49 +0000 (Wed,
 * 24 May 2006) $
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
package wicket.examples.displaytag.utils;

import wicket.MarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.PageableListView;
import wicket.markup.html.navigation.paging.PagingNavigator;
import wicket.model.PropertyModel;
import wicket.util.string.AppendingStringBuffer;

/**
 * A customized navigation bar for the lists
 * 
 * @author Juergen Donnerstag
 */
public class MyPageableListViewNavigator extends PagingNavigator
{
	private final PageableListView pageableListView;

	/**
	 * 
	 * @param parent
	 * @param id
	 * @param pageableListView
	 */
	public MyPageableListViewNavigator(final MarkupContainer parent, final String id,
			final PageableListView pageableListView)
	{
		super(parent, id, pageableListView);

		this.pageableListView = pageableListView;
		new Label(this, "headline", new PropertyModel(this, "headlineText"));
	}

	/**
	 * Subclasses may override it to provide their own text.
	 * 
	 * @return head line text
	 */
	public CharSequence getHeadlineText()
	{
		int firstListItem = pageableListView.getCurrentPage() * pageableListView.getRowsPerPage();
		AppendingStringBuffer buf = new AppendingStringBuffer(80);
		buf.append(String.valueOf(pageableListView.getList().size())).append(
				" items found, displaying ").append(String.valueOf(firstListItem + 1)).append(
				" to ").append(String.valueOf(firstListItem + pageableListView.getRowsPerPage()))
				.append(".");

		return buf;
	}
}

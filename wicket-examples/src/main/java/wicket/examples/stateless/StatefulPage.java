/*
 * $Id: HelloWorld.java 5394 2006-04-16 13:36:52 +0000 (Sun, 16 Apr 2006)
 * jdonnerstag $ $Revision: 5394 $ $Date: 2006-04-16 13:36:52 +0000 (Sun, 16 Apr
 * 2006) $
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
package wicket.examples.stateless;

import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.BookmarkablePageLink;
import wicket.markup.html.link.Link;
import wicket.model.PropertyModel;

/**
 * Another page of the stateless example.
 * 
 * @author Eelco Hillenius
 */
public class StatefulPage extends WebPage
{
	/** click count for Link. */
	private int linkClickCount = 0;

	/**
	 * Construct.
	 */
	public StatefulPage()
	{
		new Label(this, "message", new SessionModel());
		new BookmarkablePageLink(this, "indexLink", Index.class);

		// Action link counts link clicks
		final Link actionLink = new Link(this, "actionLink")
		{
			@Override
			public void onClick()
			{
				linkClickCount++;
			}
		};
		new Label(actionLink, "linkClickCount", new PropertyModel(this, "linkClickCount"));
	}

	/**
	 * Gets linkClickCount.
	 * 
	 * @return linkClickCount
	 */
	public int getLinkClickCount()
	{
		return linkClickCount;
	}

	/**
	 * Sets linkClickCount.
	 * 
	 * @param linkClickCount
	 *            linkClickCount
	 */
	public void setLinkClickCount(int linkClickCount)
	{
		this.linkClickCount = linkClickCount;
	}
}
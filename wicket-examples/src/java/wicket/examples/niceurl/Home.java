/*
 * $Id$ $Revision$ $Date:
 * 2006-05-26 00:57:30 +0200 (vr, 26 mei 2006) $
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
package wicket.examples.niceurl;

import wicket.examples.WicketExamplePage;
import wicket.examples.niceurl.mounted.Page3;
import wicket.examples.niceurl.mounted.Page4;
import wicket.examples.niceurl.mounted.Page5;
import wicket.markup.html.link.BookmarkablePageLink;

/**
 * Has links to bookmarkable pages with 'nice' uls.
 * 
 * @author Eelco Hillenius
 */
public class Home extends WicketExamplePage
{
	/**
	 * Construct.
	 */
	public Home()
	{
		// references to single mounts
		new BookmarkablePageLink(this, "page1Link", Page1.class);
		new BookmarkablePageLink(this, "page2Link", Page2.class);
		new BookmarkablePageLink(this, "page2LinkQP", Page2QP.class);

		// references to package mounts
		new BookmarkablePageLink(this, "page3Link", Page3.class);
		new BookmarkablePageLink(this, "page4Link", Page4.class);
		new BookmarkablePageLink(this, "page5Link", Page5.class);
	}

	/**
	 * @see wicket.Component#isVersioned()
	 */
	@Override
	public boolean isVersioned()
	{
		return false;
	}
}

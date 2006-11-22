/*
 * $Id: LeftFrame.java 5394 2006-04-16 06:36:52 -0700 (Sun, 16 Apr 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-04-16 06:36:52 -0700 (Sun, 16 Apr
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
package wicket.examples.frames;

import wicket.PageMap;
import wicket.examples.linkomatic.Home;
import wicket.markup.html.WebPage;
import wicket.markup.html.link.BookmarkablePageLink;
import wicket.markup.html.link.Link;

/**
 * The left frame. This page is not bookmarkable, but it's instance is created
 * by {@link wicket.examples.frames.BodyFrame} and hold in the same page map as
 * index. It uses the frameTarget object as a shared model; this page updates
 * that model, and as the Index uses that to set the frame tag, any changes to
 * it should be reflected with the next render.
 * 
 * @author Eelco Hillenius
 */
public class LeftFrame extends WebPage
{
	/**
	 * Link that, when clicked, changes the frame target's frame class (and as
	 * that is a shared model which is also being used by the 'master page'
	 * {@link BodyFrame}, changes are immediately reflected) and set the
	 * response page to the top level page {@link BodyFrame}. Tags that use
	 * this link should have a <code>target="_parent"</code> attribute, so
	 * that the top frame will be refreshed.
	 */
	private static final class ChangeFramePageLink extends Link
	{
		private static final long serialVersionUID = 1L;

		/** parent frame class. */
		private final BodyFrame bodyFrame;

		/** this link's target. */
		private final Class pageClass;

		/**
		 * Construct.
		 * 
		 * @param id
		 * @param bodyFrame
		 * @param pageClass
		 */
		public ChangeFramePageLink(String id, BodyFrame bodyFrame, Class pageClass)
		{
			super(id);
			this.bodyFrame = bodyFrame;
			this.pageClass = pageClass;
		}

		/**
		 * @see wicket.markup.html.link.Link#onClick()
		 */
		public void onClick()
		{
			// change frame class
			bodyFrame.getFrameTarget().setFrameClass(pageClass);

			// trigger re-rendering of the page
			setResponsePage(bodyFrame);
		}
	}

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param index
	 *            parent frame class
	 */
	public LeftFrame(BodyFrame index)
	{
		add(new ChangeFramePageLink("linkToPage1", index, Page1.class));
		add(new ChangeFramePageLink("linkToPage2", index, Page2.class));
		BookmarkablePageLink link = new BookmarkablePageLink("directLink", Home.class);
		link.setPageMap(PageMap.forName(BodyFrame.RIGHT_FRAME_NAME));
		add(link);
	}

	/**
	 * No need for versioning this frame.
	 * 
	 * @see wicket.Component#isVersioned()
	 */
	public boolean isVersioned()
	{
		return false;
	}
}
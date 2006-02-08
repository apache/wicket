/*
 * $Id$ $Revision:
 * 1.5 $ $Date$
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

import wicket.markup.html.WebPage;
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
	private static final class ChangeFramePageLink extends Link
	{
		/** parent frame class. */
		private final BodyFrame index;

		/** this link's target. */
		private final String pageClass;

		/**
		 * Construct.
		 * 
		 * @param id
		 * @param index
		 * @param pageClass
		 */
		public ChangeFramePageLink(String id, BodyFrame index, String pageClass)
		{
			super(id);
			this.index = index;
			this.pageClass = pageClass;
		}

		/**
		 * @see wicket.markup.html.link.Link#onClick()
		 */
		public void onClick()
		{
			// change frame class
			index.getFrameTarget().setFrameClass(pageClass);

			// trigger re-rendering of the page
			setResponsePage(index);
		}
	}

	/**
	 * Constructor
	 * 
	 * @param index
	 *            parent frame class
	 */
	public LeftFrame(BodyFrame index)
	{
		add(new ChangeFramePageLink("linkToPage1", index, Page1.class.getName()));
		add(new ChangeFramePageLink("linkToPage2", index, Page2.class.getName()));
	}

	/**
	 * @see wicket.Component#isVersioned()
	 */
	public boolean isVersioned()
	{
		return false;
	}
}
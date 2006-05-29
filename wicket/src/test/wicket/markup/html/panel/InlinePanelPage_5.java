/*
 * $Id: InlinePanelPage_1.java 5227 2006-04-01 21:57:15Z jdonnerstag $
 * $Revision$ $Date: 2006-04-01 23:57:15 +0200 (Sa, 01 Apr 2006) $
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
package wicket.markup.html.panel;

import wicket.MarkupContainer;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebPage;


/**
 * 
 * @author Juergen Donnerstag
 */
public class InlinePanelPage_5 extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 */
	public InlinePanelPage_5()
	{
		new FragmentWithAssociatedMarkupStream(this, "myPanel1", "frag1");
		new FragmentWithAssociatedMarkupStream(this, "myPanel2", "frag2");
	}

	/**
	 * A special Fragment (WebMarkupContainer) which searches for the fragment
	 * in the markup file associated with the class.
	 */
	public static class FragmentWithAssociatedMarkupStream extends Fragment
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param parent
		 * @param id
		 * @param markupId
		 */
		public FragmentWithAssociatedMarkupStream(MarkupContainer parent, final String id,
				final String markupId)
		{
			super(parent, id, markupId);
		}

		@Override
		protected MarkupStream chooseMarkupStream(MarkupStream markupStream)
		{
			return getAssociatedMarkupStream(false);
		}
	}
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.markup.html.panel;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.html.WebPage;


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
		add(new FragmentWithAssociatedMarkupStream("myPanel1", "frag1"));
		add(new FragmentWithAssociatedMarkupStream("myPanel2", "frag2"));
	}

	/**
	 * A special Fragment (WebMarkupContainer) which searches for the fragment in the markup file
	 * associated with the class.
	 */
	public static class FragmentWithAssociatedMarkupStream extends Fragment
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param id
		 * @param markupId
		 */
        FragmentWithAssociatedMarkupStream(final String id, final String markupId)
		{
			super(id, markupId, null);
		}

		@Override
		protected IMarkupFragment chooseMarkup(final MarkupContainer provider)
		{
			return getAssociatedMarkup();
		}
	}
}

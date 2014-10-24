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
package org.apache.wicket.markup.html;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

/**
 * HTML5 helper
 * 
 * @author Juergen Donnerstag
 */
public class MarkupUtil
{
	/**
	 * 
	 * @param container
	 * @return True if the Page and all it's Panels, Borders etc. have HTML5 compliant markup. HTML5
	 *         markup is identified by &lt;DOCTYPE html&gt;
	 */
	public static boolean isMarkupHtml5Compliant(final MarkupContainer container)
	{
		Args.notNull(container, "container");

		Page page = container.getPage();
		if (page == null)
		{
			throw new WicketRuntimeException("Component not attached to Page. Component: " +
				container.toString());
		}


		final boolean rtn[] = new boolean[] { true };
		page.visitChildren(MarkupContainer.class, new IVisitor<MarkupContainer, Void>()
		{
			@Override
			public void component(final MarkupContainer comp, final IVisit<Void> visit)
			{
				IMarkupFragment associatedMarkup = comp.getAssociatedMarkup();
				if (associatedMarkup != null)
				{
					MarkupResourceStream rs = associatedMarkup.getMarkupResourceStream();
					if (rs.isHtml5() == false)
					{
						rtn[0] = false;
						visit.stop();
					}
				}
			}
		});

		return rtn[0];
	}
	
	/**
	 * Searches for {@code tagName} in the given {@code markup}.
	 * 
	 * @param markup
	 * @param tagName
	 * @return The {@link IMarkupFragment} corresponding to {@code tagName}. Null, if such {@code tagName} is not found
	 */
	public static IMarkupFragment findStartTag(final IMarkupFragment markup, final String tagName)
	{
		MarkupStream stream = new MarkupStream(markup);

		while (stream.skipUntil(WicketTag.class))
		{
			ComponentTag tag = stream.getTag();
			if (tag.isOpen() || tag.isOpenClose())
			{
				WicketTag wtag = (WicketTag)tag;
				if (tagName.equalsIgnoreCase(wtag.getName()))
				{
					return stream.getMarkupFragment();
				}

				stream.skipToMatchingCloseTag(tag);
			}

			stream.next();
		}

		return null;
	}
}

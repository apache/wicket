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
package wicket.markup.loader;

import java.io.IOException;

import wicket.Application;
import wicket.MarkupContainer;
import wicket.markup.MarkupElement;
import wicket.markup.MarkupFragment;
import wicket.markup.MarkupResourceStream;
import wicket.util.resource.ResourceStreamNotFoundException;

/**
 * On Pages with <wicket:head> and automatically added head tag, it
 * can happen that <wicket:head> tags are not inside the <head> tag.
 * Change it by moving <wicket:head> into the <head> tag
 * 
 * @author Juergen Donnerstag
 */
public class HeaderCleanupMarkupLoader extends AbstractMarkupLoader
{
	/** The Wicket application */
	private final Application application;

	/**
	 * Constructor.
	 * 
	 * @param application
	 */
	public HeaderCleanupMarkupLoader(final Application application)
	{
		this.application = application;
	}

	/**
	 * @see wicket.markup.loader.IMarkupLoader#loadMarkup(wicket.MarkupContainer,
	 *      wicket.markup.MarkupResourceStream)
	 */
	public final MarkupFragment loadMarkup(final MarkupContainer container,
			final MarkupResourceStream markupResourceStream) throws IOException,
			ResourceStreamNotFoundException
	{
		// Invoke the parent markup loader to read and parse the markup
		final MarkupFragment markup = super.loadMarkup(container, markupResourceStream);

		// On Pages with <wicket:head> and automatically added head tag, it
		// can happen that <wicket:head> tags are not inside the <head> tag.
		// Change it by moviing <wicket:head> into the <head> tag

		final MarkupFragment header = MarkupFragmentUtils.getHeadTag(markup);
		if (header != null)
		{
			markup.visitChildren(MarkupFragment.class, new MarkupFragment.IVisitor()
			{
				public Object visit(final MarkupElement element, final MarkupFragment parent)
				{
					if (element == header)
					{
						return CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER;
					}

					if (((MarkupFragment)element).getTag().isWicketHeadTag())
					{
						if (parent.removeMarkupElement(element) == true)
						{
							header.addMarkupElement(header.size() - 1, element);
						}
					}

					return CONTINUE_TRAVERSAL;
				}
			});
		}

		return markup;
	}
}

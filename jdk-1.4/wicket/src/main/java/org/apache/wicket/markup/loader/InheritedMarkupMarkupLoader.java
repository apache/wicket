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
package org.apache.wicket.markup.loader;

import java.io.IOException;

import org.apache.wicket.Application;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupNotFoundException;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.markup.MergedMarkup;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Merge the 2+ markups involved in markup inheritance. From a users perspective there is only one
 * markup associated with the component, the merged one.
 * 
 * @author Juergen Donnerstag
 */
public class InheritedMarkupMarkupLoader implements IMarkupLoader
{
	/** Log for reporting. */
	private static final Logger log = LoggerFactory.getLogger(InheritedMarkupMarkupLoader.class);

	/**
	 * Constructor.
	 */
	public InheritedMarkupMarkupLoader()
	{
	}

	/**
	 * 
	 * @see org.apache.wicket.markup.loader.IMarkupLoader#loadMarkup(org.apache.wicket.MarkupContainer,
	 *      org.apache.wicket.markup.MarkupResourceStream,
	 *      org.apache.wicket.markup.loader.IMarkupLoader, boolean)
	 */
	public final Markup loadMarkup(final MarkupContainer container,
			final MarkupResourceStream markupResourceStream, final IMarkupLoader baseLoader,
			final boolean enforceReload) throws IOException, ResourceStreamNotFoundException
	{
		// read and parse the markup
		Markup markup = baseLoader.loadMarkup(container, markupResourceStream, null, enforceReload);
		markup = checkForMarkupInheritance(container, markup, enforceReload);
		return markup;
	}

	/**
	 * The markup has just been loaded and now we check if markup inheritance applies, which is if
	 * <wicket:extend> is found in the markup. If yes, than load the base markups and merge the
	 * markup elements to create an updated (merged) list of markup elements.
	 * 
	 * @param container
	 *            The original requesting markup container
	 * @param markup
	 *            The markup to checked for inheritance
	 * @param enforceReload
	 *            The cache will be ignored and all, including inherited markup files, will be
	 *            reloaded. Whatever is in the cache, it will be ignored
	 * @return A markup object with the the base markup elements resolved.
	 * @TODO move into IMarkupLoader
	 */
	private Markup checkForMarkupInheritance(final MarkupContainer container, final Markup markup,
			final boolean enforceReload)
	{
		// Check if markup contains <wicket:extend> which tells us that
		// we need to read the inherited markup as well.
		int extendIndex = requiresBaseMarkup(markup);
		if (extendIndex == -1)
		{
			// return a MarkupStream for the markup
			return markup;
		}

		// get the base markup
		final Markup baseMarkup = Application.get().getMarkupSettings().getMarkupCache().getMarkup(
				container,
				markup.getMarkupResourceData().getResource().getMarkupClass().getSuperclass(),
				enforceReload);

		if (baseMarkup == Markup.NO_MARKUP)
		{
			throw new MarkupNotFoundException(
					"Base markup of inherited markup not found. Component class: " +
							markup.getMarkupResourceData().getResource().getContainerInfo()
									.getContainerClass().getName() +
							" Enable debug messages for org.apache.wicket.util.resource.Resource to get a list of all filenames tried.");
		}

		// Merge base and derived markup
		return new MergedMarkup(markup, baseMarkup, extendIndex);
	}

	/**
	 * Check if markup contains &lt;wicket:extend&gt; which tells us that we need to read the
	 * inherited markup as well. &lt;wicket:extend&gt; MUST BE the first wicket tag in the markup.
	 * Skip raw markup
	 * 
	 * @param markup
	 * @return == 0, if no wicket:extend was found
	 * @TODO move into IMarkupLoader
	 */
	private int requiresBaseMarkup(final Markup markup)
	{
		for (int i = 0; i < markup.size(); i++)
		{
			MarkupElement elem = markup.get(i);
			if (elem instanceof WicketTag)
			{
				WicketTag wtag = (WicketTag)elem;
				if (wtag.isExtendTag())
				{
					// Ok, inheritance is on and we must get the
					// inherited markup as well.
					return i;
				}
			}
		}
		return -1;
	}
}

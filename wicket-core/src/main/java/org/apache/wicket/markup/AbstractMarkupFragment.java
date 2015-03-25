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
package org.apache.wicket.markup;

import java.util.Deque;
import java.util.LinkedList;

import org.apache.wicket.util.lang.Args;

/**
 * A base implementation of {@link IMarkupFragment}.
 */
public abstract class AbstractMarkupFragment implements IMarkupFragment
{
	/**
	 * Find the markup fragment of component with id equal to {@code id}, starting at offset {@code streamOffset}.
	 * 
	 * @param id
	 *		The id of the component tag being searched for.
	 * @param streamOffset
	 *		The offset in the markup stream from which to start searching.
	 * @return the {@link IMarkupFragment} of the component tag if found, {@code null} is not found.
	 */
	protected final IMarkupFragment find(final String id, int streamOffset)
	{
		/*
		 * We need streamOffset because MarkupFragment starts searching from offset 1.
		 */
		Args.notEmpty(id, "id");
		Args.withinRange(0, size() - 1, streamOffset, "streamOffset");

		Deque<Boolean> openTagUsability = new LinkedList<>();
		boolean canFind = true;

		MarkupStream stream = new MarkupStream(this);
		stream.setCurrentIndex(streamOffset);
		while (stream.hasMore())
		{
			MarkupElement elem = stream.get();

			if (elem instanceof ComponentTag)
			{
				ComponentTag tag = stream.getTag();

				if (tag.isOpen() || tag.isOpenClose())
				{
					if (canFind && tag.getId().equals(id))
					{
						return stream.getMarkupFragment();
					}
					else if (tag.isOpen() && !tag.hasNoCloseTag())
					{
						openTagUsability.push(canFind);

						if (tag instanceof WicketTag)
						{
							WicketTag wtag = (WicketTag)tag;

							if (wtag.isExtendTag())
							{
								canFind = true;
							}
							else if (wtag.isFragmentTag() || wtag.isContainerTag())
							{
								canFind = false;
							}
							/*
							 * We should potentially also not try find child markup inside some other
							 * Wicket tags. Other tags that we should think about refusing to look for
							 * child markup inside include: container, body, border, child (we already
							 * have special extend handling).
							 */
						}
						else if (!"head".equals(tag.getName()) && !tag.isAutoComponentTag())
						{
							canFind = false;
						}
					}
				}
				else if (tag.isClose())
				{
					if (openTagUsability.isEmpty())
					{
						canFind = false;
					}
					else
					{
						canFind = openTagUsability.pop();
					}
				}
			}

			stream.next();
		}

		return null;
	}

	@Override
	public String toString()
	{
		return toString(false);
	}
}

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
package org.apache.wicket;

import java.util.Iterator;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.MarkupNotFoundException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.resolver.ComponentResolvers;

/**
 *
 */
class MarkupDrivenComponentTreeBuilder
{
	void rebuild(final MarkupContainer container)
	{
		IMarkupFragment markup = getMarkup(container);

		if (markup != null && markup.size() > 1)
		{
			MarkupStream stream = new MarkupStream(markup);

			// Skip the first component tag which already belongs to 'this' container
			if (stream.skipUntil(ComponentTag.class))
			{
				stream.next();
			}

			ComponentTag tag;
			while ((tag = stream.nextOpenTag()) != null)
			{
				if (!tag.isAutoComponentTag())
				{
					String componentId = tag.getId();

					if (tag instanceof WicketTag)
					{
						WicketTag wicketTag = (WicketTag) tag;
						if (wicketTag.isEnclosureTag())
						{
							Component component = ComponentResolvers.resolve(container, stream, tag, null);
							if ((component != null) && (component.getParent() == null))
							{
								if (component.getId().equals(tag.getId()) == false)
								{
									// make sure we are able to get() the component during rendering
									tag.setId(component.getId());
									tag.setModified(true);
								}
								container.add(component);
							}
						}
					}
					else
					{
						Component component = container.get(componentId);
						if (component == null)
						{
							try
							{
								component = findAutoAnnotatedComponent(container, componentId);

								if (component != null)
								{
									container.add(component);
								}

							} catch (Exception e)
							{
								throw new WicketRuntimeException(e);
							}
						}
//						print(tag);
					}
				}

				if (tag.isOpen() && tag.hasNoCloseTag() == false)
				{
					stream.skipToMatchingCloseTag(tag);
				}

				stream.next();
			}

		}
	}

	/**
	 * Find the markup of the container.
	 * If there is associated markup (Panel, Border) then it is preferred.
	 *
	 * @param container
	 *              The container which markup should be get
	 * @return the container's markup
	 */
	private IMarkupFragment getMarkup(MarkupContainer container)
	{
		IMarkupFragment markup = container.getAssociatedMarkup();
		if (markup == null)
		{
			try
			{
				markup = container.getMarkup();
			}
			catch (MarkupNotFoundException mnfx)
			{
				//
			}
		}
		return markup;
	}

	/**
	 * Searches for a member field that is a Component with the expected component id
	 *
	 * @param cursor
	 * @param componentId
	 * @return
	 * @throws IllegalAccessException
	 */
	private Component findAutoAnnotatedComponent(MarkupContainer cursor, String componentId) throws Exception
	{
		if (cursor == null)
		{
			return null;
		}

		if (cursor.queuedComponents != null)
		{
			Component queuedComponent = cursor.queuedComponents.remove(componentId);
			if (queuedComponent != null)
            {
                return queuedComponent;
            }
		}

		return findAutoAnnotatedComponent(cursor.getParent(), componentId);
	}

	private void print(ComponentTag tag)
	{
		System.err.println("tag: id=" + tag.getId() +
				"\n\t\topen=" + tag.isOpen() +
				"\n\t\tclose=" + tag.isClose() +
				"\n\t\topenclose=" + tag.isOpenClose() +
				"\n\t\tautoLinkEnabled=" + tag.isAutolinkEnabled() +
				"\n\t\tauto=" + tag.isAutoComponentTag());

	}
}

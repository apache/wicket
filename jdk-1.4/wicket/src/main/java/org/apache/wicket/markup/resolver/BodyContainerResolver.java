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
package org.apache.wicket.markup.resolver;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;

/**
 * The &lt;body&gt; tag has always a HtmlBodyContainer associated which is always added to the Page.
 * But it might be that a container has been added to &lt;html&gt; which means that when trying to
 * find the component associated with the BODY_ID, it can not be found.
 * <p>
 * Someone might want to attach a component to the html tag for example to change the xmlns:lang or
 * lang attribute.
 * 
 * @author Juergen Donnerstag
 */
public class BodyContainerResolver implements IComponentResolver
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see org.apache.wicket.markup.resolver.IComponentResolver#resolve(MarkupContainer,
	 *      MarkupStream, ComponentTag)
	 * 
	 * @param container
	 *            The container parsing its markup
	 * @param markupStream
	 *            The current markupStream
	 * @param tag
	 *            The current component tag while parsing the markup
	 * @return true, if componentId was handle by the resolver. False, otherwise
	 */
	public boolean resolve(final MarkupContainer container, final MarkupStream markupStream,
			final ComponentTag tag)
	{
		if (tag.isOpen() && tag.getName().equals("body") && (tag.getNamespace() == null))
		{
			String id = tag.getId();
			Component body = container.getPage().get(id);
			if (body != null)
			{
				body.render(markupStream);
				return true;
			}
		}

		// We were not able to handle the tag
		return false;
	}
}
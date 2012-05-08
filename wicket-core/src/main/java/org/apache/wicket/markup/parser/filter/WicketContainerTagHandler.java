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
package org.apache.wicket.markup.parser.filter;

import java.text.ParseException;

import org.apache.wicket.Application;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.parser.AbstractMarkupFilter;
import org.apache.wicket.util.value.IValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This markup filter warns if a wicket:container tag has an attribute besides wicket:id. This is
 * most likely a programmer mistake because the wicket:container tag won't be available in
 * deployment mode.
 *
 * The filter is only active in development mode and does nothing in deployment mode.
 *
 * @since 6.0
 */
public class WicketContainerTagHandler extends AbstractMarkupFilter
{
	private static final Logger log = LoggerFactory.getLogger(WicketContainerTagHandler.class);

	private final boolean usesDevelopmentConfig;

	public WicketContainerTagHandler(boolean usesDevelopmentConfig)
	{
		this.usesDevelopmentConfig = usesDevelopmentConfig;
	}

	@Override
	protected final MarkupElement onComponentTag(ComponentTag tag) throws ParseException
	{
		if (usesDevelopmentConfig)
		{
			if (tag instanceof WicketTag)
			{
				WicketTag wtag = (WicketTag)tag;
				if (tag.isOpen() && wtag.isContainerTag())
				{
					handleContainerTag(wtag);
				}
			}
		}

		return tag;
	}

	private void handleContainerTag(WicketTag containerTag)
	{
		IValueMap attributes = containerTag.getAttributes();
		for (String attribute : attributes.keySet())
		{
			if (ignoreAttribute(attribute))
			{
				continue;
			}

			reportAttribute(containerTag, attribute);
		}
	}

	private void reportAttribute(WicketTag containerTag, String attribute)
	{
		log.warn(
			"wicket:container with id '{}' has attribute '{}' in markup, which will be ignored in deployment mode",
			containerTag.getId(), attribute);
	}

	private boolean ignoreAttribute(String attribute)
	{
		return attribute.equalsIgnoreCase(getWicketNamespace() + ":id");
	}
}

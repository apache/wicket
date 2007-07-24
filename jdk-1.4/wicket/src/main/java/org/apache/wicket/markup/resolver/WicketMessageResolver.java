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
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.markup.parser.filter.WicketTagIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is a tag resolver which handles &lt;wicket:message
 * key="myKey"&gt;Default Text&lt;/wicket:message&gt;. The resolver
 * will replace the whole tag with the message found in the properties file
 * associated with the Page. If no message is found, the default body text will
 * remain.
 * 
 * @author Juergen Donnerstag
 */
public class WicketMessageResolver implements IComponentResolver
{
	private static final Logger log = LoggerFactory.getLogger(WicketMessageResolver.class);

	static
	{
		// register "wicket:message"
		WicketTagIdentifier.registerWellKnownTagName("message");
	}


	private static final long serialVersionUID = 1L;

	/**
	 * Try to resolve the tag, then create a component, add it to the container
	 * and render it.
	 * 
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
		if (tag instanceof WicketTag)
		{
			WicketTag wtag = (WicketTag)tag;
			if (wtag.isMessageTag())
			{
				String messageKey = wtag.getAttributes().getString("key");
				if ((messageKey == null) || (messageKey.trim().length() == 0))
				{
					throw new MarkupException(
							"Wrong format of <wicket:message key='xxx'>: attribute 'key' is missing");
				}

				// If the key can't be resolved and the default is null, an
				// exception will be thrown. Instead, we default to a unique
				// string and check against this later. Don't just use an empty
				// string here, as people might want to override wicket:messages
				// to empty strings.
				final String defaultValue = "DEFAULT_WICKET_MESSAGE_RESOLVER_VALUE";
				
				final String value = container.getApplication().getResourceSettings()
						.getLocalizer().getString(messageKey, container, defaultValue);

				final String id = "_message_" + container.getPage().getAutoIndex();
				Component component = null;
				if (value != null && !defaultValue.equals(value))
				{
					component = new MyLabel(id, value.trim());
				}
				else
				{
					log.info("No value found for message key: " + messageKey);
					component = new WebMarkupContainer(id);
				}

				component.setRenderBodyOnly(container.getApplication().getMarkupSettings()
						.getStripWicketTags());

				container.autoAdd(component, markupStream);

				// Yes, we handled the tag
				return true;
			}
		}

		// We were not able to handle the tag
		return false;
	}

	/**
	 * A Label with expands open-close tags to open-body-close if required
	 */
	public static class MyLabel extends Label
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param id
		 * @param value
		 */
		public MyLabel(final String id, final String value)
		{
			super(id, value);
			setEscapeModelStrings(false);
		}

		/**
		 * 
		 * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
		 */
		protected void onComponentTag(ComponentTag tag)
		{
			// Convert <wicket:message /> into
			// <wicket:message>...</wicket:message>
			if (tag.isOpenClose())
			{
				tag.setType(XmlTag.OPEN);
			}
			super.onComponentTag(tag);
		}
	}
}
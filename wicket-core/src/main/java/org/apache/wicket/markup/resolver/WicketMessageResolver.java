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

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.parser.XmlTag.TagType;
import org.apache.wicket.markup.parser.filter.WicketTagIdentifier;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Response;
import org.apache.wicket.response.StringResponse;
import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a tag resolver which handles &lt;wicket:message key="myKey"&gt;Default
 * Text&lt;/wicket:message&gt;. The resolver will replace the whole tag with the message found in
 * the properties file associated with the Page. If no message is found, the default body text will
 * remain.
 * <p>
 * You can also nest child components inside a wicket:message and then reference them from the
 * properties file. For example in the html
 * 
 * <pre>
 *     &lt;wicket:message key=&quot;myKey&quot;&gt;
 *        This text will be replaced with text from the properties file.
 *        &lt;span wicket:id=&quot;amount&quot;&gt;[amount]&lt;/span&gt;.
 *        &lt;a wicket:id=&quot;link&quot;&gt;
 *            &lt;wicket:message key=&quot;linkText&quot;/&gt;
 *        &lt;/a&gt;
 *     &lt;/wicket:message&gt;
 * </pre>
 * 
 * Then in the properties file have a variable with a name that matches the wicket:id for each child
 * component. The variables can be in any order, they do NOT have to match the order in the HTML
 * file.
 * 
 * <pre>
 *     myKey=Your balance is ${amount}. Click ${link} to view the details.
 *     linkText=here
 * </pre>
 * 
 * And in the java
 * 
 * <pre>
 * add(new Label(&quot;amount&quot;, new Model&lt;String&gt;(&quot;$5.00&quot;)));
 * add(new BookmarkablePageLink&lt;Void&gt;(&quot;link&quot;, DetailsPage.class));
 * </pre>
 * 
 * This will output
 * 
 * <pre>
 * Your balance is $5.00. Click &lt;a href=&quot;#&quot;&gt;here&lt;/a&gt; to view the details.
 * </pre>
 * 
 * If variables are not found via child component, the search will continue with the parents
 * container model object and if still not found with the parent container itself.
 * 
 * It is possible to switch between logging a warning and throwing an exception if either the
 * property key/value or any of the variables can not be found.
 * 
 * @author Juergen Donnerstag
 * @author John Ray
 */
public class WicketMessageResolver implements IComponentResolver
{
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(WicketMessageResolver.class);

	/** */
	public static final String MESSAGE = "message";

	static
	{
		// register "wicket:message"
		WicketTagIdentifier.registerWellKnownTagName(MESSAGE);
	}

	/**
	 * If the key can't be resolved and the default is null, an exception will be thrown. Instead,
	 * we default to a unique string and check against this later. Don't just use an empty string
	 * here, as people might want to override wicket:messages to empty strings.
	 */
	private static final String DEFAULT_VALUE = "DEFAULT_WICKET_MESSAGE_RESOLVER_VALUE";

	@Override
	public Component resolve(final MarkupContainer container, final MarkupStream markupStream,
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

				final String id = "_message_" + container.getPage().getAutoIndex();
				MessageContainer label = new MessageContainer(id, messageKey);
				label.setRenderBodyOnly(container.getApplication()
					.getMarkupSettings()
					.getStripWicketTags());

				return label;
			}
		}

		// We were not able to handle the tag
		return null;
	}

	/**
	 * If true, than throw an exception if a property key is not found. If false, just a warning is
	 * issued in the logged.
	 * 
	 * @return throwExceptionIfPropertyNotFound
	 */
	private static boolean isThrowExceptionIfPropertyNotFound()
	{
		return Application.get().getResourceSettings().getThrowExceptionOnMissingResource();
	}

	/**
	 * A Container which expands open-close tags to open-body-close if required. It gets a
	 * properties value and replaces variable such as ${myVar} with the rendered output of its child
	 * tags.
	 * 
	 */
	private static class MessageContainer extends MarkupContainer implements IComponentResolver
	{
		private static final long serialVersionUID = 1L;

		private static final String NOT_FOUND = "[Warning: Property for '%s' not found]";

		/**
		 * Construct.
		 * 
		 * @param id
		 * @param messageKey
		 */
		public MessageContainer(final String id, final String messageKey)
		{
			// The message key becomes the model
			super(id, new Model<String>(messageKey));

			setEscapeModelStrings(false);
		}

		@Override
		public Component resolve(MarkupContainer container, MarkupStream markupStream,
			ComponentTag tag)
		{
			return getParent().get(tag.getId());
		}

		@Override
		protected void onComponentTag(final ComponentTag tag)
		{
			// Convert <wicket:message /> into <wicket:message>...</wicket:message>
			if (tag.isOpenClose())
			{
				tag.setType(TagType.OPEN);
			}
			super.onComponentTag(tag);
		}

		@Override
		public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
		{
			// Get the value from the properties file
			final String key = getDefaultModelObjectAsString();
			final String value = getLocalizer().getString(key, getParent(), DEFAULT_VALUE);

			// if found, than render it after replacing the variables
			if ((value != null) && !DEFAULT_VALUE.equals(value))
			{
				renderMessage(markupStream, openTag, key, value);
			}
			else
			{
				if (isThrowExceptionIfPropertyNotFound() == true)
				{
					throw new WicketRuntimeException("Property '" + key +
						"' not found in property files. Markup: " + markupStream.toString());
				}

				log.warn("No value found for wicket:message tag with key: {}", key);

				String formatedNotFound = String.format(NOT_FOUND, key);
				// If open tag was open-close
				if (markupStream.hasMore() == false)
				{
					getResponse().write(formatedNotFound);
				}
				super.onComponentTagBody(markupStream, openTag);
			}
		}

		/**
		 * A property key has been found. Now render the property value.
		 * 
		 * @param markupStream
		 * @param openTag
		 * @param key
		 * @param value
		 */
		private void renderMessage(final MarkupStream markupStream, final ComponentTag openTag,
			final String key, final String value)
		{
			// Find all direct child tags, render them separately into a String, and remember them
			// in a hash map associated with the wicket id
			final Map<String, CharSequence> childTags = findAndRenderChildWicketTags(markupStream,
				openTag);

			final Map<String, Object> variablesReplaced = new HashMap<String, Object>();

			// Replace all ${var} within the property value with real values
			String text = new MapVariableInterpolator(value, childTags)
			{
				@Override
				protected String getValue(final String variableName)
				{
					// First check if a child tag with the same id exists.
					String value = super.getValue(variableName);

					// Remember that we successfully used the tag
					if (value != null)
					{
						variablesReplaced.put(variableName, null);
					}

					// If not, try to resolve the name with containers model data
					if (value == null)
					{
						value = Strings.toString(PropertyResolver.getValue(variableName,
							getParent().getDefaultModelObject()));
					}

					// If still not found, try the component itself
					if (value == null)
					{
						value = Strings.toString(PropertyResolver.getValue(variableName,
							getParent()));
					}

					// If still not found, don't know what to do
					if (value == null)
					{
						String msg = "The localized text for <wicket:message key=\"" + key +
							"\"> has a variable ${" + variableName +
							"}. However the wicket:message element does not have a child " +
							"element with a wicket:id=\"" + variableName + "\".";

						if (isThrowExceptionIfPropertyNotFound() == true)
						{
							markupStream.throwMarkupException(msg);
						}
						else
						{
							log.warn(msg);
							value = "### VARIABLE NOT FOUND: " + variableName + " ###";
						}
					}

					return value;
				}
			}.toString();

			getResponse().write(text);

			// Make sure all of the children were rendered
			for (String id : childTags.keySet())
			{
				if (variablesReplaced.containsKey(id) == false)
				{
					String msg = "The <wicket:message key=\"" + key +
						"\"> has a child element with wicket:id=\"" + id +
						"\". You must add the variable ${" + id +
						"} to the localized text for the wicket:message.";

					if (isThrowExceptionIfPropertyNotFound() == true)
					{
						markupStream.throwMarkupException(msg);
					}
					else
					{
						log.warn(msg);
					}
				}
			}
		}

		/**
		 * If the tag is of form <wicket:message>{foo}</wicket:message> then scan for any child
		 * wicket component and save their tag index
		 * 
		 * @param markupStream
		 * @param openTag
		 * @return map of child components
		 */
		private Map<String, CharSequence> findAndRenderChildWicketTags(
			final MarkupStream markupStream, final ComponentTag openTag)
		{
			Map<String, CharSequence> childTags = new HashMap<String, CharSequence>();

			// get original tag from markup because we modified openTag to always be open
			ComponentTag tag = markupStream.getPreviousTag();

			// if the tag is of form <wicket:message>{foo}</wicket:message> then scan for any
			// child component and save their tag index
			if (!tag.isOpenClose())
			{
				while (markupStream.hasMore() && !markupStream.get().closes(openTag))
				{
					MarkupElement element = markupStream.get();

					// If it a tag like <wicket..> or <span wicket:id="..." >
					if ((element instanceof ComponentTag) && !markupStream.atCloseTag())
					{
						ComponentTag currentTag = (ComponentTag)element;
						String id = currentTag.getId();

						// Temporarily replace the web response with a String response
						final Response webResponse = getResponse();

						try
						{
							final StringResponse response = new StringResponse();
							getRequestCycle().setResponse(response);

							Component component = getParent().get(id);
							if (component == null)
							{
								component = ComponentResolvers.resolve(getParent(), markupStream,
									currentTag, null);

								// Must not be a Page and it must be connected to a parent.
								if (component.getParent() == null)
								{
									component = null;
								}
							}

							if (component != null)
							{
								component.render();
								markupStream.skipComponent();
							}
							else
							{
								markupStream.next();
							}
							childTags.put(id, response.getBuffer());
						}
						finally
						{
							// Restore the original response
							getRequestCycle().setResponse(webResponse);
						}
					}
					else
					{
						markupStream.next();
					}
				}
			}

			return childTags;
		}
	}
}
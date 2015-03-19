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
package org.apache.wicket.markup.html.border;

import java.io.IOException;
import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.core.util.resource.locator.IResourceStreamLocator;
import org.apache.wicket.markup.ContainerInfo;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupFactory;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.MarkupType;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.request.Response;
import org.apache.wicket.util.resource.IResourceStream;

/**
 * This is a behavior implementation that can be used if you have markup that should be around a
 * component. It works just like {@link Border} so you have to have a <wicket:border>HTML
 * before<wicket:body/>HTML after</wicket:border> in the html of your subclass. But different than
 * Border you can not add components to the Border markup, only to the BorderBody.
 * 
 * @author jcompagner
 */
public class BorderBehavior extends Behavior
{
	private static final long serialVersionUID = 1L;

	// markup stream associated with this border. bonus of keeping a reference
	// is that when renderAfter starts the stream will be very close to its
	// needed position because renderBefore has executed
	private transient MarkupStream markupStream;

	@Override
	public void beforeRender(final Component component)
	{
		final MarkupStream stream = getMarkupStream(component);
		final Response response = component.getResponse();
		stream.setCurrentIndex(0);

		boolean insideBorderMarkup = false;
		while (stream.hasMore())
		{
			MarkupElement elem = stream.get();
			stream.next();
			if (elem instanceof WicketTag)
			{
				WicketTag wTag = (WicketTag)elem;
				if (!insideBorderMarkup)
				{
					if (wTag.isBorderTag() && wTag.isOpen())
					{
						insideBorderMarkup = true;
						continue;
					}
					else
					{
						throw new WicketRuntimeException(
							"Unexpected tag encountered in markup of component border " +
								getClass().getName() + ". Tag: " + wTag.toString() +
								", expected tag: <wicket:border>");
					}
				}
				else
				{
					if (wTag.isBodyTag())
					{
						break;
					}
					else
					{
						throw new WicketRuntimeException(
							"Unexpected tag encountered in markup of component border " +
								getClass().getName() + ". Tag: " + wTag.toString() +
								", expected tag: <wicket:body> or </wicket:body>");
					}
				}
			}
			if (insideBorderMarkup)
			{
				response.write(elem.toCharSequence());
			}
		}

		if (!stream.hasMore())
		{
			throw new WicketRuntimeException("Markup for component border " + getClass().getName() +
				" ended prematurely, was expecting </wicket:border>");
		}
	}

	@Override
	public void afterRender(final Component component)
	{
		final MarkupStream stream = getMarkupStream(component);
		final Response response = component.getResponse();

		while (stream.hasMore())
		{
			MarkupElement elem = stream.get();
			stream.next();
			if (elem instanceof WicketTag)
			{
				WicketTag wTag = (WicketTag)elem;
				if (wTag.isBorderTag() && wTag.isClose())
				{
					break;
				}
				else
				{
					throw new WicketRuntimeException(
						"Unexpected tag encountered in markup of component border " +
							getClass().getName() + ". Tag: " + wTag.toString() +
							", expected tag: </wicket:border>");
				}
			}
			response.write(elem.toCharSequence());
		}
	}

	/**
	 * 
	 * @param component
	 * @return markup stream
	 */
	private MarkupStream getMarkupStream(final Component component)
	{
		if (markupStream == null)
		{
			markupStream = findMarkupStream(component);
		}
		return markupStream;
	}

	/**
	 * 
	 * @param owner
	 * @return markup stream
	 */
	private MarkupStream findMarkupStream(final Component owner)
	{
		final MarkupType markupType = getMarkupType(owner);
		if (markupType == null)
		{
			return null;
		}

		// TODO we need to expose this functionality for any class not just for
		// markupcontainers in markupcache so we don't have to replicate this
		// logic here

		// Get locator to search for the resource
		final IResourceStreamLocator locator = Application.get()
			.getResourceSettings()
			.getResourceStreamLocator();

		final String style = owner.getStyle();
		final String variation = owner.getVariation();
		final Locale locale = owner.getLocale();

		MarkupResourceStream markupResourceStream = null;
		Class<?> containerClass = getClass();

		while (!(containerClass.equals(BorderBehavior.class)))
		{
			String path = containerClass.getName().replace('.', '/');
			IResourceStream resourceStream = locator.locate(containerClass, path, style, variation,
				locale, markupType.getExtension(), false);

			// Did we find it already?
			if (resourceStream != null)
			{
				ContainerInfo ci = new ContainerInfo(containerClass, locale, style, variation,
					markupType);
				markupResourceStream = new MarkupResourceStream(resourceStream, ci, containerClass);
				break;
			}

			// Walk up the class hierarchy one level, if markup has not
			// yet been found
			containerClass = containerClass.getSuperclass();
		}

		if (markupResourceStream == null)
		{
			throw new WicketRuntimeException("Could not find markup for component border `" +
				getClass().getName() + "`");
		}

		try
		{
			IMarkupFragment markup = MarkupFactory.get()
				.newMarkupParser(markupResourceStream)
				.parse();

			return new MarkupStream(markup);
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException(
				"Could not parse markup from markup resource stream: " +
					markupResourceStream.toString(), e);
		}
		finally
		{
			try
			{
				markupResourceStream.close();
			}
			catch (IOException e)
			{
				throw new WicketRuntimeException("Cannot close markup resource stream: " +
					markupResourceStream, e);
			}
		}
	}

	/**
	 * 
	 * @param component
	 * @return markup type
	 */
	private MarkupType getMarkupType(final Component component)
	{
		final MarkupType markupType;
		if (component instanceof MarkupContainer)
		{
			markupType = ((MarkupContainer)component).getMarkupType();
		}
		else
		{
			markupType = component.getParent().getMarkupType();
		}
		return markupType;
	}
}

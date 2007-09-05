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

import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.IComponentBorder;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ContainerInfo;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.parser.filter.WicketTagIdentifier;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.locator.IResourceStreamLocator;

/**
 * This is a IComponentBorder implementation that can be used if you have markup
 * that should be around a component. It works just like {@link Border} so you
 * have to have a <wicket:border>HTML before<wicket:body/>HTML after</wicket:border>
 * in the html of your subclass.
 * 
 * @author jcompagner
 */
public class MarkupComponentBorder implements IComponentBorder
{
	static
	{
		// register "wicket:border" and "wicket:body"
		WicketTagIdentifier.registerWellKnownTagName(Border.BORDER);
		WicketTagIdentifier.registerWellKnownTagName(Border.BODY);
	}

	private static final long serialVersionUID = 1L;

	// markup stream associated with this border. bonus of keeping a reference
	// is that when renderAfter starts the stream will be very close to its
	// needed position because renderBefore has executed
	private transient MarkupStream markupStream;

	/**
	 * 
	 * @see org.apache.wicket.IComponentBorder#renderBefore(org.apache.wicket.Component)
	 */
	public void renderBefore(Component component)
	{
		final MarkupStream stream = getMarkupStream(component);
		final Response response = component.getResponse();
		stream.setCurrentIndex(0);

		boolean insideBorderMarkup = false;
		while (stream.hasMore())
		{
			MarkupElement e = stream.next();
			if (e instanceof WicketTag)
			{
				WicketTag wt = (WicketTag)e;
				if (!insideBorderMarkup)
				{
					if (wt.isBorderTag() && wt.isOpen())
					{
						insideBorderMarkup = true;
						continue;
					}
					else
					{
						throw new WicketRuntimeException(
								"Unexpected tag encountered in markup of component border "
										+ getClass().getName() + ". Tag: " + wt.toString()
										+ ", expected tag: <wicket:border>");
					}
				}
				else
				{
					if (wt.isBodyTag())
					{
						break;
					}
					else
					{
						throw new WicketRuntimeException(
								"Unexpected tag encountered in markup of component border "
										+ getClass().getName() + ". Tag: " + wt.toString()
										+ ", expected tag: <wicket:body> or </wicket:body>");
					}
				}
			}
			if (insideBorderMarkup)
			{
				response.write(e.toCharSequence());
			}
		}

		if (!stream.hasMore())
		{
			throw new WicketRuntimeException("Markup for component border " + getClass().getName()
					+ " ended prematurely, was expecting </wicket:border>");
		}
	}

	/**
	 * 
	 * @see IComponentBorder#renderAfter(org.apache.wicket.Component) 
	 */
	public void renderAfter(Component component)
	{
		final MarkupStream stream = getMarkupStream(component);
		final Response response = component.getResponse();

		while (stream.hasMore())
		{
			MarkupElement e = stream.next();
			if (e instanceof WicketTag)
			{
				WicketTag wt = (WicketTag)e;
				if (wt.isBorderTag() && wt.isClose())
				{
					break;
				}
				else
				{
					throw new WicketRuntimeException(
							"Unexpected tag encountered in markup of component border "
									+ getClass().getName() + ". Tag: " + wt.toString()
									+ ", expected tag: </wicket:border>");
				}
			}
			response.write(e.toCharSequence());
		}
	}

	private MarkupStream getMarkupStream(Component component)
	{
		if (markupStream == null)
		{
			markupStream = findMarkupStream(component);
		}
		return markupStream;
	}

	private MarkupStream findMarkupStream(Component owner)
	{
		final String markupType = getMarkupType(owner);

		// TODO we need to expose this functionality for any class not just for
		// markupcontainers in markupcache so we dont have to replicate this
		// logic here

		// Get locator to search for the resource
		final IResourceStreamLocator locator = Application.get().getResourceSettings()
				.getResourceStreamLocator();


		final Session session = Session.get();
		final String style = session.getStyle();
		final Locale locale = session.getLocale();

		MarkupResourceStream markupResourceStream = null;
		Class containerClass = getClass();

		while (!(containerClass.equals(MarkupComponentBorder.class)))
		{
			String path = containerClass.getName().replace('.', '/');
			IResourceStream resourceStream = locator.locate(containerClass, path, style, locale,
					markupType);

			// Did we find it already?
			if (resourceStream != null)
			{
				ContainerInfo ci = new ContainerInfo(containerClass, locale, style, null,
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
			throw new WicketRuntimeException("Could not find markup for component border `"
					+ getClass().getName() + "`");
		}

		try
		{
			Markup markup = Application.get().getMarkupSettings().getMarkupParserFactory()
					.newMarkupParser(markupResourceStream).parse();
			return new MarkupStream(markup);
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException("Could not parse markup from markup resource stream: "
					+ markupResourceStream.toString());
		}
	}

	private String getMarkupType(Component component)
	{
		String extension;
		if (component instanceof MarkupContainer)
		{
			extension = ((MarkupContainer)component).getMarkupType();
		}
		else
		{
			extension = component.getParent().getMarkupType();
		}
		return extension;
	}
}

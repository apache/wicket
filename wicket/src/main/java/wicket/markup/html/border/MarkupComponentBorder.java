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
package wicket.markup.html.border;

import java.util.List;
import java.util.Locale;

import wicket.Application;
import wicket.Component;
import wicket.IComponentBorder;
import wicket.MarkupContainer;
import wicket.Response;
import wicket.Session;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.ContainerInfo;
import wicket.markup.MarkupElement;
import wicket.markup.MarkupFragment;
import wicket.markup.MarkupResourceStream;
import wicket.markup.parser.filter.WicketTagIdentifier;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.locator.IResourceStreamFactory;

/**
 * @TODO Comment
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

	/**
	 * 
	 * @see wicket.IComponentBorder#renderAfter(wicket.Component)
	 */
	public void renderAfter(Component<?> component)
	{
		final String extension;
		if (component instanceof MarkupContainer)
		{
			extension = ((MarkupContainer<?>)component).getMarkupType();
		}
		else
		{
			extension = component.getParent().getMarkupId();
		}
		MarkupFragment markupFragment = findMarkup(extension);
		MarkupFragment childFragment = markupFragment.getWicketFragment(Border.BORDER, true);
		List<MarkupElement> allElementsFlat = childFragment.getAllElementsFlat();
		Response response = component.getResponse();
		boolean render = false;
		for (MarkupElement markupElement : allElementsFlat)
		{
			if (markupElement instanceof ComponentTag)
			{
				ComponentTag tag = (ComponentTag)markupElement;
				if (tag.isWicketBodyTag())
				{
					render = true;
					continue;
				}
				else if (tag.isBorderTag())
				{
					continue;
				}
			}
			if (render)
			{
				response.write(markupElement.toCharSequence());
			}
		}
	}

	/**
	 * 
	 * @see wicket.IComponentBorder#renderBefore(wicket.Component)
	 */
	public void renderBefore(Component<?> component)
	{
		final String extension;
		if (component instanceof MarkupContainer)
		{
			extension = ((MarkupContainer<?>)component).getMarkupType();
		}
		else
		{
			extension = component.getParent().getMarkupId();
		}
		MarkupFragment markupFragment = findMarkup(extension);
		MarkupFragment childFragment = markupFragment.getWicketFragment(Border.BORDER, true);
		
		List<MarkupElement> allElementsFlat = childFragment.getAllElementsFlat();
		Response response = component.getResponse();
		
		for (MarkupElement markupElement : allElementsFlat)
		{
			if (markupElement instanceof ComponentTag)
			{
				ComponentTag ct = (ComponentTag)markupElement;
				if (ct.isWicketBodyTag())
				{
					break;
				}
				else if (ct.isBorderTag())
				{
					continue;
				}
			}
			response.write(markupElement.toCharSequence());
		}
	}

	/**
	 * 
	 * @param extension
	 * @return MarkupFragment
	 */
	@SuppressWarnings("unchecked")
	private MarkupFragment findMarkup(final String extension)
	{
		// Get locator to search for the resource
		final IResourceStreamFactory locator = Application.get().getResourceSettings()
				.getResourceStreamFactory();

		final Session session = Session.get();
		final String style = session.getStyle();
		final Locale locale = session.getLocale();

		MarkupResourceStream markupResourceStream = null;
		Class containerClass = getClass();

		while (containerClass != MarkupComponentBorder.class)
		{
			String path = containerClass.getName().replace('.', '/');
			IResourceStream resourceStream = locator.locate(containerClass, path, style, locale,
					extension);

			// Did we find it already?
			if (resourceStream != null)
			{
				ContainerInfo ci = new ContainerInfo(containerClass, locale, style, null,
						extension, null);
				markupResourceStream = new MarkupResourceStream(resourceStream, ci, containerClass);
			}

			// Walk up the class hierarchy one level, if markup has not
			// yet been found
			containerClass = containerClass.getSuperclass();
		}
		MarkupFragment markup;
		try
		{
			markup = Application.get().getMarkupSettings().getMarkupParserFactory()
					.newMarkupParser(markupResourceStream).readAndParse();
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException(e);
		}

		return markup;
	}
}

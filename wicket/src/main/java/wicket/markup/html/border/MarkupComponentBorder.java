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

import wicket.Application;
import wicket.Component;
import wicket.IComponentBorder;
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
import wicket.util.resource.locator.IResourceStreamLocator;

/**
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
	
	public void renderAfter(Component<?> component)
	{
		MarkupFragment mf = findMarkup();
		MarkupFragment child = mf.getWicketFragment(Border.BORDER, true);
		List<MarkupElement> allElementsFlat = child.getAllElementsFlat();
		Response response = component.getResponse();
		boolean render = false;
		for (MarkupElement markupElement : allElementsFlat)
		{
			if(markupElement instanceof ComponentTag)
			{
				ComponentTag ct = (ComponentTag)markupElement;
				if(ct.isWicketBodyTag())
				{
					render = true;
					continue;
				}
				else if(ct.isBorderTag())
				{
					continue;
				}
			}
			if(render) response.write(markupElement.toCharSequence());
		}
	}

	public void renderBefore(Component<?> component)
	{
		MarkupFragment mf = findMarkup();
		MarkupFragment child = mf.getWicketFragment(Border.BORDER, true);
		List<MarkupElement> allElementsFlat = child.getAllElementsFlat();
		Response response = component.getResponse();
		for (MarkupElement markupElement : allElementsFlat)
		{
			if(markupElement instanceof ComponentTag)
			{
				ComponentTag ct = (ComponentTag)markupElement;
				if(ct.isWicketBodyTag())
				{
					break;
				}
				else if(ct.isBorderTag())
				{
					continue;
				}
			}
			response.write(markupElement.toCharSequence());
		}
	}

	@SuppressWarnings("unchecked")
	private MarkupFragment findMarkup()
	{
		// Get locator to search for the resource
		final IResourceStreamLocator locator = Application.get().getResourceSettings()
				.getResourceStreamLocator();

		final Session session = Session.get();
		MarkupResourceStream markupResourceStream = null;
		Class containerClass = getClass();
		while (containerClass != MarkupComponentBorder.class)
		{
			final IResourceStream resourceStream = locator.locate(containerClass, containerClass
					.getName().replace('.', '/'), session.getStyle(), session.getLocale(), "html");

			// Did we find it already?
			if (resourceStream != null)
			{
				ContainerInfo ci = new ContainerInfo(containerClass,session.getLocale(),session.getStyle(),null,"html",null);
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

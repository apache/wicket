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
package org.apache.wicket.markup.html.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.PageHeaderItem;
import org.apache.wicket.markup.head.internal.HeaderResponse;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.renderStrategy.AbstractHeaderRenderStrategy;
import org.apache.wicket.request.Response;
import org.apache.wicket.response.StringResponse;


/**
 * The HtmlHeaderContainer is automatically created and added to the component hierarchy by a
 * HtmlHeaderResolver instance. HtmlHeaderContainer tries to handle/render the &lt;head&gt; tag and
 * its body. However depending on the parent component, the behavior must be different. E.g. if
 * parent component is a Page all components of the page's hierarchy must be asked if they have
 * something to contribute to the &lt;head&gt; section of the html response. If yes, it must
 * <b>immediately</b> be rendered.
 * <p>
 * &lt;head&gt; regions may contain additional wicket components, which can be added by means of
 * add(Component) as usual.
 * <p>
 * &lt;wicket:head&gt; tags are handled by simple {@link TransparentWebMarkupContainer}s also created by
 * {@link org.apache.wicket.markup.resolver.HtmlHeaderResolver}.
 * <p>
 * <ul>
 * <li>&lt;head&gt; will be inserted in output automatically if required</li>
 * <li>&lt;head&gt; is <b>not</b> a wicket specific tag and you must use add() to add components
 * referenced in body of the head tag</li>
 * <li>&lt;head&gt; is supported by panels, borders and inherited markup, but is <b>not</b> copied
 * to the output. They are for previewability only (except on Pages)</li>
 * <li>&lt;wicket:head&gt; does not make sense in page markup (but does in inherited page markup)</li>
 * <li>&lt;wicket:head&gt; makes sense in Panels, Borders and inherited markup (of Panels, Borders
 * and Pages)</li>
 * <li>components within &lt;wicket:head&gt; must be added by means of add(), like always with
 * Wicket. No difference.</li>
 * <li>&lt;wicket:head&gt; and it's content is copied to the output. Components contained in
 * &lt;wicket:head&gt; are rendered as usual</li>
 * </ul>
 * 
 * @author Juergen Donnerstag
 */
public class HtmlHeaderContainer extends TransparentWebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	/**
	 * wicket:head tags (components) must only be added once. To allow for a little bit more
	 * control, each wicket:head has an associated scope which by default is equal to the java class
	 * name directly associated with the markup which contains the wicket:head. It can be modified
	 * by means of the scope attribute.
	 */
	private transient Map<String, List<String>> renderedComponentsPerScope;

	/**
	 * Header response that is responsible for filtering duplicate contributions.
	 */
	private transient IHeaderResponse headerResponse = null;

	/**
	 * Combines the {@link MarkupStream} with the open tag, together representing the header section
	 * in the markup.
	 * 
	 * @author papegaaij
	 */
	public static class HeaderStreamState
	{
		private final MarkupStream markupStream;
		private final ComponentTag openTag;

		private HeaderStreamState(MarkupStream markupStream, ComponentTag openTag)
		{
			this.markupStream = markupStream;
			this.openTag = openTag;
		}

		/**
		 * @return the {@link MarkupStream}
		 */
		public MarkupStream getMarkupStream()
		{
			return markupStream;
		}

		/**
		 * @return the {@link ComponentTag} that represents the open tag
		 */
		public ComponentTag getOpenTag()
		{
			return openTag;
		}
	}

	/**
	 * Construct
	 * 
	 * @see Component#Component(String)
	 */
	public HtmlHeaderContainer(final String id)
	{
		super(id);

		// We will render the tags manually, because if no component asked to
		// contribute to the header, the tags will not be printed either.
		// No contribution usually only happens if none of the components
		// including the page does have a <head> or <wicket:head> tag.
		setRenderBodyOnly(true);

		setAuto(true);
	}

	/**
	 * First render the body of the component. And if it is the header component of a Page (compared
	 * to a Panel or Border), then get the header sections from all component in the hierarchy and
	 * render them as well.
	 */
	@Override
	public final void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
	{
		// We are able to automatically add <head> to the page if it is
		// missing. But we only want to add it, if we have content to be
		// written to its body. Thus we first write the output into a
		// StringResponse and if not empty, we copy it to the original
		// web response.

		// Temporarily replace the web response with a String response
		final Response webResponse = getResponse();

		try
		{
			// Create a (string) response for all headers contributed by any component on the Page.
			final StringResponse response = new StringResponse();
			getRequestCycle().setResponse(response);

			IHeaderResponse headerResponse = getHeaderResponse();
			if (!response.equals(headerResponse.getResponse()))
			{
				getRequestCycle().setResponse(headerResponse.getResponse());
			}

			// Render the header sections of all components on the page
			AbstractHeaderRenderStrategy.get().renderHeader(this,
				new HeaderStreamState(markupStream, openTag), getPage());

			// Close the header response before rendering the header container itself
			// See https://issues.apache.org/jira/browse/WICKET-3728
			headerResponse.close();

			// Cleanup extraneous CR and LF from the response
			CharSequence output = getCleanResponse(response);

			// Automatically add <head> if necessary
			if (output.length() > 0)
			{
				if (renderOpenAndCloseTags())
				{
					webResponse.write("<head>");
				}

				webResponse.write(output);

				if (renderOpenAndCloseTags())
				{
					webResponse.write("</head>");
				}
			}
		}
		finally
		{
			// Restore the original response
			getRequestCycle().setResponse(webResponse);
		}
	}

	/**
	 * Renders the content of the &lt;head&gt; section of the page, including &lt;wicket:head&gt;
	 * sections in subclasses of the page. For every child-component, the content is rendered to a
	 * string and passed to {@link IHeaderResponse}.
	 * 
	 * @param headerStreamState
	 */
	public void renderHeaderTagBody(HeaderStreamState headerStreamState)
	{
		if (headerStreamState == null)
			return;

		final Response oldResponse = getRequestCycle().getResponse();
		try
		{
			// Create a separate (string) response for the header container itself
			final StringResponse bodyResponse = new StringResponse();
			getRequestCycle().setResponse(bodyResponse);

			// render the header section directly associated with the markup
			super.onComponentTagBody(headerStreamState.getMarkupStream(),
				headerStreamState.getOpenTag());
			CharSequence bodyOutput = getCleanResponse(bodyResponse);
			if (bodyOutput.length() > 0)
			{
				getHeaderResponse().render(new PageHeaderItem(bodyOutput));
			}
		}
		finally
		{
			getRequestCycle().setResponse(oldResponse);
		}
	}

	/**
	 * 
	 * @param response
	 * @return Cleaned up response
	 */
	private CharSequence getCleanResponse(final StringResponse response)
	{
		CharSequence output = response.getBuffer();
		if (output.length() > 0)
		{
			if (output.charAt(0) == '\r')
			{
				for (int i = 2; i < output.length(); i += 2)
				{
					char ch = output.charAt(i);
					if (ch != '\r')
					{
						output = output.subSequence(i - 2, output.length());
						break;
					}
				}
			}
			else if (output.charAt(0) == '\n')
			{
				for (int i = 1; i < output.length(); i++)
				{
					char ch = output.charAt(i);
					if (ch != '\n')
					{
						output = output.subSequence(i - 1, output.length());
						break;
					}
				}
			}
		}
		return output;
	}

	/**
	 * 
	 * @return True if open and close tag are to be rendered.
	 */
	protected boolean renderOpenAndCloseTags()
	{
		return true;
	}

	/**
	 * Check if the header component is ok to render within the scope given.
	 * 
	 * @param scope
	 *            The scope of the header component
	 * @param id
	 *            The component's id
	 * @return true, if the component ok to render
	 */
	public boolean okToRenderComponent(final String scope, final String id)
	{
		if (renderedComponentsPerScope == null)
		{
			renderedComponentsPerScope = new HashMap<String, List<String>>();
		}

		List<String> componentScope = renderedComponentsPerScope.get(scope);
		if (componentScope == null)
		{
			componentScope = new ArrayList<String>();
			renderedComponentsPerScope.put(scope, componentScope);
		}

		if (componentScope.contains(id))
		{
			return false;
		}
		componentScope.add(id);
		return true;
	}

	@Override
	protected void onDetach()
	{
		super.onDetach();

		renderedComponentsPerScope = null;
		headerResponse = null;
	}

	/**
	 * Factory method for creating header response
	 * 
	 * @return new header response
	 */
	protected IHeaderResponse newHeaderResponse()
	{
		return new HeaderResponse()
		{
			@Override
			protected Response getRealResponse()
			{
				return HtmlHeaderContainer.this.getResponse();
			}
		};
	}

	/**
	 * Returns the header response.
	 * 
	 * @return header response
	 */
	public IHeaderResponse getHeaderResponse()
	{
		if (headerResponse == null)
		{
			headerResponse = getApplication().decorateHeaderResponse(newHeaderResponse());
		}
		return headerResponse;
	}

	@Override
	public IMarkupFragment getMarkup()
	{
		if (getParent() == null)
		{
			throw new WicketRuntimeException(
				"Bug: The Wicket internal instance of HtmlHeaderContainer is not connected to a parent");
		}

		// Get the page markup
		IMarkupFragment markup = getPage().getMarkup();
		if (markup == null)
		{
			throw new MarkupException("Unable to get page markup: " + getPage().toString());
		}

		// Find the markup fragment
		MarkupStream stream = new MarkupStream(markup);
		IMarkupFragment headerMarkup = null;
		while (stream.skipUntil(ComponentTag.class) && (headerMarkup == null))
		{
			ComponentTag tag = stream.getTag();
			if (tag.isOpen() || tag.isOpenClose())
			{
				if (tag instanceof WicketTag)
				{
					WicketTag wtag = (WicketTag)tag;
					if (wtag.isHeadTag() || wtag.isHeaderItemsTag())
					{
						headerMarkup = stream.getMarkupFragment();
						break;
					}
				}
				else if (tag.getName().equalsIgnoreCase("head") && tag.isAutoComponentTag())
				{
					headerMarkup = stream.getMarkupFragment();
					break;
				}
			}

			stream.next();
		}

		setMarkup(headerMarkup);
		return headerMarkup;
	}
}

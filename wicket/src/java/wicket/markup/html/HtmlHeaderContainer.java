/*
 * $Id$ $Revision$
 * $Date$
 *
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html;

import wicket.MarkupContainer;
import wicket.Response;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.WicketHeaderTag;
import wicket.response.StringResponse;

/**
 * The HtmlHeaderContainer is automatically created and added to the component
 * hierarchie by a HtmlHeaderResolver instance. HtmlHeaderContainer tries to
 * handle/render the &gt;wicket:head&gt; tag including its body. However
 * depending on the parent component, the behaviour must be different. E.g. if
 * parent component is a Page all components must be asked if they have
 * something to contribute to the &lt;head&gt; section of html response. If yes,
 * it must <b>now</b> be rendered. If parent component is not a Page,
 * HtmlHeaderContainer is no different than a WebMarkupContainer and will just
 * render its tag and its body.
 * <p>
 * Of course &lt;wicket:head&gt; regions may contain additional wicket
 * components, which must be added by means of addToHeader() instead of add() to
 * be handled properly.
 * 
 * @author Juergen Donnerstag
 */
public class HtmlHeaderContainer extends WebMarkupContainer
{
	/**
	 * Constructor used by HtmlHeaderResolver. The id is fix "_header"
	 * and the markup stream will be provided by the parent component.
	 */
	public HtmlHeaderContainer()
	{
		// There is only one HtmlHeaderContainer allowed. That is we
		// don't have to worry about creating a unique id.
		super("_header");

		// Skip <wicket:head> and render just the body
		setRenderBodyOnly(true);
	}

	/**
	 * Constructor used to add child component header sections. Because there
	 * can more than just one component contributing to the header section, 
	 * the id must be unique. And because the markup stream must the child
	 * components header section, it must be provided as well.
	 * 
	 * @param id
	 *            a unique component id
	 * @param associatedMarkupStream
	 *            the markup stream associated with the the &lt;wicket:head&gt;
	 *            tag of the child component
	 */
	public HtmlHeaderContainer(final String id, final MarkupStream associatedMarkupStream)
	{
		super(id);
		setMarkupStream(associatedMarkupStream);
	}

	/**
	 * First render the body of component. And if it is the header component of
	 * a Page (compared to a Panel or Border), than get the header sections from
	 * all component in the hierachie and render them as well.
	 * 
	 * @see wicket.MarkupContainer#onComponentTagBody(wicket.markup.MarkupStream,
	 *      wicket.markup.ComponentTag)
	 */
	protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
	{
	    final Response webResponse = this.getResponse();
	    final StringResponse response = new StringResponse();
	    this.getRequestCycle().setResponse(response);
	    
	    try
	    {
		    // In any case, first render the header section associated with the markup
			super.onComponentTagBody(markupStream, openTag);
	
			// If the parent component is a Page (or a bordered Page), we must
			// now include the header sections of all components in the component
			// hierarchie.
			MarkupContainer parent = getParent();
			if (parent instanceof IHeaderRenderer)
			{
				((IHeaderRenderer)parent).renderHeadSections(this);
			}
			
			final String output = response.toString();
			if (output.length() > 0)
			{
			    final boolean requiresHeadTag = (openTag instanceof WicketHeaderTag) && ((WicketHeaderTag)openTag).isRequiresHtmlHeadTag();
			    if (requiresHeadTag)
			    {
			        webResponse.write("<head>");
			    }
			    webResponse.write(output);
			    if (requiresHeadTag)
			    {
				    webResponse.write("</head>");
			    }
			}
	    }
	    finally
	    {
		    this.getRequestCycle().setResponse(webResponse);
	    }
	}
}

/*
 * $Id: HtmlHeaderContainer.java 5860 2006-05-25 20:29:28 +0000 (Thu, 25 May
 * 2006) eelco12 $ $Revision$ $Date: 2006-05-25 20:29:28 +0000 (Thu, 25
 * May 2006) $
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
package wicket.markup.html.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.Response;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.IHeaderResponse;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.WebPage;
import wicket.markup.html.border.Border;
import wicket.response.StringResponse;

/**
 * The HtmlHeaderContainer is automatically created and added to the component
 * hierarchy by a HtmlHeaderResolver instance. HtmlHeaderContainer tries to
 * handle/render the &gt;head&gt; tag and its body. However depending on the
 * parent component, the behavior must be different. E.g. if parent component is
 * a Page all components of the page's hierarchy must be asked if they have
 * something to contribute to the &lt;head&gt; section of the html response. If
 * yes, it must <b>immediately</b> be rendered.
 * <p>
 * &lt;head&gt; regions may contain additional wicket components, which can be
 * added by means of add(Component) as usual.
 * <p>
 * &lt;wicket:head&gt; tags are handled by simple WebMarkupContainers also
 * created by a HtmlHeaderResolver.
 * <p>
 * <ul>
 * <li> &lt;head&gt; will be inserted in output automatically if required</li>
 * <li> &lt;head&gt; is <b>not</b> a wicket specific tag and you must use add()
 * to add components referenced in body of the head tag</li>
 * <li> &lt;head&gt; is supported by panels, borders and inherited markup, but
 * is <b>not</b> copied to the output. They are for previewability only (except
 * on Pages)</li>
 * <li> &lt;wicket:head&gt; does not make sense in page markup (but does in
 * inherited page markup)</li>
 * <li> &lt;wicket:head&gt; makes sense in Panels, Borders and inherited markup
 * (of Panels, Borders and Pages)</li>
 * <li> components within &lt;wicket:head&gt; must be added by means of add(),
 * like allways with Wicket. No difference.</li>
 * <li> &lt;wicket:head&gt; and it's content is copied to the output. Components
 * contained in &lt;wicket.head&gt; are rendered as usual</li>
 * </ul>
 * 
 * @author Juergen Donnerstag
 */
public class HtmlHeaderContainer extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	/**
	 * wicket:head tags (components) must only be added once. To allow for a
	 * little bit more control, each wicket:head has an associated scope which
	 * by default is equal to the java class name directly associated with the
	 * markup which contains the wicket:head. It can be modified by means of the
	 * scope attribute.
	 */
	private Map<String, List<String>> renderedComponentsPerScope;
	
	/**
	 * Header response that is responsible for filtering duplicate contributions.
	 */
	private IHeaderResponse headerResponse = null;	

	/**
	 * Construct
	 * 
	 * @see Component#Component(MarkupContainer,String)
	 */
	public HtmlHeaderContainer(MarkupContainer parent, final String id)
	{
		super(parent, id);

		// We will render the tags manually, because if no component asked to
		// contribute to the header, the tags will not be printed either.
		// No contribution usually only happens if none of the components
		// including the page does have a <head> or <wicket:head> tag.
		setRenderBodyOnly(true);
	}

	/**
	 * First render the body of the component. And if it is the header component
	 * of a Page (compared to a Panel or Border), than get the header sections
	 * from all component in the hierachie and render them as well.
	 * 
	 * @see wicket.MarkupContainer#onComponentTagBody(wicket.markup.MarkupStream,
	 *      wicket.markup.ComponentTag)
	 */
	@Override
	protected final void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
	{
		// We are able to automatically add <head> to the page if it is
		// missing. But we only want to add it, if we have content to be
		// written to its body. Thus we first write the output into a
		// StringResponse and if not empty, we copy it to the original
		// web response.

		// Temporarily replace the web response with a String response
		final Response webResponse = this.getResponse();

		try
		{
			final StringResponse response = new StringResponse();
			this.getRequestCycle().setResponse(response);
			
			// In any case, first render the header section directly associated
			// with the markup
			super.onComponentTagBody(markupStream, openTag);

			// If the parent component is a Page (or a bordered Page), we must
			// now include the header sections of all components in the
			// component hierarchie.
			MarkupContainer parent = getParent();

			// If bordered page ...
			while ((parent instanceof Border))
			{
				parent = parent.getParent();
			}

			// must be a Page
			if (parent instanceof WebPage)
			{
				renderHeaderSections((WebPage)parent, this);
			}
			else
			{
				throw new WicketRuntimeException(
						"Programming error: 'parent' should be a Page or a Border.");
			}

			// Automatically add <head> if necessary
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

			if (output.length() > 0)
			{
				webResponse.write("<head>");
				webResponse.write(output);
				webResponse.write("</head>");
			}						
		}
		finally
		{
			// Restore the original response
			this.getRequestCycle().setResponse(webResponse);
		}
	}

	/**
	 * Ask all child components of the Page if they have something to contribute
	 * to the &lt;head&gt; section of the HTML output. Every component
	 * interested must implement IHeaderContributor.
	 * <p>
	 * Note: HtmlHeaderContainer will be removed from the component hierachie at
	 * the end of the request (@see #onEndRequest()) and thus can not transport
	 * status from one request to the next. This is true for all components
	 * added to the header.
	 * 
	 * @param page
	 *            The page object
	 * @param container
	 *            The header component container
	 */
	private final void renderHeaderSections(final WebPage page, final HtmlHeaderContainer container)
	{
		// Make sure all Components interested in contributing to the header
		// and there attached behaviors are asked.
		page.visitChildren(new IVisitor()
		{
			/**
			 * @see wicket.Component.IVisitor#component(wicket.Component)
			 */
			public Object component(Component component)
			{
				if (component.isVisible())
				{
					component.renderHead(container);
					return IVisitor.CONTINUE_TRAVERSAL;
				}
				else
				{
					return IVisitor.CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER;
				}
			}
		});

		page.renderHead(container);
	}

	/**
	 * @see wicket.MarkupContainer#isTransparentResolver()
	 */
	@Override
	public boolean isTransparentResolver()
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
	public final boolean okToRenderComponent(final String scope, final String id)
	{
		if (this.renderedComponentsPerScope == null)
		{
			this.renderedComponentsPerScope = new HashMap<String, List<String>>();
		}

		// if (scope == null)
		// {
		// scope = header.getMarkupStream().getContainerClass().getName();
		// }

		List<String> componentScope = this.renderedComponentsPerScope.get(scope);
		if (componentScope == null)
		{
			componentScope = new ArrayList<String>();
			this.renderedComponentsPerScope.put(scope, componentScope);
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

		this.renderedComponentsPerScope = null;
		this.headerResponse = null;
	}
	
	/**
	 * Returns the header response. 
	 * 
	 * @return header response
	 */
	public IHeaderResponse getHeaderResponse() {
		if (this.headerResponse == null)
		{
			headerResponse = new HeaderResponse(getResponse());
		}
		return headerResponse;
	}
	
}

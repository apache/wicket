/*
 * $Id$
 * $Revision$ $Date$
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

import wicket.Component;
import wicket.IComponentResolver;
import wicket.MarkupContainer;
import wicket.Response;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.response.StringResponse;

/**
 * The HtmlHeaderContainer is automatically created and added to the component
 * hierarchie by a HtmlHeaderResolver instance. HtmlHeaderContainer tries to
 * handle/render the &gt;head&gt; tag and its body. However depending on the
 * parent component, the behaviour must be different. E.g. if parent component
 * is a Page all components of the page's hierarchy must be asked if they have
 * something to contribute to the &lt;head&gt; section of the html response. If
 * yes, it must <b>immediately </b> be rendered.
 * <p>
 * Of course &lt;head&gt; regions may contain additional wicket components,
 * which must be added by means of addToHeader() instead of add() to be handled
 * properly.
 * <p>
 * &gt;wicket:head&gt; tags are handled by simple WebMarkupContainers also
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
 * <li> &lt;wicket:head&gt; and it's content is copied into the output.
 * Component contained in &lt;wicket.head&gt; are rendered as usual</li>
 * </ul>
 * 
 * @author Juergen Donnerstag
 */
public class HtmlHeaderContainer extends WebMarkupContainer implements IComponentResolver
{
	private static final long serialVersionUID = 1L;
	
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
	}

	/**
	 * First render the body of the component. And if it is the header component
	 * of a Page (compared to a Panel or Border), than get the header sections
	 * from all component in the hierachie and render them as well.
	 * 
	 * @see wicket.MarkupContainer#onComponentTagBody(wicket.markup.MarkupStream,
	 *      wicket.markup.ComponentTag)
	 */
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

			// Usually only Page and Border implement IHeaderRenderer. Border
			// does in order to support bordered pages.
			if (parent instanceof IHeaderRenderer)
			{
				((IHeaderRenderer)parent).renderHeaderSections(this);
			}
			else
			{
				throw new WicketRuntimeException(
						"Programming error: 'parent' should be a Page or a Border implementing IHeaderRenderer");
			}

			// Automatically add <head> if necessary
			String output = response.toString();
			if (output.length() > 0)
			{
				if (output.charAt(0) == '\r')
				{
					for (int i = 2; i < output.length(); i += 2)
					{
						char ch = output.charAt(i);
						if (ch != '\r')
						{
							output = output.substring(i - 2);
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
							output = output.substring(i - 1);
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
	 * HtmlHeaderContainer has been autoAdded, it has been injected similiar to
	 * an AOP interceptor. Thus it must forward any request to find a component
	 * based on an ID to its parent container.
	 * 
	 * @see wicket.IComponentResolver#resolve(wicket.MarkupContainer,
	 *      wicket.markup.MarkupStream, wicket.markup.ComponentTag)
	 */
	public boolean resolve(MarkupContainer container, MarkupStream markupStream, ComponentTag tag)
	{
		// Try to find the component with the parent component.
		MarkupContainer parent = getParent();
		if (parent != null)
		{
			if (parent.getId().equals(tag.getId()))
			{
				parent.render();
				return true;
			}

			Component component = parent.get(tag.getId());
			if (component != null)
			{
				component.render();
				return true;
			}

			if (parent instanceof IComponentResolver)
			{
				return ((IComponentResolver)parent).resolve(container, markupStream, tag);
			}
		}

		return false;
	}
}

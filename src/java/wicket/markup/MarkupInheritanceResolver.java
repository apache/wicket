/*
 * $Id: WicketTagComponentResolver.java,v 1.4 2005/01/18 08:04:29 jonathanlocke
 * Exp $ $Revision$ $Date$
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
package wicket.markup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.MarkupContainer;

/**
 * THIS CLASS IS CURRENTLY EXPERIMENTAL ONLY.
 * 
 * Detect &lt;wicket:extend&gt; regions and thus allows to implement
 * markup inheritance
 * 
 * @author Juergen Donnerstag
 */
public class MarkupInheritanceResolver implements IComponentResolver
{
    /** Logging */
    private static Log log = LogFactory.getLog(MarkupInheritanceResolver.class);

    /**
     * @see wicket.markup.IComponentResolver#resolve(MarkupContainer, MarkupStream,
     *      ComponentTag)
     * @param container
     *            The container parsing its markup
     * @param markupStream
     *            The current markupStream
     * @param tag
     *            The current component tag while parsing the markup
     * @return true, if componentId was handle by the resolver. False,
     *         otherwise
     */
    public boolean resolve(final MarkupContainer container, final MarkupStream markupStream,
            final ComponentTag tag)
    {
        // It must be <wicket:...>
        if (tag instanceof ComponentWicketTag)
        {
            // It must be <wicket:component...>
            final ComponentWicketTag wicketTag = (ComponentWicketTag)tag;
            if (wicketTag.isExtendTag())
            {
                markupStream.next();
                
                // weave in the parent's java classes markup
                //resolveComponent(markupStream, wicketTag);
                
                // now go on with 
                return true;
            }
        }

        // We were not able to handle the componentId
        return false;
    }

	/**
	 * Renders the entire associated markup stream for a container such as a
	 * Border or Panel. Any leading or trailing raw markup in the associated
	 * markup is skipped.
	 * 
	 * @param openTagName
	 *			  the tag to render the associated markup for
	 * @param exceptionMessage
	 *			  message that will be used for exceptions
	 */
/*    
	protected final void renderAssociatedMarkup(final String openTagName,
			final String exceptionMessage)
	{
		// Get markup associated with Border or Panel component
		final MarkupStream originalMarkupStream = getMarkupStream();
		final MarkupStream associatedMarkupStream = getAssociatedMarkupStream();

		associatedMarkupStream.skipRawMarkup();
		setMarkupStream(associatedMarkupStream);

		// Get open tag in associated markup of border component
		final ComponentTag associatedMarkupOpenTag = associatedMarkupStream.getTag();

		// Check for required open tag name
		if (!(associatedMarkupStream.atOpenTag(openTagName) && (associatedMarkupOpenTag instanceof ComponentWicketTag)))
		{
			associatedMarkupStream.throwMarkupException(exceptionMessage);
		}

		renderComponentTag(associatedMarkupOpenTag);
		associatedMarkupStream.next();
		renderComponentTagBody(associatedMarkupStream, associatedMarkupOpenTag);
		renderClosingComponentTag(associatedMarkupStream, associatedMarkupOpenTag);
		setMarkupStream(originalMarkupStream);
	}
*/
	/**
	 *
	 * @param markupStream
	 *            The current markup stream
	 * @param tag
	 *            The current component tag
	 * @return True, if MarkupContainer was able to resolve the component name and to
	 *         render the component
	 */
/*    
	protected boolean resolveComponent(final MarkupStream markupStream, final ComponentTag tag)
	{
		// Determine if tag is a <wicket:body> tag
		final boolean isBodyTag = (tag instanceof ComponentWicketTag && markupStream.atOpenCloseTag("body"));

		// If we're being asked to resolve a component for a <wicket:body> tag
		if (!isBodyTag)
        {
            return false;
        }
        else
		{
			// Check that it's <wicket:body/> not <wicket:body>
			if (!markupStream.atOpenCloseTag())
			{
				markupStream.throwMarkupException("A <wicket:body> tag must be an open-close tag.");
			}

			// Render the body tag
			renderComponentTag(tag);
			markupStream.next();

			// Find nearest Border at or above this container
			Border border = (Border)((this instanceof Border) ? this : findParent(Border.class));

			// If markup stream is null, that indicates we already recursed into
			// this block of log and set it to null (below). If we did that,
			// then we want to go up another level of border nesting.
			if (border.getMarkupStream() == null)
			{
				// Find Border at or above parent of this border
				final MarkupContainer borderParent = border.getParent();
				border = (Border)((borderParent instanceof Border) ? borderParent : borderParent
						.findParent(Border.class));
			}

			// Get the border's markup
			final MarkupStream borderMarkup = border.findMarkupStream();

			// Set markup of border to null. This allows us to find the border's
			// parent's markup. It also indicates that we've been here in the
			// log just above.
			border.setMarkupStream(null);

			// Draw the children of the border component using its original
			// in-line markup stream (not the border's associated markup stream)
			border.renderComponentTagBody(border.findMarkupStream(), border.openTag);

			// Restore border markup so it can continue rendering
			border.setMarkupStream(borderMarkup);
			return true;
		}
	}
	*/
}
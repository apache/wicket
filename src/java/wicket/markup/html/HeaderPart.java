/*
 * $Id$
 * $Revision$
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


/**
 * Base class for header parts.
 *
 * @author Eelco Hillenius
 */
public class HeaderPart extends WebMarkupContainer
{
	/** the contributing component. */
	final WebMarkupContainer contributor;

	/** the contribution number. */
	private final int contribution;

	/**
	 * Construct.
	 * @param contributor the contributing component
	 * @param contribution the contribution number
	 */
	public HeaderPart(WebMarkupContainer contributor, int contribution)
	{
		super(Integer.toString(contribution));
		if(contributor == null)
		{
			throw new NullPointerException("argument contributor must be provided");
		}
		this.contributor = contributor;
		this.contribution = contribution;
	}

    /**
     * Renders this component.
     */
    protected final void onRender()
    {
        // Render the associated markup; if we don't find it, return silently
        contributor.tryRenderAssociatedMarkup("header");
    }
}

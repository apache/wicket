/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.markup.parser.filter;

import java.text.ParseException;
import java.util.Stack;

import wicket.markup.ComponentTag;
import wicket.markup.ComponentWicketTag;
import wicket.markup.MarkupElement;
import wicket.markup.parser.IMarkupFilter;

/**
 * This is a markup inline filter. It identifies xml tags which include a href
 * attribute and which are not Wicket specific components and flags these tags
 * (ComponentTag) as autolink enabled. A component resolver will later only
 * resolve the href and assign a BookmarkablePageLink to it (automatically).
 * <p>
 * An application settings is used as default value, which might be modified for
 * specific regions. These regions are identified by &lt;wicket:link&gt; tags
 * with an optional 'autolink' attribute. The default value for the attribute is
 * true, thus enabling autolinking. An open-close &lt;wicket:link/&gt tag will
 * change the autolink status until the end of the markup document or the next
 * &lt;wicket:link&gt; tag respectively. &lt;wicket:link&gt; regions may be
 * nested as well.
 * 
 * @author Juergen Donnerstag
 */
public class AutolinkHandler implements IMarkupFilter
{
	/** The next MarkupFilter in the chain */
	private final IMarkupFilter parent;

	/** Allow to have link regions within link regions */
	private Stack autolinkStatus;

	/** Current status */
	private boolean autolinking = true;

	/**
	 * Construct.
	 * 
	 * @param nextInChain
	 *           The next element in the chain.
	 */
	public AutolinkHandler(final IMarkupFilter nextInChain)
	{
		parent = nextInChain;
	}

	/**
	 * @return The next MarkupFilter in the chain
	 */
	public final IMarkupFilter getParent()
	{
		return parent;
	}

	/**
	 * Set the default value for autolinking
	 * 
	 * @param enable
	 *           if true, autolinks are enabled
	 */
	public void setAutomaticLinking(final boolean enable)
	{
		this.autolinking = enable;
	}

	/**
	 * Get the next MarkupElement from the parent MarkupFilter and handles it if
	 * the specific filter criteria are met. Depending on the filter, it may
	 * return the MarkupElement unchanged, modified or it remove by asking the
	 * parent handler for the next tag.
	 * 
	 * @see wicket.util.xml.IMarkupFilter#nextTag()
	 * @return Return the next eligible MarkupElement
	 */
	public MarkupElement nextTag() throws ParseException
	{
		// Get next tag. Null, if no more tag available
		final ComponentTag tag = (ComponentTag)parent.nextTag();
		if (tag == null)
		{
			return tag;
		}

		// Only xml tags not already identified as Wicket components will be
		// considered
		// for autolinking. This is because it is assumed that Wicket components
		// like images, or all other kind of Wicket Links will handle it
		// themselves.
		final String href = tag.getAttributes().getString("href");
		if ((autolinking == true) && (tag.getComponentName() == null) && (href != null)
				&& (href.endsWith(".html") || (href.indexOf(".html?") != -1)) && !href.startsWith("/")
				&& (href.indexOf(":") == -1))
		{
			// Mark it as autolink enabled
			tag.enableAutolink(true);

			// Just a dummy name. The ComponentTag will not be forwarded.
			tag.setComponentName("_autolink_");
			return tag;
		}

		// For all <wicket:link ..> tags which probably change the
		// current autolink status.
		if (tag instanceof ComponentWicketTag)
		{
			final ComponentWicketTag wtag = (ComponentWicketTag)tag;
			if (wtag.isLinkTag())
			{
				// Beginning of the region
				if (tag.isOpen() || tag.isOpenClose())
				{
					if (tag.isOpen())
					{
						if (autolinkStatus == null)
						{
							autolinkStatus = new Stack();
						}

						// remember the current setting to be reset after the region
						autolinkStatus.push(new Boolean(autolinking));
					}

					// html allows to represent true in different ways
					final String autolink = tag.getAttributes().getString("autolink");
					if ((autolink == null) || "".equals(autolink) || "true".equalsIgnoreCase(autolink)
							|| "1".equals(autolink))
					{
						autolinking = true;
					}
					else
					{
						autolinking = false;
					}
				}
				else if (tag.isClose())
				{
					// restore the autolink setting from before the region
					autolinking = ((Boolean)autolinkStatus.pop()).booleanValue();
				}

				return nextTag();
			}
		}

		return tag;
	}
}

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
package wicket.markup.parser.filter;

import java.text.ParseException;

import wicket.Application;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupElement;
import wicket.markup.WicketTag;
import wicket.markup.parser.AbstractMarkupFilter;
import wicket.util.collections.ArrayListStack;
import wicket.util.string.StringValueConversionException;
import wicket.util.string.Strings;
import wicket.util.value.ValueMap;

/**
 * This is a markup inline filter. It identifies xml tags which include a href
 * attribute and which are not Wicket specific components and flags these tags
 * (ComponentTag) as autolink enabled. A component resolver will later resolve
 * the href and assign a BookmarkablePageLink to it (automatically).
 * <p>
 * An application setting is used as default value, which might be modified for
 * specific regions. These regions are identified by &lt;wicket:link&gt; tags
 * with an optional 'autolink' attribute. The default value for the attribute is
 * true, thus enabling autolinking. An open-close &lt;wicket:link/&gt tag will
 * change the autolink status until the end of the markup document or the next
 * &lt;wicket:link&gt; tag respectively. &lt;wicket:link&gt; regions may be
 * nested.
 * 
 * @author Juergen Donnerstag
 */
public class WicketLinkTagHandler extends AbstractMarkupFilter
{
	/** The id of autolink components */
	public static final String AUTOLINK_ID = "_autolink_";

	static
	{
		// register "wicket:fragement"
		WicketTagIdentifier.registerWellKnownTagName("link");
	}

	/** Allow to have link regions within link regions */
	private ArrayListStack autolinkStatus;

	/** Current status */
	private boolean autolinking = true;
	
	/**
	 * Construct.
	 */
	public WicketLinkTagHandler()
	{
		setAutomaticLinking(Application.get().getMarkupSettings().getAutomaticLinking());
	}

	/**
	 * Set the default value for autolinking
	 * 
	 * @param enable
	 *            if true, autolinks are enabled
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
	 * @see wicket.markup.parser.IMarkupFilter#nextTag()
	 * @return Return the next eligible MarkupElement
	 */
	public final MarkupElement nextTag() throws ParseException
	{
		// Get next tag. Null, if no more tag available
		final ComponentTag tag = (ComponentTag)getParent().nextTag();
		if (tag == null)
		{
			return tag;
		}

		// Only xml tags not already identified as Wicket components will be
		// considered for autolinking. This is because it is assumed that Wicket
		// components like images or all other kind of Wicket Links will handle
		// it themselves.
		// Subclass analyzeAutolinkCondition() to implement you own implementation
		// and register the new tag handler with the markup parser through
		// Application.newMarkupParser().
		if ((autolinking == true) && (analyzeAutolinkCondition(tag) == true))
		{
			// Mark it as autolink enabled
			tag.enableAutolink(true);

			// Just a dummy name. The ComponentTag will not be forwarded.
			tag.setId(AUTOLINK_ID);
			return tag;
		}

		// For all <wicket:link ..> tags which probably change the
		// current autolink status.
		if (tag instanceof WicketTag)
		{
			final WicketTag wtag = (WicketTag)tag;
			if (wtag.isLinkTag())
			{
				// Beginning of the region
				if (tag.isOpen() || tag.isOpenClose())
				{
					if (tag.isOpen())
					{
						if (autolinkStatus == null)
						{
							autolinkStatus = new ArrayListStack();
						}

						// remember the current setting to be reset after the
						// region
						autolinkStatus.push(new Boolean(autolinking));
					}

					// html allows to represent true in different ways
					final String autolink = tag.getAttributes().getString("autolink");
					try
					{
						autolinking = Strings.isEmpty(autolink) || Strings.isTrue(autolink);
					}
					catch (StringValueConversionException e)
					{
						throw new WicketRuntimeException("Invalid autolink attribute value \"" + autolink + "\"");
					}
				}
				else if (tag.isClose())
				{
					// restore the autolink setting from before the region
					autolinking = ((Boolean)autolinkStatus.pop()).booleanValue();
				}

				return wtag;
			}
		}

		return tag;
	}
	
	/**
	 * Analyze the tag. If return value == true, a autolink component will be
	 * created.
	 * <p>
	 * Subclass analyzeAutolinkCondition() to implement you own implementation
	 * and register the new tag handler with the markup parser through
	 * Application.newMarkupParser().
	 * 
	 * @param tag
	 *            The current tag being parsed
	 * @return If true, tag will become auto-component
	 */
	protected boolean analyzeAutolinkCondition(final ComponentTag tag)
	{
		if (tag.getId() == null)
		{
			ValueMap attributes = tag.getAttributes();
			String ref = attributes.getString("href");
			if (checkRef(ref))
			{
				return true;
			}
			ref = attributes.getString("src");
			if (checkRef(ref))
			{
				return true;
			}
		}

		return false;
	}

	private final boolean checkRef(String ref)
	{
		return (ref != null) && (ref.indexOf(":") == -1);
	}
}

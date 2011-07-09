package org.apache.wicket.markup.html.form;

import java.text.ParseException;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.parser.AbstractMarkupFilter;

/**
 * Markup filter that identifies tags with the {@code wicket:for} attribute. See
 * {@link AutoLabelResolver} for details.
 * 
 * @author igor
 */
public class AutoLabelTagHandler extends AbstractMarkupFilter
{
	@Override
	protected MarkupElement onComponentTag(ComponentTag tag) throws ParseException
	{
		if (tag == null || tag.isClose() || tag instanceof WicketTag)
		{
			return tag;
		}

		String related = tag.getAttribute("wicket:for");
		if (related == null)
		{
			return tag;
		}

		related = related.trim();
		if (related.isEmpty())
		{
			throw new ParseException("Tag contains an empty wicket:for attribute", tag.getPos());
		}
		if (!"label".equalsIgnoreCase(tag.getName()))
		{
			throw new ParseException("Attribute wicket:for can only be attached to <label> tag",
				tag.getPos());
		}
		if (tag.getId() != null)
		{
			throw new ParseException(
				"Attribute wicket:for cannot be used in conjunction with wicket:id", tag.getPos());
		}

		tag.setId(getClass().getName());
		tag.setModified(true);
		return tag;
	}

}

package org.apache.wicket.bean.validation;

import jakarta.validation.constraints.Size;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.FormComponent;

/**
 * A tag modifier that adds the {@code maxlength} and  {@code minlength} attributes to the {@code input}
 * and {@code textarea} tag with the max/min value from the {@link Size} constraint annotation.
 * 
 * @author igor
 * 
 */
public class SizeTagModifier implements ITagModifier<Size>
{
	@Override
	public void modify(FormComponent<?> component, ComponentTag tag, Size annotation)
	{
		if (hasLengthAttribute(tag.getName()))
		{
			tag.put("maxlength", annotation.max());

			if (annotation.min() > 0)
			{
				tag.put("minlength", annotation.min());
			}
		}
	}

	protected boolean hasLengthAttribute(String tagName)
	{
		return "input".equalsIgnoreCase(tagName) || "textarea".equalsIgnoreCase(tagName);
	}
}

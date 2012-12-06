package org.apache.wicket.bean.validation;

import javax.validation.constraints.Size;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.FormComponent;

/**
 * A tag modifier that adds the {@code maxlen} attribute to the {@code input} tag with the max value
 * from the {@link Size} constraint annotation.
 * 
 * @author igor
 * 
 */
public class SizeTagModifier implements ITagModifier<Size>
{
	@Override
	public void modify(FormComponent<?> component, ComponentTag tag, Size annotation)
	{
		if ("input".equalsIgnoreCase(tag.getName()))
		{
			Size size = (Size)annotation;
			tag.put("maxlen", size.max());
		}
	}
}

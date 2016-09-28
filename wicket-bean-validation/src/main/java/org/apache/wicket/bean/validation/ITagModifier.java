package org.apache.wicket.bean.validation;

import java.lang.annotation.Annotation;

import javax.validation.constraints.Size;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.FormComponent;

/**
 * An object that can modify a component's markup tag during render based on values of a constraint
 * annotation. An example would be a modifier that adds the {@code maxlen} attribute to the tag
 * given a {@link Size} annotation.
 * 
 * @author igor
 * 
 */
public interface ITagModifier<T extends Annotation>
{
	/**
	 * Modify the tag
	 * 
	 * @param component
	 *            component the tag belongs to
	 * @param tag
	 *            markup tag to be modified
	 * @param annotation
	 *            constraint annotation
	 */
	void modify(FormComponent<?> component, ComponentTag tag, T annotation);
}

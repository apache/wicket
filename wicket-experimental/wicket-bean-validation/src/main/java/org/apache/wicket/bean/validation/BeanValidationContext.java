package org.apache.wicket.bean.validation;

import java.lang.annotation.Annotation;

import javax.validation.Validator;

import org.apache.wicket.markup.html.form.FormComponent;

/**
 * A read-only view of {@link BeanValidationConfiguration} that can be retrieved by components to
 * access the validator and other helpers.
 * 
 * @see BeanValidationConfiguration#get()
 * 
 * @author igor
 * 
 */
public interface BeanValidationContext extends IPropertyResolver
{

	/**
	 * Gets the tag modifier for the specified annotation type
	 * 
	 * @param annotationType
	 * @return tag modifier or {@code null} if none
	 */
	public abstract <T extends Annotation> ITagModifier<T> getTagModifier(Class<T> annotationType);

	/**
	 * @return the validator
	 */
	public abstract Validator getValidator();

	/**
	 * @return the violation translator
	 */
	public abstract IViolationTranslator getViolationTranslator();

	@Override
	public abstract Property resolveProperty(FormComponent<?> component);

}
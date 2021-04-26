package org.apache.wicket.bean.validation;

import java.lang.annotation.Annotation;

import jakarta.validation.Validator;
import jakarta.validation.metadata.ConstraintDescriptor;

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
	<T extends Annotation> ITagModifier<T> getTagModifier(Class<T> annotationType);

	/**
	 * @return the validator
	 */
	Validator getValidator();

	/**
	 * @return the violation translator
	 */
	IViolationTranslator getViolationTranslator();

	/**
	 * Resolve the property for a component.
	 * 
	 * @param component component
	 */
	@Override
	Property resolveProperty(FormComponent<?> component);
	
	/**
	 * Does the given constraint make a component required.
	 * 
	 * @param constraint constraint
	 * @return <code>true</code> if required
	 */
	boolean isRequiredConstraint(ConstraintDescriptor<?> constraint);
}

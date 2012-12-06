package org.apache.wicket.bean.validation;

import javax.validation.ConstraintViolation;

import org.apache.wicket.validation.ValidationError;

/**
 * Converts {@link ConstraintViolation}s into Wicket's {@link ValidationError}s
 * 
 * @author igor
 * 
 */
public interface IViolationTranslator
{
	/**
	 * Converts a {@link ConstraintViolation} into a {@link ValidationError}
	 * 
	 * @param <T>
	 * @param violation
	 *            constraint violation from bean validation
	 * @return validation validation error to be reported on the component
	 */
	<T> ValidationError convert(ConstraintViolation<T> violation);
}
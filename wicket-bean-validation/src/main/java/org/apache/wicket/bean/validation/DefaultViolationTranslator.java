package org.apache.wicket.bean.validation;

import javax.validation.ConstraintViolation;
import javax.validation.metadata.ConstraintDescriptor;

import org.apache.wicket.util.string.Strings;
import org.apache.wicket.validation.ValidationError;

/**
 * A default implementation of {@link IViolationTranslator}. The validation error is created with
 * the constraint violation's default error message. Further, the violation is checked for a message
 * key and if is found it is also added as a message key to the validation error. The keys are only
 * used if they are in the bean validation's default format of '{key}'.
 * 
 * @author igor
 */
public class DefaultViolationTranslator implements IViolationTranslator
{
	@Override
	public <T> ValidationError convert(ConstraintViolation<T> violation)
	{
		ConstraintDescriptor<?> desc = violation.getConstraintDescriptor();

		ValidationError error = new ValidationError();
		error.setMessage(violation.getMessage());

		String messageKey = getMessageKey(desc);
		if (messageKey != null)
		{
			if (violation.getInvalidValue() != null)
			{
				error.addKey(messageKey + "." +
					violation.getInvalidValue().getClass().getSimpleName());
			}
			error.addKey(messageKey);
		}

		for (String key : desc.getAttributes().keySet())
		{
			error.setVariable(key, desc.getAttributes().get(key));
		}
		return error;
	}

	private String getMessageKey(ConstraintDescriptor<?> desc)
	{
		final Object val = desc.getAttributes().get("message");
		if (val != null)
		{
			String str = val.toString();
			if (!Strings.isEmpty(str) && str.startsWith("{") && str.endsWith("}"))
			{
				return str.substring(1, str.length() - 1);
			}
		}
		return null;
	}


}
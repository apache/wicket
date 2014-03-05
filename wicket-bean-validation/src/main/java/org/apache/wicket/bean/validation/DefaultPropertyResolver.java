package org.apache.wicket.bean.validation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.IPropertyReflectionAwareModel;
import org.apache.wicket.model.IWrapModel;
import org.apache.wicket.model.PropertyModel;

/**
 * Default property resolver. This resolver supports common Wicket models like the
 * {@link PropertyModel}, and other implementations of {@link IPropertyReflectionAwareModel}
 * 
 * @author igor
 * 
 */
public class DefaultPropertyResolver implements IPropertyResolver
{

	@Override
	public Property resolveProperty(FormComponent<?> component)
	{
		IModel<?> model = component.getModel();

		while (true)
		{
			if (model == null)
			{
				return null;
			}
			if (model instanceof IPropertyReflectionAwareModel)
			{
				break;
			}
			if (model instanceof IWrapModel<?>)
			{
				model = ((IWrapModel<?>)model).getWrappedModel();
				continue;
			}
			return null;
		}

		IPropertyReflectionAwareModel<?> delegate = (IPropertyReflectionAwareModel<?>)model;

		String name;
		Method getter = delegate.getPropertyGetter();
		if (getter != null)
		{
			String methodName = getter.getName();
			if (methodName.startsWith("get"))
			{
				name = methodName.substring(3, 4).toLowerCase() +
					methodName.substring(4);
			}
			else if (methodName.startsWith("is"))
			{
				name = methodName.substring(2, 3).toLowerCase() +
						methodName.substring(3);
			}
			else
			{
				throw new WicketRuntimeException("Invalid name for a getter method: '"
						+ methodName + "'. It must start either with 'get' or 'is'.");
			}
			return new Property(getter.getDeclaringClass(), name);
		}

		Field field = delegate.getPropertyField();
		if (field != null)
		{
			return new Property(field.getDeclaringClass(), field.getName());
		}

		return null;
	}

}

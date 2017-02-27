package org.apache.wicket.bean.validation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.core.util.lang.IPropertyExpressionResolver;
import org.apache.wicket.core.util.reflection.ObjectWithGetAndSet;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.AbstractPropertyModel;
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
		AbstractPropertyModel<?> delegate = ValidationModelResolver.resolvePropertyModelFrom(component);
		if (delegate == null)
		{
			return null;
		}
		Object target = delegate.getInnermostModelOrObject();
		if(target == null)
			return null;
		
		String expression = delegate.getPropertyExpression();
		IPropertyExpressionResolver propertyExpressionResolver = Application.get().getApplicationSettings().getPropertyExpressionResolver();
		ObjectWithGetAndSet objectWithGetAndSet = propertyExpressionResolver.resolve(expression, target, target.getClass(), IPropertyExpressionResolver.RESOLVE_CLASS);

		Method getter = objectWithGetAndSet.getGetter();
		if (getter != null)
		{
			String name;
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

		Field field = objectWithGetAndSet.getField();
		if (field != null)
		{
			return new Property(field.getDeclaringClass(), field.getName());
		}

		return null;
	}

}

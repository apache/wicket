package org.apache.wicket.core.util.parser;

import org.apache.wicket.core.util.lang.IPropertyExpressionResolver;
import org.apache.wicket.core.util.lang.PropertyResolverConverter;

public class PropertyExpressionResolver implements IPropertyExpressionResolver
{

	@Override
	public <T> T getValue(String expression, T object)
	{
		return null;
	}

	@Override
	public <T> Class<T> getPropertyClass(String expression, Object object, Class<?> targetClass)
	{
		return null;
	}

	@Override
	public void setValue(String expression, Object object, Object value,
		PropertyResolverConverter prc)
	{
		
	}

}

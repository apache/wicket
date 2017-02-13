package org.apache.wicket.core.util.parser;

import org.apache.wicket.core.util.lang.IPropertyExpressionResolver;
import org.apache.wicket.core.util.lang.PropertyResolverConverter;
import org.apache.wicket.core.util.reflection.ObjectWithGetAndSet;

public class PropertyExpressionResolver implements IPropertyExpressionResolver
{
	@Override
	public ObjectWithGetAndSet resolve(String expression, Object object, Class<? extends Object> clz)
	{
		return null;
	}

	@Override
	public void setValue(String expression, Object object, Object value, PropertyResolverConverter prc)
	{

	}

}
